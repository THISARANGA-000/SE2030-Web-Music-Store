package com.melodymart.complaintreview.controller;
import com.melodymart.complaintreview.service.ComplaintService;

import com.melodymart.complaintreview.service.ComplaintService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Handles listener-side complaint viewing and submission.
 *
 * SECURITY: Every route verifies the user is logged in as a non-admin listener.
 *           ListenerID is ALWAYS taken from the HTTP session — never from the form or URL.
 *           Listeners can only see and create their own complaints.
 */
@Controller
@RequestMapping("/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;

    @Autowired
    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    // ─── Guard helper ─────────────────────────────────────────────────────────

    /**
     * Returns null if the session is valid (logged-in listener).
     * Returns a redirect string if access should be denied.
     */
    private String checkListenerAccess(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";
        if ("ADMIN".equals(session.getAttribute("userRole"))) return "redirect:/admin/dashboard";
        return null;
    }

    // ─── List my complaints ────────────────────────────────────────────────────

    @GetMapping({"", "/"})
    public String myComplaints(HttpSession session, Model model,
                               @RequestParam(name = "success", required = false) String success) {
        String redirect = checkListenerAccess(session);
        if (redirect != null) return redirect;

        Integer userId = (Integer) session.getAttribute("userId");

        List<Object[]> complaints = complaintService.getComplaintsForListener(userId);
        model.addAttribute("complaints", complaints);
        model.addAttribute("complaint-review/complaints", complaints);

        if ("true".equals(success)) {
            model.addAttribute("successMsg", "Complaint submitted successfully. We will review it shortly.");
        }
        return "complaint-review/complaints";
    }

    // ─── New complaint form ────────────────────────────────────────────────────

    @GetMapping("/new")
    public String newComplaintForm(HttpSession session, Model model) {
        String redirect = checkListenerAccess(session);
        if (redirect != null) return redirect;

        model.addAttribute("subject", "");
        model.addAttribute("description", "");
        return "complaint-review/complaint-form";
    }

    // ─── Submit complaint ──────────────────────────────────────────────────────

    @PostMapping({"", "/"})
    public String submitComplaint(@RequestParam(name = "subject", defaultValue = "") String subject,
                                  @RequestParam(name = "description", defaultValue = "") String description,
                                  HttpSession session,
                                  Model model,
                                  RedirectAttributes ra) {
        String redirect = checkListenerAccess(session);
        if (redirect != null) return redirect;

        // ListenerID comes from session only — never from the request body
        Integer userId = (Integer) session.getAttribute("userId");

        // Server-side validation
        if (subject.isBlank() || description.isBlank()) {
            model.addAttribute("errorMsg", "Both Subject and Description are required.");
            model.addAttribute("subject", subject);
            model.addAttribute("description", description);
            return "complaint-review/complaint-form";
        }

        try {
            complaintService.createComplaint(userId, subject, description);
            return "redirect:/complaints?success=true";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMsg", e.getMessage());
            model.addAttribute("subject", subject);
            model.addAttribute("description", description);
            return "complaint-review/complaint-form";
        } catch (Exception e) {
            model.addAttribute("errorMsg", "Could not submit your complaint. Please try again.");
            model.addAttribute("subject", subject);
            model.addAttribute("description", description);
            return "complaint-review/complaint-form";
        }
    }

    // ─── View complaint details ────────────────────────────────────────────────

    @GetMapping("/{complaintId}")
    public String viewComplaint(@PathVariable("complaintId") Integer complaintId,
                                HttpSession session,
                                Model model,
                                RedirectAttributes ra) {
        String redirect = checkListenerAccess(session);
        if (redirect != null) return redirect;

        Integer userId = (Integer) session.getAttribute("userId");
        Object[] complaint = complaintService.getComplaintForListener(complaintId, userId);

        if (complaint == null) {
            ra.addFlashAttribute("errorMsg", "Complaint not found or access denied.");
            return "redirect:/complaints";
        }

        model.addAttribute("complaint", complaint);
        return "complaint-review/complaint-detail";
    }

    // ─── Delete complaint ──────────────────────────────────────────────────────

    /**
     * Deletes a complaint that belongs to the currently logged-in listener.
     * ListenerID is taken from the server-side session — never from the request.
     * The service verifies ownership before deleting, so cross-user deletion
     * is impossible even if a malicious user manipulates the complaintId in the URL.
     */
    @PostMapping("/{complaintId}/delete")
    public String deleteComplaint(@PathVariable("complaintId") Integer complaintId,
                                  HttpSession session,
                                  RedirectAttributes ra) {
        String redirect = checkListenerAccess(session);
        if (redirect != null) return redirect;

        // Ownership key always comes from the authenticated session
        Integer userId = (Integer) session.getAttribute("userId");

        try {
            complaintService.deleteComplaintForListener(complaintId, userId);
            ra.addFlashAttribute("successMsg", "Complaint deleted successfully.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Could not delete complaint. Please try again.");
        }
        return "redirect:/complaints";
    }
    @GetMapping("/{complaintId}/edit")
    public String editComplaintForm(@PathVariable("complaintId") Integer complaintId,
                                    HttpSession session,
                                    Model model,
                                    RedirectAttributes ra) {
        String redirect = checkListenerAccess(session);
        if (redirect != null) return redirect;

        Integer userId = (Integer) session.getAttribute("userId");
        Object[] complaint = complaintService.getComplaintForListener(complaintId, userId);

        if (complaint == null) {
            ra.addFlashAttribute("errorMsg", "Complaint not found or access denied.");
            return "redirect:/complaints";
        }

        model.addAttribute("complaint", complaint);
        model.addAttribute("subject", complaint[1]);
        model.addAttribute("description", complaint[2]);
        model.addAttribute("editMode", true);
        return "complaint-review/complaint-form";
    }

    @PostMapping("/{complaintId}/edit")
    public String submitEditComplaint(@PathVariable("complaintId") Integer complaintId,
                                      @RequestParam(name = "subject", defaultValue = "") String subject,
                                      @RequestParam(name = "description", defaultValue = "") String description,
                                      HttpSession session,
                                      Model model,
                                      RedirectAttributes ra) {
        String redirect = checkListenerAccess(session);
        if (redirect != null) return redirect;

        Integer userId = (Integer) session.getAttribute("userId");

        if (subject.isBlank() || description.isBlank()) {
            model.addAttribute("errorMsg", "Both Subject and Description are required.");
            model.addAttribute("subject", subject);
            model.addAttribute("description", description);
            model.addAttribute("editMode", true);
            Object[] complaint = complaintService.getComplaintForListener(complaintId, userId);
            model.addAttribute("complaint", complaint);
            return "complaint-review/complaint-form";
        }

        try {
            complaintService.updateComplaint(userId, complaintId, subject, description);
            ra.addFlashAttribute("successMsg", "Complaint updated successfully.");
            return "redirect:/complaints";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/complaints";
        } catch (Exception e) {
            model.addAttribute("errorMsg", "Could not update complaint. Please try again.");
            model.addAttribute("subject", subject);
            model.addAttribute("description", description);
            model.addAttribute("editMode", true);
            Object[] complaint = complaintService.getComplaintForListener(complaintId, userId);
            model.addAttribute("complaint", complaint);
            return "complaint-review/complaint-form";
        }
    }
}
