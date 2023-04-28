package com.github.gavvydizzle.bottomlessbackpacks.commands.admin;

import com.github.gavvydizzle.bottomlessbackpacks.BottomlessBackpacks;
import com.github.gavvydizzle.bottomlessbackpacks.backpack.BackpackManager;
import com.github.gavvydizzle.bottomlessbackpacks.commands.AdminCommandManager;
import com.github.gavvydizzle.bottomlessbackpacks.commands.ConfirmationCommand;
import com.github.gavvydizzle.bottomlessbackpacks.storage.PlayerData;
import com.github.mittenmc.serverutils.PermissionCommand;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class ResetAllBackpacksCommand extends SubCommand implements ConfirmationCommand, PermissionCommand {

    private final AdminCommandManager adminCommandManager;
    private final BackpackManager backpackManager;
    private final PlayerData data;

    public ResetAllBackpacksCommand(AdminCommandManager adminCommandManager, BackpackManager backpackManager, PlayerData data) {
        this.adminCommandManager = adminCommandManager;
        this.backpackManager = backpackManager;
        this.data = data;
    }

    @Override
    public String getPermission() {
        return adminCommandManager.getPermissionPrefix() + getName().toLowerCase();
    }

    @Override
    public String getName() {
        return "resetAllData";
    }

    @Override
    public String getDescription() {
        return "Resets all backpacks";
    }

    @Override
    public String getSyntax() {
        return "/" + adminCommandManager.getCommandDisplayName() + " resetAllData";
    }

    @Override
    public String getColoredSyntax() {
        return ChatColor.YELLOW + "Usage: " + getSyntax();
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        Bukkit.getScheduler().runTaskAsynchronously(BottomlessBackpacks.getInstance(), () -> {
           if (data.resetAllData()) {
                backpackManager.resetAllLoadedBackpacks();
               sender.sendMessage(ChatColor.GREEN + "Successfully reset all backpacks");
           }
           else {
               sender.sendMessage(ChatColor.RED + "Failed to reset backpack data. Check the console for errors");
           }
        });
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    @Override
    public String getConfirmationMessage() {
        return ChatColor.DARK_RED + "Using this command will reset EVERY BACKPACK! Only run /" + adminCommandManager.getCommandDisplayName() + " confirm if you are certain you want to continue";
    }
}
