package com.melodymart.common.controller;
import com.melodymart.albumcatalog.model.Catalog;
import com.melodymart.ordersales.service.CartService;
import com.melodymart.common.model.Listener;
import com.melodymart.common.service.ListenerService;
import com.melodymart.faqpromotion.service.PromotionService;
import com.melodymart.complaintreview.service.ReviewService;
import com.melodymart.libraryplaylist.service.PlaylistService;
import com.melodymart.ordersales.model.Cart;
import com.melodymart.albumcatalog.model.Album;
import com.melodymart.faqpromotion.service.FaqService;

import com.melodymart.albumcatalog.model.Album;
import com.melodymart.ordersales.model.Cart;
import com.melodymart.albumcatalog.model.Catalog;
import com.melodymart.ordersales.service.CartService;
import com.melodymart.faqpromotion.service.FaqService;
import com.melodymart.common.service.ListenerService;
import com.melodymart.faqpromotion.service.PromotionService;
import com.melodymart.complaintreview.service.ReviewService;
import com.melodymart.libraryplaylist.service.PlaylistService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class ListenerController {

    private final ListenerService listenerService;
    private final CartService cartService;
    private final PromotionService promotionService;
    private final FaqService faqService;
    private final ReviewService reviewService;
    private final PlaylistService playlistService;

    @Autowired
    public ListenerController(ListenerService listenerService,
                              CartService cartService,
                              PromotionService promotionService,
                              FaqService faqService,
                              ReviewService reviewService,
                              PlaylistService playlistService) {
        this.listenerService = listenerService;
        this.cartService = cartService;
        this.promotionService = promotionService;
        this.faqService = faqService;
        this.reviewService = reviewService;
        this.playlistService = playlistService;
    }

    // ─── Catalogs ─────────────────────────────────────────────────────────────

    @GetMapping("/catalogs")
    public String listCatalogs(@RequestParam(name = "search", required = false) String search, Model model) {
        List<Catalog> catalogs = listenerService.getAllCatalogs(search);
        model.addAttribute("catalogs", catalogs);
        model.addAttribute("album-catalog/catalogs", catalogs);
        model.addAttribute("currentSearch", search);
        return "album-catalog/catalogs";
    }

    @GetMapping("/catalogs/{id}")
    public String catalogDetail(@PathVariable("id") Integer id, Model model) {
        Optional<Catalog> catOpt = listenerService.getCatalogById(id);
        if (catOpt.isEmpty()) {
            return "redirect:/catalogs";
        }
        Catalog catalog = catOpt.get();
        List<Album> albums = listenerService.getAlbumsForCatalog(id);

        model.addAttribute("catalog", catalog);
        model.addAttribute("albums", albums);
        model.addAttribute("album-catalog/albums", albums);
        return "album-catalog/catalog-detail";
    }

    // ─── Legacy Search Redirect ───────────────────────────────────────────────

    @GetMapping("/search")
    public String searchRedirect(@RequestParam(name = "q", required = false) String query) {
        if (query != null && !query.isBlank()) {
            return "redirect:/albums?search=" + query;
        }
        return "redirect:/albums";
    }

    // ─── Checkout & Purchase ──────────────────────────────────────────────────

    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        if ("ADMIN".equals(session.getAttribute("userRole"))) {
            return "redirect:/admin/dashboard";
        }

        Cart cart = cartService.getCartForListener(userId);
        if (cart == null || cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }

        model.addAttribute("cart", cart);
        model.addAttribute("order-sales/cart", cart);
        model.addAttribute("items", cart.getItems());
        model.addAttribute("cartTotal", cart.getTotalAmount());
        model.addAttribute("totalAmount", cart.getTotalAmount());
        model.addAttribute("totalCount", cart.getTotalItemCount());
        return "order-sales/checkout";
    }

    @PostMapping({"/checkout", "/checkout/confirm"})
    public String confirmPurchase(@RequestParam(name = "paymentMethod", defaultValue = "Credit Card") String paymentMethod,
                                  HttpSession session,
                                  RedirectAttributes ra) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        if ("ADMIN".equals(session.getAttribute("userRole"))) {
            return "redirect:/admin/dashboard";
        }

        try {
            Integer orderId = listenerService.checkout(userId, paymentMethod);
            ra.addAttribute("orderId", orderId);
            return "redirect:/checkout/success";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Checkout failed: " + e.getMessage());
            return "redirect:/cart";
        }
    }

    @GetMapping("/checkout/success")
    public String checkoutSuccess(@RequestParam(name = "orderId", required = false) Integer orderId,
                                  HttpSession session,
                                  Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        model.addAttribute("orderId", orderId);
        return "order-sales/checkout-success";
    }

    // ─── Digital Library (Listener Side) ──────────────────────────────────────

    @GetMapping({"/library", "/my-library"})
    public String myLibrary(@RequestParam(name = "downloaded", required = false) String downloaded,
                            HttpSession session,
                            Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        if ("ADMIN".equals(session.getAttribute("userRole"))) {
            return "redirect:/admin/dashboard";
        }

        List<Map<String, Object>> libraryItems = listenerService.getDigitalLibraryForListener(userId);
        int pendingOrdersCount = listenerService.getPendingOrdersCountForListener(userId);
        List<Integer> declinedOrderIds = listenerService.getDeclinedOrderIdsForListener(userId);

        // Filter out acknowledged declined orders
        List<Integer> acknowledgedDeclinedOrders = (List<Integer>) session.getAttribute("acknowledgedDeclinedOrders");
        if (acknowledgedDeclinedOrders == null) {
            acknowledgedDeclinedOrders = new ArrayList<>();
        }
        
        List<Integer> newDeclinedOrders = new ArrayList<>();
        for (Integer id : declinedOrderIds) {
            if (!acknowledgedDeclinedOrders.contains(id)) {
                newDeclinedOrders.add(id);
                acknowledgedDeclinedOrders.add(id);
            }
        }
        session.setAttribute("acknowledgedDeclinedOrders", acknowledgedDeclinedOrders);
        
        List<Map<String, Object>> userReviews = reviewService.getReviewsForListener(userId);
        Map<Integer, Map<String, Object>> reviewsByAlbum = new HashMap<>();
        for (Map<String, Object> r : userReviews) {
            reviewsByAlbum.put(((Number) r.get("albumId")).intValue(), r);
        }
        
        model.addAttribute("libraryItems", libraryItems);
        model.addAttribute("pendingOrdersCount", pendingOrdersCount);
        model.addAttribute("declinedOrderIds", newDeclinedOrders);
        model.addAttribute("reviewsByAlbum", reviewsByAlbum);
        model.addAttribute("userPlaylists", playlistService.getPlaylistsForListener(userId));
        if (downloaded != null) {
            model.addAttribute("successMsg", "Download request recorded successfully.");
        }
        return "digital-library-playlist/my-library";
    }

    @PostMapping({"/library/download/{id}", "/my-library/download/{id}"})
    public String downloadAlbum(@PathVariable("id") Integer libraryItemId,
                                HttpSession session,
                                RedirectAttributes ra) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        boolean success = listenerService.recordDownload(userId, libraryItemId);
        if (success) {
            ra.addAttribute("downloaded", "true");
        } else {
            ra.addFlashAttribute("errorMsg", "Item not found in your library.");
        }
        return "redirect:/my-library";
    }

    @PostMapping({"/library/remove/{id}", "/my-library/remove/{id}"})
    public String removeLibraryItem(@PathVariable("id") Integer libraryItemId,
                                    HttpSession session,
                                    RedirectAttributes ra) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        boolean success = listenerService.removeLibraryItem(userId, libraryItemId);
        if (success) {
            ra.addFlashAttribute("successMsg", "Item removed from your library.");
        } else {
            ra.addFlashAttribute("errorMsg", "Unable to remove item. Access denied or item not found.");
        }
        return "redirect:/my-library";
    }

    // ─── Listener Promotions ──────────────────────────────────────────────────

    @GetMapping("/promotions")
    public String promotions(Model model) {
        List<Map<String, Object>> activePromotions = promotionService.getActivePromotionsWithAlbums();
        model.addAttribute("promotions", activePromotions);
        model.addAttribute("faq-promotion/promotions", activePromotions);
        return "faq-promotion/promotions";
    }

    // ─── Listener FAQs ────────────────────────────────────────────────────────

    @GetMapping({"/faqs", "/faq"})
    public String faqs(Model model) {
        List<Map<String, Object>> publishedFaqs = faqService.getPublishedFaqs();
        model.addAttribute("faqs", publishedFaqs);
        model.addAttribute("faq-promotion/faqs", publishedFaqs);
        return "faq-promotion/faqs";
    }

    // ─── Legacy order/purchase redirects to My Library ───────────────────────

    @GetMapping({"/orders", "/purchase-history"})
    public String purchaseHistoryRedirect() {
        return "redirect:/my-library";
    }
}
