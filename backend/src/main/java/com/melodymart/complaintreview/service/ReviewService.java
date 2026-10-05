package com.melodymart.complaintreview.service;
import com.melodymart.artistgenretrack.model.Artist;
import com.melodymart.common.model.Listener;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReviewService {

    @PersistenceContext
    private EntityManager em;

    /**
     * Fetch all approved reviews for a given album.
     * Displays listener first name and last initial to protect privacy.
     */
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getReviewsForAlbum(Integer albumId) {
        if (albumId == null) return List.of();

        List<Object[]> rows = em.createNativeQuery(
            "SELECT r.ReviewID, r.Rating, r.Comment, r.ReviewDate, " +
            "u.FirstName, u.LastName, r.ListenerID " +
            "FROM dbo.[REVIEW] r " +
            "JOIN dbo.[LISTENER] l ON r.ListenerID = l.ListenerID " +
            "JOIN dbo.[USER] u ON l.ListenerID = u.UserID " +
            "WHERE r.AlbumID = ? " +
            "ORDER BY r.ReviewDate DESC")
          .setParameter(1, albumId)
          .getResultList();

        List<Map<String, Object>> reviews = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> map = new HashMap<>();
            map.put("reviewId", r[0]);
            map.put("rating", r[1]);
            map.put("comment", r[2]);
            map.put("reviewDate", r[3]);
            String firstName = r[4] != null ? r[4].toString() : "Listener";
            String lastName = r[5] != null ? r[5].toString() : "";
            String fullName = (firstName + " " + lastName).trim();
            map.put("displayName", fullName.isEmpty() ? "Anonymous Listener" : fullName);
            map.put("reviewerName", fullName.isEmpty() ? "Anonymous Listener" : fullName);
            map.put("listenerId", r[6]);
            reviews.add(map);
        }
        return reviews;
    }

    /**
     * Check if a listener has already reviewed this album.
     */
    @Transactional(readOnly = true)
    public boolean hasListenerReviewedAlbum(Integer listenerId, Integer albumId) {
        if (listenerId == null || albumId == null) return false;
        Number count = (Number) em.createNativeQuery(
            "SELECT COUNT(*) FROM dbo.[REVIEW] WHERE ListenerID = ? AND AlbumID = ?")
          .setParameter(1, listenerId)
          .setParameter(2, albumId)
          .getSingleResult();
        return count != null && count.intValue() > 0;
    }

    /**
     * Check if a listener is eligible to review this album (purchased and active in library).
     */
    @Transactional(readOnly = true)
    public boolean canListenerReviewAlbum(Integer listenerId, Integer albumId) {
        if (listenerId == null || albumId == null) return false;
        if (hasListenerReviewedAlbum(listenerId, albumId)) return false;
        Number purchaseCount = (Number) em.createNativeQuery(
            "SELECT COUNT(*) FROM dbo.[LIBRARY_ITEMS] li JOIN dbo.[DIGITAL_LIBRARY] dl ON li.LibraryID = dl.LibraryID " +
            "WHERE dl.ListenerID = ? AND li.AlbumID = ? AND li.AccessStatus = 'Active'")
          .setParameter(1, listenerId)
          .setParameter(2, albumId)
          .getSingleResult();
        return purchaseCount != null && purchaseCount.intValue() > 0;
    }

    /**
     * Submits a new review.
     * Rating must be between 1 and 5.
     * ListenerID is taken strictly from the server-side session.
     */
    @Transactional
    public void createReview(Integer listenerId, Integer albumId, int rating, String comment) {
        if (listenerId == null) {
            throw new IllegalArgumentException("You must be logged in to submit a review.");
        }
        if (albumId == null) {
            throw new IllegalArgumentException("Invalid album specified.");
        }
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5 stars.");
        }

        String sanitizedComment = (comment != null) ? comment.trim() : "";

        // Check if the listener has purchased this album and access is active
        Number purchaseCount = (Number) em.createNativeQuery(
            "SELECT COUNT(*) FROM dbo.[LIBRARY_ITEMS] li JOIN dbo.[DIGITAL_LIBRARY] dl ON li.LibraryID = dl.LibraryID " +
            "WHERE dl.ListenerID = ? AND li.AlbumID = ? AND li.AccessStatus = 'Active'")
          .setParameter(1, listenerId)
          .setParameter(2, albumId)
          .getSingleResult();
        if (purchaseCount == null || purchaseCount.intValue() == 0) {
            throw new IllegalArgumentException("You can only review albums you have purchased.");
        }

        // Insert new review with ReviewStatus = 'Approved'
        em.createNativeQuery(
            "INSERT INTO dbo.[REVIEW] (ListenerID, AlbumID, Rating, Comment, ReviewDate, ReviewStatus) " +
            "VALUES (?, ?, ?, ?, GETDATE(), 'Approved')")
          .setParameter(1, listenerId)
          .setParameter(2, albumId)
          .setParameter(3, rating)
          .setParameter(4, sanitizedComment)
          .executeUpdate();
    }

    @Transactional
    public void updateReview(Integer listenerId, Integer reviewId, int rating, String comment) {
        if (listenerId == null) throw new IllegalArgumentException("You must be logged in.");
        
        List<?> check = em.createNativeQuery("SELECT ListenerID FROM dbo.[REVIEW] WHERE ReviewID = ?")
            .setParameter(1, reviewId)
            .getResultList();
        
        if (check.isEmpty() || ((Number)check.get(0)).intValue() != listenerId.intValue()) {
            throw new IllegalArgumentException("You can only edit your own reviews.");
        }
        
        if (rating < 1 || rating > 5) throw new IllegalArgumentException("Rating must be between 1 and 5 stars.");
        String sanitizedComment = (comment != null) ? comment.trim() : "";
        
        em.createNativeQuery("UPDATE dbo.[REVIEW] SET Rating = ?, Comment = ?, ReviewDate = GETDATE() WHERE ReviewID = ?")
            .setParameter(1, rating)
            .setParameter(2, sanitizedComment)
            .setParameter(3, reviewId)
            .executeUpdate();
    }

    @Transactional
    public void deleteReview(Integer listenerId, Integer reviewId) {
        if (listenerId == null) throw new IllegalArgumentException("You must be logged in.");
        
        List<?> check = em.createNativeQuery("SELECT ListenerID FROM dbo.[REVIEW] WHERE ReviewID = ?")
            .setParameter(1, reviewId)
            .getResultList();
        
        if (check.isEmpty() || ((Number)check.get(0)).intValue() != listenerId.intValue()) {
            throw new IllegalArgumentException("You can only delete your own reviews.");
        }
        
        em.createNativeQuery("DELETE FROM dbo.[REVIEW] WHERE ReviewID = ?")
            .setParameter(1, reviewId)
            .executeUpdate();
    }

    /**
     * Fetch all reviews submitted by the logged-in listener.
     */
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getReviewsForListener(Integer listenerId) {
        if (listenerId == null) return List.of();

        List<Object[]> rows = em.createNativeQuery(
            "SELECT r.ReviewID, r.Rating, r.Comment, r.ReviewDate, r.ReviewStatus, " +
            "a.AlbumID, a.AlbumTitle, a.CoverImageUrl, COALESCE(art.ArtistName, 'Unknown Artist') " +
            "FROM dbo.[REVIEW] r " +
            "JOIN dbo.[ALBUM] a ON r.AlbumID = a.AlbumID " +
            "LEFT JOIN dbo.[ARTIST] art ON a.ArtistID = art.ArtistID " +
            "WHERE r.ListenerID = ? " +
            "ORDER BY r.ReviewDate DESC")
          .setParameter(1, listenerId)
          .getResultList();

        List<Map<String, Object>> reviews = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> map = new HashMap<>();
            map.put("reviewId", r[0]);
            map.put("rating", r[1]);
            map.put("comment", r[2]);
            map.put("reviewDate", r[3]);
            map.put("reviewStatus", r[4]);
            map.put("albumId", r[5]);
            map.put("albumTitle", r[6]);
            map.put("coverImageUrl", r[7]);
            map.put("artistName", r[8]);
            reviews.add(map);
        }
        return reviews;
    }
}
