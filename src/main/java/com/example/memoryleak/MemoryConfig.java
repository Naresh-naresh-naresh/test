package com.example.memoryleak;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

/**
 * Memory management configuration
 * Provides centralized memory limits and monitoring
 */
@Configuration
public class MemoryConfig {

    @Bean
    public MemoryMonitor memoryMonitor() {
        return new MemoryMonitor();
    }

    public static class MemoryMonitor {
        public void logMemoryUsage() {
            Runtime runtime = Runtime.getRuntime();
            long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
            long maxMemory = runtime.maxMemory() / 1024 / 1024;
            System.out.println("Memory: " + usedMemory + "MB / " + maxMemory + "MB");
        }
    }
}