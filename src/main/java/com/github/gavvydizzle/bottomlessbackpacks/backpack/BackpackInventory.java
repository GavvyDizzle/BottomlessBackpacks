package com.github.gavvydizzle.bottomlessbackpacks.backpack;

import com.github.gavvydizzle.bottomlessbackpacks.config.MenusConfig;
import com.github.gavvydizzle.bottomlessbackpacks.utils.Messages;
import com.github.gavvydizzle.bottomlessbackpacks.utils.Sounds;
import com.github.mittenmc.serverutils.ColoredItems;
import com.github.mittenmc.serverutils.Colors;
import com.github.mittenmc.serverutils.ConfigUtils;
import com.github.mittenmc.serverutils.Numbers;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

public class BackpackInventory {

    private final BackpackManager backpackManager;
    private String inventoryName, adminInventoryName;
    private final HashMap<UUID, Backpack> playersInInventory;
    private final HashMap<UUID, AdminViewer> adminsInInventory;

    private final int inventorySize;
    private int pageDownSlot, pageInfoSlot, pageUpSlot, togglePageSaveRowSlot;
    private ItemStack previousPageItem, pageInfoItem, nextPageItem, pageRowFiller, savingOn, savingOff;
    private int lowPageAmount, highPageAmount;

    public BackpackInventory(BackpackManager backpackManager) {
        this.backpackManager = backpackManager;
        inventorySize = Backpack.ITEMS_PER_PAGE + 9;
        playersInInventory = new HashMap<>();
        adminsInInventory = new HashMap<>();
        reload();
    }

    public void reload() {
        FileConfiguration config = MenusConfig.get();
        config.options().copyDefaults(true);

        config.addDefault("backpack.name", "Backpack");
        config.addDefault("backpack.adminName", "{name}'s Backpack");
        config.addDefault("backpack.filler", ColoredItems.GRAY.name());

        config.addDefault("backpack.pageAmounts.low", 5);
        config.addDefault("backpack.pageAmounts.high", 20);

        config.addDefault("backpack.rowSlots.pageInfo", 4);
        config.addDefault("backpack.rowSlots.pageDown", 3);
        config.addDefault("backpack.rowSlots.pageUp", 5);
        config.addDefault("backpack.rowSlots.savingToggle", 0);

        config.addDefault("backpack.items.pageInfo.material", Material.OAK_SIGN.name());
        config.addDefault("backpack.items.pageInfo.name", "&ePage: {curr}/{max}");

        config.addDefault("backpack.items.pageDown.material", Material.PAPER.name());
        config.addDefault("backpack.items.pageDown.name", "&ePage Down");
        config.addDefault("backpack.items.pageDown.lore", Arrays.asList(
                "&7Left          &c-1",
                "&7Shift-left   &c-5",
                "&7Right         &c-20",
                "&7Shift-right  &cPage 1"
        ));

        config.addDefault("backpack.items.pageUp.material", Material.PAPER.name());
        config.addDefault("backpack.items.pageUp.name", "&ePage Up");
        config.addDefault("backpack.items.pageUp.lore", Arrays.asList(
                "&7Left          &a+1",
                "&7Shift-left   &a+5",
                "&7Right         &a+20",
                "&7Shift-right  &aMax"
        ));

        config.addDefault("backpack.items.savingOn.material", Material.LIME_STAINED_GLASS_PANE.name());
        config.addDefault("backpack.items.savingOn.name", "&aPage Saving Active");
        config.addDefault("backpack.items.savingOn.lore", Collections.singletonList("&7Click to toggle"));

        config.addDefault("backpack.items.savingOff.material", Material.RED_STAINED_GLASS_PANE.getBlastResistance());
        config.addDefault("backpack.items.savingOff.name", "&cPage Saving Inactive");
        config.addDefault("backpack.items.savingOff.lore", Collections.singletonList("&7Click to toggle"));

        MenusConfig.save();

        inventoryName = Colors.conv(config.getString("backpack.name"));
        adminInventoryName = Colors.conv(config.getString("backpack.adminName"));
        pageRowFiller = ColoredItems.getGlassByName(config.getString("backpack.filler"));

        lowPageAmount = Math.max(1, config.getInt("backpack.pageAmounts.low"));
        highPageAmount = Math.max(lowPageAmount + 1, config.getInt("backpack.pageAmounts.high"));

        pageInfoSlot = inventorySize - 9 + Numbers.constrain(config.getInt("backpack.rowSlots.pageInfo"), 0, 8);
        pageDownSlot = inventorySize - 9 + Numbers.constrain(config.getInt("backpack.rowSlots.pageDown"), 0, 8);
        pageUpSlot =   inventorySize - 9 + Numbers.constrain(config.getInt("backpack.rowSlots.pageUp"), 0, 8);
        togglePageSaveRowSlot = inventorySize - 9 + Numbers.constrain(config.getInt("backpack.rowSlots.savingToggle"), 0, 8);

        pageInfoItem = new ItemStack(ConfigUtils.getMaterial(config.getString("backpack.items.pageInfo.material"), Material.OAK_SIGN));
        ItemMeta meta = pageInfoItem.getItemMeta();
        assert meta != null;
        meta.setDisplayName(Colors.conv(config.getString("backpack.items.pageInfo.name")));
        pageInfoItem.setItemMeta(meta);

        previousPageItem = new ItemStack(ConfigUtils.getMaterial(config.getString("backpack.items.pageDown.material"), Material.PAPER));
        meta = previousPageItem.getItemMeta();
        assert meta != null;
        meta.setDisplayName(Colors.conv(config.getString("backpack.items.pageDown.name")));
        meta.setLore(Colors.conv(config.getStringList("backpack.items.pageDown.lore")));
        previousPageItem.setItemMeta(meta);

        nextPageItem = new ItemStack(ConfigUtils.getMaterial(config.getString("backpack.items.pageUp.material"), Material.PAPER));
        meta = nextPageItem.getItemMeta();
        assert meta != null;
        meta.setDisplayName(Colors.conv(config.getString("backpack.items.pageUp.name")));
        meta.setLore(Colors.conv(config.getStringList("backpack.items.pageUp.lore")));
        nextPageItem.setItemMeta(meta);

        savingOn = new ItemStack(ConfigUtils.getMaterial(config.getString("backpack.items.savingOn.material"), Material.LIME_STAINED_GLASS_PANE));
        meta = savingOn.getItemMeta();
        assert meta != null;
        meta.setDisplayName(Colors.conv(config.getString("backpack.items.savingOn.name")));
        meta.setLore(Colors.conv(config.getStringList("backpack.items.savingOn.lore")));
        savingOn.setItemMeta(meta);

        savingOff = new ItemStack(ConfigUtils.getMaterial(config.getString("backpack.items.savingOff.material"), Material.RED_STAINED_GLASS_PANE));
        meta = savingOff.getItemMeta();
        assert meta != null;
        meta.setDisplayName(Colors.conv(config.getString("backpack.items.savingOff.name")));
        meta.setLore(Colors.conv(config.getStringList("backpack.items.savingOff.lore")));
        savingOff.setItemMeta(meta);
    }

    public void openInventory(Player player, int page) {
        Backpack backpack = backpackManager.getBackpack(player);
        if (backpack == null) return;

        int openPage = page >= 1 ? page : backpack.getCurrentPage();
        openPage = Numbers.constrain(openPage, 1, backpack.getMaxPage());
        backpack.setCurrentPage(openPage);

        Inventory inventory = Bukkit.createInventory(player, inventorySize, inventoryName);
        onPageLoad(inventory, backpack, openPage);
        backpack.getBackpackViewers().addViewer(player.getUniqueId(), openPage);

        for (int i = Backpack.ITEMS_PER_PAGE; i < inventorySize; i++) {
            inventory.setItem(i, pageRowFiller);
        }
        inventory.setItem(togglePageSaveRowSlot, getSavingItem(backpack));
        inventory.setItem(pageDownSlot, previousPageItem);
        inventory.setItem(pageInfoSlot, getPageItem(openPage, backpack.getMaxPage()));
        inventory.setItem(pageUpSlot, nextPageItem);

        player.openInventory(inventory);
        playersInInventory.put(player.getUniqueId(), backpack);
    }

    public void adminOpenInventory(Player admin, OfflinePlayer owner, int page) {
        Backpack backpack = backpackManager.getBackpack(owner);
        if (backpack == null) return;

        adminOpenInventory(admin, backpack, page, owner.getName());
    }

    public void adminOpenInventory(Player admin, @NotNull Backpack backpack, int page, String ownerName) {
        int openPage = Math.max(page, 1);
        openPage = Numbers.constrain(openPage, 1, backpack.getMaxPage());

        Inventory inventory = Bukkit.createInventory(admin, inventorySize, adminInventoryName.replace("{name}", ownerName));
        onPageLoad(inventory, backpack, openPage);
        backpack.getBackpackViewers().addViewer(admin.getUniqueId(), openPage);

        for (int i = Backpack.ITEMS_PER_PAGE; i < inventorySize; i++) {
            inventory.setItem(i, pageRowFiller);
        }
        inventory.setItem(togglePageSaveRowSlot, getSavingItem(backpack));
        inventory.setItem(pageDownSlot, previousPageItem);
        inventory.setItem(pageInfoSlot, getPageItem(openPage, backpack.getMaxPage()));
        inventory.setItem(pageUpSlot, nextPageItem);

        admin.openInventory(inventory);
        adminsInInventory.put(admin.getUniqueId(), new AdminViewer(backpack, openPage));
    }

    public void closeInventory(Player player) {
        Backpack backpack = null;
        int page = 0;
        boolean shouldSavePage = false;
        boolean isAdmin = false;

        if (playersInInventory.containsKey(player.getUniqueId())) {
            backpack = playersInInventory.remove(player.getUniqueId());
            page = backpack.getCurrentPage();
            shouldSavePage = true;
        }
        else if (adminsInInventory.containsKey(player.getUniqueId())) {
            AdminViewer adminViewer = adminsInInventory.remove(player.getUniqueId());
            backpack = adminViewer.getBackpack();
            page = adminViewer.getPage();
            isAdmin = true;
        }

        if (backpack != null && page >= 1) {
            onPageChange(player, player.getOpenInventory().getTopInventory(), backpack, page, -1);
            backpack.onInventoryClose(shouldSavePage);
            backpack.getBackpackViewers().removeViewer(player.getUniqueId());
            if (isAdmin) {
                backpackManager.onAdminCloseBackpack(backpack);
            }
        }
    }

    public void handleClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Inventory topInv = e.getView().getTopInventory();

        if (adminsInInventory.containsKey(player.getUniqueId())) {
            // Cancel clicks to the menu bar
            if (e.getClickedInventory() == topInv && e.getSlot() >= Backpack.ITEMS_PER_PAGE) {
                e.setCancelled(true);
            }

            handleAdminClick(e, player);
            return;
        }

        if (!playersInInventory.containsKey(player.getUniqueId())) {
            return;
        }

        // Cancel clicks to the menu bar
        if (e.getClickedInventory() == topInv && e.getSlot() >= Backpack.ITEMS_PER_PAGE) {
            e.setCancelled(true);
        }

        Backpack backpack = playersInInventory.get(player.getUniqueId());
        int slot = e.getSlot();

        // Change page
        if (e.getClickedInventory() == topInv && e.getSlot() >= Backpack.ITEMS_PER_PAGE) {
            ClickType clickType = e.getClick();
            int page = backpack.getCurrentPage();
            int maxPage = backpack.getMaxPage();

            if (slot == pageUpSlot) {
                if (page < maxPage) {
                    switch (clickType) {
                        case LEFT:
                            backpack.setCurrentPage(page + 1);
                            break;
                        case SHIFT_LEFT:
                            backpack.setCurrentPage(page + lowPageAmount);
                            break;
                        case RIGHT:
                            backpack.setCurrentPage(page + highPageAmount);
                            break;
                        case SHIFT_RIGHT:
                            backpack.setCurrentPage(maxPage);
                            break;
                        default:
                            return;
                    }

                    onPageChange(player, topInv, backpack, page, backpack.getCurrentPage());
                    backpack.getBackpackViewers().updatePage(player.getUniqueId(), backpack.getCurrentPage());
                    topInv.setItem(pageInfoSlot, getPageItem(backpack.getCurrentPage(), maxPage));
                    Sounds.pageTurnSound.playSound(player);
                }
                else {
                    Sounds.generalFailSound.playSound(player);
                }
            }
            else if (slot == pageDownSlot) {
                if (page > 1) {
                    switch (clickType) {
                        case LEFT:
                            backpack.setCurrentPage(page - 1);
                            break;
                        case SHIFT_LEFT:
                            backpack.setCurrentPage(page - lowPageAmount);
                            break;
                        case RIGHT:
                            backpack.setCurrentPage(page - highPageAmount);
                            break;
                        case SHIFT_RIGHT:
                            backpack.setCurrentPage(1);
                            break;
                        default:
                            return;
                    }

                    onPageChange(player, topInv, backpack, page, backpack.getCurrentPage());
                    backpack.getBackpackViewers().updatePage(player.getUniqueId(), backpack.getCurrentPage());
                    topInv.setItem(pageInfoSlot, getPageItem(backpack.getCurrentPage(), maxPage));
                    Sounds.pageTurnSound.playSound(player);
                }
                else {
                    Sounds.generalFailSound.playSound(player);
                }
            }
            else if (slot == togglePageSaveRowSlot) {
                backpack.onSavePageToggle();
                topInv.setItem(togglePageSaveRowSlot, getSavingItem(backpack));
                Sounds.generalClickSound.playSound(player);
            }
        }
        else {
            // Cancel clicks if the page is locked
            if (backpack.getBackpackViewers().isPageLocked(player.getUniqueId())) {
                e.setCancelled(true);
                if (e.getClick() != ClickType.SHIFT_LEFT) { // Ignore spammy shift + double left click
                    player.sendMessage(Messages.backpackPageLocked);
                    Sounds.generalFailSound.playSound(player);
                }
            }
        }
    }

    private void handleAdminClick(InventoryClickEvent e, Player player) {
        AdminViewer adminViewer = adminsInInventory.get(player.getUniqueId());
        Backpack backpack = adminViewer.getBackpack();
        Inventory topInv = e.getView().getTopInventory();
        int slot = e.getSlot();

        // Change page
        if (e.getClickedInventory() == topInv && e.getSlot() >= Backpack.ITEMS_PER_PAGE) {
            ClickType clickType = e.getClick();
            int page = adminViewer.getPage();
            int maxPage = backpack.getMaxPage();

            if (slot == pageUpSlot) {
                if (page < maxPage) {
                    switch (clickType) {
                        case LEFT:
                            adminViewer.setPage(Math.min(page + 1, maxPage));
                            break;
                        case SHIFT_LEFT:
                            adminViewer.setPage(Math.min(page + lowPageAmount, maxPage));
                            break;
                        case RIGHT:
                            adminViewer.setPage(Math.min(page + highPageAmount, maxPage));
                            break;
                        case SHIFT_RIGHT:
                            adminViewer.setPage(maxPage);
                            break;
                        default:
                            return;
                    }

                    onPageChange(player, topInv, backpack, page, adminViewer.getPage());
                    backpack.getBackpackViewers().updatePage(player.getUniqueId(), adminViewer.getPage());
                    e.getView().getTopInventory().setItem(pageInfoSlot, getPageItem(adminViewer.getPage(), maxPage));
                    Sounds.generalClickSound.playSound(player);
                } else {
                    Sounds.generalFailSound.playSound(player);
                }
            } else if (slot == pageDownSlot) {
                if (page > 1) {
                    switch (clickType) {
                        case LEFT:
                            adminViewer.setPage(Math.max(page - 1, 1));
                            break;
                        case SHIFT_LEFT:
                            adminViewer.setPage(Math.max(page - lowPageAmount, 1));
                            break;
                        case RIGHT:
                            adminViewer.setPage(Math.max(page - highPageAmount, 1));
                            break;
                        case SHIFT_RIGHT:
                            adminViewer.setPage(1);
                            break;
                        default:
                            return;
                    }

                    onPageChange(player, topInv, backpack, page, adminViewer.getPage());
                    backpack.getBackpackViewers().updatePage(player.getUniqueId(), adminViewer.getPage());
                    e.getView().getTopInventory().setItem(pageInfoSlot, getPageItem(adminViewer.getPage(), maxPage));
                    Sounds.generalClickSound.playSound(player);
                } else {
                    Sounds.generalFailSound.playSound(player);
                }
            }
        } else {
            // Cancel clicks if the page is locked
            if (backpack.getBackpackViewers().isPageLocked(player.getUniqueId())) {
                e.setCancelled(true);
                if (e.getClick() != ClickType.SHIFT_LEFT) { // Ignore spammy shift + double left click
                    player.sendMessage(Messages.backpackPageLocked);
                    Sounds.generalFailSound.playSound(player);
                }
            }
        }
    }

    /**
     * Reads the contents of the backpack from this page and updates the inventory
     * @param inventory The inventory to update
     * @param backpack The backpack
     * @param newPage The page
     */
    private void onPageLoad(Inventory inventory, Backpack backpack, int newPage) {
        ItemStack[] items = backpack.getStoredContents(getItemIndex(newPage));
        for (int i = 0; i < Backpack.ITEMS_PER_PAGE; i++) {
            inventory.setItem(i, items[i]);
        }
    }

    /**
     * Loads the contents of the backpack for the new page while saving the contents from the old page.
     * If the old page is locked, no update will be pushed to the backpack.
     * @param player The player
     * @param inventory The inventory to update
     * @param backpack The backpack
     * @param oldPage The current page
     * @param newPage The new page
     */
    private void onPageChange(Player player, Inventory inventory, Backpack backpack, int oldPage, int newPage) {
        if (!backpack.getBackpackViewers().isPageLocked(player.getUniqueId())) {
            backpack.updateStoredContents(inventory.getContents(), getItemIndex(oldPage));
        }

        if (newPage >= 1) {
            ItemStack[] items = backpack.getStoredContents(getItemIndex(newPage));
            for (int i = 0; i < Backpack.ITEMS_PER_PAGE; i++) {
                inventory.setItem(i, items[i]);
            }
        }
    }

    /**
     * Handles when a backpack is cleared or reset by editing the viewed inventory of anyone looking at the backpack.
     * @param backpack The backpack
     */
    public void handleBackpackChange(Backpack backpack) {
        for (UUID uuid : playersInInventory.keySet()) {
            Backpack b = playersInInventory.get(uuid);

            if (b == backpack) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    Inventory topInv = player.getOpenInventory().getTopInventory();
                    onPageLoad(topInv, backpack, 1);
                    backpack.getBackpackViewers().updatePage(player.getUniqueId(), 1);
                    topInv.setItem(pageInfoSlot, getPageItem(1, backpack.getMaxPage()));
                }
                break; // Only one player can have their backpack open
            }
        }

        for (UUID uuid : adminsInInventory.keySet()) {
            AdminViewer adminViewer = adminsInInventory.get(uuid);
            Backpack b = adminViewer.getBackpack();
            adminViewer.setPage(1);

            if (b == backpack) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    Inventory topInv = player.getOpenInventory().getTopInventory();
                    onPageLoad(topInv, backpack, 1);
                    backpack.getBackpackViewers().updatePage(player.getUniqueId(), 1);
                    topInv.setItem(pageInfoSlot, getPageItem(1, backpack.getMaxPage()));
                }
            }
        }
    }

    /**
     * Handles when a backpack's page count changes.
     * If the player is on an invalid page, they will be moved to the highest available page
     * @param backpack The backpack
     */
    public void handleBackpackPageChange(Backpack backpack) {
        for (UUID uuid : playersInInventory.keySet()) {
            Backpack b = playersInInventory.get(uuid);

            if (b == backpack) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    Inventory topInv = player.getOpenInventory().getTopInventory();

                    if (backpack.getCurrentPage() > backpack.getMaxPage()) {
                        backpack.setCurrentPage(backpack.getMaxPage());
                        onPageLoad(topInv, backpack, backpack.getCurrentPage());
                        backpack.getBackpackViewers().updatePage(player.getUniqueId(), backpack.getCurrentPage());
                    }
                    topInv.setItem(pageInfoSlot, getPageItem(backpack.getCurrentPage(), backpack.getMaxPage()));
                }
                break; // Only one player can have their backpack open
            }
        }

        for (UUID uuid : adminsInInventory.keySet()) {
            AdminViewer adminViewer = adminsInInventory.get(uuid);
            Backpack b = adminViewer.getBackpack();

            if (b == backpack) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    Inventory topInv = player.getOpenInventory().getTopInventory();

                    if (adminViewer.getPage() > backpack.getMaxPage()) {
                        adminViewer.setPage(backpack.getMaxPage());
                        onPageLoad(topInv, backpack, adminViewer.getPage());
                        backpack.getBackpackViewers().updatePage(player.getUniqueId(), adminViewer.getPage());
                    }
                    topInv.setItem(pageInfoSlot, getPageItem(adminViewer.getPage(), backpack.getMaxPage()));
                }
            }
        }
    }

    /**
     * Handles when a backpack's page saving status changes.
     * Admins will have the item in their menu updated
     * @param backpack The backpack
     */
    public void handleBackpackSavePageToggle(Backpack backpack) {
        for (UUID uuid : adminsInInventory.keySet()) {
            AdminViewer adminViewer = adminsInInventory.get(uuid);
            Backpack b = adminViewer.getBackpack();

            if (b == backpack) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    player.getOpenInventory().getTopInventory().setItem(togglePageSaveRowSlot, getSavingItem(b));
                }
            }
        }
    }

    /**
     * Saves the page of all open backpacks.
     * This method should only be called on server shutdown.
     * Prioritizes the admin's copy if multiple people have the backpack open to the same page
     */
    public void updateOpenBackpacks() {
        for (UUID uuid : playersInInventory.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                Backpack b = playersInInventory.get(uuid);
                onPageChange(player, player.getOpenInventory().getTopInventory(), b, b.getCurrentPage(), -1);
            }
        }

        for (UUID uuid : adminsInInventory.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                AdminViewer adminViewer = adminsInInventory.get(uuid);
                Backpack b = adminViewer.getBackpack();
                onPageChange(player, player.getOpenInventory().getTopInventory(), b, adminViewer.getPage(), -1);
                if (b.isOwnerOffline()) { // Saves offline backpacks
                    b.forceSave();
                }
            }
        }
    }

    /**
     * Refreshes the contents of the currently viewed page of the backpack.
     * @param uuid The uuid
     */
    public void refreshBackpack(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        Backpack backpack = null;
        int page = 0;

        if (playersInInventory.containsKey(player.getUniqueId())) {
            backpack = playersInInventory.get(player.getUniqueId());
            page = backpack.getCurrentPage();
        }
        else if (adminsInInventory.containsKey(player.getUniqueId())) {
            AdminViewer adminViewer = adminsInInventory.get(player.getUniqueId());
            backpack = adminViewer.getBackpack();
            page = adminViewer.getPage();
        }

        if (backpack != null && page >= 1) {
            onPageLoad(player.getOpenInventory().getTopInventory(), backpack, page);
        }
    }

    private int getItemIndex(int page) {
        return (page - 1) * Backpack.ITEMS_PER_PAGE;
    }

    private ItemStack getPageItem(int page, int maxPage) {
        ItemStack pageInfo = pageInfoItem.clone();
        ItemMeta meta = pageInfo.getItemMeta();
        assert meta != null;
        meta.setDisplayName(meta.getDisplayName().replace("{curr}", String.valueOf(page)).replace("{max}", String.valueOf(maxPage)));
        pageInfo.setItemMeta(meta);
        return pageInfo;
    }

    private ItemStack getSavingItem(Backpack backpack) {
        return backpack.isSavePageOnClose() ? savingOn : savingOff;
    }
}
