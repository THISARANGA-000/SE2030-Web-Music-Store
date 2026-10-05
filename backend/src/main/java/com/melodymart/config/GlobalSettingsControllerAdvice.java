package com.melodymart.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Controller Advice that exposes the MelodyMartApplicationSettings Singleton
 * to all Spring MVC / Thymeleaf views globally under the model attribute 'appSettings'.
 *
 * This allows Thymeleaf templates to access application-level settings such as:
 * - ${appSettings.storeName}
 * - ${appSettings.storeTagline}
 * - ${appSettings.currencySymbol}
 * - ${appSettings.supportEmail}
 * - ${appSettings.defaultPageSize}
 */
@ControllerAdvice
public class GlobalSettingsControllerAdvice {

    /**
     * Supplies the shared MelodyMartApplicationSettings Singleton instance to the model.
     *
     * @return the unique MelodyMartApplicationSettings instance obtained via getInstance()
     */
    @ModelAttribute("appSettings")
    public MelodyMartApplicationSettings populateAppSettings() {
        return MelodyMartApplicationSettings.getInstance();
    }
}
