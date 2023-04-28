package com.github.gavvydizzle.bottomlessbackpacks.backpack;

import com.github.gavvydizzle.bottomlessbackpacks.BottomlessBackpacks;
import com.github.mittenmc.serverutils.Numbers;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public class Backpack {

    public static final int MAX_PAGE_AMOUNT = 1000;
    protected static final int ITEM_ROWS = 5;
    protected static final int ITEMS_PER_PAGE = ITEM_ROWS * 9;

    private final UUID uuid;
    private ItemStack[] items;
    private boolean savePageOnClose;
    private int maxPage, currentPage;
    private final BackpackViewers backpackViewers;

    private boolean isSavePageDirty, areItemsDirty;

    public Backpack(UUID owner, @Nullable ItemStack[] items, int maxPage, boolean savePageOnClose) {
        this.uuid = owner;
        this.maxPage = Numbers.constrain(maxPage, 1, MAX_PAGE_AMOUNT);
        this.savePageOnClose = savePageOnClose;
        this.items = items == null ? new ItemStack[ITEMS_PER_PAGE] : items;
        backpackViewers = new BackpackViewers();
        updateArraySize();

        this.currentPage = 1;
    }

    /**
     * Reducing the size of the backpack will result in data loss.
     */
    private void updateArraySize() {
        if (items.length != maxPage * ITEMS_PER_PAGE) {
            items = Arrays.copyOf(items, maxPage * ITEMS_PER_PAGE);
        }
    }

    /**
     * Updates all items in the backpack if they have changed
     * @param current The set of items from the inventory
     * @param startIndex The index to start checking from
     */
    public void updateStoredContents(ItemStack[] current, int startIndex) {
        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            if (!Objects.equals(current[i], items[i + startIndex])) {
                items[i + startIndex] = current[i];
                areItemsDirty = true;
            }
        }
    }

    /**
     * Creates a copy of the items array
     * @param startIndex The index to start copying from
     * @return A copy of the backpack's contents for the provided range
     */
    public ItemStack[] getStoredContents(int startIndex) {
        return Arrays.copyOfRange(items, startIndex, startIndex + ITEMS_PER_PAGE);
    }

    /**
     * Resets this backpack back to its default state.
     * This will clear all items from the backpack and reset the number of pages to the default.
     */
    public void resetBackpack() {
        currentPage = 1;
        maxPage = 1;
        items = new ItemStack[ITEMS_PER_PAGE];

        Bukkit.getServer().getScheduler().runTaskAsynchronously(BottomlessBackpacks.getInstance(), () -> {
            if (BottomlessBackpacks.getInstance().getBackpackManager().getData().deletePlayerData(uuid)) {
                areItemsDirty = false;
                BottomlessBackpacks.getInstance().getBackpackManager().getBackpackInventory().handleBackpackChange(this);
            }
        });
    }

    /**
     * Clears all items from this backpack
     */
    public void clearBackpack() {
        currentPage = 1;
        items = new ItemStack[ITEMS_PER_PAGE * maxPage];

        Bukkit.getServer().getScheduler().runTaskAsynchronously(BottomlessBackpacks.getInstance(), () -> {
            BottomlessBackpacks.getInstance().getBackpackManager().getData().updateItems(uuid, items);
            areItemsDirty = false;

            BottomlessBackpacks.getInstance().getBackpackManager().getBackpackInventory().handleBackpackChange(this);
        });
    }

    /**
     * Updates the number of pages this backpack has
     * @param maxPage The new max page
     */
    public void updateMaxPage(int maxPage) {
        if (this.maxPage == maxPage) return;

        Bukkit.getServer().getScheduler().runTaskAsynchronously(BottomlessBackpacks.getInstance(), () -> {
            int newMaxPage = Numbers.constrain(maxPage, 1, MAX_PAGE_AMOUNT);
            if (BottomlessBackpacks.getInstance().getBackpackManager().getData().updatePages(uuid, newMaxPage)) {
                this.maxPage = newMaxPage;
                updateArraySize();

                BottomlessBackpacks.getInstance().getBackpackManager().getBackpackInventory().handleBackpackPageChange(this);
            }
        });
    }

    /**
     * Toggles if this backpack will save the page it was on when it is closed.
     */
    public void onSavePageToggle() {
        savePageOnClose = !savePageOnClose;
        isSavePageDirty = true;

        BottomlessBackpacks.getInstance().getBackpackManager().getBackpackInventory().handleBackpackSavePageToggle(this);
    }

    /**
     * Should be called when the backpack's inventory gets closed.
     * This will push any outstanding updates to the database
     * @param shouldSavePage If the backpack should save the page (saving must be active for this backpack too)
     */
    public void onInventoryClose(boolean shouldSavePage) {
        if (!(shouldSavePage && savePageOnClose)) {
            currentPage = 1;
        }

        if (isSavePageDirty)  {
            Bukkit.getServer().getScheduler().runTaskAsynchronously(BottomlessBackpacks.getInstance(), () -> {
                BottomlessBackpacks.getInstance().getBackpackManager().getData().updateSavePages(uuid, savePageOnClose);
                isSavePageDirty = false;
            });
        }

        if (areItemsDirty) {
            Bukkit.getServer().getScheduler().runTaskAsynchronously(BottomlessBackpacks.getInstance(), () -> {
                BottomlessBackpacks.getInstance().getBackpackManager().getData().updateItems(uuid, items);
                areItemsDirty = false;
            });
        }
    }

    /**
     * Forces a save of this backpack.
     * These database calls are made synchronously because this should only be called during server shutdown
     */
    protected void forceSave() {
        if (areItemsDirty) {
            BottomlessBackpacks.getInstance().getBackpackManager().getData().updateItems(uuid, items);
        }
    }

    public int getCurrentPage() {
        return currentPage;
    }

    /**
     * Sets the current page of this backpack while keeping the page within bounds
     * @param page The page to set to
     */
    public void setCurrentPage(int page) {
        this.currentPage = Numbers.constrain(page, 1, maxPage);
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getMaxPage() {
        return maxPage;
    }

    public boolean isSavePageOnClose() {
        return savePageOnClose;
    }

    public boolean isOwnerOffline() {
        return Bukkit.getPlayer(uuid) == null;
    }

    protected BackpackViewers getBackpackViewers() {
        return backpackViewers;
    }

    @Override
    public String toString() {
        return uuid.toString() + " - " + items.length + " items - Pages: " + currentPage + "/" + maxPage;
    }
}
