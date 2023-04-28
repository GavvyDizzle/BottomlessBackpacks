package com.github.gavvydizzle.bottomlessbackpacks.commands;

import com.github.gavvydizzle.bottomlessbackpacks.BottomlessBackpacks;
import com.github.mittenmc.serverutils.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CommandConfirmation {

    private final SubCommand subCommand;
    private final Player player;
    private final String[] args;
    private final int taskID;

    public CommandConfirmation(SubCommand subCommand, Player player, String[] args) {
        this.subCommand = subCommand;
        this.player = player;
        this.args = args;

        taskID = Bukkit.getScheduler().scheduleSyncDelayedTask(BottomlessBackpacks.getInstance(), () ->
                BottomlessBackpacks.getInstance().getCommandConfirmationManager().onConfirmExpire(player), 200);
    }

    public SubCommand getSubCommand() {
        return subCommand;
    }

    public Player getCommandSender() {
        return player;
    }

    public String[] getArgs() {
        return args;
    }

    public void cancelTask() {
        Bukkit.getScheduler().cancelTask(taskID);
    }
}
