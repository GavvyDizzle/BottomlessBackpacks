package com.github.gavvydizzle.bottomlessbackpacks.commands.admin;

import com.github.gavvydizzle.bottomlessbackpacks.BottomlessBackpacks;
import com.github.gavvydizzle.bottomlessbackpacks.backpack.Backpack;
import com.github.gavvydizzle.bottomlessbackpacks.backpack.BackpackManager;
import com.github.gavvydizzle.bottomlessbackpacks.commands.AdminCommandManager;
import com.github.gavvydizzle.bottomlessbackpacks.storage.PlayerData;
import com.github.gavvydizzle.bottomlessbackpacks.utils.Messages;
import com.github.mittenmc.serverutils.PermissionCommand;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class AdminOpenBackpackCommand extends SubCommand implements PermissionCommand {

    private final AdminCommandManager adminCommandManager;
    private final BackpackManager backpackManager;
    private final PlayerData data;

    public AdminOpenBackpackCommand(AdminCommandManager adminCommandManager, BackpackManager backpackManager, PlayerData data) {
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
        return "open";
    }

    @Override
    public String getDescription() {
        return "Open a player's backpack";
    }

    @Override
    public String getSyntax() {
        return "/" + adminCommandManager.getCommandDisplayName() + " open <player> [page]";
    }

    @Override
    public String getColoredSyntax() {
        return ChatColor.YELLOW + "Usage: " + getSyntax();
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return;

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

        if (((Player) sender).getUniqueId().equals(destination.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "Unable to view your own backpack with this command");
            return;
        }

        int page = -1;
        if (args.length >= 3) {
            try {
                page = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(Messages.invalidPageNumber);
                return;
            }
        }

        Backpack backpack = backpackManager.getOfflineBackpack(destination);
        if (backpack != null) {
            backpackManager.adminOpenBackpack((Player) sender, destination, page);
        }
        else { // Load backpack from database and show it to the admin
            if (destination.isOnline()) {
                sender.sendMessage(ChatColor.RED + "No backpack loaded for " + destination.getName());
                return;
            }

            sender.sendMessage(ChatColor.YELLOW + "Generating backpack for " + destination.getName() + "...");

            OfflinePlayer finalDestination = destination;
            int finalPage = page;
            Bukkit.getScheduler().runTaskAsynchronously(BottomlessBackpacks.getInstance(), () -> {
                Backpack offlineBackpack = data.getPlayerData(finalDestination);

                // Inventory opens must be handled synchronously
                Bukkit.getScheduler().runTask(BottomlessBackpacks.getInstance(), () ->
                        backpackManager.adminOpenOfflineBackpack((Player) sender, offlineBackpack, finalPage, finalDestination.getName()));
            });
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
