package com.github.shynixn.petblocks.bukkit.logic.business.listener;

import com.github.shynixn.petblocks.api.PetBlocksApi;
import com.github.shynixn.petblocks.api.bukkit.event.PetBlockMoveEvent;
import com.github.shynixn.petblocks.api.bukkit.event.PetBlockRideEvent;
import com.github.shynixn.petblocks.api.business.entity.PetBlock;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.bukkit.PetBlocksPlugin;
import com.github.shynixn.petblocks.bukkit.lib.SimpleListener;
import com.github.shynixn.petblocks.bukkit.logic.business.PetBlockManager;
import com.github.shynixn.petblocks.bukkit.logic.business.PetRunnable;
import com.github.shynixn.petblocks.bukkit.logic.business.configuration.Config;
import com.github.shynixn.petblocks.bukkit.logic.business.configuration.ConfigPet;
import com.github.shynixn.petblocks.bukkit.nms.NMSRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
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
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * Listens to events related to the petblock entity.
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
public class PetBlockListener extends SimpleListener {
    private final PetBlockManager manager;
    private final Set<PetBlock> jumped = new HashSet<>();

    /**
     * Initializes a new petblockListener from the manager and plugin.
     *
     * @param manager manager
     * @param plugin  plugin
     */
    public PetBlockListener(PetBlockManager manager, Plugin plugin) {
        super(plugin);
        this.manager = manager;
        NMSRegistry.registerListener19(manager.carryingPet, plugin);
        this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new ParticleRunnable(), 0L, 60L);
        this.plugin.getServer().getScheduler().runTaskTimer(this.plugin, new PetHunterRunnable(), 0L, 20);
    }

    /**
     * Gets called when the player starts riding and caches the regions he has spawned in.
     *
     * @param event event
     */
    @EventHandler
    public void onPetBlockRideEvent(PetBlockRideEvent event) {
        NMSRegistry.canEnterRegionOnPetRiding(event.getPlayer(), true);
    }

    /**
     * Gets called when the petblock moves and kicks the player off the pet when he enters a region with a different owner.
     *
     * @param event event
     */
    @EventHandler
    public void onPetBlockMoveEvent(PetBlockMoveEvent event) {
        if (!NMSRegistry.canEnterRegionOnPetRiding(event.getPlayer(), false)) {
            ((ArmorStand) event.getPetBlock().getArmorStand()).eject();
        }
    }

    /**
     * Kicks a player off the pet when the region he is membership of gets modified.
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
                    if ((petBlock = PetBlocksApi.getDefaultPetBlockController().getByPlayer(player)) != null && NMSRegistry.shouldKickOffPet(player, regionName)) {
                        ((ArmorStand) petBlock.getArmorStand()).eject();
                    }
                }
            } catch (final Exception ex) {
                PetBlocksPlugin.logger().log(Level.WARNING, "Failed to kick member from pet.");
            }
        }
    }

    @EventHandler
    public void onEntityToggleSneakEvent(final PlayerToggleSneakEvent event) {
        final PetBlock petBlock;
        if (event.getPlayer().getPassenger() != null && this.isPet(event.getPlayer().getPassenger()) && (petBlock = this.manager.getPetBlockController().getByPlayer(event.getPlayer())) != null) {
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
            if (petBlock != null && petBlock.getPlayer() != null && petBlock.getPlayer().equals(event.getEntity())) {
                event.setCancelled(true);
            }
        }
        if (ConfigPet.getInstance().isFleesInCombat()) {
            if (event.getDamager() instanceof Player && this.manager.getPetBlockController().getByPlayer(event.getDamager()) != null) {
                this.manager.timeBlocked.put((Player) event.getDamager(), ConfigPet.getInstance().getReappearsInSeconds());
                this.manager.getPetBlockController().removeByPlayer(event.getDamager());
            } else if (event.getEntity() instanceof Player && this.manager.getPetBlockController().getByPlayer(event.getEntity()) != null) {
                this.manager.timeBlocked.put((Player) event.getEntity(), ConfigPet.getInstance().getReappearsInSeconds());
                this.manager.getPetBlockController().removeByPlayer(event.getEntity());
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
        if (this.manager.getPetBlockController().getByPlayer(event.getPlayer()) != null) {
            if (!event.getTo().getWorld().getName().equals(event.getFrom().getWorld().getName())) {
                this.manager.getPetBlockController().removeByPlayer(event.getPlayer());
                if (Config.getInstance().allowPetSpawning(event.getTo())) {
                    this.providePet(event.getPlayer(), (petMeta, petBlock) -> Bukkit.getServer().getScheduler().runTaskLater(this.plugin, () -> {
                        final PetBlock petBlock1 = this.manager.getPetBlockController().create(event.getPlayer(), petMeta);
                        this.manager.getPetBlockController().store(petBlock1);
                    }, ConfigPet.getInstance().getWarpDelay() * 20L));
                }
            } else if (event.getPlayer().getPassenger() != null && this.isPet(event.getPlayer().getPassenger())) {
                if (!ConfigPet.getInstance().isFollow_fallOffHead()) {
                    final PetBlock petBlock = this.manager.getPetBlockController().getByPlayer(event.getPlayer());
                    if (petBlock != null)
                        petBlock.teleportWithOwner(event.getTo());
                    event.setCancelled(true);
                } else {
                    final PetBlock petBlock = this.manager.getPetBlockController().getByPlayer(event.getPlayer());
                    if (petBlock != null)
                        petBlock.eject(event.getPlayer());
                }
            }
        }
    }

    @EventHandler
    public void onPlayerRespawnEvent(final PlayerRespawnEvent event) {
        if (this.manager.getPetBlockController().getByPlayer(event.getPlayer()) != null) {
            this.manager.getPetBlockController().remove(this.manager.getPetBlockController().getByPlayer(event.getPlayer()));
            this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> this.providePet(event.getPlayer(), (petMeta, petBlock) -> Bukkit.getServer().getScheduler().runTaskLater(this.plugin, () -> {
                final PetBlock petBlock1 = this.manager.getPetBlockController().create(event.getPlayer(), petMeta);
                this.manager.getPetBlockController().store(petBlock1);
            }, ConfigPet.getInstance().getWarpDelay() * 20L)), 60L);
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
            if (Math.floor(stand.getBodyPose().getZ() * 1000) == 301) {
                return true;
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void entityRightClickEvent(final PlayerInteractAtEntityEvent event) {
        if (this.manager.carryingPet.contains(event.getPlayer())) {
            NMSRegistry.setItemInHand19(event.getPlayer(), null, true);
            if (this.manager.getPetBlockController().getByPlayer(event.getPlayer()) != null)
                this.manager.getPetBlockController().removeByPlayer(event.getPlayer());
            event.setCancelled(true);
        } else if (this.isPet(event.getRightClicked())) {
            final PetBlock petBlock = this.getPet(event.getRightClicked());
            if (petBlock != null && petBlock.getPlayer().equals(event.getPlayer())) {
                if (ConfigPet.getInstance().isFeedingEnabled() && NMSRegistry.getItemInHand19(event.getPlayer(), false) != null && NMSRegistry.getItemInHand19(event.getPlayer(), false).getType() == Material.CARROT_ITEM) {
                    petBlock.getEffectPipeline().playParticleEffect(event.getRightClicked().getLocation(), ConfigPet.getInstance().getFeedingClickParticleEffect());
                    petBlock.getEffectPipeline().playSound(event.getRightClicked().getLocation(), ConfigPet.getInstance().getFeedingClickSound());
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
                else if (ConfigPet.getInstance().isFollow_carry() && (event.getPlayer().getInventory() == null || NMSRegistry.getItemInHand19(event.getPlayer(), true).getType() == Material.AIR)) {
                    NMSRegistry.setItemInHand19(event.getPlayer(), ((ArmorStand) petBlock.getArmorStand()).getHelmet().clone(), true);
                    this.manager.getPetBlockController().remove(petBlock);
                    this.manager.carryingPet.add(event.getPlayer());
                }
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (this.manager.carryingPet.contains(event.getPlayer())) {
            this.removePetFromArm(event.getPlayer(), true);
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
            this.removePetFromArm(event.getPlayer(), false);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        if (this.manager.carryingPet.contains(event.getEntity())) {
            this.removePetFromArm(event.getEntity(), false);
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
            this.removePetFromArm((Player) event.getWhoClicked(), false);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (this.manager.carryingPet.contains(event.getPlayer())) {
            this.removePetFromArm(event.getPlayer(), false);
            event.getItemDrop().remove();
        }
    }

    @EventHandler
    public void onSlotChange(PlayerItemHeldEvent event) {
        if (this.manager.carryingPet.contains(event.getPlayer())) {
            this.removePetFromArm(event.getPlayer(), false);
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
            for (final PetBlock block : this.manager.getPetBlockController().getAll()) {
                if (block != null && entity != null && block.getArmorStand() != null && block.getEngineEntity() != null && (block.getArmorStand().equals(entity) || block.getEngineEntity().equals(entity)))
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
            for (final PetBlock petBlock : PetBlockListener.this.manager.getPetBlockController().getAll()) {
                if (petBlock.isDead() || !Config.getInstance().allowPetSpawning(((Player) petBlock.getPlayer()).getLocation())) {
                    PetBlockListener.this.manager.getPetBlockController().remove(petBlock);
                    if (((Player) petBlock.getPlayer()).isOnline() && Config.getInstance().allowPetSpawning(((Player) petBlock.getPlayer()).getLocation())) {
                        PetBlockListener.this.plugin.getServer().getScheduler().runTaskAsynchronously(PetBlockListener.this.plugin, () -> {
                            final PetMeta petMeta = PetBlockListener.this.manager.getPetMetaController().getByPlayer(petBlock.getPlayer());
                            PetBlockListener.this.plugin.getServer().getScheduler().runTask(PetBlockListener.this.plugin, () -> PetBlockListener.this.setPetBlock((Player) petBlock.getPlayer(), petMeta));
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
                    PetBlockListener.this.providePet(player, (petMeta, petBlock) -> PetBlockListener.this.setPetBlock(player, petMeta));

                }
            }
            for (final World world : Bukkit.getWorlds()) {
                for (final Entity entity : world.getEntities()) {
                    if (entity instanceof ArmorStand && PetBlockListener.this.isDeadPet(entity)) {
                        entity.remove();
                    } else if (!PetBlockListener.this.isPet(entity) && entity.getCustomName() != null && entity.getCustomName().equals("PetBlockIdentifier")) {
                        entity.remove();
                    }
                }
            }
        }
    }

    private void removePetFromArm(Player player, boolean launch) {
        this.providePet(player, (petMeta, petBlock) -> {
            if (petBlock == null) {
                this.setPetBlock(player, petMeta);
            }
            NMSRegistry.setItemInHand19(player, null, true);
            this.manager.carryingPet.remove(player);
            if (launch) {
                final PetBlock managedPetBlock = this.manager.getPetBlockController().getByPlayer(player);
                ((LivingEntity) managedPetBlock.getEngineEntity()).setVelocity(this.getDirection(player));
            }
        });
    }

    /**
     * Creates a new petblock for the player and petMeta and sets it managed for the default controller.
     *
     * @param player  player
     * @param petMeta petMeta
     */
    private void setPetBlock(Player player, PetMeta petMeta) {
        final PetBlock petBlock = this.manager.getPetBlockController().create(player, petMeta);
        this.manager.getPetBlockController().store(petBlock);
    }

    /**
     * Gets the pet meta and petblock and calls the callBack .
     *
     * @param player   player
     * @param runnable Runnable
     */
    private void providePet(Player player, PetRunnable runnable) {
        final PetBlock petBlock;
        if ((petBlock = this.manager.getPetBlockController().getByPlayer(player)) != null) {
            runnable.run(petBlock.getMeta(), petBlock);
        } else {
            this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
                final PetMeta petMeta = this.manager.getPetMetaController().getByPlayer(player);
                this.plugin.getServer().getScheduler().runTask(this.plugin, () -> runnable.run(petMeta, null));
            });
        }
    }

    /**
     * Returns the launch direction for holding pets.
     *
     * @param player player
     * @return launchDirection
     */
    private Vector getDirection(Player player) {
        final Vector vector = new Vector();
        final double rotX = player.getLocation().getYaw();
        final double rotY = player.getLocation().getPitch();
        vector.setY(-Math.sin(Math.toRadians(rotY)));
        final double h = Math.cos(Math.toRadians(rotY));
        vector.setX(-h * Math.sin(Math.toRadians(rotX)));
        vector.setZ(h * Math.cos(Math.toRadians(rotX)));
        return vector.multiply(1.2);
    }
}
