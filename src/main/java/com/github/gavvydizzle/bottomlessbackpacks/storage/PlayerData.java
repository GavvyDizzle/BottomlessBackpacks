package com.github.gavvydizzle.bottomlessbackpacks.storage;

import com.github.gavvydizzle.bottomlessbackpacks.backpack.Backpack;
import com.github.mittenmc.serverutils.ItemStackSerializer;
import com.github.mittenmc.serverutils.Numbers;
import com.github.mittenmc.serverutils.UUIDConverter;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.sql.*;
import java.util.UUID;

public class PlayerData extends PluginDataHolder {

    private static final int DEFAULT_NUM_PAGES = 1;

    private final static String tableName = "backpacks";

    private final static String INSERT_PLAYER = "INSERT IGNORE INTO " + tableName + "(uuid, pages, items) VALUES(?,?,?)";
    private final static String LOAD_PLAYER_DATA = "SELECT * FROM " + tableName + " WHERE uuid = ?";

    private final static String GET_NUM_PAGES = "SELECT pages FROM " + tableName + " WHERE uuid=?";
    private final static String UPDATE_NUM_PAGES = "UPDATE " + tableName + " SET pages=? WHERE uuid=?";
    private final static String UPDATE_SAVING_PAGES = "UPDATE " + tableName + " SET savePage=? WHERE uuid=?";
    private final static String UPDATE_ITEMS = "UPDATE " + tableName + " SET items=? WHERE uuid=?";

    private final static String RESET_PLAYER_DATA = "UPDATE " + tableName + " SET pages=?, items=? WHERE uuid=?";
    private final static String RESET_ALL_DATA = "UPDATE " + tableName + " SET pages=?, items=?";


    /**
     * Create a new {@link PluginDataHolder} with a datasource to server connections and a plugin for logging.
     *
     * @param plugin plugin for logging
     * @param source source to provide connections.
     */
    public PlayerData(Plugin plugin, DataSource source) {
        super(plugin, source);
    }

    /**
     * Gets the player's backpack
     * @param offlinePlayer The player
     * @return The player's backpack or null if an error occurred
     */
    @Nullable
    public Backpack getPlayerData(OfflinePlayer offlinePlayer) {
        Connection conn;
        try {
            conn = conn();
        } catch (SQLException e) {
            logSQLError("Could not connect to the database", e);
            return null;
        }

        try {
            PreparedStatement stmt = conn.prepareStatement(INSERT_PLAYER);
            stmt.setBytes(1, UUIDConverter.convert(offlinePlayer.getUniqueId()));
            stmt.setInt(2, DEFAULT_NUM_PAGES);
            stmt.setBytes(3, null);
            if (stmt.execute()) { // If the entry was added, then the backpack is empty
                return new Backpack(offlinePlayer.getUniqueId(), null, DEFAULT_NUM_PAGES, false);
            }
        } catch (SQLException e) {
            logSQLError("Failed to initialize database entry.", e);
            return null;
        }

        try {
            PreparedStatement stmt = conn.prepareStatement(LOAD_PLAYER_DATA);
            stmt.setBytes(1, UUIDConverter.convert(offlinePlayer.getUniqueId()));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Backpack(offlinePlayer.getUniqueId(),
                        ItemStackSerializer.deserializeItemStackArray(rs.getBytes("items")),
                        rs.getInt("pages"),
                        rs.getBoolean("savePage"));
            } else {
                return null;
            }
        } catch (SQLException e) {
            logSQLError("Failed to load backpack data.", e);
            return null;
        }
    }

    /**
     * Updates the number of pages for this backpack has.
     *
     * @param uuid  The player's uuid
     * @param value The number of pages
     * @return If the data updated successfully or the value was <= 0
     */
    public boolean updatePages(UUID uuid, int value) {
        if (value <= 0) return false;
        value = Numbers.constrain(value, 1, Backpack.MAX_PAGE_AMOUNT);

        Connection conn;
        try {
            conn = conn();
        } catch (SQLException e) {
            logSQLError("Could not connect to the database", e);
            return false;
        }

        try {
            PreparedStatement stmt = conn.prepareStatement(UPDATE_NUM_PAGES);
            stmt.setInt(1, value);
            stmt.setBytes(2, UUIDConverter.convert(uuid));
            stmt.execute();
            return true;
        } catch (SQLException e) {
            logSQLError("Could not update number of pages", e);
            return false;
        }
    }

    /**
     * Gets the number of pages for this backpack.
     * @param uuid  The player's uuid
     * @return The number of pages or -1 if an error occurred
     */
    public int getNumPages(UUID uuid) {
        Connection conn;
        try {
            conn = conn();
        } catch (SQLException e) {
            logSQLError("Could not connect to the database", e);
            return -1;
        }

        try {
            PreparedStatement stmt = conn.prepareStatement(GET_NUM_PAGES);
            stmt.setBytes(1, UUIDConverter.convert(uuid));
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("pages") : -1;
        } catch (SQLException e) {
            logSQLError("Could not get number of pages", e);
            return -1;
        }
    }

    /**
     * Updates if this backpack should retain its page when closed
     *
     * @param uuid  The player's uuid
     * @param value If the page should save when closed
     */
    public void updateSavePages(UUID uuid, boolean value) {
        Connection conn;
        try {
            conn = conn();
        } catch (SQLException e) {
            logSQLError("Could not connect to the database", e);
            return;
        }

        try {
            PreparedStatement stmt = conn.prepareStatement(UPDATE_SAVING_PAGES);
            stmt.setBoolean(1, value);
            stmt.setBytes(2, UUIDConverter.convert(uuid));
            stmt.execute();

        } catch (SQLException e) {
            logSQLError("Could not update saving pages", e);
        }
    }

    /**
     * Updates the number of pages for this backpack has.
     *
     * @param uuid  The player's uuid
     * @param items The array of ItemStacks
     */
    public void updateItems(UUID uuid, ItemStack[] items) {
        Connection conn;
        try {
            conn = conn();
        } catch (SQLException e) {
            logSQLError("Could not connect to the database", e);
            return;
        }

        try {
            PreparedStatement stmt = conn.prepareStatement(UPDATE_ITEMS);
            stmt.setBytes(1, ItemStackSerializer.serializeItemStackArray(items));
            stmt.setBytes(2, UUIDConverter.convert(uuid));
            stmt.execute();

        } catch (SQLException e) {
            logSQLError("Could not update items", e);
        }
    }

    /**
     * Resets the items and number of pages of this backpack to the default state.
     *
     * @param uuid The player's uuid
     * @return If the data updated successfully
     */
    public boolean deletePlayerData(UUID uuid) {
        Connection conn;
        try {
            conn = conn();
        } catch (SQLException e) {
            logSQLError("Could not connect to the database", e);
            return false;
        }

        try {
            PreparedStatement stmt = conn.prepareStatement(RESET_PLAYER_DATA);
            stmt.setInt(1, DEFAULT_NUM_PAGES);
            stmt.setBytes(2, null);
            stmt.setBytes(3, UUIDConverter.convert(uuid));
            stmt.execute();
            return true;
        } catch (SQLException e) {
            logSQLError("Failed to reset player data", e);
            return false;
        }
    }

    /**
     * Resets all data in the database to its default state
     * @return If the data reset successfully
     */
    public boolean resetAllData() {
        Connection conn;
        try {
            conn = conn();
        } catch (SQLException e) {
            logSQLError("Could not connect to the database", e);
            return false;
        }

        try {
            PreparedStatement stmt = conn.prepareStatement(RESET_ALL_DATA);
            stmt.setInt(1, DEFAULT_NUM_PAGES);
            stmt.setBytes(2, null);
            stmt.execute();
            return true;
        } catch (SQLException e) {
            logSQLError("Failed to delete all data", e);
            return false;
        }
    }
}