package com.melodymart.ordersales.service;
import com.melodymart.albumcatalog.model.Catalog;
import com.melodymart.common.model.Listener;
import com.melodymart.ordersales.repository.CartRepository;
import com.melodymart.ordersales.repository.CartItemRepository;
import com.melodymart.ordersales.model.CartItem;
import com.melodymart.albumcatalog.repository.AlbumRepository;
import com.melodymart.ordersales.model.Cart;
import com.melodymart.albumcatalog.model.Album;
import com.melodymart.albumcatalog.repository.CatalogRepository;
import com.melodymart.common.repository.ListenerRepository;


import com.melodymart.albumcatalog.repository.AlbumRepository;
import com.melodymart.ordersales.repository.CartItemRepository;
import com.melodymart.ordersales.repository.CartRepository;
import com.melodymart.albumcatalog.repository.CatalogRepository;
import com.melodymart.common.repository.ListenerRepository;
import com.melodymart.faqpromotion.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final AlbumRepository albumRepository;
    private final CatalogRepository catalogRepository;
    private final ListenerRepository listenerRepository;
    private final PromotionService promotionService;

    @Autowired
    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       AlbumRepository albumRepository,
                       CatalogRepository catalogRepository,
                       ListenerRepository listenerRepository,
                       PromotionService promotionService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.albumRepository = albumRepository;
        this.catalogRepository = catalogRepository;
        this.listenerRepository = listenerRepository;
        this.promotionService = promotionService;
    }

    private void refreshCartPrices(Cart cart) {
        if (cart == null || cart.getItems() == null) return;
        boolean changed = false;
        for (CartItem item : cart.getItems()) {
            if (item.getAlbum() != null) {
                promotionService.applyActivePromotion(item.getAlbum());
                BigDecimal currentEffective = promotionService.getEffectivePriceForAlbum(
                        item.getAlbum().getAlbumId(),
                        item.getAlbum().getPrice()
                );
                if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(currentEffective) != 0) {
                    item.setUnitPrice(currentEffective);
                    cartItemRepository.save(item);
                    changed = true;
                }
            }
        }
        if (changed) {
            cart.setLastUpdatedDate(LocalDateTime.now());
            cartRepository.save(cart);
        }
    }

    @Transactional
    public Cart getOrCreateActiveCart(Integer listenerId) {
        Optional<Cart> activeCartOpt = cartRepository.findByListener_UserIdAndCartStatus(listenerId, "Active");
        if (activeCartOpt.isPresent()) {
            Cart cart = activeCartOpt.get();
            refreshCartPrices(cart);
            return cart;
        }

        // Check if a cart exists for this listener with another status or create new
        Optional<Listener> listenerOpt = listenerRepository.findById(listenerId);
        if (listenerOpt.isEmpty()) {
            throw new IllegalArgumentException("Listener not found with ID: " + listenerId);
        }

        Cart newCart = new Cart(listenerOpt.get());
        newCart.setCartStatus("Active");
        newCart.setCreatedDate(LocalDateTime.now());
        newCart.setLastUpdatedDate(LocalDateTime.now());
        return cartRepository.save(newCart);
    }

    @Transactional
    public Cart getCartForListener(Integer listenerId) {
        if (listenerId == null) return null;
        Optional<Cart> cartOpt = cartRepository.findByListener_UserIdAndCartStatus(listenerId, "Active");
        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            refreshCartPrices(cart);
            return cart;
        }
        return null;
    }

    @Transactional
    public CartItem addAlbumToCart(Integer listenerId, Integer albumId, int quantity) {
        if (listenerId == null) {
            throw new IllegalArgumentException("Must be logged in to add items to cart.");
        }
        if (quantity <= 0) quantity = 1;

        Cart cart = getOrCreateActiveCart(listenerId);
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new IllegalArgumentException("Album not found with ID: " + albumId));

        BigDecimal effectivePrice = promotionService.getEffectivePriceForAlbum(albumId, album.getPrice());

        // Check if album is already in the cart
        Optional<CartItem> existingItemOpt = cartItemRepository.findByCart_CartIdAndAlbum_AlbumId(cart.getCartId(), albumId);
        CartItem item;
        if (existingItemOpt.isPresent()) {
            item = existingItemOpt.get();
            item.setQuantity(item.getQuantity() + quantity);
            item.setUnitPrice(effectivePrice);
            item.setAddedDate(LocalDateTime.now());
        } else {
            Integer maxItemNo = cartItemRepository.findMaxItemNoByCartId(cart.getCartId());
            int nextItemNo = (maxItemNo != null ? maxItemNo : 0) + 1;

            item = new CartItem();
            item.setCart(cart);
            item.setItemNo(nextItemNo);
            item.setAlbum(album);
            item.setQuantity(quantity);
            item.setUnitPrice(effectivePrice);
            item.setAddedDate(LocalDateTime.now());
        }

        CartItem savedItem = cartItemRepository.save(item);

        cart.setLastUpdatedDate(LocalDateTime.now());
        cartRepository.save(cart);

        return savedItem;
    }

    @Transactional
    public CartItem addCatalogToCart(Integer listenerId, Integer catalogId, int quantity) {
        if (listenerId == null) {
            throw new IllegalArgumentException("Must be logged in to add items to cart.");
        }
        if (quantity <= 0) quantity = 1;

        Cart cart = getOrCreateActiveCart(listenerId);
        com.melodymart.albumcatalog.model.Catalog catalog = catalogRepository.findById(catalogId)
                .orElseThrow(() -> new IllegalArgumentException("Catalog not found with ID: " + catalogId));

        // Check if catalog is already in the cart
        Optional<CartItem> existingItemOpt = cartItemRepository.findByCart_CartIdAndCatalog_CatalogId(cart.getCartId(), catalogId);
        CartItem item;
        if (existingItemOpt.isPresent()) {
            item = existingItemOpt.get();
            item.setQuantity(item.getQuantity() + quantity);
            item.setAddedDate(LocalDateTime.now());
        } else {
            Integer maxItemNo = cartItemRepository.findMaxItemNoByCartId(cart.getCartId());
            int nextItemNo = (maxItemNo != null ? maxItemNo : 0) + 1;

            item = new CartItem();
            item.setCart(cart);
            item.setItemNo(nextItemNo);
            item.setCatalog(catalog);
            item.setQuantity(quantity);
            item.setUnitPrice(catalog.getPrice());
            item.setAddedDate(LocalDateTime.now());
        }

        CartItem savedItem = cartItemRepository.save(item);

        cart.setLastUpdatedDate(LocalDateTime.now());
        cartRepository.save(cart);

        return savedItem;
    }

    @Transactional
    public void clearCartForListener(Integer listenerId) {
        if (listenerId == null) return;
        Cart cart = getCartForListener(listenerId);
        if (cart != null) {
            cartItemRepository.clearCart(cart.getCartId());
            cart.setLastUpdatedDate(LocalDateTime.now());
            cartRepository.save(cart);
        }
    }

    @Transactional
    public void updateItemQuantity(Integer listenerId, Integer itemNo, int quantity) {
        if (listenerId == null) return;
        Cart cart = getCartForListener(listenerId);
        if (cart == null) return;

        if (quantity <= 0) {
            cartItemRepository.deleteByCartIdAndItemNo(cart.getCartId(), itemNo);
        } else {
            Optional<CartItem> itemOpt = cartItemRepository.findByCart_CartIdAndId_ItemNo(cart.getCartId(), itemNo);
            if (itemOpt.isPresent()) {
                CartItem item = itemOpt.get();
                item.setQuantity(quantity);
                if (item.getAlbum() != null) {
                    BigDecimal effectivePrice = promotionService.getEffectivePriceForAlbum(
                            item.getAlbum().getAlbumId(),
                            item.getAlbum().getPrice()
                    );
                    item.setUnitPrice(effectivePrice);
                }
                cartItemRepository.save(item);
            }
        }

        cart.setLastUpdatedDate(LocalDateTime.now());
        cartRepository.save(cart);
    }

    @Transactional
    public void removeItemFromCart(Integer listenerId, Integer itemNo) {
        if (listenerId == null) return;
        Cart cart = getCartForListener(listenerId);
        if (cart == null) return;

        cartItemRepository.deleteByCartIdAndItemNo(cart.getCartId(), itemNo);

        cart.setLastUpdatedDate(LocalDateTime.now());
        cartRepository.save(cart);
    }

    @Transactional(readOnly = true)
    public int getCartItemCount(Integer listenerId) {
        if (listenerId == null) return 0;
        Cart cart = getCartForListener(listenerId);
        return cart != null ? cart.getTotalItemCount() : 0;
    }
}
