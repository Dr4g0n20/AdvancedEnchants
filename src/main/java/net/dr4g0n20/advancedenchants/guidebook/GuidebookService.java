package net.dr4g0n20.advancedenchants.guidebook;

import net.dr4g0n20.advancedenchants.enchantments.EnchantmentDefinition;
import net.dr4g0n20.advancedenchants.enchantments.EnchantmentRegistry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Builds the interactive chest-based AdvancedEnchants catalogue. */
public final class GuidebookService {

    private static final int SIZE = 54;
    private static final int CONTENT_SLOTS = 45;
    private static final int PREVIOUS_SLOT = 45;
    private static final int HOME_SLOT = 49;
    private static final int NEXT_SLOT = 53;

    private final EnchantmentRegistry registry;

    public GuidebookService(EnchantmentRegistry registry) {
        this.registry = registry;
    }

    public void open(Player player) {
        openMain(player);
    }

    public void openMain(Player player) {
        GuidebookMenuHolder holder = new GuidebookMenuHolder(
                GuidebookMenuHolder.View.MAIN, null, null, 0);
        Inventory inventory = createInventory(holder, ChatColor.DARK_GRAY + "AE Enchantments");
        fillBorder(inventory);

        List<String> categories = getCategories();
        Material[] icons = {Material.IRON_CHESTPLATE, Material.IRON_PICKAXE,
                Material.FISHING_ROD, Material.WHEAT, Material.BOOK};
        int[] slots = {20, 22, 24, 30, 32};
        for (int i = 0; i < categories.size(); i++) {
            String category = categories.get(i);
            Material icon = icons[i % icons.length];
            inventory.setItem(slots[i % slots.length], item(icon,
                    ChatColor.AQUA + titleCase(category),
                    Arrays.asList(ChatColor.GRAY + "Browse " + countCategory(category)
                            + " enchantments", ChatColor.YELLOW + "Click to open")));
        }

        inventory.setItem(4, item(Material.ENCHANTED_BOOK, ChatColor.GOLD + "All Enchantments",
                Arrays.asList(ChatColor.GRAY + "Browse every enabled enchantment",
                        ChatColor.YELLOW + "Click to open")));
        inventory.setItem(40, item(Material.BOOK_AND_QUILL, ChatColor.LIGHT_PURPLE + "Alphabetical Index",
                Arrays.asList(ChatColor.GRAY + "View every enchantment A-Z",
                        ChatColor.YELLOW + "Click to open")));
        inventory.setItem(42, item(Material.ANVIL, ChatColor.RED + "Incompatibilities",
                Arrays.asList(ChatColor.GRAY + "See which enchantments conflict",
                        ChatColor.YELLOW + "Click to open")));
        player.openInventory(inventory);
    }

    public void handleClick(Player player, GuidebookMenuHolder holder, int slot) {
        if (holder.getView() == GuidebookMenuHolder.View.MAIN) {
            if (slot == 4) {
                openCategory(player, "all", 0);
                return;
            }
            if (slot == 40) {
                openIndex(player, 0);
                return;
            }
            if (slot == 42) {
                openConflicts(player, 0);
                return;
            }
            List<String> categories = getCategories();
            int[] slots = {20, 22, 24, 30, 32};
            for (int i = 0; i < categories.size() && i < slots.length; i++) {
                if (slot == slots[i]) {
                    openCategory(player, categories.get(i), 0);
                    return;
                }
            }
            return;
        }

        if (holder.getView() == GuidebookMenuHolder.View.DETAIL) {
            if (slot == HOME_SLOT) {
                if ("all".equals(holder.getCategory())) openCategory(player, "all", 0);
                else if ("index".equals(holder.getCategory())) openIndex(player, 0);
                else if ("conflicts".equals(holder.getCategory())) openConflicts(player, 0);
                else openCategory(player, holder.getCategory(), 0);
            }
            return;
        }

        if (slot == HOME_SLOT) {
            openMain(player);
            return;
        }
        if (slot == PREVIOUS_SLOT && holder.getPage() > 0) {
            openPage(player, holder, holder.getPage() - 1);
            return;
        }
        if (slot == NEXT_SLOT) {
            openPage(player, holder, holder.getPage() + 1);
            return;
        }
        if (slot >= 0 && slot < CONTENT_SLOTS) {
            List<EnchantmentDefinition> entries = entriesFor(holder);
            int index = holder.getPage() * CONTENT_SLOTS + slot;
            if (index >= 0 && index < entries.size()) {
                EnchantmentDefinition definition = entries.get(index);
                openDetail(player, definition, holder.getView(), holder.getCategory(), holder.getPage());
            }
        }
    }

    private void openPage(Player player, GuidebookMenuHolder old, int page) {
        if (old.getView() == GuidebookMenuHolder.View.CATEGORY) {
            openCategory(player, old.getCategory(), page);
        } else if (old.getView() == GuidebookMenuHolder.View.INDEX) {
            openIndex(player, page);
        } else if (old.getView() == GuidebookMenuHolder.View.CONFLICTS) {
            openConflicts(player, page);
        }
    }

    private void openCategory(Player player, String category, int page) {
        GuidebookMenuHolder holder = new GuidebookMenuHolder(
                GuidebookMenuHolder.View.CATEGORY, category, null, page);
        String title = "all".equals(category) ? "All Enchantments" : titleCase(category);
        Inventory inventory = createInventory(holder, ChatColor.DARK_GRAY + title + " (" + (page + 1) + ")");
        populateEntries(inventory, entriesFor(holder), page);
        player.openInventory(inventory);
    }

    private void openIndex(Player player, int page) {
        GuidebookMenuHolder holder = new GuidebookMenuHolder(
                GuidebookMenuHolder.View.INDEX, "index", null, page);
        Inventory inventory = createInventory(holder, ChatColor.DARK_GRAY + "Enchantment Index (" + (page + 1) + ")");
        populateEntries(inventory, entriesFor(holder), page);
        player.openInventory(inventory);
    }

    private void openConflicts(Player player, int page) {
        GuidebookMenuHolder holder = new GuidebookMenuHolder(
                GuidebookMenuHolder.View.CONFLICTS, "conflicts", null, page);
        Inventory inventory = createInventory(holder, ChatColor.DARK_GRAY + "Incompatibilities (" + (page + 1) + ")");
        populateEntries(inventory, entriesFor(holder), page);
        player.openInventory(inventory);
    }

    private void openDetail(Player player, EnchantmentDefinition definition,
                            GuidebookMenuHolder.View sourceView, String category, int page) {
        GuidebookMenuHolder holder = new GuidebookMenuHolder(
                GuidebookMenuHolder.View.DETAIL, category, definition.getId(), page);
        Inventory inventory = createInventory(holder, ChatColor.DARK_GRAY + definition.getDisplayName());
        fillBorder(inventory);

        Material icon = iconFor(definition);
        inventory.setItem(13, item(icon, rarityColor(definition.getRarity()) + definition.getDisplayName(),
                Arrays.asList(ChatColor.GRAY + "Rarity: " + definition.getRarity(),
                        ChatColor.GRAY + "Maximum level: " + definition.getMaximumLevel())));
        inventory.setItem(20, item(Material.PAPER, ChatColor.AQUA + "Description",
                wrapLore(definition.getDescription())));
        inventory.setItem(22, item(Material.CHEST, ChatColor.GREEN + "Applicable Items",
                splitMaterials(definition.getApplicableMaterials())));
        List<String> conflicts = new ArrayList<String>();
        for (String id : definition.getIncompatibleIds()) {
            EnchantmentDefinition other = registry.get(id);
            conflicts.add(ChatColor.GRAY + (other == null ? id : other.getDisplayName()));
        }
        Collections.sort(conflicts, String.CASE_INSENSITIVE_ORDER);
        if (conflicts.isEmpty()) conflicts.add(ChatColor.GRAY + "None");
        inventory.setItem(24, item(Material.BARRIER, ChatColor.RED + "Incompatible With", conflicts));
        inventory.setItem(HOME_SLOT, item(Material.ARROW, ChatColor.YELLOW + "Back",
                Arrays.asList(ChatColor.GRAY + "Return to the previous catalogue")));
        player.openInventory(inventory);
    }

    private void populateEntries(Inventory inventory, List<EnchantmentDefinition> entries, int page) {
        fillBorder(inventory);
        int start = page * CONTENT_SLOTS;
        for (int slot = 0; slot < CONTENT_SLOTS && start + slot < entries.size(); slot++) {
            EnchantmentDefinition definition = entries.get(start + slot);
            List<String> lore = new ArrayList<String>();
            lore.add(ChatColor.GRAY + "Rarity: " + definition.getRarity());
            lore.add(ChatColor.GRAY + "Maximum level: " + definition.getMaximumLevel());
            lore.add(ChatColor.YELLOW + "Click for details");
            inventory.setItem(slot, item(iconFor(definition),
                    rarityColor(definition.getRarity()) + definition.getDisplayName(), lore));
        }
        inventory.setItem(HOME_SLOT, item(Material.BOOK, ChatColor.YELLOW + "Main Menu",
                Arrays.asList(ChatColor.GRAY + "Return to categories")));
        if (page > 0) inventory.setItem(PREVIOUS_SLOT,
                item(Material.ARROW, ChatColor.YELLOW + "Previous Page",
                        Arrays.asList(ChatColor.GRAY + "Page " + page)));
        if (start + CONTENT_SLOTS < entries.size()) inventory.setItem(NEXT_SLOT,
                item(Material.ARROW, ChatColor.YELLOW + "Next Page",
                        Arrays.asList(ChatColor.GRAY + "Page " + (page + 2))));
    }

    private List<EnchantmentDefinition> entriesFor(GuidebookMenuHolder holder) {
        List<EnchantmentDefinition> entries = new ArrayList<EnchantmentDefinition>();
        for (EnchantmentDefinition definition : registry.getAll()) {
            if (holder.getView() == GuidebookMenuHolder.View.CATEGORY
                    && !"all".equals(holder.getCategory())
                    && !holder.getCategory().equalsIgnoreCase(definition.getCategory())) continue;
            if (holder.getView() == GuidebookMenuHolder.View.CONFLICTS
                    && definition.getIncompatibleIds().isEmpty()) continue;
            entries.add(definition);
        }
        if (holder.getView() == GuidebookMenuHolder.View.INDEX) {
            Collections.sort(entries, new Comparator<EnchantmentDefinition>() {
                @Override public int compare(EnchantmentDefinition a, EnchantmentDefinition b) {
                    return a.getDisplayName().compareToIgnoreCase(b.getDisplayName());
                }
            });
        } else {
            Collections.sort(entries, new Comparator<EnchantmentDefinition>() {
                @Override public int compare(EnchantmentDefinition a, EnchantmentDefinition b) {
                    int rarity = rarityRank(a.getRarity()) - rarityRank(b.getRarity());
                    return rarity != 0 ? rarity : a.getDisplayName().compareToIgnoreCase(b.getDisplayName());
                }
            });
        }
        return entries;
    }

    private List<String> getCategories() {
        List<String> categories = new ArrayList<String>();
        List<String> preferred = Arrays.asList("armour", "tools", "fishing", "farming", "utility", "weapons");
        for (String category : preferred) {
            if (countCategory(category) > 0) categories.add(category);
        }
        List<String> extra = new ArrayList<String>();
        for (EnchantmentDefinition definition : registry.getAll()) {
            String category = definition.getCategory();
            boolean known = false;
            for (String value : categories) if (value.equalsIgnoreCase(category)) known = true;
            if (!known && !extra.contains(category)) extra.add(category);
        }
        Collections.sort(extra, String.CASE_INSENSITIVE_ORDER);
        categories.addAll(extra);
        return categories;
    }

    private int countCategory(String category) {
        int count = 0;
        for (EnchantmentDefinition definition : registry.getAll()) {
            if (category.equalsIgnoreCase(definition.getCategory())) count++;
        }
        return count;
    }

    private Inventory createInventory(GuidebookMenuHolder holder, String title) {
        Inventory inventory = Bukkit.createInventory(holder, SIZE, title);
        holder.setInventory(inventory);
        return inventory;
    }

    private void fillBorder(Inventory inventory) {
        ItemStack filler = item(Material.STAINED_GLASS_PANE, " ", Collections.<String>emptyList());
        for (int i = 45; i < 54; i++) inventory.setItem(i, filler);
    }

    private ItemStack item(Material material, String name, List<String> lore) {
        ItemStack stack = new ItemStack(material == null ? Material.BOOK : material);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null && !lore.isEmpty()) meta.setLore(lore);
        stack.setItemMeta(meta);
        return stack;
    }

    private Material iconFor(EnchantmentDefinition definition) {
        if (!definition.getApplicableMaterials().isEmpty()) {
            Material material = definition.getApplicableMaterials().iterator().next();
            if (material != Material.AIR) return material;
        }
        return Material.ENCHANTED_BOOK;
    }

    private List<String> splitMaterials(Set<Material> materials) {
        List<String> names = new ArrayList<String>();
        for (Material material : materials) {
            names.add(ChatColor.GRAY + titleCase(material.name().replace('_', ' ')));
        }
        Collections.sort(names, String.CASE_INSENSITIVE_ORDER);
        if (names.isEmpty()) names.add(ChatColor.GRAY + "Not specified");
        return names;
    }

    private List<String> wrapLore(String text) {
        List<String> lore = new ArrayList<String>();
        if (text == null || text.trim().isEmpty()) {
            lore.add(ChatColor.GRAY + "No description provided.");
            return lore;
        }
        StringBuilder line = new StringBuilder();
        for (String word : text.split("\\s+")) {
            if (line.length() + word.length() >  thirty()) {
                lore.add(ChatColor.GRAY + line.toString());
                line.setLength(0);
            }
            if (line.length() > 0) line.append(' ');
            line.append(word);
        }
        if (line.length() > 0) lore.add(ChatColor.GRAY + line.toString());
        return lore;
    }

    private int thirty() { return 30; }

    private static String titleCase(String value) {
        String[] words = value.toLowerCase(Locale.ENGLISH).replace('_', ' ').split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) continue;
            if (result.length() > 0) result.append(' ');
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return result.toString();
    }

    private static ChatColor rarityColor(String rarity) {
        String value = rarity.toUpperCase(Locale.ENGLISH);
        if ("COMMON".equals(value)) return ChatColor.GRAY;
        if ("UNCOMMON".equals(value)) return ChatColor.AQUA;
        if ("RARE".equals(value)) return ChatColor.GREEN;
        if ("EPIC".equals(value)) return ChatColor.LIGHT_PURPLE;
        if ("LEGENDARY".equals(value)) return ChatColor.GOLD;
        if ("MYTHIC".equals(value)) return ChatColor.RED;
        return ChatColor.DARK_GRAY;
    }

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
