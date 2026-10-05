package com.melodymart.ordersales.model;
import com.melodymart.albumcatalog.model.Catalog;
import com.melodymart.albumcatalog.model.Album;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "[CART_ITEM]", schema = "dbo")
public class CartItem {

    @EmbeddedId
    private CartItemId id = new CartItemId();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("cartId")
    @JoinColumn(name = "CartID", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "AlbumID")
    private Album album;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CatalogID")
    private Catalog catalog;

    @Column(name = "Quantity", nullable = false)
    private Integer quantity = 1;

    @Column(name = "UnitPrice", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "AddedDate", nullable = false)
    private LocalDateTime addedDate = LocalDateTime.now();

    public CartItem() {
    }

    public CartItemId getId() {
        return id;
    }

    public void setId(CartItemId id) {
        this.id = id;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
        if (cart != null && cart.getCartId() != null) {
            this.id.setCartId(cart.getCartId());
        }
    }

    public Integer getItemNo() {
        return id != null ? id.getItemNo() : null;
    }

    public void setItemNo(Integer itemNo) {
        if (this.id == null) {
            this.id = new CartItemId();
        }
        this.id.setItemNo(itemNo);
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public Catalog getCatalog() {
        return catalog;
    }

    public void setCatalog(Catalog catalog) {
        this.catalog = catalog;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        if (unitPrice != null) return unitPrice;
        if (album != null) return album.getEffectivePrice();
        if (catalog != null && catalog.getPrice() != null) return catalog.getPrice();
        return BigDecimal.ZERO;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public LocalDateTime getAddedDate() {
        return addedDate;
    }

    public void setAddedDate(LocalDateTime addedDate) {
        this.addedDate = addedDate;
    }

    public BigDecimal getSubtotal() {
        BigDecimal price = getUnitPrice();
        int qty = (quantity != null && quantity > 0) ? quantity : 1;
        return price.multiply(BigDecimal.valueOf(qty)).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public String getItemTitle() {
        if (album != null) return album.getAlbumTitle();
        if (catalog != null) return catalog.getCatalogName();
        return "Unknown Item";
    }

    public String getItemSubtitle() {
        if (album != null && album.getArtist() != null) return album.getArtist().getArtistName();
        if (catalog != null) return "Catalog Bundle";
        return "";
    }

    public String getItemImageUrl() {
        if (album != null && album.getCoverImageUrl() != null && !album.getCoverImageUrl().isEmpty()) {
            return album.getCoverImageUrl();
        }
        return "/assets/covers/celestial_drift.jpg";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        return Objects.equals(id, cartItem.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
