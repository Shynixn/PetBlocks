package com.github.shynixn.petblocks.business.logic.business;

import com.github.shynixn.petblocks.api.business.entity.GUIItemContainer;
import com.github.shynixn.petblocks.api.business.entity.PetBlock;
import com.github.shynixn.petblocks.api.business.enumeration.GUIItem;
import com.github.shynixn.petblocks.api.persistence.entity.EngineContainer;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.business.bukkit.PetBlocksPlugin;
import com.github.shynixn.petblocks.business.bukkit.nms.NMSRegistry;
import com.github.shynixn.petblocks.business.logic.configuration.Config;
import com.github.shynixn.petblocks.business.logic.persistence.entity.PetData;
import com.github.shynixn.petblocks.lib.BukkitChatColor;
import com.github.shynixn.petblocks.lib.BukkitUtilities;
import com.github.shynixn.petblocks.lib.SimpleCommandExecutor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

class PetBlockCommandExecutor extends SimpleCommandExecutor.UnRegistered {
    private final PetBlockManager manager;

    PetBlockCommandExecutor(PetBlockManager manager) throws Exception {
        super(((MemorySection) JavaPlugin.getPlugin(PetBlocksPlugin.class).getConfig().get("petblocks-configuration")).getValues(false)
                , JavaPlugin.getPlugin(PetBlocksPlugin.class));
        this.manager = manager;
    }

    /**
     * Can be overwritten to listener to all executed commands
     *
     * @param sender sender
     * @param args   args
     */
    @Override
    public void onCommandSenderExecuteCommand(CommandSender sender, String[] args) {
        if (args.length == 2 && args[0].equalsIgnoreCase("engine") && sender instanceof Player && BukkitUtilities.tryParseInt(args[1]))
            this.setEngineCommand((Player) sender, Integer.parseInt(args[1]));
        else if (args.length == 3 && args[0].equalsIgnoreCase("engine") && this.getOnlinePlayer(args[2]) != null && BukkitUtilities.tryParseInt(args[1]))
            this.setEngineCommand(this.getOnlinePlayer(args[2]), Integer.parseInt(args[1]));
        else if (args.length == 3 && args[0].equalsIgnoreCase("costume") && sender instanceof Player && BukkitUtilities.tryParseInt(args[2]))
            this.setCostumeCommand((Player) sender, args[1], Integer.parseInt(args[1]));
        else if (args.length == 4 && args[0].equalsIgnoreCase("costume") && this.getOnlinePlayer(args[3]) != null && BukkitUtilities.tryParseInt(args[2]))
            this.setCostumeCommand(this.getOnlinePlayer(args[3]), args[1], Integer.parseInt(args[1]));
        else if (args.length == 1 && args[0].equalsIgnoreCase("disable") && sender instanceof Player)
            this.removePetCommand((Player) sender);
        else if (args.length == 2 && args[0].equalsIgnoreCase("disable") && this.getOnlinePlayer(args[1]) != null)
            this.removePetCommand(this.getOnlinePlayer(args[1]));
        else if (args.length == 1 && args[0].equalsIgnoreCase("toggle") && sender instanceof Player)
            this.togglePetCommand((Player) sender);
        else if (args.length == 2 && args[0].equalsIgnoreCase("toggle") && this.getOnlinePlayer(args[1]) != null)
            this.togglePetCommand(this.getOnlinePlayer(args[1]));
        else if (args.length == 2 && args[0].equalsIgnoreCase("name") && sender instanceof Player)
            this.namePetCommand((Player) sender, args[1]);
        else if (args.length == 3 && args[0].equalsIgnoreCase("name") && this.getOnlinePlayer(args[2]) != null)
            this.namePetCommand(this.getOnlinePlayer(args[2]), args[1]);
        else if (args.length == 2 && args[0].equalsIgnoreCase("skin") && sender instanceof Player)
            this.changePetSkinCommand((Player) sender, args[1]);
        else if (args.length == 3 && args[0].equalsIgnoreCase("skin") && this.getOnlinePlayer(args[2]) != null)
            this.changePetSkinCommand(this.getOnlinePlayer(args[2]), args[1]);
        else if (args.length == 2 && args[0].equalsIgnoreCase("particle") && sender instanceof Player && BukkitUtilities.tryParseInt(args[1]))
            this.setParticleCommand((Player) sender, Integer.parseInt(args[1]));
        else if (args.length == 3 && args[0].equalsIgnoreCase("particle") && this.getOnlinePlayer(args[2]) != null && BukkitUtilities.tryParseInt(args[1]))
            this.setParticleCommand(this.getOnlinePlayer(args[2]), Integer.parseInt(args[1]));
        else if (args.length == 1 && args[0].equalsIgnoreCase("hat") && sender instanceof Player)
            this.hatPetCommand((Player) sender);
        else if (args.length == 2 && args[0].equalsIgnoreCase("hat") && this.getOnlinePlayer(args[1]) != null)
            this.hatPetCommand(this.getOnlinePlayer(args[1]));
        else if (args.length == 1 && args[0].equalsIgnoreCase("ride") && sender instanceof Player)
            this.ridePetCommand((Player) sender);
        else if (args.length == 2 && args[0].equalsIgnoreCase("ride") && this.getOnlinePlayer(args[1]) != null)
            this.ridePetCommand(this.getOnlinePlayer(args[1]));

        else if (args.length == 2 && args[0].equalsIgnoreCase("skullname") && sender instanceof Player)
            this.setSkullName((Player) sender, args[1]);
        else if (args.length == 3 && args[0].equalsIgnoreCase("skullname") && this.getOnlinePlayer(args[2]) != null)
            this.setSkullName(this.getOnlinePlayer(args[2]), args[1]);

        else if (args.length == 1 && args[0].equalsIgnoreCase("togglesounds") && sender instanceof Player)
            this.toggleSounds((Player) sender);
        else if (args.length == 2 && args[0].equalsIgnoreCase("togglesounds") && this.getOnlinePlayer(args[1]) != null)
            this.toggleSounds(this.getOnlinePlayer(args[2]));

        else if (args.length == 3 && args[0].equalsIgnoreCase("skulllore") && sender instanceof Player && BukkitUtilities.tryParseInt(args[1]))
            this.setLore((Player) sender, args[2], Integer.parseInt(args[1]));
        else if (args.length == 4 && args[0].equalsIgnoreCase("skulllore") && this.getOnlinePlayer(args[3]) != null && BukkitUtilities.tryParseInt(args[1]))
            this.setLore(this.getOnlinePlayer(args[3]), args[2], Integer.parseInt(args[1]));

        else if (args.length == 1 && args[0].equalsIgnoreCase("killnext") && sender instanceof Player && sender.hasPermission("petblocks.reload"))
            this.killNextCommand((Player) sender);
        else {
            sender.sendMessage("");
            sender.sendMessage(BukkitChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "                   PetBlocks " + "                       ");
            sender.sendMessage("");
            sender.sendMessage(Config.getInstance().getPrefix() + "/petblocks engine <number> [player]");
            sender.sendMessage(Config.getInstance().getPrefix() + "/petblocks costume <category> <number> [player]");
            sender.sendMessage(Config.getInstance().getPrefix() + "/petblocks name <name> [player]");
            sender.sendMessage(Config.getInstance().getPrefix() + "/petblocks skin <account/url> [player]");
            sender.sendMessage(Config.getInstance().getPrefix() + "/petblocks particle <number> [player]");
            sender.sendMessage(Config.getInstance().getPrefix() + "/petblocks disable [player]");
            sender.sendMessage(Config.getInstance().getPrefix() + "/petblocks toggle [player]");
            sender.sendMessage(Config.getInstance().getPrefix() + "/petblocks hat [player]");
            sender.sendMessage(Config.getInstance().getPrefix() + "/petblocks ride [player]");
            sender.sendMessage(Config.getInstance().getPrefix() + "/petblocks togglesounds [player]");
            sender.sendMessage(Config.getInstance().getPrefix() + "/petblocks skullname <name> [player]");
            sender.sendMessage(Config.getInstance().getPrefix() + "/petblocks skulllore <line> <lore> [player]");
            sender.sendMessage(Config.getInstance().getPrefix() + "/petblocks killnext - Kills nearest entity");
            sender.sendMessage("");
            sender.sendMessage(BukkitChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "                           ┌1/1┐                            ");
            sender.sendMessage("");
        }
    }

    private void setCostumeCommand(Player player, String category, int number) {
        this.providePet(player, (petMeta, petBlock) -> {
            GUIItemContainer item;
            if (category.equalsIgnoreCase("rare")) {
                item = Config.getInstance().getRareCostumesController().getContainerByPosition(number);
            } else if (category.equalsIgnoreCase("ordinary")) {
                item = Config.getInstance().getOrdinaryCostumesController().getContainerByPosition(number);
            } else if (category.equalsIgnoreCase("color")) {
                item = Config.getInstance().getColorCostumesController().getContainerByPosition(number);
            } else {
                return;
            }
            ItemStack itemStack = (ItemStack) item.generate(player);
            ((PetData) petMeta).setSkin(itemStack.getType(), itemStack.getDurability(), item.getSkin());
        });
    }

    private void setEngineCommand(Player player, int number) {
        this.providePet(player, (petMeta, petBlock) -> {
            final EngineContainer container = Config.getInstance().getEngineController().getById(number);
            if (container == null) {
                player.sendMessage(Config.getInstance().getPrefix() + "Engine not found.");
            } else {
                petMeta.setEngine(container);
            }
        });
    }

    private void setParticleCommand(Player player, int number) {
        this.providePet(player, (petMeta, petBlock) -> {
            final GUIItemContainer container = Config.getInstance().getParticleController().getContainerByPosition(number);
            if (container == null) {
                player.sendMessage(Config.getInstance().getPrefix() + "Particle not found.");
            } else {
                petMeta.setParticleEffectMeta(Config.getInstance().getParticleController().getByItem(container));
            }
        });
    }

    private void toggleSounds(Player player) {
        this.providePet(player, (petMeta, petBlock) -> {
            petMeta.setSoundEnabled(!petMeta.isSoundEnabled());
            this.persistAsynchronously(petMeta);
        });
    }

    private void setLore(Player player, String nameBump, int line) {
        this.providePet(player, (meta, petBlock) -> {
            PetData petData = (PetData) meta;
            final String name = nameBump.replace('_', ' ');
            try {
                if (petData.getHeadLore() == null) {
                    petData.setHeadLore(new String[line + 1]);
                }
                if (petData.getHeadLore().length <= line) {
                    final String[] data = new String[line + 1];
                    for (int i = 0; i < petData.getHeadLore().length; i++) {
                        data[i] = petData.getHeadLore()[i];
                    }
                    petData.setHeadLore(data);
                }
                final String[] data = petData.getHeadLore();
                data[line] = ChatColor.translateAlternateColorCodes('&', name);
                petData.setHeadLore(data);
            } catch (final Exception ex) {
                petData.setHeadLore(null);
            }
            this.persistAsynchronously(meta);
            if (petBlock != null)
                petBlock.refreshHeadMeta();
        });
    }

    private void setSkullName(Player player, String name) {
        final String bname = name.replace('_', ' ');
        this.providePet(player, (meta, petBlock) -> {
            final PetData petData = (PetData) meta;
            petData.setHeadDisplayName(ChatColor.translateAlternateColorCodes('&', bname));
            this.persistAsynchronously(meta);
            if (petBlock != null)
                petBlock.refreshHeadMeta();
        });
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
        if ((petBlock = this.manager.getByPlayer(player)) != null)
            petBlock.ride(player);
    }

    private void hatPetCommand(Player player) {
        final PetBlock petBlock;
        if ((petBlock = this.manager.getByPlayer(player)) != null)
            petBlock.wear(player);
    }

    private void changePetSkinCommand(Player player, String skinBump) {
        try {
            this.providePet(player, (meta, petBlock) -> {
                String skin = skinBump;
                if (skin.contains("textures.minecraft")) {
                    if (!skin.contains("http://"))
                        skin = "http://" + skin;
                }
                PetData petData = (PetData) meta;
                petData.setSkin(Material.SKULL_ITEM, (short) 3, skin);
                this.persistAsynchronously(meta);
                if (petBlock != null)
                    petBlock.respawn();
            });
        } catch (final Exception e) {
            player.sendMessage(Config.getInstance().getPrefix() + Config.getInstance().getSkullNamingErrorMessage());
        }
    }

    private void namePetCommand(Player player, String name) {
        try {
            this.providePet(player, (meta, petBlock) -> {
                meta.setDisplayName(name);
                this.persistAsynchronously(meta);
                if (petBlock != null)
                    petBlock.respawn();
            });
        } catch (final Exception e) {
            player.sendMessage(Config.getInstance().getPrefix() + Config.getInstance().getNamingErrorMessage());
        }
    }

    private void togglePetCommand(Player player) {
        this.removePetCommand(player);
        this.providePet(player, (meta, petBlock) -> {
            final PetBlock petBlock1 = this.manager.create(player, meta);
            this.manager.store(petBlock1);
        });
    }

    private void removePetCommand(Player player) {
        PetBlock petBlock;
        if ((petBlock = this.manager.getByPlayer(player)) != null) {
            this.manager.remove(petBlock);
        }
    }

    private void setPet(Player player, ItemStack itemStack) {
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
            if (this.manager.dataManager.hasPetMeta(player)) {
                this.manager.dataManager.remove(this.manager.dataManager.getPetMeta(player));
            }
            this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {

                final PetData meta = (PetData) this.manager.dataManager.createPetMeta(player);
                if (itemStack.getType() == Material.SKULL_ITEM) {
                    final SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
                    if (skullMeta.getOwner() == null) {
                        meta.setSkin(itemStack.getType(), itemStack.getDurability(), NMSRegistry.getSkinUrl(itemStack));
                    } else {
                        meta.setSkin(itemStack.getType(), itemStack.getDurability(), ((SkullMeta) itemStack.getItemMeta()).getOwner());
                    }
                } else {
                    meta.setSkin(itemStack.getType(), itemStack.getDurability(), null);
                }
                this.persistAsynchronously(meta);
                PetBlock petBlock;
                if ((petBlock = this.manager.getByPlayer(player)) != null) {
                    petBlock.respawn();
                }
            });
        });
    }

    private Player getOnlinePlayer(String name) {
        for (final Player player : BukkitUtilities.getOnlinePlayers()) {
            if (player.getName().equals(name))
                return player;
        }
        return null;
    }

    private void persistAsynchronously(com.github.shynixn.petblocks.api.persistence.entity.PetMeta petMeta) {
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> this.manager.dataManager.persist(petMeta));
    }

    private void providePet(Player player, PetBlockListener.PetRunnable runnable) {
        PetBlock petBlock;
        if ((petBlock = this.manager.getByPlayer(player)) != null) {
            runnable.run(petBlock.getMeta(), petBlock);
        } else {
            this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
                if (!this.manager.dataManager.hasPetMeta(player))
                    return;
                final PetMeta petMeta = this.manager.dataManager.getPetMeta(player);
                this.plugin.getServer().getScheduler().runTask(this.plugin, () -> runnable.run(petMeta, null));
            });
        }
    }
}
