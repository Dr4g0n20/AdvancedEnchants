package net.dr4g0n20.advancedenchants.enchantments;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/** Tracks per-player, per-enchantment cooldowns using server time. */
public final class EnchantmentCooldownManager {

    private final Map<String, Long> expirations = new HashMap<String, Long>();

    public boolean isOnCooldown(UUID playerId, String enchantmentId) {
        if (playerId == null || enchantmentId == null) {
            return false;
        }
        Long expiry = expirations.get(key(playerId, enchantmentId));
        if (expiry == null) {
            return false;
        }
        if (expiry.longValue() <= System.currentTimeMillis()) {
            expirations.remove(key(playerId, enchantmentId));
            return false;
        }
        return true;
    }

    public void startCooldown(UUID playerId, String enchantmentId, int seconds) {
        if (playerId == null || enchantmentId == null || seconds <= 0) {
            return;
        }
        long duration = seconds * 1000L;
        long now = System.currentTimeMillis();
        long expiry = now > Long.MAX_VALUE - duration ? Long.MAX_VALUE : now + duration;
        expirations.put(key(playerId, enchantmentId), Long.valueOf(expiry));
    }

    public long getRemainingMillis(UUID playerId, String enchantmentId) {
        if (playerId == null || enchantmentId == null) {
            return 0L;
        }
        Long expiry = expirations.get(key(playerId, enchantmentId));
        if (expiry == null) {
            return 0L;
        }
        long remaining = expiry.longValue() - System.currentTimeMillis();
        if (remaining <= 0L) {
            expirations.remove(key(playerId, enchantmentId));
            return 0L;
        }
        return remaining;
    }

    public void clearPlayer(UUID playerId) {
        if (playerId == null) {
            return;
        }
        String prefix = playerId.toString() + ":";
        Iterator<String> iterator = expirations.keySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().startsWith(prefix)) {
                iterator.remove();
            }
        }
    }

    public void clearExpired() {
        long now = System.currentTimeMillis();
        Iterator<Long> iterator = expirations.values().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().longValue() <= now) {
                iterator.remove();
            }
        }
    }

    private String key(UUID playerId, String enchantmentId) {
        return playerId.toString() + ":" + enchantmentId.toLowerCase(java.util.Locale.ENGLISH);
    }
}
