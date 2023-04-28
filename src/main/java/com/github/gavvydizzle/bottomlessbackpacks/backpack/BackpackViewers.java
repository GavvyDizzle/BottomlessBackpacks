package com.github.gavvydizzle.bottomlessbackpacks.backpack;

import com.github.gavvydizzle.bottomlessbackpacks.BottomlessBackpacks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Represents a list of players who are viewing this backpack
 */
public class BackpackViewers {

    private int viewerID;
    private final HashMap<UUID, Viewer> viewers;

    public BackpackViewers() {
        viewers = new HashMap<>();
    }

    @Nullable
    private Viewer getViewer(UUID uuid) {
        return viewers.get(uuid);
    }

    /**
     * Adds a new viewer to this backpack
     * @param uuid The uuid
     * @param page The page
     */
    public void addViewer(UUID uuid, int page) {
        viewers.put(uuid, new Viewer(viewerID, uuid, page));
        viewerID++;
    }

    /**
     * Updates the page of this viewer
     * @param uuid The uuid
     * @param newPage The new page
     */
    public void updatePage(UUID uuid, int newPage) {
        Viewer viewer = getViewer(uuid);
        if (viewer == null) return;

        pushUpdatesOnUnlock(viewer);
        viewer.setPage(newPage);
    }

    /**
     * Removes a viewer from this backpack
     * @param uuid The uuid
     */
    public void removeViewer(UUID uuid) {
        Viewer viewer = viewers.remove(uuid);
        if (viewer != null) pushUpdatesOnUnlock(viewer);
    }

    /**
     * Determines if this page is locked.
     * If multiple players are on the same page, priority will be given to the player who opened it first.
     * @param uuid The player's uuid
     * @return If this player should not be able to edit their current page
     */
    public boolean isPageLocked(UUID uuid) {
        if (viewers.size() <= 1) return false;

        Viewer viewer = getViewer(uuid);
        if (viewer == null) return true;

        ArrayList<Viewer> otherViewers = new ArrayList<>(viewers.values());
        otherViewers.remove(viewer);

        for (Viewer other : otherViewers) {
            if (other.getPage() == viewer.getPage()) { // Viewing the same page

                // If the viewer in question accessed the page after someone else
                if (viewer.getPageAccessMillis() > other.getPageAccessMillis()) return true;
                // If the viewer in question started viewing the page at the same time but opened the backpack later
                else if (viewer.getPageAccessMillis() == other.getPageAccessMillis() && viewer.getId() > other.getId()) return true;
            }
        }
        return false;
    }

    /**
     * Refreshes the inventory of any player who was locked out
     * @param oldViewer The viewer before changing its page
     */
    private void pushUpdatesOnUnlock(Viewer oldViewer) {
        ArrayList<Viewer> otherViewers = new ArrayList<>(viewers.values());
        otherViewers.remove(oldViewer);

        for (Viewer other : otherViewers) {
            if (other.getPage() == oldViewer.getPage()) { // Viewing the same page

                // If "other" is locked out by "oldViewer"
                if (oldViewer.getPageAccessMillis() < other.getPageAccessMillis() ||
                        (oldViewer.getPageAccessMillis() == other.getPageAccessMillis() && oldViewer.getId() < other.getId()) ) {
                    BottomlessBackpacks.getInstance().getBackpackManager().getBackpackInventory().refreshBackpack(other.getUuid());
                }
            }
        }
    }

    public boolean isEmpty() {
        return viewers.isEmpty();
    }


    private static class Viewer {

        private final int id;
        private final UUID uuid;
        private int page;
        private long pageAccessMillis;

        public Viewer(int id, UUID uuid, int page) {
            this.id = id;
            this.uuid = uuid;
            this.page = page;
            pageAccessMillis = System.currentTimeMillis();
        }

        public int getId() {
            return id;
        }

        public UUID getUuid() {
            return uuid;
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
            pageAccessMillis = System.currentTimeMillis();
        }

        public long getPageAccessMillis() {
            return pageAccessMillis;
        }

        @Override
        public String toString() {
            return "Viewer " + id + ": " + uuid + " (" + page + "," + pageAccessMillis + ")";
        }
    }

}
