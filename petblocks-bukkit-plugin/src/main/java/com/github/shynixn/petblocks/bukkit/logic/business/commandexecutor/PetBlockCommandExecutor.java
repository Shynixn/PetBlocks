package com.github.shynixn.petblocks.bukkit.logic.business.commandexecutor;

import com.github.shynixn.petblocks.api.business.entity.GUIItemContainer;
import com.github.shynixn.petblocks.api.business.entity.PetBlock;
import com.github.shynixn.petblocks.api.persistence.entity.EngineContainer;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.bukkit.PetBlocksPlugin;
import com.github.shynixn.petblocks.bukkit.lib.ChatBuilder;
import com.github.shynixn.petblocks.bukkit.lib.SimpleCommandExecutor;
import com.github.shynixn.petblocks.bukkit.logic.business.PetBlockManager;
import com.github.shynixn.petblocks.bukkit.logic.business.PetRunnable;
import com.github.shynixn.petblocks.bukkit.logic.business.configuration.Config;
import com.github.shynixn.petblocks.bukkit.logic.business.configuration.ConfigPet;
import com.github.shynixn.petblocks.bukkit.logic.business.helper.PetBlockModifyHelper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class PetBlockCommandExecutor extends SimpleCommandExecutor.UnRegistered {
    private final PetBlockManager manager;

    public PetBlockCommandExecutor(PetBlockManager manager) throws Exception {
        super(((MemorySection) JavaPlugin.getPlugin(PetBlocksPlugin.class).getConfig().get("petblocks-configuration")).getValues(false)
                , JavaPlugin.getPlugin(PetBlocksPlugin.class));
        this.manager = manager;
    }

    /**
     * Can be overwritten to listener to all executed commands.
     *
     * @param sender sender
     * @param args   args
     */
    @Override
    public void onCommandSenderExecuteCommand(CommandSender sender, String[] args) {
        if (args.length == 2 && args[0].equalsIgnoreCase("engine") && sender instanceof Player && tryParseInt(args[1]))
            this.setEngineCommand((Player) sender, Integer.parseInt(args[1]));
        else if (args.length == 3 && args[0].equalsIgnoreCase("engine") && this.getOnlinePlayer(args[2]) != null && tryParseInt(args[1]))
            this.setEngineCommand(this.getOnlinePlayer(args[2]), Integer.parseInt(args[1]));
        else if (args.length == 3 && args[0].equalsIgnoreCase("costume") && sender instanceof Player && tryParseInt(args[2]))
            this.setCostumeCommand((Player) sender, args[1], Integer.parseInt(args[2]));
        else if (args.length == 4 && args[0].equalsIgnoreCase("costume") && this.getOnlinePlayer(args[3]) != null && tryParseInt(args[2]))
            this.setCostumeCommand(this.getOnlinePlayer(args[3]), args[1], Integer.parseInt(args[2]));
        else if (args.length == 1 && args[0].equalsIgnoreCase("enable") && sender instanceof Player)
            this.setPetCommand((Player) sender);
        else if (args.length == 2 && args[0].equalsIgnoreCase("enable") && this.getOnlinePlayer(args[1]) != null)
            this.setPetCommand(this.getOnlinePlayer(args[1]));
        else if (args.length == 1 && args[0].equalsIgnoreCase("disable") && sender instanceof Player)
            this.removePetCommand((Player) sender);
        else if (args.length == 2 && args[0].equalsIgnoreCase("disable") && this.getOnlinePlayer(args[1]) != null)
            this.removePetCommand(this.getOnlinePlayer(args[1]));
        else if (args.length == 1 && args[0].equalsIgnoreCase("toggle") && sender instanceof Player)
            this.togglePetCommand((Player) sender);
        else if (args.length == 2 && args[0].equalsIgnoreCase("toggle") && this.getOnlinePlayer(args[1]) != null)
            this.togglePetCommand(this.getOnlinePlayer(args[1]));
        else if (args.length >= 2 && args[0].equalsIgnoreCase("rename"))
            this.namePetCommand(sender, args);
        else if (args.length == 2 && args[0].equalsIgnoreCase("skin") && sender instanceof Player)
            this.changePetSkinCommand((Player) sender, args[1]);
        else if (args.length == 3 && args[0].equalsIgnoreCase("skin") && this.getOnlinePlayer(args[2]) != null)
            this.changePetSkinCommand(this.getOnlinePlayer(args[2]), args[1]);
        else if (args.length == 2 && args[0].equalsIgnoreCase("particle") && sender instanceof Player && tryParseInt(args[1]))
            this.setParticleCommand((Player) sender, Integer.parseInt(args[1]));
        else if (args.length == 3 && args[0].equalsIgnoreCase("particle") && this.getOnlinePlayer(args[2]) != null && tryParseInt(args[1]))
            this.setParticleCommand(this.getOnlinePlayer(args[2]), Integer.parseInt(args[1]));
        else if (args.length == 1 && args[0].equalsIgnoreCase("hat") && sender instanceof Player)
            this.hatPetCommand((Player) sender);
        else if (args.length == 2 && args[0].equalsIgnoreCase("hat") && this.getOnlinePlayer(args[1]) != null)
            this.hatPetCommand(this.getOnlinePlayer(args[1]));
        else if (args.length == 1 && args[0].equalsIgnoreCase("ride") && sender instanceof Player)
            this.ridePetCommand((Player) sender);
        else if (args.length == 2 && args[0].equalsIgnoreCase("ride") && this.getOnlinePlayer(args[1]) != null)
            this.ridePetCommand(this.getOnlinePlayer(args[1]));
        else if (args.length >= 2 && args[0].equalsIgnoreCase("item-name"))
            this.setSkullName(sender, args);
        else if (args.length >= 3 && args[0].equalsIgnoreCase("item-lore") && sender instanceof Player && tryParseInt(args[1]))
            this.setLore(sender, args);
        else if (args.length == 1 && args[0].equalsIgnoreCase("toggle-sound") && sender instanceof Player)
            this.toggleSounds((Player) sender);
        else if (args.length == 2 && args[0].equalsIgnoreCase("toggle-sound") && this.getOnlinePlayer(args[1]) != null)
            this.toggleSounds(this.getOnlinePlayer(args[2]));
        else if (args.length == 1 && args[0].equalsIgnoreCase("killnext") && sender instanceof Player && sender.hasPermission("petblocks.reload"))
            this.killNextCommand((Player) sender);
        else if (args.length == 1 && args[0].equalsIgnoreCase("3")) {
            sender.sendMessage("");
            sender.sendMessage(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + ChatColor.UNDERLINE + "                   PetBlocks " + "                       ");
            sender.sendMessage("");
            this.sendMessage(sender, "hat [player]", new String[]{ChatColor.BLUE + "Description:" + ChatColor.RESET
                    , "Starts wearing the PetBlock."
                    ,ChatColor.YELLOW + "Examples:" + ChatColor.RESET
                    , this.getCommandName() + "hat"
                    , this.getCommandName() + "hat " + sender.getName()
                    , ChatColor.GOLD + "<<Click me>>"});
            this.sendMessage(sender, "ride [player]", new String[]{ChatColor.BLUE + "Description:" + ChatColor.RESET
                    , "Starts riding the PetBlock."
                    ,ChatColor.YELLOW + "Examples:" + ChatColor.RESET
                    , this.getCommandName() + "ride"
                    , this.getCommandName() + "ride " + sender.getName()
                    , ChatColor.GOLD + "<<Click me>>"});
            this.sendMessage(sender, "item-name <text> [player]", new String[]{ChatColor.BLUE + "Description:" + ChatColor.RESET
                    , "Changes the name of the PetBlock item when it is inside of the inventory of the player."
                    ,ChatColor.YELLOW + "Examples:" + ChatColor.RESET
                    , this.getCommandName() + "item-name Petblock"
                    , this.getCommandName() + "item-name Amazing Beast"
                    , this.getCommandName() + "item-name My block " + sender.getName()
                    , ChatColor.GOLD + "<<Click me>>"});
            this.sendMessage(sender, "item-lore <line> <text> [player]", new String[]{ChatColor.BLUE + "Description:" + ChatColor.RESET
                    , "Changes the lore of the PetBlock item when it is inside of the inventory of the player."
                    ,ChatColor.YELLOW + "Examples:" + ChatColor.RESET
                    , this.getCommandName() + "item-lore 1 Beast"
                    , this.getCommandName() + "item-lore 2 This is my pet"
                    , this.getCommandName() + "item-lore 2 PetBlock " + sender.getName()
                    , ChatColor.GOLD + "<<Click me>>"});
            this.sendMessage(sender, "killnext", new String[]{ChatColor.BLUE + "Description:" + ChatColor.RESET
                    , "Kills the nearest entity to the player. Does not kill other players."
                    ,ChatColor.YELLOW + "Examples:" + ChatColor.RESET
                    , this.getCommandName() + "killnext"
                    , ChatColor.GOLD + "<<Click me>>"});
            sender.sendMessage("");
            sender.sendMessage(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "" + ChatColor.UNDERLINE + "                           ┌3/3┐                            ");
            sender.sendMessage("");
        } else if (args.length == 1 && args[0].equalsIgnoreCase("2")) {
            sender.sendMessage("");
            sender.sendMessage(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + ChatColor.UNDERLINE + "                   PetBlocks " + "                       ");
            sender.sendMessage("");
            this.sendMessage(sender, "engine <number> [player]", new String[]{ChatColor.BLUE + "Description:" + ChatColor.RESET
                    , "Changes the engine being used of the PetBlock."
                    ,ChatColor.YELLOW + "Examples:" + ChatColor.RESET
                    , this.getCommandName() + "engine 1"
                    , this.getCommandName() + "engine 2 " + sender.getName()
                    , ChatColor.GOLD + "<<Click me>>"});
            this.sendMessage(sender, "costume <category> <number> [player]", new String[]{ChatColor.BLUE + "Description:" + ChatColor.RESET
                    , "Changes the costume of the PetBlock."
                    ,ChatColor.YELLOW + "Examples:" + ChatColor.RESET
                    , this.getCommandName() + "costume simple-blocks 1"
                    , this.getCommandName() + "costume simple-blocks 1 " + sender.getName()
                    , this.getCommandName() + "costume colored-blocks 2"
                    , this.getCommandName() + "costume player-heads 3"
                    , this.getCommandName() + "costume minecraft-heads 1"
                    , ChatColor.GOLD + "<<Click me>>"});
            this.sendMessage(sender, "rename <name> [player]", new String[]{ChatColor.BLUE + "Description:" + ChatColor.RESET
                    , "Renames the PetBlock."
                    ,ChatColor.YELLOW + "Examples:" + ChatColor.RESET
                    , this.getCommandName() + "rename Beast"
                    , this.getCommandName() + "rename My awesome Pet"
                    , this.getCommandName() + "rename My Pet " + sender.getName()
                    , ChatColor.GOLD + "<<Click me>>"});
            this.sendMessage(sender, "skin <account/url> [player]", new String[]{ChatColor.BLUE + "Description:" + ChatColor.RESET
                    , "Replaces the costume of the PetBlock with the given skin."
                    ,ChatColor.YELLOW + "Examples:" + ChatColor.RESET
                    , this.getCommandName() + "skin Shynixn"
                    , this.getCommandName() + "skin Shynixn " + sender.getName()
                    , this.getCommandName() + "skin http://textures.minecraft.net/texture/707dab2cbebea539b64d5ad246f9ccc1fcda7aa94b88e59fc2829852f46071"
                    , ChatColor.GOLD + "<<Click me>>"});
            this.sendMessage(sender, "particle <number> [player]", new String[]{ChatColor.BLUE + "Description:" + ChatColor.RESET
                    , "Changes the particle of the PetBlock."
                    ,ChatColor.YELLOW + "Examples:" + ChatColor.RESET
                    , this.getCommandName() + "particle 2"
                    , this.getCommandName() + "particle 3 " + sender.getName()
                    , ChatColor.GOLD + "<<Click me>>"});
            sender.sendMessage("");
            sender.sendMessage(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "" + ChatColor.UNDERLINE + "                           ┌2/3┐                            ");
            sender.sendMessage("");
        } else if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("1"))) {
            sender.sendMessage("");
            sender.sendMessage(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + ChatColor.UNDERLINE + "                   PetBlocks " + "                       ");
            if (sender instanceof Player) {
                sender.sendMessage("");
                sender.sendMessage(ChatColor.DARK_GREEN + "" +ChatColor.ITALIC + "Move your mouse over the commands to display tooltips!");
            }
            sender.sendMessage("");
            this.sendMessage(sender, "enable [player]", new String[]{ChatColor.BLUE + "Description:" + ChatColor.RESET
                    , "Respawns the PetBlock of the given player."
                    ,ChatColor.YELLOW + "Examples:" + ChatColor.RESET
                    , this.getCommandName() + "enable"
                    , this.getCommandName() + "enable " + sender.getName()
                    , ChatColor.GOLD + "<<Click me>>"});
            this.sendMessage(sender, "disable [player]", new String[]{ChatColor.BLUE + "Description:" + ChatColor.RESET
                    , "Removes the PetBlock of the given player.",
                    ChatColor.YELLOW + "Examples:" + ChatColor.RESET
                    , this.getCommandName() + "disable"
                    , this.getCommandName() + "disable " + sender.getName()
                    , ChatColor.GOLD + "<<Click me>>"});
            this.sendMessage(sender, "toggle [player]", new String[]{ChatColor.BLUE + "Description:" + ChatColor.RESET
                    , "Enables or disables the PetBlock.",
                    ChatColor.YELLOW + "Examples:" + ChatColor.RESET
                    , this.getCommandName() + "toggle"
                    , this.getCommandName() + "toggle " + sender.getName()
                    , ChatColor.GOLD + "<<Click me>>"});
            this.sendMessage(sender, "toggle-sound [player]", new String[]{ChatColor.BLUE + "Description:" + ChatColor.RESET
                    , "Enables or disables the sounds of the PetBlock.",
                    ChatColor.YELLOW + "Examples:" + ChatColor.RESET
                    , this.getCommandName() + "toggle-sound"
                    , this.getCommandName() + "toggle-sound " + sender.getName()
                    , ChatColor.GOLD + "<<Click me>>"});
            sender.sendMessage("");
            sender.sendMessage(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "" + ChatColor.UNDERLINE + "                           ┌1/3┐                            ");
            sender.sendMessage("");
        }
    }

    private String getCommandName() {
        return '/' + this.getName() + ' ';
    }

    private void sendMessage(CommandSender commandSender, String message, String[] hoverText) {
        if (commandSender instanceof Player) {
            final StringBuilder builder = new StringBuilder();
            for (final String s : hoverText) {
                if (builder.length() != 0) {
                    builder.append('\n');
                }
                builder.append(s);
            }
            String fullCommand = (this.getCommandName() + message);
            if (fullCommand.contains("<")) {
                fullCommand = fullCommand.substring(0, fullCommand.indexOf("<"));
            } else if (fullCommand.contains("[")) {
                fullCommand = fullCommand.substring(0, fullCommand.indexOf("["));
            } else if (fullCommand.contains("-")) {
                fullCommand = fullCommand.substring(0, fullCommand.indexOf("-"));
            }
            new ChatBuilder()
                    .component(Config.getInstance().getPrefix() + this.getCommandName() + message)
                    .setClickAction(ChatBuilder.ClickAction.SUGGEST_COMMAND, fullCommand)
                    .setHoverText(builder.toString())
                    .builder().sendMessage((Player) commandSender);
        } else {
            commandSender.sendMessage(Config.getInstance().getPrefix() + '/' + this.getName() + ' ' + message);
        }
    }

    private void setCostumeCommand(Player player, String category, int number) {
        this.providePet(player, (petMeta, petBlock) -> {
            final GUIItemContainer item;
            if (category.equalsIgnoreCase("simple-blocks")) {
                item = Config.getInstance().getOrdinaryCostumesController().getContainerByPosition(number);
            } else if (category.equalsIgnoreCase("colored-blocks")) {
                item = Config.getInstance().getColorCostumesController().getContainerByPosition(number);
            } else if (category.equalsIgnoreCase("player-heads")) {
                item = Config.getInstance().getRareCostumesController().getContainerByPosition(number);
            } else if (category.equalsIgnoreCase("minecraft-heads")) {
                item = Config.getInstance().getMinecraftHeadsCostumesController().getContainerByPosition(number);
            } else {
                return;
            }
            PetBlockModifyHelper.setCostume(petMeta, petBlock, item);
            this.persistAsynchronously(petMeta);
        });
    }

    private void setEngineCommand(Player player, int number) {
        this.providePet(player, (petMeta, petBlock) -> {
            final EngineContainer container = Config.getInstance().getEngineController().getById(number);
            if (container == null) {
                player.sendMessage(Config.getInstance().getPrefix() + "Engine not found.");
            } else {
                PetBlockModifyHelper.setEngine(petMeta, petBlock, container);
                this.persistAsynchronously(petMeta);
            }
        });
    }

    private void setParticleCommand(Player player, int number) {
        this.providePet(player, (petMeta, petBlock) -> {
            final GUIItemContainer container = Config.getInstance().getParticleController().getContainerByPosition(number);
            if (container == null) {
                player.sendMessage(Config.getInstance().getPrefix() + "Particle not found.");
            } else {
                PetBlockModifyHelper.setParticleEffect(petMeta, petBlock, container);
                this.persistAsynchronously(petMeta);
            }
        });
    }

    private void toggleSounds(Player player) {
        this.providePet(player, (petMeta, petBlock) -> {
            petMeta.setSoundEnabled(!petMeta.isSoundEnabled());
            this.persistAsynchronously(petMeta);
        });
    }

    private void setLore(CommandSender commandSender, String[] args) {
        Player player = null;
        if (commandSender instanceof Player) {
            player = (Player) commandSender;
        }
        final Object[] mergedArgs = this.mergeArgs(args, 2);
        if (mergedArgs[1] != null) {
            player = (Player) mergedArgs[1];
        }
        final String text = (String) mergedArgs[0];
        final int line = Integer.parseInt(args[1]) - 1;
        this.providePet(player, (meta, petBlock) -> {
            if (petBlock != null && line >= 0) {
                final ArmorStand armorStand = (ArmorStand) petBlock.getArmorStand();
                final ItemStack itemStack = armorStand.getHelmet();
                final ItemMeta itemMeta = itemStack.getItemMeta();
                final List<String> lore = new ArrayList<>();
                if (itemMeta.getLore() != null) {
                    lore.addAll(itemMeta.getLore());
                }
                while (line >= lore.size()) {
                    lore.add("");
                }
                lore.set(line, ChatColor.translateAlternateColorCodes('&', text));
                itemMeta.setLore(lore);
                itemStack.setItemMeta(itemMeta);
                armorStand.setHelmet(itemStack);
            }
        });
    }

    private void setSkullName(CommandSender commandSender, String[] args) {
        Player player = null;
        if (commandSender instanceof Player) {
            player = (Player) commandSender;
        }
        final Object[] mergedArgs = this.mergeArgs(args, 1);
        if (mergedArgs[1] != null) {
            player = (Player) mergedArgs[1];
        }
        final String text = (String) mergedArgs[0];
        if (player != null) {
            this.providePet(player, (meta, petBlock) -> {
                if (petBlock != null) {
                    final ArmorStand armorStand = (ArmorStand) petBlock.getArmorStand();
                    final ItemStack itemStack = armorStand.getHelmet();
                    final ItemMeta itemMeta = itemStack.getItemMeta();
                    itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', text));
                    itemStack.setItemMeta(itemMeta);
                    armorStand.setHelmet(itemStack);
                }
            });
        }
    }

    private void killNextCommand(Player sender) {
        double distance = 100;
        Entity nearest = null;
        for (final Entity entity : sender.getLocation().getChunk().getEntities()) {
            if (!(entity instanceof Player) && sender.getLocation().distance(entity.getLocation()) < distance) {
                distance = sender.getLocation().distance(entity.getLocation());
                nearest = entity;
            }
        }
        if (nearest != null) {
            nearest.remove();
            sender.sendMessage(Config.getInstance().getPrefix() + "" + ChatColor.GREEN + "You removed entity " + nearest.getType() + '.');
        }
    }

    private void ridePetCommand(Player player) {
        final PetBlock petBlock;
        if ((petBlock = this.manager.getPetBlockController().getByPlayer(player)) != null) {
            petBlock.ride(player);
        }
    }

    private void hatPetCommand(Player player) {
        final PetBlock petBlock;
        if ((petBlock = this.manager.getPetBlockController().getByPlayer(player)) != null) {
            petBlock.wear(player);
        }
    }

    private void changePetSkinCommand(Player player, String skin) {
        try {
            this.providePet(player, (meta, petBlock) -> {
                PetBlockModifyHelper.setSkin(meta, petBlock, skin);
                this.persistAsynchronously(meta);
            });
        } catch (final Exception e) {
            player.sendMessage(Config.getInstance().getPrefix() + Config.getInstance().getSkullNamingErrorMessage());
        }
    }

    private void namePetCommand(CommandSender commandSender, String[] args) {
        try {
            Player player = null;
            if (commandSender instanceof Player) {
                player = (Player) commandSender;
            }
            final Object[] mergedArgs = this.mergeArgs(args, 1);
            if (mergedArgs[1] != null) {
                player = (Player) mergedArgs[1];
            }
            final String message = (String) mergedArgs[0];
            if (player != null) {
                if (message.length() > ConfigPet.getInstance().getDesign_maxPetNameLength()) {
                    commandSender.sendMessage(Config.getInstance().getPrefix() + Config.getInstance().getNamingErrorMessage());

                } else {
                    this.providePet(player, (meta, petBlock) -> {
                        PetBlockModifyHelper.rename(meta, petBlock, message);
                        this.persistAsynchronously(meta);
                    });
                }
            }
        } catch (final Exception e) {
            commandSender.sendMessage(Config.getInstance().getPrefix() + Config.getInstance().getNamingErrorMessage());
        }
    }

    private Object[] mergeArgs(String[] args, int up) {
        final StringBuilder builder = new StringBuilder();
        Player player = null;
        for (int i = up; i < args.length; i++) {
            if (i + 1 == args.length && Bukkit.getPlayer(args[i]) != null) {
                player = Bukkit.getPlayer(args[i]);
                return new Object[]{builder.toString(), player};
            }
            if (builder.length() != 0) {
                builder.append(' ');
            }
            builder.append(args[i]);
        }
        return new Object[]{builder.toString(), player};
    }

    private void togglePetCommand(Player player) {
        this.providePet(player, (meta, petBlock) -> {
            if (petBlock == null) {
                this.setPetCommand(player);
            } else {
                this.removePetCommand(player);
            }
        });
    }

    private void setPetCommand(Player player) {
        this.removePetCommand(player);
        this.providePet(player, (meta, petBlock) -> {
            final PetBlock petBlock1 = this.manager.getPetBlockController().create(player, meta);
            this.manager.getPetBlockController().store(petBlock1);
        });
    }

    private void removePetCommand(Player player) {
        final PetBlock petBlock;
        if ((petBlock = this.manager.getPetBlockController().getByPlayer(player)) != null) {
            this.manager.getPetBlockController().remove(petBlock);
        }
    }

    private Player getOnlinePlayer(String name) {
        for (final World world : Bukkit.getWorlds()) {
            for (final Player player : world.getPlayers()) {
                if (player.getName().equals(name))
                    return player;
            }
        }
        return null;
    }

    private void persistAsynchronously(com.github.shynixn.petblocks.api.persistence.entity.PetMeta petMeta) {
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> this.manager.getPetMetaController().store(petMeta));
    }

    private void providePet(Player player, PetRunnable runnable) {
        final PetBlock petBlock;
        if ((petBlock = this.manager.getPetBlockController().getByPlayer(player)) != null) {
            runnable.run(petBlock.getMeta(), petBlock);
        } else {
            this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
                if (!this.manager.getPetMetaController().hasEntry(player))
                    return;
                final PetMeta petMeta = this.manager.getPetMetaController().getByPlayer(player);
                this.plugin.getServer().getScheduler().runTask(this.plugin, () -> runnable.run(petMeta, null));
            });
        }
    }

    private static boolean tryParseInt(String value) {
        try {
            Integer.parseInt(value);
        } catch (final NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}
