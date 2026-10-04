package com.melodymart.config;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * Singleton Design Pattern Implementation for MelodyMart.
 *
 * Responsibility:
 * Centralized manager for application-level settings, store metadata,
 * and global display configuration (e.g., store name, currency symbol,
 * default catalog pagination limits, and support contact details).
 *
 * Design Pattern Characteristics:
 * 1. Private constructor: Prevents direct external instantiation via the {@code new} keyword.
 * 2. Static volatile instance: Holds the single global instance and ensures visibility across threads.
 * 3. Double-Checked Locking in {@link #getInstance()}: Ensures lazy, thread-safe initialization.
 * 4. Encapsulated instance state: Settings are accessed through instance methods.
 */
public final class MelodyMartApplicationSettings {

    private static volatile MelodyMartApplicationSettings instance;

    // Application-level configuration properties
    private String storeName;
    private String storeTagline;
    private String currencySymbol;
    private String currencyCode;
    private int defaultPageSize;
    private int maxCartQuantityPerItem;
    private String supportEmail;
    private String appVersion;

    /**
     * Private constructor to prevent direct instantiation from other classes.
     * Initializes the default application configuration.
     */
    private MelodyMartApplicationSettings() {
        this.storeName = "MelodyMart";
        this.storeTagline = "Digital Music Store";
        this.currencySymbol = "$";
        this.currencyCode = "USD";
        this.defaultPageSize = 12;
        this.maxCartQuantityPerItem = 10;
        this.supportEmail = "support@melodymart.com";
        this.appVersion = "1.0.0";
    }

    /**
     * Provides global access point to the single instance of MelodyMartApplicationSettings.
     * Uses double-checked locking for thread-safe lazy initialization.
     *
     * @return the unique shared MelodyMartApplicationSettings instance
     */
    public static MelodyMartApplicationSettings getInstance() {
        if (instance == null) {
            synchronized (MelodyMartApplicationSettings.class) {
                if (instance == null) {
                    instance = new MelodyMartApplicationSettings();
                }
            }
        }
        return instance;
    }

    // ─── Getters ─────────────────────────────────────────────────────────────

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        if (storeName != null && !storeName.trim().isEmpty()) {
            this.storeName = storeName.trim();
        }
    }

    public String getStoreTagline() {
        return storeTagline;
    }

    public void setStoreTagline(String storeTagline) {
        if (storeTagline != null) {
            this.storeTagline = storeTagline.trim();
        }
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        if (currencySymbol != null && !currencySymbol.trim().isEmpty()) {
            this.currencySymbol = currencySymbol.trim();
        }
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        if (currencyCode != null && !currencyCode.trim().isEmpty()) {
            this.currencyCode = currencyCode.trim().toUpperCase();
        }
    }

    public int getDefaultPageSize() {
        return defaultPageSize;
    }

    public void setDefaultPageSize(int defaultPageSize) {
        if (defaultPageSize > 0) {
            this.defaultPageSize = defaultPageSize;
        }
    }

    public int getMaxCartQuantityPerItem() {
        return maxCartQuantityPerItem;
    }

    public void setMaxCartQuantityPerItem(int maxCartQuantityPerItem) {
        if (maxCartQuantityPerItem > 0) {
            this.maxCartQuantityPerItem = maxCartQuantityPerItem;
        }
    }

    public String getSupportEmail() {
        return supportEmail;
    }

    public void setSupportEmail(String supportEmail) {
        if (supportEmail != null && !supportEmail.trim().isEmpty()) {
            this.supportEmail = supportEmail.trim();
        }
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        if (appVersion != null && !appVersion.trim().isEmpty()) {
            this.appVersion = appVersion.trim();
        }
    }

    /**
     * Helper method to format a monetary amount with the configured currency symbol.
     *
     * @param amount the price amount
     * @return formatted price string, e.g. "$19.99"
     */
    public String formatPrice(BigDecimal amount) {
        if (amount == null) {
            return currencySymbol + "0.00";
        }
        DecimalFormat df = new DecimalFormat("#,##0.00");
        return currencySymbol + df.format(amount);
    }

    /**
     * Helper method to format a double price with the configured currency symbol.
     *
     * @param amount the price amount
     * @return formatted price string, e.g. "$19.99"
     */
    public String formatPrice(double amount) {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        return currencySymbol + df.format(amount);
    }

    @Override
    public String toString() {
        return "MelodyMartApplicationSettings{" +
                "storeName='" + storeName + '\'' +
                ", storeTagline='" + storeTagline + '\'' +
                ", currencySymbol='" + currencySymbol + '\'' +
                ", currencyCode='" + currencyCode + '\'' +
                ", defaultPageSize=" + defaultPageSize +
                ", maxCartQuantityPerItem=" + maxCartQuantityPerItem +
                ", supportEmail='" + supportEmail + '\'' +
                ", appVersion='" + appVersion + '\'' +
                '}';
    }
}
