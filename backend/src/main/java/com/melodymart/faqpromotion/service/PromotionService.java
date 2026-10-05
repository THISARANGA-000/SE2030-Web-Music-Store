package com.melodymart.faqpromotion.service;
import com.melodymart.artistgenretrack.model.Artist;

import com.melodymart.albumcatalog.model.Album;
import com.melodymart.albumcatalog.pricing.BaseAlbumPrice;
import com.melodymart.albumcatalog.pricing.PricingComponent;
import com.melodymart.albumcatalog.pricing.PromotionPriceDecorator;
import com.melodymart.faqpromotion.strategy.DiscountStrategy;
import com.melodymart.faqpromotion.strategy.FixedAmountDiscountStrategy;
import com.melodymart.faqpromotion.strategy.PercentageDiscountStrategy;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PromotionService {

    @PersistenceContext
    private EntityManager em;

    /**
     * Strategy Pattern — Context method.
     * Selects the appropriate DiscountStrategy based on the promotion's DiscountType.
     */
    public DiscountStrategy selectStrategy(String discountType) {
        if ("Percentage".equalsIgnoreCase(discountType)) {
            return new PercentageDiscountStrategy();
        }
        // Defaults to FixedAmount for 'FixedAmount' or any other type
        return new FixedAmountDiscountStrategy();
    }

    /**
     * Retrieves all currently active promotions with their applicable albums.
     * Active condition: PromotionStatus = 'Active' AND GETDATE() BETWEEN StartDate AND EndDate.
     */
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getActivePromotionsWithAlbums() {
        List<Object[]> promoRows = em.createNativeQuery(
            "SELECT p.PromotionID, p.PromotionName, p.Description, p.DiscountType, " +
            "p.DiscountValue, p.StartDate, p.EndDate, p.PromotionStatus " +
            "FROM dbo.[PROMOTION] p " +
            "WHERE p.PromotionStatus = 'Active' " +
            "  AND GETDATE() BETWEEN p.StartDate AND p.EndDate " +
            "ORDER BY p.StartDate DESC")
          .getResultList();

        List<Map<String, Object>> promotions = new ArrayList<>();

        for (Object[] pr : promoRows) {
            Map<String, Object> promo = new HashMap<>();
            Integer promoId = ((Number) pr[0]).intValue();
            String promoName = (String) pr[1];
            String description = (String) pr[2];
            String discountType = (String) pr[3];
            BigDecimal discountVal = (BigDecimal) pr[4];
            Object startDate = pr[5];
            Object endDate = pr[6];
            String status = (String) pr[7];

            promo.put("promotionId", promoId);
            promo.put("promotionName", promoName);
            promo.put("description", description);
            promo.put("discountType", discountType);
            promo.put("discountValue", discountVal);
            promo.put("startDate", startDate);
            promo.put("endDate", endDate);
            promo.put("promotionStatus", status);

            String discountLabel;
            if ("Percentage".equalsIgnoreCase(discountType)) {
                discountLabel = discountVal.stripTrailingZeros().toPlainString() + "% OFF";
            } else {
                discountLabel = "$" + discountVal.setScale(2, RoundingMode.HALF_UP) + " OFF";
            }
            promo.put("discountLabel", discountLabel);

            // Strategy Pattern — select the algorithm based on the promotion's discount type
            DiscountStrategy strategy = selectStrategy(discountType);

            // Fetch applicable albums for this promotion
            List<Object[]> albumRows = em.createNativeQuery(
                "SELECT a.AlbumID, a.AlbumTitle, a.CoverImageUrl, a.Price, " +
                "COALESCE(art.ArtistName, 'Unknown Artist') AS ArtistName, " +
                "COALESCE(g.GenreName, 'General') AS GenreName " +
                "FROM dbo.[ALBUM_PROMOTION] ap " +
                "JOIN dbo.[ALBUM] a ON ap.AlbumID = a.AlbumID " +
                "LEFT JOIN dbo.[ARTIST] art ON a.ArtistID = art.ArtistID " +
                "LEFT JOIN dbo.[GENRE] g ON a.GenreID = g.GenreID " +
                "WHERE ap.PromotionID = ? " +
                "ORDER BY a.AlbumTitle ASC")
              .setParameter(1, promoId)
              .getResultList();

            List<Map<String, Object>> albums = new ArrayList<>();
            for (Object[] ar : albumRows) {
                Map<String, Object> album = new HashMap<>();
                Integer albumId = ((Number) ar[0]).intValue();
                String albumTitle = (String) ar[1];
                String coverUrl = (String) ar[2];
                BigDecimal originalPrice = (BigDecimal) ar[3];
                String artistName = (String) ar[4];
                String genreName = (String) ar[5];

                // Decorator Pattern — wrap the base price, then apply the promotion decorator
                PricingComponent basePrice = new BaseAlbumPrice(originalPrice);
                PricingComponent promotedPrice = new PromotionPriceDecorator(basePrice, strategy, discountVal);
                BigDecimal discountedPrice = promotedPrice.getPrice();

                album.put("albumId", albumId);
                album.put("albumTitle", albumTitle);
                album.put("coverImageUrl", coverUrl);
                album.put("price", originalPrice.setScale(2, RoundingMode.HALF_UP));
                album.put("discountedPrice", discountedPrice);
                album.put("artistName", artistName);
                album.put("genreName", genreName);

                albums.add(album);
            }
            promo.put("albums", albums);
            promotions.add(promo);
        }

        return promotions;
    }

    /**
     * Calculates the effective promotional price for an album.
     * If one or more promotions are active (PromotionStatus = 'Active' and current date within StartDate and EndDate),
     * computes the discounted price using the Decorator and Strategy patterns.
     * Returns the lowest discounted price if multiple promotions apply.
     * If no active promotion exists, returns the original price.
     */
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public BigDecimal getEffectivePriceForAlbum(Integer albumId, BigDecimal originalPrice) {
        if (albumId == null || originalPrice == null) {
            return originalPrice != null ? originalPrice.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        }

        List<Object[]> activePromos = em.createNativeQuery(
            "SELECT p.DiscountType, p.DiscountValue " +
            "FROM dbo.[ALBUM_PROMOTION] ap " +
            "JOIN dbo.[PROMOTION] p ON ap.PromotionID = p.PromotionID " +
            "WHERE ap.AlbumID = ? " +
            "  AND p.PromotionStatus = 'Active' " +
            "  AND GETDATE() BETWEEN p.StartDate AND p.EndDate")
          .setParameter(1, albumId)
          .getResultList();

        if (activePromos.isEmpty()) {
            return originalPrice.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal lowestPrice = originalPrice.setScale(2, RoundingMode.HALF_UP);
        for (Object[] pr : activePromos) {
            String discountType = (String) pr[0];
            BigDecimal discountVal = (BigDecimal) pr[1];
            DiscountStrategy strategy = selectStrategy(discountType);

            PricingComponent base = new BaseAlbumPrice(originalPrice);
            PricingComponent decorated = new PromotionPriceDecorator(base, strategy, discountVal);
            BigDecimal discounted = decorated.getPrice();
            if (discounted.compareTo(lowestPrice) < 0) {
                lowestPrice = discounted;
            }
        }
        return lowestPrice;
    }

    /**
     * Applies any active promotion details (discountedPrice, discountLabel) to an Album object.
     */
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public void applyActivePromotion(Album album) {
        if (album == null || album.getAlbumId() == null || album.getPrice() == null) return;

        List<Object[]> activePromos = em.createNativeQuery(
            "SELECT p.PromotionName, p.DiscountType, p.DiscountValue, p.EndDate " +
            "FROM dbo.[ALBUM_PROMOTION] ap " +
            "JOIN dbo.[PROMOTION] p ON ap.PromotionID = p.PromotionID " +
            "WHERE ap.AlbumID = ? " +
            "  AND p.PromotionStatus = 'Active' " +
            "  AND GETDATE() BETWEEN p.StartDate AND p.EndDate " +
            "ORDER BY p.DiscountValue DESC")
          .setParameter(1, album.getAlbumId())
          .getResultList();

        if (!activePromos.isEmpty()) {
            BigDecimal basePrice = album.getPrice();
            BigDecimal lowestPrice = basePrice.setScale(2, RoundingMode.HALF_UP);
            String bestLabel = null;

            for (Object[] pr : activePromos) {
                String promoName = (String) pr[0];
                String discountType = (String) pr[1];
                BigDecimal discountVal = (BigDecimal) pr[2];
                DiscountStrategy strategy = selectStrategy(discountType);

                PricingComponent base = new BaseAlbumPrice(basePrice);
                PricingComponent decorated = new PromotionPriceDecorator(base, strategy, discountVal);
                BigDecimal discounted = decorated.getPrice();

                if (discounted.compareTo(lowestPrice) < 0) {
                    lowestPrice = discounted;
                    if ("Percentage".equalsIgnoreCase(discountType)) {
                        bestLabel = discountVal.stripTrailingZeros().toPlainString() + "% OFF";
                    } else {
                        bestLabel = "$" + discountVal.setScale(2, RoundingMode.HALF_UP) + " OFF";
                    }
                }
            }

            if (lowestPrice.compareTo(basePrice) < 0) {
                album.setDiscountedPrice(lowestPrice);
                album.setDiscountLabel(bestLabel);
            } else {
                album.setDiscountedPrice(null);
                album.setDiscountLabel(null);
            }
        } else {
            album.setDiscountedPrice(null);
            album.setDiscountLabel(null);
        }
    }

    /**
     * Applies active promotion details to a list of Album objects.
     */
    @Transactional(readOnly = true)
    public void applyActivePromotions(List<Album> albums) {
        if (albums == null || albums.isEmpty()) return;
        for (Album album : albums) {
            applyActivePromotion(album);
        }
    }
}
