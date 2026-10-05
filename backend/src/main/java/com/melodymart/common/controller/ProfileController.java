package com.melodymart.common.controller;
import com.melodymart.common.model.User;
import com.melodymart.common.service.AuthService;

import com.melodymart.common.model.User;
import com.melodymart.common.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@Controller
public class ProfileController {

    private final AuthService authService;

    @Autowired
    public ProfileController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        Optional<User> userOpt = authService.findById(userId);
        if (userOpt.isEmpty()) {
            session.invalidate();
            return "redirect:/login";
        }
        User user = userOpt.get();
        model.addAttribute("user", user);
        model.addAttribute("userRole", authService.getUserRole(user));
        model.addAttribute("isAdmin", authService.isAdmin(user));

        if (authService.isAdmin(user)) {
            model.addAttribute("adminInfo", authService.getAdminInfo(user));
        }

        return "common/profile";
    }
}
