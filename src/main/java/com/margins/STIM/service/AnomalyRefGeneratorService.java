/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.service;

import com.margins.STIM.entity.AccessAnomaly;
import jakarta.ejb.Stateless;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import java.math.BigInteger;
import java.time.LocalDate;

/**
 *
 * @author PhilipManteAsare
 */
@Stateless
@Named
public class AnomalyRefGeneratorService {

    @PersistenceContext
    private EntityManager entityManager;

    private static final String ANOMALY_PREFIX = "AN";
    private static final int REF_LENGTH = 6;

    /**
     * Generates a unique anomaly reference ID Format: AN000001, AN000002, etc.
     */
    public String generateUniqueAnomalyRef() {
        String anomalyRef;
        boolean isUnique = false;
        int attempts = 0;
        int maxAttempts = 50;

        do {
            anomalyRef = generateAnomalyRef();
            isUnique = isAnomalyRefUnique(anomalyRef);
            attempts++;

            if (attempts > maxAttempts) {
                // Fallback to timestamp-based generation
                long timestamp = System.currentTimeMillis();
                anomalyRef = String.format("%s%0" + REF_LENGTH + "d",
                        ANOMALY_PREFIX, timestamp % 1000000);
                break;
            }

        } while (!isUnique && attempts < maxAttempts);

        return anomalyRef;
    }

    /**
     * Generates anomaly ref based on next available number
     */
    private String generateAnomalyRef() {
        try {
            // Get the highest existing number
            String maxRef = (String) entityManager
                    .createQuery("SELECT MAX(a.anomalyRef) FROM AccessAnomaly a WHERE a.anomalyRef IS NOT NULL")
                    .getSingleResult();

            long nextNumber = 1;

            if (maxRef != null && maxRef.startsWith(ANOMALY_PREFIX)) {
                // Extract number part and increment
                String numberPart = maxRef.substring(ANOMALY_PREFIX.length());
                try {
                    nextNumber = Long.parseLong(numberPart) + 1;
                } catch (NumberFormatException e) {
                    // If parsing fails, get count + 1
                    nextNumber = getAnomalyCount() + 1;
                }
            } else {
                // No existing refs, start from 1 or use count
                nextNumber = getAnomalyCount() + 1;
            }

            return String.format("%s%0" + REF_LENGTH + "d", ANOMALY_PREFIX, nextNumber);

        } catch (Exception e) {
            // Fallback to count-based generation
            long count = getAnomalyCount();
            return String.format("%s%0" + REF_LENGTH + "d", ANOMALY_PREFIX, count + 1);
        }
    }

    /**
     * Alternative: Database sequence-based generation (most reliable)
     */
    public String generateSequenceBasedAnomalyRef() {
        try {
            // First, try to use database sequence
            BigInteger nextVal = (BigInteger) entityManager
                    .createNativeQuery("SELECT NEXTVAL('anomaly_ref_seq')")
                    .getSingleResult();

            return String.format("%s%0" + REF_LENGTH + "d", ANOMALY_PREFIX, nextVal.longValue());

        } catch (Exception e) {
            // Fallback to regular generation if sequence doesn't exist
            return generateUniqueAnomalyRef();
        }
    }

    /**
     * Date-based anomaly reference generation Format: ANOM-2024-000001
     */
    public String generateDateBasedAnomalyRef() {
        try {
            LocalDate today = LocalDate.now();
            String datePrefix = String.format("ANOM-%d", today.getYear());

            // Count today's anomalies
            Long todayCount = (Long) entityManager
                    .createQuery("SELECT COUNT(a) FROM AccessAnomaly a WHERE DATE(a.timestamp) = CURRENT_DATE")
                    .getSingleResult();

            return String.format("%s-%06d", datePrefix, todayCount + 1);

        } catch (Exception e) {
            // Fallback
            return generateUniqueAnomalyRef();
        }
    }

    /**
     * Check if anomaly reference is unique
     */
    private boolean isAnomalyRefUnique(String anomalyRef) {
        try {
            Long count = (Long) entityManager
                    .createQuery("SELECT COUNT(a) FROM AccessAnomaly a WHERE a.anomalyRef = :anomalyRef")
                    .setParameter("anomalyRef", anomalyRef)
                    .getSingleResult();

            return count == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get total count of anomalies
     */
    private long getAnomalyCount() {
        try {
            Long count = (Long) entityManager
                    .createQuery("SELECT COUNT(a) FROM AccessAnomaly a")
                    .getSingleResult();
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Find anomaly by reference ID
     */
    public AccessAnomaly findByAnomalyRef(String anomalyRef) {
        try {
            return entityManager
                    .createQuery("SELECT a FROM AccessAnomaly a WHERE a.anomalyRef = :anomalyRef", AccessAnomaly.class)
                    .setParameter("anomalyRef", anomalyRef)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
