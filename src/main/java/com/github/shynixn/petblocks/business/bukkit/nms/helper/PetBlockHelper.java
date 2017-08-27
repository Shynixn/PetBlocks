package com.github.shynixn.petblocks.business.bukkit.nms.helper;

import com.github.shynixn.petblocks.api.PetBlocksApi;
import com.github.shynixn.petblocks.api.entities.*;
import com.github.shynixn.petblocks.api.events.PetBlockCannonEvent;
import com.github.shynixn.petblocks.api.events.PetBlockMoveEvent;
import com.github.shynixn.petblocks.api.events.PetBlockRideEvent;
import com.github.shynixn.petblocks.api.events.PetBlockWearEvent;
import com.github.shynixn.petblocks.api.persistence.entity.SoundMeta;
import com.github.shynixn.petblocks.business.bukkit.nms.NMSRegistry;
import com.github.shynixn.petblocks.business.logic.configuration.ConfigPet;
import com.github.shynixn.petblocks.business.logic.persistence.entity.SoundBuilder;
import com.github.shynixn.petblocks.lib.BukkitUtilities;
import com.github.shynixn.petblocks.lib.ParticleBuilder;
import com.github.shynixn.petblocks.lib.ParticleEffect;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

/**
 * Created by Shynixn
 */
public final class PetBlockHelper {
    private static final Random random = new Random();
    private static final SoundMeta explosionSound = new SoundBuilder("EXPLODE", 1.0F, 2.0F);

    private PetBlockHelper() {
        super();
    }

    public static int afraidWaterEffect(Entity entity, int counter) {
        if (ConfigPet.getInstance().isAfraidOfwater()) {
            if (entity.getLocation().getBlock().isLiquid() && counter <= 0) {
                final Vector vec = new Vector(random.nextInt(3) * BukkitUtilities.isNegative(random), random.nextInt(3) * BukkitUtilities.isNegative(random), random.nextInt(3) * BukkitUtilities.isNegative(random));
                entity.setVelocity(vec);
                if (ConfigPet.getInstance().isAfraidwaterParticles())
                    new ParticleBuilder(ParticleEffect.VILLAGER_ANGRY, 2, 2, 2, 0.1, 2).build().play(entity.getLocation());
                counter = 20;
            }
            counter--;
        }
        return counter;
    }

    public static void setSkin(PetBlock petBlock, String skin) {
        final ItemStack itemStack;
        if (skin.contains("textures.minecraft")) {
            if (!skin.startsWith("http://"))
                skin = "http://" + skin;
            itemStack = NMSRegistry.changeSkullSkin(new ItemStack(org.bukkit.Material.SKULL_ITEM, 1, (byte) 3), skin);
        } else {
            itemStack = BukkitUtilities.activateHead(skin, new ItemStack(org.bukkit.Material.SKULL_ITEM, 1, (byte) 3));
        }
        refreshHeadItemMeta(petBlock, itemStack);
    }

    private static ItemStack setWithUnbreakable(PetMeta petMeta, ItemStack itemStack) {
        final Map<String, Object> data = new HashMap<>();
        data.put("Unbreakable", petMeta.isUnbreakable());
        itemStack = NMSRegistry.setItemStackTag(itemStack, data);
        return itemStack;
    }

    public static long executeMovingSound(Entity entity, Player owner, PetMeta petMeta, long previous) {
        if (petMeta == null)
            return previous;
        final long milli = System.currentTimeMillis();
        if (milli - previous > 500) {
            if (petMeta.isSoundsEnabled()) {
                if (ConfigPet.getInstance().isDesign_allowOtherHearSound() && !petMeta.isHidden())
                    petMeta.getType().playMovingSound(entity.getLocation());
                else
                    petMeta.getType().playMovingSound(owner);
            }
            return milli;
        }
        return previous;
    }

    public static void setSkin(PetBlock petBlock, org.bukkit.Material material, byte data) {
        final ItemStack itemStack = new ItemStack(material, 1, data);
        refreshHeadItemMeta(petBlock, itemStack);
    }

    public static int doTick(int counter, PetBlock petBlock, TickCallBack callBack) {
        if (!petBlock.getArmorStand().isDead() && petBlock.getArmorStand().getPassenger() == null && petBlock.getMovementEntity() != null && petBlock.getArmorStand().getVehicle() == null) {
            Location location = null;
            if (petBlock.getPetMeta().getAge() == Age.LARGE)
                location = new Location(petBlock.getMovementEntity().getLocation().getWorld(), petBlock.getMovementEntity().getLocation().getX(), petBlock.getMovementEntity().getLocation().getY() - 1.2, petBlock.getMovementEntity().getLocation().getZ(), petBlock.getMovementEntity().getLocation().getYaw(), petBlock.getMovementEntity().getLocation().getPitch());
            else if (petBlock.getPetMeta().getAge() == Age.SMALL)
                location = new Location(petBlock.getMovementEntity().getLocation().getWorld(), petBlock.getMovementEntity().getLocation().getX(), petBlock.getMovementEntity().getLocation().getY() - 0.7, petBlock.getMovementEntity().getLocation().getZ(), petBlock.getMovementEntity().getLocation().getYaw(), petBlock.getMovementEntity().getLocation().getPitch());

            if (location != null)
                callBack.run(location);
            counter = doTickSounds(counter, petBlock);
        } else if (petBlock.getMovementEntity() != null) {
            petBlock.getMovementEntity().teleport(petBlock.getArmorStand().getLocation());
        }
        try {
            if (petBlock.getPetMeta().getAgeInTicks() >= ConfigPet.getInstance().getAge_maxticks()) {
                if (ConfigPet.getInstance().isAge_deathOnMaxTicks() && !petBlock.isDieing()) {
                    petBlock.setDieing();
                }
            } else {
                final Age age = petBlock.getPetMeta().getAge();
                petBlock.getPetMeta().setAgeInTicks(petBlock.getPetMeta().getAgeInTicks() + 1);
                if (petBlock.getPetMeta().getAge() != age) {
                    petBlock.respawn();
                }
            }
        } catch (final Exception ex) {
            Bukkit.getLogger().log(Level.WARNING, "Catcher prevented server crash, please report the following error to author Shynixn!", ex);
        }
        petBlock.getArmorStand().setFireTicks(0);
        if (petBlock.getMovementEntity() != null)
            petBlock.getMovementEntity().setFireTicks(0);
        Bukkit.getPluginManager().callEvent(new PetBlockMoveEvent(petBlock));
        return counter;
    }

    private static int doTickSounds(int counter, PetBlock petBlock) {
        if (counter <= 0) {
            final Random random = new Random();
            if (!petBlock.getMovementEntity().isOnGround() || petBlock.getPetMeta().getMovementType() == Movement.CRAWLING) {
                if (petBlock.getPetMeta().isSoundsEnabled()) {
                    if (ConfigPet.getInstance().isDesign_allowOtherHearSound())
                        petBlock.getPetMeta().getType().playRandomSound(petBlock.getMovementEntity().getLocation());
                    else
                        petBlock.getPetMeta().getType().playRandomSound(petBlock.getOwner());
                }
            }
            counter = 20 * random.nextInt(20) + 1;
        }
        if (petBlock.getMovementEntity().isDead()) {
            PetBlocksApi.removePetBlock(petBlock.getOwner());
        }
        if (petBlock.getPetMeta().getParticleEffect() != null) {
            if (petBlock.getPetMeta().isHidden()) {
                petBlock.getPetMeta().getParticleEffect().play(petBlock.getArmorStand().getLocation().add(0, 1, 0), petBlock.getOwner());
            } else {
                petBlock.getPetMeta().getParticleEffect().play(petBlock.getArmorStand().getLocation().add(0, 1, 0));
            }
        }
        counter--;
        return counter;
    }

    public static void refreshHeadItemMeta(PetBlock petBlock, ItemStack itemStack) {
        final String name;
        if (petBlock.getPetMeta().getHeadDisplayName() == null)
            name = petBlock.getDisplayName();
        else
            name = petBlock.getPetMeta().getHeadDisplayName();
        if (petBlock.getPetMeta().getHeadLore() != null)
            itemStack = BukkitUtilities.nameItem(itemStack, name, petBlock.getPetMeta().getHeadLore());
        else
            itemStack = BukkitUtilities.nameItem(itemStack, name, null);
        itemStack = setWithUnbreakable(petBlock.getPetMeta(), itemStack);
        petBlock.getArmorStand().setHelmet(itemStack);
    }

    public static void setItemConsideringAge(PetBlock petBlock) {
        final Age age = petBlock.getPetMeta().getAge();
        final ItemStack itemStack;
        if (petBlock.getPetMeta().getSkin() != null) {
            if (petBlock.getPetMeta().getSkin().contains("http")) {
                itemStack = NMSRegistry.changeSkullSkin(new ItemStack(petBlock.getPetMeta().getSkinMaterial(), 1, petBlock.getPetMeta().getSkinDurability()), petBlock.getPetMeta().getSkin());
            } else {
                itemStack = BukkitUtilities.activateHead(petBlock.getPetMeta().getSkin(), new ItemStack(petBlock.getPetMeta().getSkinMaterial(), 1, petBlock.getPetMeta().getSkinDurability()));
            }
        } else {
            itemStack = new ItemStack(petBlock.getPetMeta().getSkinMaterial(), 1, petBlock.getPetMeta().getSkinDurability());
        }
        if (age == Age.SMALL) {
            refreshHeadItemMeta(petBlock, itemStack);
            petBlock.getArmorStand().setSmall(true);
        } else if (age == Age.LARGE) {
            refreshHeadItemMeta(petBlock, itemStack);
            petBlock.getArmorStand().setSmall(false);
        }
    }

    public static void setRiding(PetBlock petBlock, Player player) {
        if (petBlock.getArmorStand().getPassenger() == null && player.getPassenger() == null) {
            final PetBlockRideEvent event = new PetBlockRideEvent(petBlock, true);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCanceled()) {
                petBlock.getArmorStand().setVelocity(new Vector(0, 1, 0));
                petBlock.getArmorStand().setPassenger(player);
                player.closeInventory();
            }
        }
    }

    public static boolean setDieing(final PetBlock petBlock) {
        if (!petBlock.isDieing()) {
            petBlock.jump();
            if (petBlock.getArmorStand() != null && !petBlock.getArmorStand().isDead())
                petBlock.getArmorStand().setHeadPose(new EulerAngle(0, 1, 0));
            Bukkit.getPluginManager().getPlugin("PetBlocks").getServer().getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("PetBlocks"), () -> {
                final Particle particle = new ParticleBuilder(ParticleEffect.CLOUD, 1, 1, 1, 0.1, 100).build();
                particle.play(petBlock.getLocation());
                petBlock.remove();
            }, 20 * 2);
            return true;
        }
        return petBlock.isDieing();
    }

    public static double setDamage(PetBlock petBlock, double health, double damage, TickCallBack callBack) {
        if (ConfigPet.getInstance().isDesign_showDamageAnimation()) {
            callBack.run(null);
        }
        if (!ConfigPet.getInstance().isCombat_invincible()) {
            health -= damage;
            if (health <= 0) {
                petBlock.setDieing();
            }
        }
        return health;
    }

    public static void launch(PetBlock petBlock, Vector vector) {
        final PetBlockCannonEvent event = new PetBlockCannonEvent(petBlock);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCanceled()) {
            petBlock.getMovementEntity().setVelocity(vector);
            if (petBlock.getPetMeta().isSoundsEnabled()) {
                try {
                    explosionSound.apply(petBlock.getOwner().getLocation());
                } catch (final Exception e) {
                    Bukkit.getLogger().log(Level.WARNING, "Cannot play sound.", e);
                }
            }
        }
    }

    public static void wear(PetBlock petBlock, Player player, TickCallBack callBack) {
        if (petBlock.getArmorStand().getPassenger() == null && player.getPassenger() == null) {
            final PetBlockWearEvent event = new PetBlockWearEvent(petBlock, true);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCanceled()) {
                player.setPassenger(petBlock.getArmorStand());
                player.closeInventory();
                if (callBack != null)
                    callBack.run(null);
            }
        }
    }

    public static void jump(PetBlock petBlock) {
        petBlock.getMovementEntity().setVelocity(new Vector(0, 0.5, 0));
    }

    public static void teleport(PetBlock petBlock, Location location) {
        petBlock.getMovementEntity().teleport(location);
        petBlock.getArmorStand().teleport(location);
    }

    public static boolean isDead(PetBlock petBlock) {
        return (petBlock.getMovementEntity().isDead() || petBlock.getArmorStand().isDead()) || (petBlock.getMovementEntity().getWorld().getName().equals(petBlock.getArmorStand().getWorld().getName()) && petBlock.getMovementEntity().getLocation().distance(petBlock.getArmorStand().getLocation()) > 10);
    }

    public static void setDisplayName(PetBlock petBlock, String name) {
        petBlock.getArmorStand().setCustomName(name);
        petBlock.getArmorStand().setCustomNameVisible(true);
        final ItemStack itemStack = petBlock.getArmorStand().getHelmet();
        refreshHeadItemMeta(petBlock, itemStack);
    }

    public static void eject(PetBlock petBlock, Player player, TickCallBack callBack) {
        final PetBlockWearEvent event = new PetBlockWearEvent(petBlock, false);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCanceled()) {
            player.eject();
            if (callBack != null)
                callBack.run(null);
        }
    }

    public static void respawn(PetBlock petBlock, TickCallBack callBack) {
        final Location location = petBlock.getLocation().clone();
        petBlock.remove();
        callBack.run(location);
    }

    public static void remove(PetBlock petBlock) {
        if (petBlock.getMovementEntity() != null && !petBlock.getMovementEntity().isDead())
            petBlock.getMovementEntity().remove();
        if (!petBlock.getArmorStand().isDead())
            petBlock.getArmorStand().remove();
    }

    @FunctionalInterface
    public interface TickCallBack {
        void run(Location location);
    }
}
