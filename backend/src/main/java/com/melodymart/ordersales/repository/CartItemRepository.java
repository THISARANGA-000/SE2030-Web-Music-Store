package com.melodymart.ordersales.repository;
import com.melodymart.ordersales.model.CartItem;
import com.melodymart.ordersales.model.CartItemId;

import com.melodymart.ordersales.model.CartItem;
import com.melodymart.ordersales.model.CartItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, CartItemId> {

    List<CartItem> findByCart_CartIdOrderById_ItemNoAsc(Integer cartId);

    Optional<CartItem> findByCart_CartIdAndAlbum_AlbumId(Integer cartId, Integer albumId);

    Optional<CartItem> findByCart_CartIdAndCatalog_CatalogId(Integer cartId, Integer catalogId);

    Optional<CartItem> findByCart_CartIdAndId_ItemNo(Integer cartId, Integer itemNo);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.id.cartId = :cartId AND ci.id.itemNo = :itemNo")
    void deleteByCartIdAndItemNo(@Param("cartId") Integer cartId, @Param("itemNo") Integer itemNo);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.cartId = :cartId")
    void clearCart(@Param("cartId") Integer cartId);

    @Query("SELECT COALESCE(MAX(ci.id.itemNo), 0) FROM CartItem ci WHERE ci.id.cartId = :cartId")
    Integer findMaxItemNoByCartId(@Param("cartId") Integer cartId);
}
