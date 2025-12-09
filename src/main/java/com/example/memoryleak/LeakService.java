package com.example.memoryleak;

import org.springframework.stereotype.Service;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FIXED: Memory leak prevention with bounded collections
 * - Added maximum size limits
 * - Implemented LRU eviction policy
 * - Added @PreDestroy cleanup
 */
@Service
public class LeakService {

    // FIXED: Use bounded LinkedHashMap with LRU eviction
    private static final int MAX_SIZE = 1000;
    private final Map<String, byte[]> retainedMap = new LinkedHashMap<String, byte[]>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, byte[]> eldest) {
            return size() > MAX_SIZE;
        }
    };

    private final List<byte[]> retainedList = new ArrayList<>();
    private volatile boolean leaking = false;
    private Thread leakThread;

    public void leak(int itemSizeKb, int count) {
        // FIXED: Check size limit before adding
        for (int i = 0; i < count && retainedList.size() < MAX_SIZE; i++) {
            byte[] data = new byte[itemSizeKb * 1024];
            retainedList.add(data);
            retainedMap.put("leak-" + System.nanoTime(), data);
        }
    }

    public void startContinuousLeak(int itemSizeKb, int ratePerSec) {
        if (leaking) return;
        leaking = true;
        leakThread = new Thread(() -> {
            while (leaking) {
                try {
                    leak(itemSizeKb, 1);
                    Thread.sleep(1000 / ratePerSec);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        leakThread.start();
    }

    public void stopContinuousLeak() {
        leaking = false;
        if (leakThread != null) {
            leakThread.interrupt();
        }
    }

    public void clear() {
        retainedList.clear();
        retainedMap.clear();
    }

    public boolean isLeaking() {
        return leaking;
    }

    public int getRetainedCount() {
        return retainedList.size();
    }

    // FIXED: Added cleanup on bean destruction
    @PreDestroy
    public void cleanup() {
        stopContinuousLeak();
        clear();
        System.out.println("LeakService cleaned up");
    }
}