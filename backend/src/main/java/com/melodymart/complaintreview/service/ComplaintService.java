package com.melodymart.complaintreview.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for listener-side complaint management.
 * All queries are scoped to a specific ListenerID for ownership enforcement.
 * ListenerID always comes from the authenticated server-side session — never from the browser.
 */
@Service
public class ComplaintService {

    @PersistenceContext
    private EntityManager em;

    /**
     * Returns all complaints belonging to the given listener only.
     * Columns: [0]=ComplaintID, [1]=Subject, [2]=Description, [3]=ComplaintDate, [4]=Status, [5]=Response, [6]=ResolvedDate
     */
    @Transactional(readOnly = true)
    public List<Object[]> getComplaintsForListener(Integer listenerId) {
        if (listenerId == null) return List.of();
        @SuppressWarnings("unchecked")
        List<Object[]> results = em.createNativeQuery(
            "SELECT c.ComplaintID, c.Subject, c.Description, c.ComplaintDate, " +
            "c.Status, c.Response, c.ResolvedDate " +
            "FROM dbo.[COMPLAINT] c " +
            "WHERE c.ListenerID = ? " +
            "ORDER BY c.ComplaintDate DESC")
            .setParameter(1, listenerId)
            .getResultList();
        return results;
    }

    /**
     * Retrieves a single complaint by ID if and only if it belongs to the given listener.
     * Enforces ownership: returns null if not found or if the complaint belongs to another listener.
     * Columns: [0]=ComplaintID, [1]=Subject, [2]=Description, [3]=ComplaintDate, [4]=Status, [5]=Response, [6]=ResolvedDate
     */
    @Transactional(readOnly = true)
    public Object[] getComplaintForListener(Integer complaintId, Integer listenerId) {
        if (complaintId == null || listenerId == null) return null;
        @SuppressWarnings("unchecked")
        List<Object[]> results = em.createNativeQuery(
            "SELECT c.ComplaintID, c.Subject, c.Description, c.ComplaintDate, " +
            "c.Status, c.Response, c.ResolvedDate " +
            "FROM dbo.[COMPLAINT] c " +
            "WHERE c.ComplaintID = ? AND c.ListenerID = ?")
            .setParameter(1, complaintId)
            .setParameter(2, listenerId)
            .getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Deletes a complaint if and only if it belongs to the given listener.
     * Ownership is enforced server-side: the DELETE statement includes
     * "AND ListenerID = ?" so that even if an attacker supplies a foreign
     * ComplaintID the row simply will not be found and an exception is thrown.
     *
     * @throws IllegalArgumentException if the complaint does not exist or
     *                                  belongs to a different listener.
     */
    @Transactional
    public void deleteComplaintForListener(Integer complaintId, Integer listenerId) {
        if (complaintId == null || listenerId == null) {
            throw new IllegalArgumentException("Invalid request.");
        }
        int deleted = em.createNativeQuery(
            "DELETE FROM dbo.[COMPLAINT] " +
            "WHERE ComplaintID = ? AND ListenerID = ?")
            .setParameter(1, complaintId)
            .setParameter(2, listenerId)
            .executeUpdate();

        if (deleted == 0) {
            // Either the complaint does not exist or it belongs to another listener.
            throw new IllegalArgumentException("Complaint not found or access denied.");
        }
    }

    /**
     * Creates a new complaint for the given listener.
     * Subject is trimmed and validated. ListenerID comes from session only.
     */
    @Transactional
    public void createComplaint(Integer listenerId, String subject, String description) {
        if (listenerId == null) throw new IllegalArgumentException("You must be logged in to submit a complaint.");
        if (subject == null || subject.isBlank()) throw new IllegalArgumentException("Subject is required.");
        if (description == null || description.isBlank()) throw new IllegalArgumentException("Description is required.");

        String s = subject.trim();
        if (s.length() > 200) s = s.substring(0, 200);

        em.createNativeQuery(
            "INSERT INTO dbo.[COMPLAINT] (ListenerID, Subject, Description, ComplaintDate, Status) " +
            "VALUES (?, ?, ?, GETDATE(), 'Open')")
            .setParameter(1, listenerId)
            .setParameter(2, s)
            .setParameter(3, description.trim())
            .executeUpdate();
    }

    @Transactional
    public void updateComplaint(Integer listenerId, Integer complaintId, String subject, String description) {
        if (listenerId == null) throw new IllegalArgumentException("You must be logged in.");
        
        List<Object[]> results = em.createNativeQuery(
            "SELECT ComplaintDate FROM dbo.[COMPLAINT] WHERE ComplaintID = ? AND ListenerID = ?")
            .setParameter(1, complaintId)
            .setParameter(2, listenerId)
            .getResultList();

        if (results.isEmpty()) {
            throw new IllegalArgumentException("Complaint not found or access denied.");
        }

        // Check if within 10 minutes (SQL Server check, or we can check in Java)
        // DATEDIFF check via query is safer. Let's do it in update query.
        int updated = em.createNativeQuery(
            "UPDATE dbo.[COMPLAINT] SET Subject = ?, Description = ? " +
            "WHERE ComplaintID = ? AND ListenerID = ? AND DATEDIFF(MINUTE, ComplaintDate, GETDATE()) <= 10")
            .setParameter(1, subject != null ? subject.trim() : "")
            .setParameter(2, description != null ? description.trim() : "")
            .setParameter(3, complaintId)
            .setParameter(4, listenerId)
            .executeUpdate();

        if (updated == 0) {
            throw new IllegalArgumentException("The 10-minute editing period for this complaint has expired.");
        }
    }
}
