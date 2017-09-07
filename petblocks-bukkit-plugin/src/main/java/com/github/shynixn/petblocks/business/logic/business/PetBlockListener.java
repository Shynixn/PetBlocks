package com.github.shynixn.petblocks.business.logic.business;

import com.github.shynixn.petblocks.api.PetBlocksApi;
import com.github.shynixn.petblocks.api.bukkit.event.PetBlockMoveEvent;
import com.github.shynixn.petblocks.api.bukkit.event.PetBlockRideEvent;
import com.github.shynixn.petblocks.api.entities.PetBlock;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.api.persistence.entity.SoundMeta;
import com.github.shynixn.petblocks.business.Config;
import com.github.shynixn.petblocks.business.bukkit.PetBlocksPlugin;
import com.github.shynixn.petblocks.business.bukkit.nms.NMSRegistry;
import com.github.shynixn.petblocks.business.logic.configuration.ConfigPet;
import com.github.shynixn.petblocks.business.logic.persistence.entity.SoundBuilder;
import com.github.shynixn.petblocks.lib.BukkitUtilities;
import com.github.shynixn.petblocks.lib.ParticleEffect;
import com.github.shynixn.petblocks.lib.SimpleListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;

class PetBlockListener extends SimpleListener {
    private final PetBlockManager manager;
    private final List<PetBlock> jumped = new ArrayList<>();

    private boolean running;
    private final SoundMeta eatingSound = new SoundBuilder("EAT");

    PetBlockListener(PetBlockManager manager, Plugin plugin) {
        super(plugin);
        this.manager = manager;
        NMSRegistry.registerListener19(manager.carryingPet, plugin);
        this.run();
    }

    private void run() {
        if (!this.running) {
            this.running = true;
            this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new ParticleRunnable(), 0L, 60L);
            this.plugin.getServer().getScheduler().runTaskTimer(this.plugin, new PetHunterRunnable(), 0L, 20);
        }
    }

    /**
     * Gets called when the player starts riding and caches the regions he has spawned in
     *
     * @param event event
     */
    @EventHandler
    public void onPetBlockRideEvent(PetBlockRideEvent event) {
        NMSRegistry.canEnterRegionOnPetRiding(event.getPlayer(), true);
    }

    /**
     * Gets called when the petblock moves and kicks the player off the pet when he enters a region with a different owner
     *
     * @param event event
     */
    @EventHandler
    public void onPetBlockMoveEvent(PetBlockMoveEvent event) {
        if (!NMSRegistry.canEnterRegionOnPetRiding(event.getPlayer(), false)) {
            event.getPetBlock().getArmorStand().eject();
        }
    }

    /**
     * Kicks a player off the pet when the region he is membership of gets modified
     *
     * @param event event
     */
    @EventHandler
    public void onCommandEvent(PlayerCommandPreprocessEvent event) {
        if (!Config.getInstance().allowRidingOnRegionChanging() && event.getMessage().contains("removemember")) {
            try {
                final String[] data = event.getMessage().split(Pattern.quote(" "));
                final String playerName = data[3];
                final String regionName = data[2];
                final Player player;
                if ((player = Bukkit.getPlayer(playerName)) != null) {
                    final PetBlock petBlock;
                    if ((petBlock = PetBlocksApi.getPetBlock(player)) != null && NMSRegistry.shouldKickOffPet(player, regionName)) {
                        petBlock.getArmorStand().eject();
                    }
                }
            } catch (final Exception ex) {
                Bukkit.getLogger().log(Level.WARNING, "Failed to kick member from pet.");
            }
        }
    }

    @EventHandler
    public void onEntityToggleSneakEvent(final PlayerToggleSneakEvent event) {
        final PetBlock petBlock;
        if (event.getPlayer().getPassenger() != null && this.isPet(event.getPlayer().getPassenger()) && (petBlock = this.manager.getPetBlock(event.getPlayer())) != null) {
            petBlock.eject(event.getPlayer());
        }
    }

    @EventHandler
    public void onEntityLeashEvent(PlayerLeashEntityEvent event) {
        if (this.isPet(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (this.isPet(event.getDamager())) {
            final PetBlock petBlock = this.getPet(event.getDamager());
            if (petBlock != null && petBlock.getOwner() != null && petBlock.getOwner().equals(event.getEntity())) {
                event.setCancelled(true);
            }
        }
        if (ConfigPet.getInstance().isFleesInCombat()) {
            if (event.getDamager() instanceof Player && this.manager.hasPetBlock((Player) event.getDamager())) {
                this.manager.timeBlocked.put((Player) event.getDamager(), ConfigPet.getInstance().getReappearsInSeconds());
                this.manager.removePetBlock((Player) event.getDamager());
            } else if (event.getEntity() instanceof Player && this.manager.hasPetBlock((Player) event.getEntity())) {
                this.manager.timeBlocked.put((Player) event.getEntity(), ConfigPet.getInstance().getReappearsInSeconds());
                this.manager.removePetBlock((Player) event.getEntity());
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityInteractEvent event) {
        if (this.isPet(event.getEntity()) && event.getBlock().getType() == Material.SOIL) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerTeleportEvent(final PlayerTeleportEvent event) {
        if (this.manager.hasPetBlock(event.getPlayer())) {
            if (!event.getTo().getWorld().getName().equals(event.getFrom().getWorld().getName())) {
                this.manager.removePetBlock(event.getPlayer());
                if (Config.getInstance().allowPetSpawning(event.getTo())) {
                    this.providePet(event.getPlayer(), (petMeta, petBlock) -> PetBlockListener.this.manager.setPetBlock(event.getPlayer(), petMeta, ConfigPet.getInstance().getWarpDelay()));
                }
            } else if (event.getPlayer().getPassenger() != null && this.isPet(event.getPlayer().getPassenger())) {
                if (!ConfigPet.getInstance().isFollow_fallOffHead()) {
                    final PetBlock petBlock = this.manager.getPetBlock(event.getPlayer());
                    if (petBlock != null)
                        petBlock.teleportWithOwner(event.getTo());
                    event.setCancelled(true);
                } else {
                    final PetBlock petBlock = this.manager.getPetBlock(event.getPlayer());
                    if (petBlock != null)
                        petBlock.eject(event.getPlayer());
                }
            }
        }
    }

    @EventHandler
    public void onPlayerRespawnEvent(final PlayerRespawnEvent event) {
        if (this.manager.hasPetBlock(event.getPlayer())) {
            this.manager.removePetBlock(event.getPlayer());
            this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> this.providePet(event.getPlayer(), (petMeta, petBlock) -> PetBlockListener.this.manager.setPetBlock(event.getPlayer(), petMeta, ConfigPet.getInstance().getWarpDelay())), 60L);
        }
    }

    @EventHandler
    public void entityUnknownRightClickEvent(final PlayerInteractAtEntityEvent event) {
        if (this.isDeadPet(event.getRightClicked())) {
            event.setCancelled(true);
        }
    }

    private boolean isDeadPet(Entity entity) {
        if (entity instanceof ArmorStand && !this.isPet(entity)) {
            final ArmorStand stand = (ArmorStand) entity;
            final int xidentifier = (int) stand.getBodyPose().getZ();
            final int identifier = (int) stand.getRightArmPose().getX();
            final int lidentifier = (int) stand.getLeftArmPose().getX();
            if (xidentifier == 2877 && (identifier == 2877 || lidentifier == 2877)) {
                return true;
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void entityRightClickEvent(final PlayerInteractAtEntityEvent event) {
        if (this.manager.carryingPet.contains(event.getPlayer())) {
            NMSRegistry.setItemInHand19(event.getPlayer(), null, true);
            if (this.manager.hasPetBlock(event.getPlayer()))
                this.manager.petblocks.remove(event.getPlayer());
            event.setCancelled(true);
        } else if (this.isPet(event.getRightClicked())) {
            final PetBlock petBlock = this.getPet(event.getRightClicked());
            if (petBlock != null && petBlock.getOwner().equals(event.getPlayer())) {
                if (NMSRegistry.getItemInHand19(event.getPlayer(), false) != null && NMSRegistry.getItemInHand19(event.getPlayer(), false).getType() == Material.CARROT_ITEM) {
                    ParticleEffect.HEART.display(1F, 1F, 1F, 0.1F, 20, event.getRightClicked().getLocation(), event.getRightClicked().getWorld().getPlayers());
                    try {
                        ((SoundBuilder)this.eatingSound).apply(event.getRightClicked().getLocation());
                    } catch (final Exception e) {
                        Bukkit.getLogger().log(Level.WARNING, "Failed to play sound.", e);
                    }
                    if (NMSRegistry.getItemInHand19(event.getPlayer(), false).getAmount() == 1)
                        event.getPlayer().getInventory().setItem(event.getPlayer().getInventory().getHeldItemSlot(), new ItemStack(Material.AIR));
                    else
                        NMSRegistry.getItemInHand19(event.getPlayer(), false).setAmount(NMSRegistry.getItemInHand19(event.getPlayer(), false).getAmount() - 1);
                    if (!this.jumped.contains(petBlock)) {
                        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> PetBlockListener.this.jumped.remove(PetBlockListener.this.getPet(event.getRightClicked())), 20L);
                        this.jumped.add(this.getPet(event.getRightClicked()));
                        petBlock.jump();
                    }
                }
                if (ConfigPet.getInstance().isFollow_carry() && (event.getPlayer().getInventory() == null || NMSRegistry.getItemInHand19(event.getPlayer(), true).getType() == Material.AIR)) {
                    NMSRegistry.setItemInHand19(event.getPlayer(), petBlock.getArmorStand().getHelmet().clone(), true);
                    this.manager.removePetBlock(event.getPlayer());
                    this.manager.carryingPet.add(event.getPlayer());
                }
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (this.manager.carryingPet.contains(event.getPlayer())) {
            this.removePetFromArm(event.getPlayer());
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerCommandEvent(PlayerCommandPreprocessEvent event) {
        if (this.manager.carryingPet.contains(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryOpenEvent(InventoryOpenEvent event) {
        final Player player = (Player) event.getPlayer();
        if (this.manager.carryingPet.contains(player)) {
            event.setCancelled(true);
            event.getPlayer().closeInventory();
        }
    }

    @EventHandler
    public void onPlayerEntityEvent(PlayerInteractEntityEvent event) {
        if (this.manager.carryingPet.contains(event.getPlayer())) {
            this.removePetFromArm(event.getPlayer());
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        if (this.manager.carryingPet.contains(event.getEntity())) {
            this.removePetFromArm(event.getEntity());
        }
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        if (this.manager.carryingPet.contains(event.getPlayer())) {
            NMSRegistry.setItemInHand19(event.getPlayer(), null, true);
            this.manager.carryingPet.remove(event.getPlayer());
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();
        if (this.manager.carryingPet.contains(player)) {
            this.removePetFromArm((Player) event.getWhoClicked());
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (this.manager.carryingPet.contains(event.getPlayer())) {
            this.removePetFromArm(event.getPlayer());
            event.getItemDrop().remove();
        }
    }

    @EventHandler
    public void onSlotChange(PlayerItemHeldEvent event) {
        if (this.manager.carryingPet.contains(event.getPlayer())) {
            this.removePetFromArm(event.getPlayer());
            event.getPlayer().getInventory().setItem(event.getPreviousSlot(), null);
        }
    }

    @EventHandler
    public void entityDamageEvent(final EntityDamageEvent event) {
        if (this.isPet(event.getEntity())) {
            final PetBlock petBlock = this.getPet(event.getEntity());
            if (petBlock == null)
                return;
            if (event.getCause() != DamageCause.FALL)
                petBlock.damage(event.getFinalDamage());
            else if (event.getCause() == DamageCause.FALL)
                petBlock.damage(-2.0);
            event.setCancelled(true);
        }
    }

    private PetBlock getPet(Entity entity) {
        try {
            for (final PetBlock block : this.manager.petblocks.values()) {
                if (block != null && entity != null && block.getArmorStand() != null && block.getMovementEntity() != null && (block.getArmorStand().equals(entity) || block.getMovementEntity().equals(entity)))
                    return block;
            }
        } catch (final Exception ignored) {
        }
        return null;
    }

    private boolean isPet(Entity entity) {
        return this.getPet(entity) != null;
    }

    private class ParticleRunnable implements Runnable {
        @Override
        public void run() {
            //ThreadSafe
            for (final Player player : PetBlockListener.this.manager.carryingPet.toArray(new Player[PetBlockListener.this.manager.carryingPet.size()])) {
                ParticleEffect.HEART.display(0.5F, 0.5F, 0.5F, 0.1F, 1, player.getLocation().add(0, 1, 0), player.getWorld().getPlayers());
            }
            for (final Player player : PetBlockListener.this.manager.petblocks.keySet().toArray(new Player[PetBlockListener.this.manager.petblocks.size()])) {
                if (PetBlockListener.this.manager.petblocks.get(player).isDead() || !Config.getInstance().allowPetSpawning(player.getLocation())) {
                    PetBlockListener.this.manager.removePetBlock(player);
                    if (player.isOnline() && Config.getInstance().allowPetSpawning(player.getLocation())) {
                        PetBlockListener.this.plugin.getServer().getScheduler().runTaskAsynchronously(PetBlockListener.this.plugin, () -> {
                            final PetMeta petMeta = PetBlockListener.this.manager.dataManager.getPetMeta(player);
                            PetBlockListener.this.plugin.getServer().getScheduler().runTask(PetBlockListener.this.plugin, () -> PetBlockListener.this.manager.setPetBlock(player, petMeta));
                        });
                    }

                }
            }
        }
    }

    private class PetHunterRunnable implements Runnable {
        @Override
        public void run() {
            for (final Player player : PetBlockListener.this.manager.timeBlocked.keySet().toArray(new Player[PetBlockListener.this.manager.timeBlocked.size()])) {
                PetBlockListener.this.manager.timeBlocked.put(player, PetBlockListener.this.manager.timeBlocked.get(player) - 1);
                if (PetBlockListener.this.manager.timeBlocked.get(player) <= 0) {
                    PetBlockListener.this.manager.timeBlocked.remove(player);
                    PetBlockListener.this.providePet(player, (petMeta, petBlock) -> PetBlockListener.this.manager.setPetBlock(player, petMeta));

                }
            }
            int counter = 0;
            for (final World world : Bukkit.getWorlds()) {
                for (final Entity entity : world.getEntities()) {
                    if (entity instanceof ArmorStand && PetBlockListener.this.isDeadPet(entity)) {
                        entity.remove();
                        counter++;
                    } else if (!PetBlockListener.this.isPet(entity) && entity.getCustomName() != null && entity.getCustomName().equals("PetBlockIdentifier")) {
                        entity.remove();
                        counter++;
                    }
                }
            }
            if (counter == 1)
                BukkitUtilities.sendColorMessage("PetHunter " + ChatColor.GREEN + '>' + ChatColor.YELLOW + " Removed " + counter + " pet.", ChatColor.YELLOW, PetBlocksPlugin.PREFIX_CONSOLE);
            else if (counter > 0)
                BukkitUtilities.sendColorMessage("PetHunter " + ChatColor.GREEN + '>' + ChatColor.YELLOW + " Removed " + counter + " pet.", ChatColor.YELLOW, PetBlocksPlugin.PREFIX_CONSOLE);
        }
    }

    private void removePetFromArm(Player player) {
        this.providePet(player, (petMeta, petBlock) -> {
            if (petBlock == null)
                this.manager.setPetBlock(player, petMeta);
            NMSRegistry.setItemInHand19(player, null, true);
            this.manager.carryingPet.remove(player);
        });
    }

    private void providePet(Player player, PetRunnable runnable) {
        if (PetBlocksApi.hasPetBlock(player)) {
            final PetBlock petBlock = PetBlocksApi.getPetBlock(player);
            runnable.run(petBlock.getPetMeta(), petBlock);
        } else {
            this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
                final PetMeta petMeta = PetBlocksApi.getPetMeta(player);
                this.plugin.getServer().getScheduler().runTask(this.plugin, () -> runnable.run(petMeta, null));
            });
        }
    }

    @FunctionalInterface
    interface PetRunnable {
        void run(PetMeta petMeta, PetBlock petBlock);
    }
}
