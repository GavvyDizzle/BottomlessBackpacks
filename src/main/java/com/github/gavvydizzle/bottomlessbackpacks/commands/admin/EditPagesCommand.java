package com.github.gavvydizzle.bottomlessbackpacks.commands.admin;

import com.github.gavvydizzle.bottomlessbackpacks.BottomlessBackpacks;
import com.github.gavvydizzle.bottomlessbackpacks.backpack.Backpack;
import com.github.gavvydizzle.bottomlessbackpacks.backpack.BackpackManager;
import com.github.gavvydizzle.bottomlessbackpacks.commands.AdminCommandManager;
import com.github.gavvydizzle.bottomlessbackpacks.storage.PlayerData;
import com.github.mittenmc.serverutils.Numbers;
import com.github.mittenmc.serverutils.PermissionCommand;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EditPagesCommand extends SubCommand implements PermissionCommand {

    private final AdminCommandManager adminCommandManager;
    private final BackpackManager backpackManager;
    private final PlayerData data;

    private final List<String> actions = Arrays.asList("add", "remove", "set");

    public EditPagesCommand(AdminCommandManager adminCommandManager, BackpackManager backpackManager, PlayerData data) {
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
        return "pages";
    }

    @Override
    public String getDescription() {
        return "Edit the number of pages of a backpack";
    }

    @Override
    public String getSyntax() {
        return "/" + adminCommandManager.getCommandDisplayName() + " pages <player> <action> <amount>";
    }

    @Override
    public String getColoredSyntax() {
        return ChatColor.YELLOW + "Usage: " + getSyntax();
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 4) {
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

        Backpack backpack = backpackManager.getOfflineBackpack(destination);

        String action = args[2];

        int amount;
        try {
            amount = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid amount: " + args[3]);
            return;
        }
        if (amount <= 0) {
            sender.sendMessage(ChatColor.RED + "amount must be greater than 0");
            return;
        }

        if (action.equalsIgnoreCase("add")) {
            handlePageIncrement(sender, destination, backpack, amount);
        }
        else if (action.equalsIgnoreCase("remove")) {
            handlePageIncrement(sender, destination, backpack, -amount);
        }
        else if (action.equalsIgnoreCase("set")) {
            handlePageSet(sender, destination, backpack, amount);
        }
        else {
            sender.sendMessage(ChatColor.RED + "Invalid action: " + action);
        }
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        ArrayList<String> list = new ArrayList<>();

        if (args.length == 2) {
            return null;
        }
        else if (args.length == 3) {
            StringUtil.copyPartialMatches(args[2], actions, list);
        }

        return list;
    }

    /**
     * Updates the number of pages of this backpack
     * @param sender The command sender
     * @param destination The player who owns the backpack
     * @param backpack The backpack or null
     * @param amount The amount to add to the current page count
     */
    private void handlePageIncrement(CommandSender sender, OfflinePlayer destination, @Nullable Backpack backpack, int amount) {
        if (backpack != null) {
            int numPages = Numbers.constrain(backpack.getMaxPage() + amount, 1, Backpack.MAX_PAGE_AMOUNT);

            if (numPages == backpack.getMaxPage()) {
                sender.sendMessage(ChatColor.YELLOW + "Command ignored. Page count is already " + numPages);
                return;
            }

            sender.sendMessage(ChatColor.GREEN + "New page count: " + numPages + " (was " + backpack.getMaxPage() + ")");
            backpack.updateMaxPage(numPages);
        }
        else {
            Bukkit.getScheduler().runTaskAsynchronously(BottomlessBackpacks.getInstance(), () -> {
                int maxPages = data.getNumPages(destination.getUniqueId());
                if (maxPages == -1) {
                    sender.sendMessage(ChatColor.RED + "Failed to update the page total");
                    return;
                }

                int numPages = Numbers.constrain(maxPages + amount, 1, Backpack.MAX_PAGE_AMOUNT);

                if (numPages == maxPages) {
                    sender.sendMessage(ChatColor.YELLOW + "Command ignored. Page count is already " + numPages);
                    return;
                }

                if (data.updatePages(destination.getUniqueId(), numPages)) {
                    sender.sendMessage(ChatColor.GREEN + "New page count: " + numPages + " (was " + maxPages + ")");
                }
                else {
                    sender.sendMessage(ChatColor.RED + "Failed to update the page total");
                }
            });
        }
    }

    /**
     * Updates the number of pages of this backpack
     * @param sender The command sender
     * @param destination The player who owns the backpack
     * @param backpack The backpack or null
     * @param newMax The new number of pages
     */
    private void handlePageSet(CommandSender sender, OfflinePlayer destination, @Nullable Backpack backpack, int newMax) {
        int numPages = Numbers.constrain(newMax, 1, Backpack.MAX_PAGE_AMOUNT);

        if (backpack != null) {
            if (numPages == backpack.getMaxPage()) {
                sender.sendMessage(ChatColor.YELLOW + "Command ignored. Page count is already " + numPages);
                return;
            }

            sender.sendMessage(ChatColor.GREEN + "New page count: " + numPages + " (was " + backpack.getMaxPage() + ")");
            backpack.updateMaxPage(numPages);
        }
        else {
            Bukkit.getScheduler().runTaskAsynchronously(BottomlessBackpacks.getInstance(), () -> {
                int maxPages = data.getNumPages(destination.getUniqueId());
                if (maxPages == -1) {
                    sender.sendMessage(ChatColor.RED + "Failed to update the page total");
                    return;
                }

                if (numPages == maxPages) {
                    sender.sendMessage(ChatColor.YELLOW + "Command ignored. Page count is already " + numPages);
                    return;
                }

                if (data.updatePages(destination.getUniqueId(), numPages)) {
                    sender.sendMessage(ChatColor.GREEN + "New page count: " + numPages + " (was " + maxPages + ")");
                }
                else {
                    sender.sendMessage(ChatColor.RED + "Failed to update the page total");
                }
            });
        }
    }
}