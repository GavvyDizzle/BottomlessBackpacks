package com.github.gavvydizzle.bottomlessbackpacks.commands.admin;

import com.github.gavvydizzle.bottomlessbackpacks.BottomlessBackpacks;
import com.github.gavvydizzle.bottomlessbackpacks.backpack.Backpack;
import com.github.gavvydizzle.bottomlessbackpacks.backpack.BackpackManager;
import com.github.gavvydizzle.bottomlessbackpacks.commands.AdminCommandManager;
import com.github.gavvydizzle.bottomlessbackpacks.storage.PlayerData;
import com.github.mittenmc.serverutils.PermissionCommand;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class ClearBackpackCommand extends SubCommand implements PermissionCommand {

    private final AdminCommandManager adminCommandManager;
    private final BackpackManager backpackManager;
    private final PlayerData data;

    public ClearBackpackCommand(AdminCommandManager adminCommandManager, BackpackManager backpackManager, PlayerData data) {
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
        return "clear";
    }

    @Override
    public String getDescription() {
        return "Clear all items from a backpack";
    }

    @Override
    public String getSyntax() {
        return "/" + adminCommandManager.getCommandDisplayName() + " clear <player>";
    }

    @Override
    public String getColoredSyntax() {
        return ChatColor.YELLOW + "Usage: " + getSyntax();
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(getColoredSyntax());
            return;
        }

        OfflinePlayer destination = Bukkit.getPlayer(args[1]);
        if (destination == null) {
            destination = Bukkit.getOfflinePlayer(args[1]);
            if (!destination.hasPlayedBefore() && !destination.isOnline()) {
                sender.sendMessage(ChatColor.RED + args[1] + " is not a valid player.");
                return;
            }
        }

        Backpack backpack = backpackManager.getBackpack(destination);

        try {
            if (backpack != null) {
                backpack.clearBackpack();
            } else {
                OfflinePlayer finalDestination1 = destination;
                Bukkit.getServer().getScheduler().runTaskAsynchronously(BottomlessBackpacks.getInstance(),
                        () -> data.updateItems(finalDestination1.getUniqueId(), null));
            }
            sender.sendMessage(ChatColor.GREEN + "Successfully cleared " + destination.getName() + "'s backpack");

        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Failed to clear " + destination.getName() + "'s backpack. Check the console for the error");
            e.printStackTrace();
        }
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        if (args.length == 2) {
            return null;
        }
        return Collections.emptyList();
    }
}
