package com.melodymart.common.controller;
import com.melodymart.common.model.User;
import com.melodymart.common.model.Administrator;
import com.melodymart.common.service.AuthService;

import com.melodymart.common.model.User;
import com.melodymart.common.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ─── LOGIN ───────────────────────────────────────────────────────────────

    @GetMapping("/login")
    public String loginPage(@RequestParam(name = "error", required = false) String error,
                            @RequestParam(name = "logout", required = false) String logout,
                            @RequestParam(name = "registered", required = false) String registered,
                            HttpSession session, Model model) {
        // Already logged in
        if (session.getAttribute("userId") != null) {
            return "redirect:/albums";
        }
        if (error != null) {
            model.addAttribute("errorMsg", "Invalid email or password. Please try again.");
        }
        if (logout != null) {
            model.addAttribute("successMsg", "You have been logged out successfully.");
        }
        if (registered != null) {
            model.addAttribute("successMsg", "Account created! Please log in.");
        }
        return "common/login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam("email") String email,
                          @RequestParam("password") String password,
                          HttpSession session) {
        User user = authService.authenticate(email, password);
        if (user == null) {
            return "redirect:/login?error";
        }
        String role = authService.getUserRole(user);
        session.setAttribute("userId", user.getUserId());
        session.setAttribute("userEmail", user.getEmail());
        session.setAttribute("userName", user.getFirstName() + " " + user.getLastName());
        session.setAttribute("userRole", role);

        if ("ADMIN".equals(role)) {
            com.melodymart.common.model.Administrator adminInfo = authService.getAdminInfo(user);
            if (adminInfo != null) {
                session.setAttribute("adminRole", adminInfo.getAdminRole());
            }
            return "redirect:/admin/dashboard";
        }
        return "redirect:/albums";
    }

    // ─── LOGOUT ──────────────────────────────────────────────────────────────

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }

    // ─── REGISTRATION ────────────────────────────────────────────────────────

    @GetMapping("/register")
    public String registerPage(HttpSession session, Model model) {
        if (session.getAttribute("userId") != null) {
            return "redirect:/albums";
        }
        return "common/register";
    }

    @PostMapping("/register")
    public String doRegister(@RequestParam("firstName") String firstName,
                             @RequestParam("lastName") String lastName,
                             @RequestParam("email") String email,
                             @RequestParam("password") String password,
                             @RequestParam(name = "phone", required = false) String phone,
                             Model model) {
        try {
            authService.registerListener(firstName.trim(), lastName.trim(),
                    email.trim().toLowerCase(), password, phone != null ? phone.trim() : null);
            return "redirect:/login?registered";
        } catch (Exception e) {
            model.addAttribute("errorMsg", e.getMessage() != null ? e.getMessage() : "Registration failed. Please try again.");
            model.addAttribute("firstName", firstName);
            model.addAttribute("lastName", lastName);
            model.addAttribute("email", email);
            model.addAttribute("phone", phone);
            return "common/register";
        }
    }
}
