package com.melodymart.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verification and regression tests for the MelodyMartApplicationSettings Singleton.
 *
 * Demonstrates:
 * 1. Same instance identity across multiple calls (assertSame).
 * 2. Thread-safety and instance consistency under multi-threaded concurrency.
 * 3. Correct default configuration properties.
 * 4. Helper formatting logic with currency symbols.
 * 5. MVC Integration: Global ControllerAdvice automatically injecting the Singleton into models.
 * 6. REST API endpoint returning the Singleton instance data.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class MelodyMartApplicationSettingsTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Singleton Test: Consecutive getInstance() calls return the exact same object reference")
    void testSingletonInstanceIdentity() {
        MelodyMartApplicationSettings first = MelodyMartApplicationSettings.getInstance();
        MelodyMartApplicationSettings second = MelodyMartApplicationSettings.getInstance();

        assertNotNull(first, "First Singleton instance must not be null");
        assertNotNull(second, "Second Singleton instance must not be null");
        assertSame(first, second, "Both calls to getInstance() must return the exact same object instance");
    }

    @Test
    @DisplayName("Singleton Test: Multi-threaded concurrent access returns identical instance")
    void testSingletonMultiThreadedConcurrency() throws InterruptedException {
        final int threadCount = 20;
        final ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch doneLatch = new CountDownLatch(threadCount);
        final List<MelodyMartApplicationSettings> instances = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await(); // Wait for simultaneous start signal
                    instances.add(MelodyMartApplicationSettings.getInstance());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // Trigger all threads simultaneously
        doneLatch.await();      // Await all threads completion
        executorService.shutdown();

        assertThat(instances).hasSize(threadCount);
        MelodyMartApplicationSettings expectedInstance = MelodyMartApplicationSettings.getInstance();

        for (MelodyMartApplicationSettings instance : instances) {
            assertSame(expectedInstance, instance, "Every concurrent thread must receive the exact same Singleton instance");
        }
    }

    @Test
    @DisplayName("Singleton Test: Verify default configuration settings")
    void testDefaultSettings() {
        MelodyMartApplicationSettings settings = MelodyMartApplicationSettings.getInstance();

        assertEquals("MelodyMart", settings.getStoreName());
        assertEquals("Digital Music Store", settings.getStoreTagline());
        assertEquals("$", settings.getCurrencySymbol());
        assertEquals("USD", settings.getCurrencyCode());
        assertEquals(12, settings.getDefaultPageSize());
        assertEquals(10, settings.getMaxCartQuantityPerItem());
        assertEquals("support@melodymart.com", settings.getSupportEmail());
        assertEquals("1.0.0", settings.getAppVersion());
    }

    @Test
    @DisplayName("Singleton Test: Verify price formatting helper")
    void testPriceFormatting() {
        MelodyMartApplicationSettings settings = MelodyMartApplicationSettings.getInstance();

        String formatted1 = settings.formatPrice(new BigDecimal("19.99"));
        assertEquals("$19.99", formatted1);

        String formatted2 = settings.formatPrice(0.00);
        assertEquals("$0.00", formatted2);

        String formatted3 = settings.formatPrice((BigDecimal) null);
        assertEquals("$0.00", formatted3);
    }

    @Test
    @DisplayName("Integration Test: Global ControllerAdvice injects Singleton instance into Thymeleaf model")
    void testControllerAdviceModelInjection() throws Exception {
        mockMvc.perform(get("/albums"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("appSettings"))
                .andExpect(model().attribute("appSettings", MelodyMartApplicationSettings.getInstance()))
                .andExpect(model().attribute("defaultPageSize", 12));
    }

    @Test
    @DisplayName("Integration Test: /api/settings endpoint returns Singleton configuration")
    void testApiSettingsEndpoint() throws Exception {
        mockMvc.perform(get("/api/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storeName").value("MelodyMart"))
                .andExpect(jsonPath("$.currencySymbol").value("$"))
                .andExpect(jsonPath("$.defaultPageSize").value(12))
                .andExpect(jsonPath("$.supportEmail").value("support@melodymart.com"));
    }
}
