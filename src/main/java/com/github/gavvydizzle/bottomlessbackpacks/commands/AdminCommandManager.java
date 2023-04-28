package com.github.gavvydizzle.bottomlessbackpacks.commands;

import com.github.gavvydizzle.bottomlessbackpacks.BottomlessBackpacks;
import com.github.gavvydizzle.bottomlessbackpacks.backpack.BackpackManager;
import com.github.gavvydizzle.bottomlessbackpacks.commands.admin.*;
import com.github.gavvydizzle.bottomlessbackpacks.config.CommandsConfig;
import com.github.gavvydizzle.bottomlessbackpacks.storage.PlayerData;
import com.github.mittenmc.serverutils.Colors;
import com.github.mittenmc.serverutils.PermissionCommand;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdminCommandManager implements TabExecutor {

    private final PluginCommand command;
    private final CommandConfirmationManager confirmationManager;
    private final ArrayList<SubCommand> subcommands = new ArrayList<>();
    private final ArrayList<String> subcommandStrings = new ArrayList<>();
    private String commandDisplayName, helpCommandPadding;

    public AdminCommandManager(PluginCommand command, CommandConfirmationManager confirmationManager, BackpackManager backpackManager, PlayerData data) {
        this.command = command;
        command.setExecutor(this);

        this.confirmationManager = confirmationManager;

        subcommands.add(new AdminConfirmCommand(this, confirmationManager));
        subcommands.add(new AdminHelpCommand(this));
        subcommands.add(new AdminOpenBackpackCommand(this, backpackManager, data));
        subcommands.add(new ClearBackpackCommand(this, backpackManager, data));
        subcommands.add(new EditPagesCommand(this, backpackManager, data));
        subcommands.add(new ReloadCommand(this, backpackManager));
        subcommands.add(new ResetAllBackpacksCommand(this, backpackManager, data));
        subcommands.add(new ResetBackpackCommand(this, backpackManager, data));
        Collections.sort(subcommands);

        for (SubCommand subCommand : subcommands) {
            subcommandStrings.add(subCommand.getName());
        }

        reload();
    }

    public void reload() {
        FileConfiguration config = CommandsConfig.get();
        config.options().copyDefaults(true);
        config.addDefault("commandDisplayName.admin", command.getName());
        config.addDefault("helpCommandPadding.admin", "&6-----(" + BottomlessBackpacks.getInstance().getName() + " Admin Commands)-----");

        for (SubCommand subCommand : subcommands) {
            CommandsConfig.setAdminDescriptionDefault(subCommand);
        }
        CommandsConfig.save();

        commandDisplayName = config.getString("commandDisplayName.admin");
        helpCommandPadding = Colors.conv(config.getString("helpCommandPadding.admin"));
    }

    public String getCommandDisplayName() {
        return commandDisplayName;
    }

    public String getHelpCommandPadding() {
        return helpCommandPadding;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length > 0) {
            for (int i = 0; i < getSubcommands().size(); i++) {
                if (args[0].equalsIgnoreCase(getSubcommands().get(i).getName())) {

                    SubCommand subCommand = subcommands.get(i);

                    if (subCommand instanceof PermissionCommand &&
                            !sender.hasPermission(((PermissionCommand) subCommand).getPermission())) {
                        sender.sendMessage(ChatColor.RED + "Insufficient permission");
                        return true;
                    }

                    if (subCommand instanceof ConfirmationCommand) {
                        confirmationManager.attemptConfirmationCreation(subCommand, ((ConfirmationCommand) subCommand).getConfirmationMessage(), sender, args);
                        return true;
                    }

                    subCommand.perform(sender, args);
                    return true;
                }
            }
            sender.sendMessage(ChatColor.RED + "Invalid command");
        }
        sender.sendMessage(ChatColor.YELLOW + "Use '/" + commandDisplayName + " help' to see a list of valid commands");

        return true;
    }

    public ArrayList<SubCommand> getSubcommands(){
        return subcommands;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) {
            ArrayList<String> subcommandsArguments = new ArrayList<>();

            StringUtil.copyPartialMatches(args[0], subcommandStrings, subcommandsArguments);

            return subcommandsArguments;
        }
        else if (args.length >= 2) {
            for (SubCommand subcommand : subcommands) {
                if (args[0].equalsIgnoreCase(subcommand.getName())) {
                    return subcommand.getSubcommandArguments(sender, args);
                }
            }
        }

        return null;
    }

    public String getPermissionPrefix() {
        return command.getPermission() + ".";
    }

    public PluginCommand getCommand() {
        return command;
    }
}