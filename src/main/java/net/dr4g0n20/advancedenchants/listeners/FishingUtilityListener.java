package net.dr4g0n20.advancedenchants.listeners;

import net.dr4g0n20.advancedenchants.enchantments.ItemEnchantmentStorage;
import org.bukkit.Bukkit;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;

/** Handles fishing-bite assistance and automatic hook retrieval. */
public final class FishingUtilityListener implements Listener {

    private final JavaPlugin plugin;
    private boolean autoReelWarningLogged;

    public FishingUtilityListener(JavaPlugin plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null.");
        }
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFishingState(PlayerFishEvent event) {
        Player player = event.getPlayer();
        ItemStack rod = player.getItemInHand();
        if (rod == null) {
            return;
        }

        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH
                && ItemEnchantmentStorage.hasEnchantment(rod, "bait")) {
            int level = ItemEnchantmentStorage.getLevel(rod, "bait");
            event.setExpToDrop(event.getExpToDrop() + level);
        }

        if (event.getState() == PlayerFishEvent.State.BITE
                && ItemEnchantmentStorage.hasEnchantment(rod, "auto_reel")) {
            final Player fisher = player;
            final FishHook hook = event.getHook();
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    if (!fisher.isOnline() || !hook.isValid()
                            || !ItemEnchantmentStorage.hasEnchantment(
                                    fisher.getItemInHand(), "auto_reel")) {
                        return;
                    }
                    retrieveHook(hook);
                }
            }, 1L);
        }
    }

    private void retrieveHook(FishHook hook) {
        try {
            Method getHandle = hook.getClass().getMethod("getHandle");
            Object handle = getHandle.invoke(hook);
            Method retrieve = findRetractionMethod(handle.getClass());
            if (retrieve == null) {
                warnOnce("Could not find the 1.12.2 fishing-hook retraction method; Auto Reel is inactive on this server build.");
                return;
            }
            retrieve.setAccessible(true);
            retrieve.invoke(handle);
        } catch (ReflectiveOperationException | SecurityException ex) {
            warnOnce("Auto Reel could not access the server's fishing-hook retraction method: "
                    + ex.getClass().getSimpleName());
        }
    }

    private Method findRetractionMethod(Class<?> handleClass) {
        String[] knownNames = {"j", "handleHookRetraction", "retrieve"};
        for (String name : knownNames) {
            try {
                Method method = handleClass.getDeclaredMethod(name);
                if (method.getReturnType() == int.class) {
                    return method;
                }
            } catch (NoSuchMethodException ignored) {
                // Try the next known 1.12.x mapping.
            }
        }

        for (Method method : handleClass.getDeclaredMethods()) {
            if (method.getParameterTypes().length == 0
                    && method.getReturnType() == int.class) {
                return method;
            }
        }
        return null;
    }

    private void warnOnce(String message) {
        if (!autoReelWarningLogged) {
            autoReelWarningLogged = true;
            plugin.getLogger().warning(message);
        }
    }
}
