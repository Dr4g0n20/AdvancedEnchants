package net.dr4g0n20.advancedenchants.guidebook;

import net.dr4g0n20.advancedenchants.enchantments.EnchantmentDefinition;
import net.dr4g0n20.advancedenchants.enchantments.EnchantmentRegistry;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Creates the public, always-complete AdvancedEnchants written guidebook. */
public final class GuidebookService {

    public static final String BOOK_TITLE = "AE Enchant Guide";
    private static final int PAGE_LIMIT = 240;

    private final EnchantmentRegistry registry;

    public GuidebookService(EnchantmentRegistry registry) {
        this.registry = registry;
    }

    public ItemStack createBook() {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK, 1);
        refresh(book);
        return book;
    }

    public void giveTo(Player player) {
        ItemStack book = createBook();
        PlayerInventory inventory = player.getInventory();
        Map<Integer, ItemStack> overflow = inventory.addItem(book);
        for (ItemStack item : overflow.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        }
        player.sendMessage(ChatColor.GREEN + "Here is the AdvancedEnchants guidebook.");
    }

    /** Rebuilds the pages from the live registry, so newly enabled entries appear. */
    public boolean refresh(ItemStack item) {
        if (item == null || item.getType() != Material.WRITTEN_BOOK
                || !item.hasItemMeta() || !(item.getItemMeta() instanceof BookMeta)) {
            return false;
        }

        BookMeta meta = (BookMeta) item.getItemMeta();
        if (!BOOK_TITLE.equals(meta.getTitle())) {
            return false;
        }

        List<String> pages = new ArrayList<String>();
        List<EnchantmentDefinition> all = new ArrayList<EnchantmentDefinition>(registry.getAll());
        pages.add(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "AdvancedEnchants\n"
                + ChatColor.BLACK + "Complete Enchantment Guide\n\n"
                + ChatColor.DARK_GRAY + "Every enabled enchantment is listed. "
                + "Entries are visible immediately; there are no discovery locks.");

        List<String> categories = new ArrayList<String>();
        List<String> preferred = Arrays.asList("armour", "tools", "utility", "weapons");
        for (String category : preferred) {
            for (EnchantmentDefinition definition : all) {
                if (category.equalsIgnoreCase(definition.getCategory())
                        && !categories.contains(category)) {
                    categories.add(category);
                    break;
                }
            }
        }
        java.util.Set<String> additionalCategories = new java.util.TreeSet<String>(
                String.CASE_INSENSITIVE_ORDER);
        for (EnchantmentDefinition definition : all) {
            String category = definition.getCategory();
            if (category != null && !categories.contains(category.toLowerCase(Locale.ENGLISH))) {
                additionalCategories.add(category);
            }
        }
        categories.addAll(additionalCategories);

        List<String> contents = new ArrayList<String>();
        for (String category : categories) {
            contents.add(titleCase(category));
        }
        contents.add("Alphabetical Index");
        contents.add("Incompatibilities");
        addSection(pages, ChatColor.DARK_BLUE + "Contents", contents);

        for (String category : categories) {
            List<EnchantmentDefinition> entries = new ArrayList<EnchantmentDefinition>();
            for (EnchantmentDefinition definition : all) {
                if (category.equalsIgnoreCase(definition.getCategory())) {
                    entries.add(definition);
                }
            }
            if (!entries.isEmpty()) {
                Collections.sort(entries, ENCHANTMENT_ORDER);
                addText(pages, ChatColor.DARK_BLUE + "" + ChatColor.BOLD
                        + titleCase(category));
                for (EnchantmentDefinition definition : entries) {
                    addEnchantment(pages, definition);
                }
            }
        }

        List<EnchantmentDefinition> alphabetical =
                new ArrayList<EnchantmentDefinition>(all);
        Collections.sort(alphabetical, new Comparator<EnchantmentDefinition>() {
            @Override
            public int compare(EnchantmentDefinition a, EnchantmentDefinition b) {
                return a.getDisplayName().compareToIgnoreCase(b.getDisplayName());
            }
        });
        addText(pages, ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "Alphabetical Index");
        for (EnchantmentDefinition definition : alphabetical) {
            addText(pages, ChatColor.BLACK + definition.getDisplayName()
                    + ChatColor.DARK_GRAY + " - " + titleCase(definition.getCategory()));
        }

        addText(pages, ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "Incompatibilities");
        boolean foundConflict = false;
        for (EnchantmentDefinition definition : alphabetical) {
            if (!definition.getIncompatibleIds().isEmpty()) {
                foundConflict = true;
                List<String> names = new ArrayList<String>();
                for (String id : definition.getIncompatibleIds()) {
                    EnchantmentDefinition conflict = registry.get(id);
                    names.add(conflict == null ? id : conflict.getDisplayName());
                }
                Collections.sort(names, String.CASE_INSENSITIVE_ORDER);
                addText(pages, ChatColor.BLACK + definition.getDisplayName()
                        + ChatColor.DARK_GRAY + ": " + join(names));
            }
        }
        if (!foundConflict) {
            addText(pages, ChatColor.DARK_GRAY + "No incompatibilities are currently configured.");
        }

        meta.setTitle(BOOK_TITLE);
        meta.setAuthor("AdvancedEnchants");
        meta.setPages(pages);
        item.setItemMeta(meta);
        return true;
    }

    private void addEnchantment(List<String> pages, EnchantmentDefinition definition) {
        String rarity = definition.getRarity().toUpperCase(Locale.ENGLISH);
        ChatColor rarityColor = rarityColor(rarity);
        addText(pages, rarityColor + "" + ChatColor.BOLD + definition.getDisplayName()
                + ChatColor.BLACK + "  " + ChatColor.GRAY + rarity);
        addText(pages, ChatColor.BLACK + "Maximum level: " + definition.getMaximumLevel());
        addText(pages, ChatColor.BLACK + "Applies to: "
                + joinMaterials(definition.getApplicableMaterials()));
        addText(pages, ChatColor.DARK_GRAY + definition.getDescription());
    }

    private void addSection(List<String> pages, String heading, List<String> lines) {
        addText(pages, heading);
        for (String line : lines) {
            addText(pages, ChatColor.BLACK + "- " + line);
        }
    }

    /**
     * Appends text to the current page where possible instead of creating a
     * new page for every line. Minecraft 1.12.2 written books have a strict
     * page-size limit, so keeping each page below 240 characters is safer.
     */
    private void addText(List<String> pages, String text) {
        if (text == null || text.isEmpty()) {
            return;
        }

        String remaining = text;
        while (!remaining.isEmpty()) {
            String currentPage = pages.isEmpty() ? "" : pages.get(pages.size() - 1);
            int separatorLength = currentPage.isEmpty() ? 0 : 1;
            int available = PAGE_LIMIT - currentPage.length() - separatorLength;

            if (available <= 0) {
                pages.add("");
                continue;
            }

            if (remaining.length() <= available) {
                if (pages.isEmpty()) {
                    pages.add(remaining);
                } else if (currentPage.isEmpty()) {
                    pages.set(pages.size() - 1, remaining);
                } else {
                    pages.set(pages.size() - 1, currentPage + "\\n" + remaining);
                }
                return;
            }

            int split = remaining.lastIndexOf(' ', available);
            if (split < 1) {
                split = available;
            }

            String part = remaining.substring(0, split);
            if (pages.isEmpty()) {
                pages.add(part);
            } else if (currentPage.isEmpty()) {
                pages.set(pages.size() - 1, part);
            } else {
                pages.set(pages.size() - 1, currentPage + "\\n" + part);
            }

            remaining = ChatColor.getLastColors(part)
                    + remaining.substring(split).trim();
        }
    }

    private static String joinMaterials(Iterable<Material> materials) {
        List<String> names = new ArrayList<String>();
        for (Material material : materials) {
            names.add(titleCase(material.name().replace('_', ' ')));
        }
        Collections.sort(names, String.CASE_INSENSITIVE_ORDER);
        return names.isEmpty() ? "Not specified" : join(names);
    }

    private static String join(List<String> values) {
        StringBuilder result = new StringBuilder();
        for (String value : values) {
            if (result.length() > 0) {
                result.append(", ");
            }
            result.append(value);
        }
        return result.toString();
    }

    private static String titleCase(String value) {
        String[] words = value.toLowerCase(Locale.ENGLISH).replace('_', ' ').split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (result.length() > 0) {
                result.append(' ');
            }
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return result.toString();
    }

    private static ChatColor rarityColor(String rarity) {
        if ("COMMON".equals(rarity)) return ChatColor.GRAY;
        if ("UNCOMMON".equals(rarity)) return ChatColor.AQUA;
        if ("RARE".equals(rarity)) return ChatColor.GREEN;
        if ("EPIC".equals(rarity)) return ChatColor.LIGHT_PURPLE;
        if ("LEGENDARY".equals(rarity)) return ChatColor.GOLD;
        if ("MYTHIC".equals(rarity)) return ChatColor.RED;
        return ChatColor.DARK_GRAY;
    }

    private static final Comparator<EnchantmentDefinition> ENCHANTMENT_ORDER =
            new Comparator<EnchantmentDefinition>() {
                @Override
                public int compare(EnchantmentDefinition a, EnchantmentDefinition b) {
                    int rarity = Integer.compare(rarityRank(a.getRarity()), rarityRank(b.getRarity()));
                    return rarity != 0 ? rarity
                            : a.getDisplayName().compareToIgnoreCase(b.getDisplayName());
                }
            };

    private static int rarityRank(String rarity) {
        String value = rarity.toUpperCase(Locale.ENGLISH);
        if ("COMMON".equals(value)) return 0;
        if ("UNCOMMON".equals(value)) return 1;
        if ("RARE".equals(value)) return 2;
        if ("EPIC".equals(value)) return 3;
        if ("LEGENDARY".equals(value)) return 4;
        if ("MYTHIC".equals(value)) return 5;
        return 6;
    }
}
