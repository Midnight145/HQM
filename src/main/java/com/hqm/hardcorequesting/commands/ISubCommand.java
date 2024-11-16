package com.hqm.hardcorequesting.commands;

import java.util.List;

import net.minecraft.command.ICommandSender;

public interface ISubCommand {

    int getPermissionLevel();

    String getCommandName();

    void handleCommand(ICommandSender sender, String[] arguments);

    List<String> addTabCompletionOptions(ICommandSender sender, String[] args);

    boolean isVisible(ICommandSender sender);

    int[] getSyntaxOptions(ICommandSender sender);
}
