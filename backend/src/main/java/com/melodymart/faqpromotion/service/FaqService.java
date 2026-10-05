package com.melodymart.faqpromotion.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FaqService {

    @PersistenceContext
    private EntityManager em;

    /**
     * Fetch all published FAQ entries for listeners.
     */
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getPublishedFaqs() {
        List<Object[]> rows = em.createNativeQuery(
            "SELECT FAQID, Question, Answer, FAQStatus, CreatedDate " +
            "FROM dbo.[FAQ] " +
            "WHERE FAQStatus = 'Published' " +
            "ORDER BY FAQID ASC")
          .getResultList();

        List<Map<String, Object>> faqs = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> map = new HashMap<>();
            map.put("faqId", r[0]);
            map.put("question", r[1]);
            map.put("answer", r[2]);
            map.put("faqStatus", r[3]);
            map.put("createdDate", r[4]);
            faqs.add(map);
        }
        return faqs;
    }
}
