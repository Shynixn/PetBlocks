package com.github.shynixn.petblocks.sponge.logic.business.commandexecutor;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Map;

/**
 * Holds the different types of commandExecutors.
 * <p>
 * Version 1.1
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2017 by Shynixn
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class SimpleCommandExecutor implements CommandExecutor {
    private CommandSpec commandSpec;
    private String permissionMessage;
    private String permission;
    private String name;

    protected PluginContainer plugin;

    public SimpleCommandExecutor(PluginContainer plugin) {
        this.plugin = plugin;
    }

    /**
     * Registers the command to the handler.
     *
     * @param data data
     */
    public CommandSpec register(Map<String, Object> data, OnCommandBuild onCommandBuild) {
        if (data.containsKey("enabled") && (boolean) data.get("enabled")) {
            this.name = (String) data.get("command");
            return this.register((String) data.get("command"), (String) data.get("description"), (String) data.get("permission"), (String) data.get("permission-message"), onCommandBuild);
        }
        return null;
    }

    /**
     * Registers the command to the handler.
     *
     * @param command           command
     * @param description       description
     * @param permission        permission
     * @param permissionMessage permissionMesssage
     */
    public CommandSpec register(String command, String description, String permission, String permissionMessage, OnCommandBuild onCommandBuild) {
        this.name = command;
        this.permission = permission;
        this.permissionMessage = permissionMessage;
        final CommandSpec.Builder builder = CommandSpec.builder()
                .description(this.parseString(description))
                .permission(permission)
                .executor(this);

        onCommandBuild.run(builder);
        this.commandSpec = builder.build();

        Sponge.getCommandManager().register(this.plugin, this.commandSpec, command);
        return this.commandSpec;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        final String[] arguments = args.getAll("text").toArray(new String[0]);
        if (src instanceof Player) {
            final Player player = (Player) src;
            if (this.permission != null && !player.hasPermission(this.permission)) {
                player.sendMessage(this.parseString(this.permissionMessage));
            } else {
                this.onPlayerExecuteCommand(player, args);
                this.onPlayerExecuteCommand(player, arguments);
            }
        }
        this.onCommandSenderExecuteCommand(src, args);
        this.onCommandSenderExecuteCommand(src, arguments);
        return CommandResult.success();
    }

    /**
     * Can be overwritten to listen to player executed commands.
     *
     * @param player player
     * @param args   args
     */
    protected void onPlayerExecuteCommand(Player player, String[] args) {

    }

    /**
     * Can be overwritten to listener to all executed commands.
     *
     * @param sender sender
     * @param args   args
     */
    protected void onCommandSenderExecuteCommand(CommandSource sender, String[] args) {

    }

    /**
     * Can be overwritten to listen to player executed commands.
     *
     * @param player player
     * @param args   args
     */
    public void onPlayerExecuteCommand(Player player, CommandContext args) {

    }

    /**
     * Can be overwritten to listener to all executed commands.
     *
     * @param sender sender
     * @param args   args
     */
    public void onCommandSenderExecuteCommand(CommandSource sender, CommandContext args) {

    }

    /**
     * Returns the name of the command.
     *
     * @return name
     */
    public String getName() {
        return this.name;
    }

    private Text parseString(String s) {
        return TextSerializers.LEGACY_FORMATTING_CODE.deserialize(s);
    }

    protected interface OnCommandBuild {
        /**
         * On command built.
         *
         * @param builder builder
         */
        void run(CommandSpec.Builder builder);
    }
}