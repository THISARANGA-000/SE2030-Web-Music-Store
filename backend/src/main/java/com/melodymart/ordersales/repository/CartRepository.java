package com.melodymart.ordersales.repository;
import com.melodymart.ordersales.model.Cart;

import com.melodymart.ordersales.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {

    Optional<Cart> findByListener_UserId(Integer listenerId);

    Optional<Cart> findByListener_UserIdAndCartStatus(Integer listenerId, String cartStatus);
}
