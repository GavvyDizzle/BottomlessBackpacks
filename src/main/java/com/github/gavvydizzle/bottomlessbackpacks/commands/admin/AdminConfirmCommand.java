package com.github.gavvydizzle.bottomlessbackpacks.commands.admin;

import com.github.gavvydizzle.bottomlessbackpacks.BottomlessBackpacks;
import com.github.gavvydizzle.bottomlessbackpacks.commands.AdminCommandManager;
import com.github.gavvydizzle.bottomlessbackpacks.commands.CommandConfirmationManager;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class AdminConfirmCommand extends SubCommand {

    private final AdminCommandManager adminCommandManager;
    private final CommandConfirmationManager confirmationManager;

    public AdminConfirmCommand(AdminCommandManager adminCommandManager, CommandConfirmationManager confirmationManager) {
        this.adminCommandManager = adminCommandManager;
        this.confirmationManager = confirmationManager;
    }

    @Override
    public String getName() {
        return "confirm";
    }

    @Override
    public String getDescription() {
        return "Confirm an action";
    }

    @Override
    public String getSyntax() {
        return "/" + adminCommandManager.getCommandDisplayName() + " confirm";
    }

    @Override
    public String getColoredSyntax() {
        return ChatColor.YELLOW + "Usage: " + getSyntax();
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return;
        confirmationManager.onConfirmCommandRun((Player) sender);
    }

    @Override
    public List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        return null;
    }
}