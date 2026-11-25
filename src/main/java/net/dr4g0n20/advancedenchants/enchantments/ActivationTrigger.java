package net.dr4g0n20.advancedenchants.enchantments;

import java.util.Locale;

/** Supported non-offensive activation events. */
public enum ActivationTrigger {
    PASSIVE,
    DEFENSE,
    FALL_DAMAGE,
    BLOCK_BREAK,
    FISHING_CATCH;

    public static ActivationTrigger fromConfig(String value) {
        if (value == null) {
            return null;
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
