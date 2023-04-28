package com.github.gavvydizzle.bottomlessbackpacks;

import com.github.gavvydizzle.bottomlessbackpacks.backpack.BackpackManager;
import com.github.gavvydizzle.bottomlessbackpacks.commands.AdminCommandManager;
import com.github.gavvydizzle.bottomlessbackpacks.commands.CommandConfirmationManager;
import com.github.gavvydizzle.bottomlessbackpacks.commands.PlayerCommandManager;
import com.github.gavvydizzle.bottomlessbackpacks.storage.Configuration;
import com.github.gavvydizzle.bottomlessbackpacks.storage.DataSourceProvider;
import com.github.gavvydizzle.bottomlessbackpacks.storage.DbSetup;
import com.github.gavvydizzle.bottomlessbackpacks.storage.PlayerData;
import com.github.gavvydizzle.bottomlessbackpacks.utils.Messages;
import com.github.gavvydizzle.bottomlessbackpacks.utils.Sounds;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Level;

public final class BottomlessBackpacks extends JavaPlugin {

    private static BottomlessBackpacks instance;
    private BackpackManager backpackManager;
    private CommandConfirmationManager commandConfirmationManager;

    private DataSource dataSource;
    private PlayerData data;
    private boolean mySQLSuccessful;

    @Override
    public void onLoad() {
        generateDefaultConfig();
        Configuration configuration = new Configuration(this);
        mySQLSuccessful = true;

        try {
            dataSource = DataSourceProvider.initMySQLDataSource(this, configuration.getDatabase());
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Could not establish database connection", e);
            mySQLSuccessful = false;
        }

        try {
            DbSetup.initDb(this, dataSource);
        } catch (SQLException | IOException e) {
            getLogger().log(Level.SEVERE, "Could not init database.", e);
            mySQLSuccessful = false;
        }
    }

    @Override
    public void onEnable() {
        if (!mySQLSuccessful) {
            getLogger().log(Level.SEVERE, "Database connection failed. Disabling plugin");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        instance = this;
        data = new PlayerData(this, dataSource);

        backpackManager = new BackpackManager(instance, data);
        getServer().getPluginManager().registerEvents(backpackManager, this);

        commandConfirmationManager = new CommandConfirmationManager();

        try {
            new AdminCommandManager(Objects.requireNonNull(getCommand("backpackadmin")), commandConfirmationManager, backpackManager, data);
        } catch (NullPointerException e) {
            getLogger().severe("The admin command name was changed in the plugin.yml file. Please make it \"backpackadmin\" and restart the server. You can change the aliases but NOT the command name.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        try {
            Objects.requireNonNull(getCommand("backpack")).setExecutor(new PlayerCommandManager(backpackManager));
        } catch (NullPointerException e) {
            getLogger().severe("The player command name was changed in the plugin.yml file. Please make it \"backpack\" and restart the server. You can change the aliases but NOT the command name.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        Messages.reloadMessages();
        Sounds.reload();
    }

    @Override
    public void onDisable() {
        if (backpackManager != null) {
            backpackManager.saveOpenBackpacksOnShutdown();
        }
    }

    private void generateDefaultConfig() {
        FileConfiguration config = getConfig();
        config.options().copyDefaults(true);

        config.addDefault("database.host", "TODO");
        config.addDefault("database.port", 3306);
        config.addDefault("database.user", "TODO");
        config.addDefault("database.password", "TODO");
        config.addDefault("database.database", "TODO");
        saveConfig();
    }


    public static BottomlessBackpacks getInstance() {
        return instance;
    }

    public BackpackManager getBackpackManager() {
        return backpackManager;
    }

    public CommandConfirmationManager getCommandConfirmationManager() {
        return commandConfirmationManager;
    }
}
