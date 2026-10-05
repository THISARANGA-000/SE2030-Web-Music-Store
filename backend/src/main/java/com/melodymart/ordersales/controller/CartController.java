package com.melodymart.ordersales.controller;
import com.melodymart.ordersales.service.CartService;
import com.melodymart.ordersales.model.Cart;

import com.melodymart.ordersales.model.Cart;
import com.melodymart.ordersales.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public String viewCart(HttpSession session,
                           @RequestParam(name = "added", required = false) String added,
                           @RequestParam(name = "updated", required = false) String updated,
                           @RequestParam(name = "removed", required = false) String removed,
                           Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        String userRole = (String) session.getAttribute("userRole");
        if ("ADMIN".equals(userRole)) {
            return "redirect:/admin/dashboard";
        }

        Cart cart = cartService.getOrCreateActiveCart(userId);
        model.addAttribute("cart", cart);
        model.addAttribute("order-sales/cart", cart);
        model.addAttribute("items", cart.getItems());
        model.addAttribute("cartTotal", cart.getTotalAmount());
        model.addAttribute("totalAmount", cart.getTotalAmount());
        model.addAttribute("totalCount", cart.getTotalItemCount());

        if (added != null) {
            model.addAttribute("successMsg", "Item added to your cart!");
        }
        if (updated != null) {
            model.addAttribute("successMsg", "Cart updated successfully.");
        }
        if (removed != null) {
            model.addAttribute("infoMsg", "Item removed from your cart.");
        }

        return "order-sales/cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam(value = "albumId", required = false) Integer albumId,
                            @RequestParam(value = "catalogId", required = false) Integer catalogId,
                            @RequestParam(name = "quantity", defaultValue = "1") int quantity,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        if ("ADMIN".equals(session.getAttribute("userRole"))) {
            return "redirect:/admin/dashboard";
        }

        try {
            if (catalogId != null) {
                cartService.addCatalogToCart(userId, catalogId, quantity);
            } else if (albumId != null) {
                cartService.addAlbumToCart(userId, albumId, quantity);
            }
            return "redirect:/cart?added";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            if (catalogId != null) {
                return "redirect:/catalogs/" + catalogId;
            }
            return "redirect:/albums" + (albumId != null ? "/" + albumId : "");
        }
    }

    @PostMapping("/add-catalog")
    public String addCatalogToCart(@RequestParam("catalogId") Integer catalogId,
                                   @RequestParam(name = "quantity", defaultValue = "1") int quantity,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        if ("ADMIN".equals(session.getAttribute("userRole"))) {
            return "redirect:/admin/dashboard";
        }

        try {
            cartService.addCatalogToCart(userId, catalogId, quantity);
            return "redirect:/cart?added";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/catalogs/" + catalogId;
        }
    }

    @PostMapping("/update")
    public String updateQuantity(@RequestParam("itemNo") Integer itemNo,
                                 @RequestParam("quantity") int quantity,
                                 HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        cartService.updateItemQuantity(userId, itemNo, quantity);
        return "redirect:/cart?updated";
    }

    @PostMapping("/remove")
    public String removeItem(@RequestParam("itemNo") Integer itemNo,
                             HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        cartService.removeItemFromCart(userId, itemNo);
        return "redirect:/cart?removed";
    }
}
