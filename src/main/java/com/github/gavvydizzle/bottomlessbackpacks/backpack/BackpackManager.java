package com.github.gavvydizzle.bottomlessbackpacks.backpack;

import com.github.gavvydizzle.bottomlessbackpacks.BottomlessBackpacks;
import com.github.gavvydizzle.bottomlessbackpacks.storage.PlayerData;
import com.github.gavvydizzle.bottomlessbackpacks.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.UUID;

public class BackpackManager implements Listener {

    private final BottomlessBackpacks instance;
    private final PlayerData data;
    private final HashMap<UUID, Backpack> backpacks;
    private final HashMap<UUID, Backpack> offlineBackpacks;
    private final BackpackInventory backpackInventory;

    public BackpackManager(BottomlessBackpacks instance, PlayerData data) {
        this.instance = instance;
        this.data = data;
        backpacks = new HashMap<>();
        offlineBackpacks = new HashMap<>();
        this.backpackInventory = new BackpackInventory(this);
    }

    public void reload() {
        backpackInventory.reload();
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent e) {
        // If the player's backpack is currently loaded by an admin
        if (offlineBackpacks.containsKey(e.getPlayer().getUniqueId())) {
            backpacks.put(e.getPlayer().getUniqueId(), offlineBackpacks.get(e.getPlayer().getUniqueId()));
            offlineBackpacks.remove(e.getPlayer().getUniqueId());
        }
        else {
            Bukkit.getServer().getScheduler().runTaskAsynchronously(instance, () -> {
                Backpack backpack = data.getPlayerData(e.getPlayer());
                if (backpack == null) {
                    e.getPlayer().sendMessage(Messages.backpackFailedToLoad);
                }
                else {
                    backpacks.put(e.getPlayer().getUniqueId(), backpack);
                }
            });
        }
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent e) {
       Backpack backpack = backpacks.remove(e.getPlayer().getUniqueId());
       if (backpack == null) return;

       // If any admins are still looking at the backpack, move it to the offline map
       if (!backpack.getBackpackViewers().isEmpty()) {
           offlineBackpacks.put(backpack.getUuid(), backpack);
       }
    }

    @EventHandler
    private void onInventoryClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;

        backpackInventory.handleClick(e);
    }

    @EventHandler
    private void onInventoryClose(InventoryCloseEvent e) {
        backpackInventory.closeInventory((Player) e.getPlayer());
    }

    /**
     * Removes a backpack from the offline backpack map only if no players are viewing it.
     * This will do nothing if the backpack is loaded by the player on the server
     * @param backpack The backpack
     */
    protected void onAdminCloseBackpack(Backpack backpack) {
        if (backpack.getBackpackViewers().isEmpty()) {
            offlineBackpacks.remove(backpack.getUuid());
        }
    }

    public void openBackpack(Player player, int page) {
        backpackInventory.openInventory(player, page);
    }

    /**
     * Opens the backpack of another player
     * @param admin The admin
     * @param owner The owner of the backpack
     * @param page The page to open to
     */
    public void adminOpenBackpack(Player admin, OfflinePlayer owner, int page) {
        backpackInventory.adminOpenInventory(admin, owner, page);
    }

    /**
     * Opens the backpack of an offline player
     * @param admin The admin
     * @param offlineBackpack The backpack
     * @param page The page to open to
     * @param ownerName The name of the owner for the inventory name placeholder
     */
    public void adminOpenOfflineBackpack(Player admin, Backpack offlineBackpack, int page, String ownerName) {
        backpackInventory.adminOpenInventory(admin, offlineBackpack, page, ownerName);
        offlineBackpacks.put(offlineBackpack.getUuid(), offlineBackpack);
    }

    /**
     * Save all open backpacks on server shutdown
     */
    public void saveOpenBackpacksOnShutdown() {
        backpackInventory.updateOpenBackpacks();
        for (Backpack backpack : backpacks.values()) {
            backpack.forceSave();
        }
    }

    /**
     * Resets all loaded backpacks.
     * This should only be run after the database has cleared all data!
     */
    public void resetAllLoadedBackpacks() {
        for (Backpack backpack : backpacks.values()) {
            backpack.resetBackpack();
        }
        for (Backpack backpack : offlineBackpacks.values()) {
            backpack.resetBackpack();
        }
    }

    public Backpack getBackpack(OfflinePlayer offlinePlayer) {
        return getBackpack(offlinePlayer.getUniqueId());
    }

    /**
     * Gets the backpack by checking the normal and offline backpack maps
     * @param offlinePlayer The player
     * @return The backpack or null if none exists
     */
    public Backpack getOfflineBackpack(OfflinePlayer offlinePlayer) {
        Backpack backpack = getBackpack(offlinePlayer);
        if (backpack == null) return offlineBackpacks.get(offlinePlayer.getUniqueId());

        return backpack;
    }

    public Backpack getBackpack(UUID uuid) {
        return backpacks.get(uuid);
    }

    protected BackpackInventory getBackpackInventory() {
        return backpackInventory;
    }

    protected PlayerData getData() {
        return data;
    }

}
