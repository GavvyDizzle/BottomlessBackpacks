package com.github.gavvydizzle.bottomlessbackpacks.commands.admin;

import com.github.gavvydizzle.bottomlessbackpacks.BottomlessBackpacks;
import com.github.gavvydizzle.bottomlessbackpacks.backpack.BackpackManager;
import com.github.gavvydizzle.bottomlessbackpacks.commands.AdminCommandManager;
import com.github.gavvydizzle.bottomlessbackpacks.config.CommandsConfig;
import com.github.gavvydizzle.bottomlessbackpacks.config.MenusConfig;
import com.github.gavvydizzle.bottomlessbackpacks.config.MessagesConfig;
import com.github.gavvydizzle.bottomlessbackpacks.config.SoundsConfig;
import com.github.gavvydizzle.bottomlessbackpacks.utils.Messages;
import com.github.gavvydizzle.bottomlessbackpacks.utils.Sounds;
import com.github.mittenmc.serverutils.PermissionCommand;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class ReloadCommand extends SubCommand implements PermissionCommand {

    private final AdminCommandManager adminCommandManager;
    private final BackpackManager backpackManager;
    private final ArrayList<String> argsList;

    public ReloadCommand(AdminCommandManager adminCommandManager, BackpackManager backpackManager) {
        this.adminCommandManager = adminCommandManager;
        this.backpackManager = backpackManager;
        argsList = new ArrayList<>();
        argsList.add("commands");
        argsList.add("gui");
        argsList.add("messages");
        argsList.add("sounds");
    }

    @Override
    public String getPermission() {
        return adminCommandManager.getPermissionPrefix() + getName().toLowerCase();
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getDescription() {
        return "Reloads this plugin or a specified portion";
    }

    @Override
    public String getSyntax() {
        return "/" + adminCommandManager.getCommandDisplayName() + " reload [arg]";
    }

    @Override
    public String getColoredSyntax() {
        return ChatColor.YELLOW + "Usage: " + getSyntax();
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length >= 2) {
            switch (args[1].toLowerCase()) {
                case "commands":
                    reloadCommands();
                    sender.sendMessage(ChatColor.GREEN + "[" + BottomlessBackpacks.getInstance().getName() + "] " + "Successfully reloaded commands");
                    break;
                case "gui":
                    reloadGUI();
                    sender.sendMessage(ChatColor.GREEN + "[" + BottomlessBackpacks.getInstance().getName() + "] " + "Successfully reloaded all GUIs");
                    break;
                case "messages":
                    reloadMessages();
                    sender.sendMessage(ChatColor.GREEN + "[" + BottomlessBackpacks.getInstance().getName() + "] " + "Successfully reloaded all messages");
                    break;
                case "sounds":
                    reloadSounds();
                    sender.sendMessage(ChatColor.GREEN + "[" + BottomlessBackpacks.getInstance().getName() + "] " + "Successfully reloaded all sounds");
                    break;

            }
        }
        else {
            reloadCommands();
            reloadGUI();
            reloadMessages();
            reloadSounds();
            sender.sendMessage(ChatColor.GREEN + "[" + BottomlessBackpacks.getInstance().getName() + "] " + "Successfully reloaded");
        }
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        ArrayList<String> list = new ArrayList<>();

        if (args.length == 2) {
            StringUtil.copyPartialMatches(args[1], argsList, list);
        }

        return list;
    }

    private void reloadCommands() {
        CommandsConfig.reload();
        adminCommandManager.reload();
    }

    private void reloadGUI() {
        MenusConfig.reload();
        backpackManager.reload();
    }

    private void reloadMessages() {
        MessagesConfig.reload();
        Messages.reloadMessages();
    }

    private void reloadSounds() {
        SoundsConfig.reload();
        Sounds.reload();
    }

}