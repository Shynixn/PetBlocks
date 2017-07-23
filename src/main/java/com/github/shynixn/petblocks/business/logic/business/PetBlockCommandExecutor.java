package com.github.shynixn.petblocks.business.logic.business;

import com.github.shynixn.petblocks.api.PetBlocksApi;
import com.github.shynixn.petblocks.api.entities.PetBlock;
import com.github.shynixn.petblocks.api.entities.PetMeta;
import com.github.shynixn.petblocks.api.entities.PetType;
import com.github.shynixn.petblocks.business.Language;
import com.github.shynixn.petblocks.business.bukkit.PetBlocksPlugin;
import com.github.shynixn.petblocks.business.bukkit.nms.NMSRegistry;
import com.github.shynixn.petblocks.business.logic.configuration.ConfigCommands;
import com.github.shynixn.petblocks.business.logic.configuration.ConfigGUI;
import com.github.shynixn.petblocks.business.logic.configuration.ConfigParticle;
import com.github.shynixn.petblocks.lib.BukkitChatColor;
import com.github.shynixn.petblocks.lib.DynamicCommandHelper;
import com.github.shynixn.petblocks.lib.BukkitUtilities;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

class PetBlockCommandExecutor extends DynamicCommandHelper {
    private final PetBlockManager manager;
    private final Plugin plugin;

    PetBlockCommandExecutor(PetBlockManager manager) {
        super(ConfigCommands.getInstance().getPetblocksConfigurationCommandContainer());
        this.manager = manager;
        this.plugin = JavaPlugin.getPlugin(PetBlocksPlugin.class);
    }

    @Override
    public void onCommandSend(CommandSender sender, String[] args) {
        if (args.length == 4 && args[0].equalsIgnoreCase("set") && BukkitUtilities.tryParseInt(args[3]) && sender instanceof Player)
            this.setPetPlayerCommand((Player) sender, args[1], args[2], Integer.parseInt(args[3]));
        else if (args.length == 5 && args[0].equalsIgnoreCase("set") && this.getOnlinePlayer(args[4]) != null)
            this.setPetPlayerCommand(this.getOnlinePlayer(args[4]), args[1], args[2], Integer.parseInt(args[3]));
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
            sender.sendMessage(Language.PREFIX + "/petblocks set <soul> <default/color/custom> <number> [player]");
            sender.sendMessage(Language.PREFIX + "/petblocks name <name> [player]");
            sender.sendMessage(Language.PREFIX + "/petblocks skin <account/url> [player]");
            sender.sendMessage(Language.PREFIX + "/petblocks particle <number> [player]");
            sender.sendMessage(Language.PREFIX + "/petblocks disable [player]");
            sender.sendMessage(Language.PREFIX + "/petblocks toggle [player]");
            sender.sendMessage(Language.PREFIX + "/petblocks hat [player]");
            sender.sendMessage(Language.PREFIX + "/petblocks ride [player]");
            sender.sendMessage(Language.PREFIX + "/petblocks togglesounds [player]");
            sender.sendMessage(Language.PREFIX + "/petblocks skullname <name> [player]");
            sender.sendMessage(Language.PREFIX + "/petblocks skulllore <line> <lore> [player]");
            sender.sendMessage(Language.PREFIX + "/petblocks killnext - Kills nearest entity");
            sender.sendMessage("");
            sender.sendMessage(BukkitChatColor.DARK_GREEN + "" + ChatColor.UNDERLINE + "                           ┌1/1┐                            ");
            sender.sendMessage("");
        }
    }

    private void toggleSounds(Player player) {
        this.providePet(player, (petMeta, petBlock) -> {
            petMeta.setSoundsEnabled(!petMeta.isSoundsEnabled());
            this.persistAsynchronously(petMeta);
        });
    }

    private void setLore(Player player, String nameBump, int line) {
        this.providePet(player, (meta, petBlock) -> {
            final String name = nameBump.replace('_', ' ');
            try {
                if (meta.getHeadLore() == null) {
                    meta.setHeadLore(new String[line + 1]);
                }
                if (meta.getHeadLore().length <= line) {
                    final String[] data = new String[line + 1];
                    for (int i = 0; i < meta.getHeadLore().length; i++) {
                        data[i] = meta.getHeadLore()[i];
                    }
                    meta.setHeadLore(data);
                }
                final String[] data = meta.getHeadLore();
                data[line] = ChatColor.translateAlternateColorCodes('&', name);
                meta.setHeadLore(data);
            } catch (final Exception ex) {
                meta.setHeadLore(null);
            }
            this.persistAsynchronously(meta);
            if (petBlock != null)
                petBlock.refreshHeadMeta();
        });
    }

    private void setSkullName(Player player, String name) {
        final String bname = name.replace('_', ' ');
        this.providePet(player, (meta, petBlock) -> {
            meta.setHeadDisplayName(ChatColor.translateAlternateColorCodes('&', bname));
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
            sender.sendMessage(Language.PREFIX + "" + ChatColor.GREEN + "You removed entity " + nearest.getType() + '.');
        }
    }

    private void ridePetCommand(Player player) {
        final PetBlock petBlock;
        if ((petBlock = PetBlocksApi.getPetBlock(player)) != null)
            petBlock.ride(player);
    }

    private void hatPetCommand(Player player) {
        final PetBlock petBlock;
        if ((petBlock = PetBlocksApi.getPetBlock(player)) != null)
            petBlock.wear(player);
    }

    private void setParticleCommand(Player player, int number) {
        if (ConfigParticle.getInstance().getParticleItemStacks().length > number && number >= 0 && ConfigParticle.getInstance().getParticle(number) != null) {
            this.providePet(player, (meta, petBlock) -> {
                meta.setParticleEffect(ConfigParticle.getInstance().getParticle(number));
                this.persistAsynchronously(meta);
                if (petBlock != null)
                    petBlock.respawn();
            });
        }
    }

    private void changePetSkinCommand(Player player, String skinBump) {
        try {
            this.providePet(player, (meta, petBlock) -> {
                String skin = skinBump;
                if (skin.contains("textures.minecraft")) {
                    if (!skin.contains("http://"))
                        skin = "http://" + skin;
                }
                meta.setSkin(Material.SKULL_ITEM, (short) 3, skin);
                this.persistAsynchronously(meta);
                if (petBlock != null)
                    petBlock.respawn();
            });
        } catch (final Exception e) {
            player.sendMessage(Language.PREFIX + Language.NAME_ERROR_MESSAGE);
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
            player.sendMessage(Language.PREFIX + Language.NAME_ERROR_MESSAGE);
        }
    }

    private void togglePetCommand(Player player) {
        if (this.manager.hasPetBlock(player))
            this.manager.removePetBlock(player);
        this.providePet(player, (meta, petBlock) -> {
            PetBlocksApi.setPetBlock(player, meta);
        });
    }

    private void removePetCommand(Player player) {
        if (this.manager.hasPetBlock(player))
            this.manager.removePetBlock(player);
    }

    private void setPetPlayerCommand(Player player, String soul, String costume, int number) {
        if (PetType.getPetTypeFromName(soul) == null) {
            player.sendMessage(Language.PREFIX + ChatColor.RED + "This soul does not exist.");
        } else if (!costume.equalsIgnoreCase("color") && !costume.equalsIgnoreCase("default") && !costume.equalsIgnoreCase("custom")) {
            player.sendMessage(Language.PREFIX + ChatColor.RED + "Select a valid costume type.");
        } else if (costume.equalsIgnoreCase("color") && ConfigGUI.getInstance().getColoredItemStacks().length > number && number >= 0 && ConfigGUI.getInstance().getColoredItemStacks()[number] != null) {
            final ItemStack itemStack = ConfigGUI.getInstance().getColoredItemStacks()[number];
            this.setPet(player, PetType.getPetTypeFromName(soul), itemStack);
        } else if (costume.equalsIgnoreCase("default") && ConfigGUI.getInstance().getDefaultItemStacks().length > number && number >= 0 && ConfigGUI.getInstance().getDefaultItemStacks()[number] != null) {
            final ItemStack itemStack = ConfigGUI.getInstance().getDefaultItemStacks()[number];
            this.setPet(player, PetType.getPetTypeFromName(soul), itemStack);
        } else if (costume.equalsIgnoreCase("custom") && ConfigGUI.getInstance().getCustomItemStacks().length > number && number >= 0 && ConfigGUI.getInstance().getCustomItemStacks()[number] != null) {
            final ItemStack itemStack = ConfigGUI.getInstance().getCustomItemStacks()[number];
            this.setPet(player, PetType.getPetTypeFromName(soul), itemStack);
        } else {
            player.sendMessage(Language.PREFIX + ChatColor.RED + "You can't set a pet with that costume number.");
        }
    }

    private void setPet(Player player, PetType petType, ItemStack itemStack) {
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
            if (PetBlocksApi.hasPetMeta(player)) {
                PetBlocksApi.removePetMeta(player);
            }
            this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                final PetMeta meta = this.manager.dataManager.createPetMeta(player, petType);
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
                this.persistAsynchronously((com.github.shynixn.petblocks.api.persistence.entity.PetMeta) meta);
                if (PetBlocksApi.getPetBlock(player) != null)
                    PetBlocksApi.getPetBlock(player).respawn();
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
        if (PetBlocksApi.hasPetBlock(player)) {
            final PetBlock petBlock = PetBlocksApi.getPetBlock(player);
            runnable.run((com.github.shynixn.petblocks.api.persistence.entity.PetMeta) petBlock.getPetMeta(), petBlock);
        } else {
            this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
                if (!PetBlocksApi.hasPetMeta(player))
                    return;
                final com.github.shynixn.petblocks.api.persistence.entity.PetMeta petMeta = PetBlocksApi.getPetMeta(player);
                this.plugin.getServer().getScheduler().runTask(this.plugin, () -> runnable.run(petMeta, null));
            });
        }
    }
}
