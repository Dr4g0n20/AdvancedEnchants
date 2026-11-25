package net.dr4g0n20.advancedenchants.enchantments;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Stores custom enchantments in ItemMeta's localized name.
 *
 * Minecraft 1.12.2 does not provide PersistentDataContainer. The localized
 * name is not displayed as normal item lore/name text, so it provides a
 * metadata slot without adding visible tooltip clutter.
 *
 * Format:
 *   AE1:<base64-original-localized-name>:<id>=<level>,<id>=<level>
 *
 * Enchantment order is the order of entries in the payload.
 */
public final class ItemEnchantmentStorage {

    private static final String PREFIX = "AE1:";
    private static final String EMPTY_ORIGINAL = "-";

    private ItemEnchantmentStorage() {
    }

    public static List<StoredEnchantment> getEnchantments(ItemStack item) {
        ItemMeta meta = getMeta(item);
        if (meta == null || !meta.hasLocalizedName()) {
            return Collections.emptyList();
        }

        String value = meta.getLocalizedName();
        if (value == null || !value.startsWith(PREFIX)) {
            return Collections.emptyList();
        }

        ParsedData parsed = parse(value);
        if (parsed == null) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(parsed.enchantments);
    }

    public static int getLevel(ItemStack item, String enchantmentId) {
        if (enchantmentId == null) {
            return 0;
        }
        String id = enchantmentId.toLowerCase(Locale.ENGLISH);
        for (StoredEnchantment enchantment : getEnchantments(item)) {
            if (enchantment.getId().equals(id)) {
                return enchantment.getLevel();
            }
        }
        return 0;
    }

    public static boolean hasEnchantment(ItemStack item, String enchantmentId) {
        return getLevel(item, enchantmentId) > 0;
    }

    /**
     * Adds or updates an enchantment. Updating an existing entry does not
     * change its position in the item's application order.
     */
    public static boolean setEnchantment(ItemStack item, String enchantmentId, int level) {
        if (item == null || item.getType().isAir() || enchantmentId == null
                || enchantmentId.trim().isEmpty() || level < 1) {
            return false;
        }

        ItemMeta meta = getMeta(item);
        if (meta == null) {
            return false;
        }

        ParsedData parsed = readData(meta);
        List<StoredEnchantment> entries = new ArrayList<StoredEnchantment>(parsed.enchantments);
        String id = enchantmentId.toLowerCase(Locale.ENGLISH);
        boolean replaced = false;

        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getId().equals(id)) {
                entries.set(i, new StoredEnchantment(id, level));
                replaced = true;
                break;
            }
        }

        if (!replaced) {
            entries.add(new StoredEnchantment(id, level));
        }

        return write(meta, item, parsed.originalLocalizedName, entries);
    }

    public static boolean removeEnchantment(ItemStack item, String enchantmentId) {
        if (item == null || enchantmentId == null) {
            return false;
        }

        ItemMeta meta = getMeta(item);
        if (meta == null) {
            return false;
        }

        ParsedData parsed = readData(meta);
        List<StoredEnchantment> entries = new ArrayList<StoredEnchantment>(parsed.enchantments);
        String id = enchantmentId.toLowerCase(Locale.ENGLISH);
        boolean removed = false;

        for (int i = entries.size() - 1; i >= 0; i--) {
            if (entries.get(i).getId().equals(id)) {
                entries.remove(i);
                removed = true;
            }
        }

        if (!removed) {
            return false;
        }

        return write(meta, item, parsed.originalLocalizedName, entries);
    }

    public static boolean clearEnchantments(ItemStack item) {
        if (item == null) {
            return false;
        }

        ItemMeta meta = getMeta(item);
        if (meta == null) {
            return false;
        }

        ParsedData parsed = readData(meta);
        if (parsed.enchantments.isEmpty()) {
            return false;
        }

        if (parsed.originalLocalizedName == null) {
            meta.setLocalizedName(null);
        } else {
            meta.setLocalizedName(parsed.originalLocalizedName);
        }
        item.setItemMeta(meta);
        return true;
    }

    public static boolean hasStoredData(ItemStack item) {
        ItemMeta meta = getMeta(item);
        return meta != null && meta.hasLocalizedName()
                && meta.getLocalizedName() != null
                && meta.getLocalizedName().startsWith(PREFIX);
    }

    private static ItemMeta getMeta(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) {
            return item == null || item.getType().isAir() ? null : item.getItemMeta();
        }
        return item.getItemMeta();
    }

    private static ParsedData readData(ItemMeta meta) {
        if (meta == null || !meta.hasLocalizedName()) {
            return new ParsedData(null, new ArrayList<StoredEnchantment>());
        }

        String value = meta.getLocalizedName();
        if (value == null || !value.startsWith(PREFIX)) {
            return new ParsedData(value, new ArrayList<StoredEnchantment>());
        }

        ParsedData parsed = parse(value);
        return parsed == null
                ? new ParsedData(value, new ArrayList<StoredEnchantment>())
                : parsed;
    }

    private static ParsedData parse(String value) {
        try {
            String[] parts = value.split(":", 3);
            if (parts.length != 3 || !"AE1".equals(parts[0])) {
                return null;
            }

            String original = EMPTY_ORIGINAL.equals(parts[1])
                    ? null
                    : new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);

            List<StoredEnchantment> entries = new ArrayList<StoredEnchantment>();
            if (!parts[2].isEmpty()) {
                String[] encodedEntries = parts[2].split(",");
                for (String encoded : encodedEntries) {
                    int separator = encoded.lastIndexOf('=');
                    if (separator <= 0 || separator == encoded.length() - 1) {
                        return null;
                    }

                    String id = encoded.substring(0, separator);
                    int level = Integer.parseInt(encoded.substring(separator + 1));
                    if (id.trim().isEmpty() || level < 1) {
                        return null;
                    }

                    entries.add(new StoredEnchantment(id, level));
                }
            }

            return new ParsedData(original, entries);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static boolean write(ItemMeta meta, ItemStack item, String original,
                                 List<StoredEnchantment> entries) {
        if (entries.isEmpty()) {
            meta.setLocalizedName(original);
            item.setItemMeta(meta);
            return true;
        }

        String encodedOriginal = original == null
                ? EMPTY_ORIGINAL
                : Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(original.getBytes(StandardCharsets.UTF_8));

        StringBuilder payload = new StringBuilder(PREFIX)
                .append(encodedOriginal)
                .append(':');

        for (int i = 0; i < entries.size(); i++) {
            if (i > 0) {
                payload.append(',');
            }
            StoredEnchantment enchantment = entries.get(i);
            payload.append(enchantment.getId())
                    .append('=')
                    .append(enchantment.getLevel());
        }

        meta.setLocalizedName(payload.toString());
        item.setItemMeta(meta);
        return true;
    }

    private static final class ParsedData {
        private final String originalLocalizedName;
        private final List<StoredEnchantment> enchantments;

        private ParsedData(String originalLocalizedName,
                           List<StoredEnchantment> enchantments) {
            this.originalLocalizedName = originalLocalizedName;
            this.enchantments = enchantments;
        }
    }
}
