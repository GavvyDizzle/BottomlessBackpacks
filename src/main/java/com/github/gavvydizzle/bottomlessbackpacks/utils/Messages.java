package com.github.gavvydizzle.bottomlessbackpacks.utils;

import com.github.gavvydizzle.bottomlessbackpacks.config.MessagesConfig;
import com.github.mittenmc.serverutils.Colors;
import org.bukkit.configuration.file.FileConfiguration;

public class Messages {

    // Errors
    public static String backpackFailedToLoad, invalidPageNumber, backpackPageLocked;

    public static void reloadMessages() {
        FileConfiguration config = MessagesConfig.get();
        config.options().copyDefaults(true);

        config.addDefault("backpackFailedToLoad", "&cYour backpack failed to load!");
        config.addDefault("invalidPageNumber", "&cInvalid page number provided");
        config.addDefault("backpackPageLocked", "&cUnable to edit page");

        MessagesConfig.save();

        backpackFailedToLoad = Colors.conv(config.getString("backpackFailedToLoad"));
        invalidPageNumber = Colors.conv(config.getString("invalidPageNumber"));
        backpackPageLocked = Colors.conv(config.getString("backpackPageLocked"));
    }
}
