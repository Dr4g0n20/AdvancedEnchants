package net.dr4g0n20.advancedenchants.guidebook;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/** Identifies AdvancedEnchants menus and stores their navigation state. */
public final class GuidebookMenuHolder implements InventoryHolder {

    public enum View { MAIN, CATEGORY, DETAIL, INDEX, CONFLICTS }

    private final View view;
    private final String category;
    private final String enchantmentId;
    private final int page;
    private Inventory inventory;

    public GuidebookMenuHolder(View view, String category, String enchantmentId, int page) {
        this.view = view;
        this.category = category;
        this.enchantmentId = enchantmentId;
        this.page = page;
    }

    public View getView() { return view; }
    public String getCategory() { return category; }
    public String getEnchantmentId() { return enchantmentId; }
    public int getPage() { return page; }
    public void setInventory(Inventory inventory) { this.inventory = inventory; }

    @Override
    public Inventory getInventory() { return inventory; }
}
