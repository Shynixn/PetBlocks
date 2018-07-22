package com.github.shynixn.petblocks.bukkit.nms.helper;

import com.github.shynixn.petblocks.api.PetBlocksApi;
import com.github.shynixn.petblocks.api.bukkit.event.PetBlockCannonEvent;
import com.github.shynixn.petblocks.api.bukkit.event.PetBlockMoveEvent;
import com.github.shynixn.petblocks.api.bukkit.event.PetBlockRideEvent;
import com.github.shynixn.petblocks.api.bukkit.event.PetBlockWearEvent;
import com.github.shynixn.petblocks.api.business.entity.PetBlock;
import com.github.shynixn.petblocks.api.business.enumeration.ParticleType;
import com.github.shynixn.petblocks.api.business.service.ParticleService;
import com.github.shynixn.petblocks.api.business.service.SoundService;
import com.github.shynixn.petblocks.api.persistence.entity.Particle;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.api.persistence.entity.Sound;
import com.github.shynixn.petblocks.bukkit.PetBlocksPlugin;
import com.github.shynixn.petblocks.bukkit.logic.business.helper.PetBlockModifyHelper;
import com.github.shynixn.petblocks.bukkit.logic.business.helper.SkinHelper;
import com.github.shynixn.petblocks.bukkit.logic.persistence.configuration.Config;
import com.github.shynixn.petblocks.bukkit.nms.v1_13_R1.MaterialCompatibility13;
import com.github.shynixn.petblocks.core.logic.persistence.entity.ParticleEntity;
import com.github.shynixn.petblocks.core.logic.persistence.entity.PetData;
import com.github.shynixn.petblocks.core.logic.persistence.entity.SoundEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

public final class PetBlockHelper {
    private static final Random random = new Random();
    private static final Sound explosionSound;
    private static final Particle angryParticle;
    private static final Particle cloud;

    static {
        angryParticle = new ParticleEntity(ParticleType.VILLAGER_ANGRY);
        angryParticle.setOffSetX(2);
        angryParticle.setOffSetY(2);
        angryParticle.setOffSetZ(2);
        angryParticle.setSpeed(0.1);
        angryParticle.setAmount(2);

        cloud = new ParticleEntity(ParticleType.CLOUD);
        cloud.setOffSetX(2);
        cloud.setOffSetY(2);
        cloud.setOffSetZ(2);
        cloud.setSpeed(0.1);
        cloud.setAmount(100);

        explosionSound = new SoundEntity("EXPLODE");
        explosionSound.setVolume(1.0);
        explosionSound.setPitch(2.0);
    }

    private PetBlockHelper() {
        super();
    }

    public static int afraidWaterEffect(PetBlock petBlock, int counter) {
        final Entity entity = (Entity) petBlock.getEngineEntity();
        if (Config.INSTANCE.isAfraidOfwater()) {
            if (entity.getLocation().getBlock().isLiquid() && counter <= 0) {
                final Vector vec = new Vector(random.nextInt(3) * isNegative(random), random.nextInt(3) * isNegative(random), random.nextInt(3) * isNegative(random));
                entity.setVelocity(vec);
                if (Config.INSTANCE.isAfraidwaterParticles()) {
                    final ParticleService particleService = PetBlocksApi.INSTANCE.resolve(ParticleService.class).get();
                    particleService.playParticle(entity.getLocation(), angryParticle, petBlock.getPlayer());
                }
                counter = 20;
            }
            counter--;
        }
        return counter;
    }

    public static int isNegative(Random rand) {
        if (rand.nextInt(2) == 0)
            return -1;
        return 1;
    }

    public static void setSkin(PetBlock petBlock, String skin) {
        final ItemStack itemStack;
        if (skin.contains("textures.minecraft")) {
            if (!skin.startsWith("http://"))
                skin = "http://" + skin;
            itemStack = new ItemStack(org.bukkit.Material.SKULL_ITEM, 1, (byte) 3);
            SkinHelper.setItemStackSkin(itemStack, skin);
        } else {
            itemStack = activateHead(skin, new ItemStack(org.bukkit.Material.SKULL_ITEM, 1, (byte) 3));
        }
        refreshHeadItemMeta(petBlock, itemStack);
    }

    private static ItemStack setWithUnbreakable(PetMeta petMeta, ItemStack itemStack) {
        final Map<String, Object> data = new HashMap<>();
        data.put("Unbreakable", petMeta.isItemStackUnbreakable());
        itemStack = PetBlockModifyHelper.setItemStackNBTTag(itemStack, data);
        return itemStack;
    }

    public static long executeMovingSound(PetBlock petBlock, long previous) {
        if (petBlock.getMeta() == null)
            return previous;
        final long milli = System.currentTimeMillis();
        if (milli - previous > 500) {
            if (petBlock.getMeta().isSoundEnabled()) {
                final SoundService soundService = PetBlocksApi.INSTANCE.resolve(SoundService.class).get();
                soundService.playSound(petBlock.getLocation(), petBlock.getMeta().getEngine().getWalkingSound(), petBlock.getPlayer());
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
        final PetData petData = (PetData) petBlock.getMeta();
        if (!getArmorstand(petBlock).isDead() && getArmorstand(petBlock).getPassenger() == null && getEngineEntity(petBlock) != null && getArmorstand(petBlock).getVehicle() == null) {
            Location location = null;
            if (petData.getAge() >= Config.INSTANCE.getAge_largeticks())
                location = new Location(getEngineEntity(petBlock).getLocation().getWorld(), getEngineEntity(petBlock).getLocation().getX(), getEngineEntity(petBlock).getLocation().getY() - 1.2, getEngineEntity(petBlock).getLocation().getZ(), getEngineEntity(petBlock).getLocation().getYaw(), getEngineEntity(petBlock).getLocation().getPitch());
            else if (petData.getAge() <= Config.INSTANCE.getAge_smallticks())
                location = new Location(getEngineEntity(petBlock).getLocation().getWorld(), getEngineEntity(petBlock).getLocation().getX(), getEngineEntity(petBlock).getLocation().getY() - 0.7, getEngineEntity(petBlock).getLocation().getZ(), getEngineEntity(petBlock).getLocation().getYaw(), getEngineEntity(petBlock).getLocation().getPitch());
            if (location != null)
                callBack.run(location);
            counter = doTickSounds(counter, petBlock);
        } else if (getEngineEntity(petBlock) != null) {
            getEngineEntity(petBlock).teleport(getArmorstand(petBlock).getLocation());
        }
        try {
            if (petData.getAge() >= Config.INSTANCE.getAge_maxticks()) {
                if (Config.INSTANCE.isAge_deathOnMaxTicks() && !petBlock.isDieing()) {
                    petBlock.setDieing();
                }
            } else {
                boolean respawn = false;
                if (petData.getAge() < Config.INSTANCE.getAge_largeticks()) {
                    respawn = true;
                }
                petData.setAge(petData.getAge() + 1);
                if (petData.getAge() >= Config.INSTANCE.getAge_largeticks() && respawn) {
                    petBlock.respawn();
                }
            }
        } catch (final Exception ex) {
            PetBlocksPlugin.logger().log(Level.WARNING, "Catcher prevented server crash, please report the following error to author Shynixn!", ex);
        }
        getArmorstand(petBlock).setFireTicks(0);
        if (getEngineEntity(petBlock) != null)
            getEngineEntity(petBlock).setFireTicks(0);
        Bukkit.getPluginManager().callEvent(new PetBlockMoveEvent(petBlock));
        return counter;
    }

    private static int doTickSounds(int counter, PetBlock petBlock) {
        final PetMeta petData = petBlock.getMeta();
        if (counter <= 0) {
            final Random random = new Random();
            if (!getEngineEntity(petBlock).isOnGround() || petData.getEngine().getEntityType().equalsIgnoreCase("ZOMBIE")) {
                if (petBlock.getMeta().isSoundEnabled()) {
                    final SoundService soundService = PetBlocksApi.INSTANCE.resolve(SoundService.class).get();
                    soundService.playSound(petBlock.getLocation(), petBlock.getMeta().getEngine().getAmbientSound(), petBlock.getPlayer());
                }
            }
            counter = 20 * random.nextInt(20) + 1;
        }
        if (getEngineEntity(petBlock).isDead()) {
            PetBlocksApi.getDefaultPetBlockController().remove(petBlock);
        }
        if (petData.getParticleEffectMeta() != null) {
            final ParticleService particleService = PetBlocksApi.INSTANCE.resolve(ParticleService.class).get();
            particleService.playParticle(getArmorstand(petBlock).getLocation().add(0, 1, 0), petData.getParticleEffectMeta(), petBlock.getPlayer());
        }
        counter--;
        return counter;
    }

    public static void refreshHeadItemMeta(PetBlock petBlock, ItemStack itemStack) {
        final String name;
        name = petBlock.getEntityDisplayName();
        itemStack = nameItem(itemStack, name);
        itemStack = setWithUnbreakable(petBlock.getMeta(), itemStack);
        getArmorstand(petBlock).setHelmet(itemStack);
    }

    private static ItemStack nameItem(ItemStack item, String name) {
        if (item.getType() != Material.AIR) {
            final ItemMeta im = item.getItemMeta();
            if (name != null) {
                im.setDisplayName(name);
            }
            im.setLore(new ArrayList<>());
            item.setItemMeta(im);
            return item;
        }
        return item;
    }

    public static void setItemConsideringAge(PetBlock petBlock) {
        final PetData petData = (PetData) petBlock.getMeta();
        final ItemStack itemStack;
        if (petData.getSkin() != null) {
            if (petData.getSkin().contains("http")) {
                itemStack = new ItemStack(MaterialCompatibility13.getMaterialFromId(petData.getItemId()), 1, (short) petData.getItemDamage());
                SkinHelper.setItemStackSkin(itemStack, petData.getSkin());
            } else {
                itemStack = activateHead(petData.getSkin(), new ItemStack(MaterialCompatibility13.getMaterialFromId(petData.getItemId()), 1, (short) petData.getItemDamage()));
            }
        } else {
            itemStack = new ItemStack(MaterialCompatibility13.getMaterialFromId(petData.getItemId()), 1, (short) petData.getItemDamage());
        }
        if (petData.getAge() >= Config.INSTANCE.getAge_largeticks()) {
            refreshHeadItemMeta(petBlock, itemStack);
            getArmorstand(petBlock).setSmall(false);

        } else {
            refreshHeadItemMeta(petBlock, itemStack);
            getArmorstand(petBlock).setSmall(true);
        }
    }

    public static void setRiding(PetBlock petBlock, Player player) {
        if (getArmorstand(petBlock).getPassenger() == null && player.getPassenger() == null) {
            final PetBlockRideEvent event = new PetBlockRideEvent(petBlock, true);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCanceled()) {
                getArmorstand(petBlock).setVelocity(new Vector(0, 1, 0));
                getArmorstand(petBlock).setPassenger(player);
                player.closeInventory();
            }
        }
    }

    public static boolean setDieing(final PetBlock petBlock) {
        if (!petBlock.isDieing()) {
            petBlock.jump();
            if (petBlock.getArmorStand() != null && !getArmorstand(petBlock).isDead())
                getArmorstand(petBlock).setHeadPose(new EulerAngle(0, 1, 0));
            Bukkit.getPluginManager().getPlugin("PetBlocks").getServer().getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("PetBlocks"), () -> {
                final ParticleService particleService = PetBlocksApi.INSTANCE.resolve(ParticleService.class).get();
                particleService.playParticle(petBlock.getLocation(), cloud, petBlock.getPlayer());
                petBlock.remove();
            }, 20 * 2);
            return true;
        }
        return petBlock.isDieing();
    }

    private static ItemStack activateHead(String name, ItemStack itemStack) {
        try {
            if (itemStack.getItemMeta() instanceof SkullMeta) {
                final SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
                meta.setOwner(name);
                itemStack.setItemMeta(meta);
            }
        } catch (final Exception e) {
            PetBlocksPlugin.logger().log(Level.WARNING, e.getMessage());
        }
        return itemStack;
    }

    public static double setDamage(PetBlock petBlock, double health, double damage, TickCallBack callBack) {
        if (Config.INSTANCE.isDesign_showDamageAnimation()) {
            callBack.run(null);
        }
        if (!Config.INSTANCE.isCombat_invincible()) {
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
            getEngineEntity(petBlock).setVelocity(vector);
            if (petBlock.getMeta().isSoundEnabled()) {
                final SoundService soundService = PetBlocksApi.INSTANCE.resolve(SoundService.class).get();
                soundService.playSound(((Player) petBlock.getPlayer()).getLocation(), explosionSound, petBlock.getPlayer());
            }
        }
    }

    public static void wear(PetBlock petBlock, Player player, TickCallBack callBack) {
        if (getArmorstand(petBlock).getPassenger() == null && player.getPassenger() == null) {
            final PetBlockWearEvent event = new PetBlockWearEvent(petBlock, true);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCanceled()) {
                player.setPassenger(getArmorstand(petBlock));
                player.closeInventory();
                if (callBack != null)
                    callBack.run(null);
            }
        }
    }

    public static void jump(PetBlock petBlock) {
        getEngineEntity(petBlock).setVelocity(new Vector(0, 0.5, 0));
    }

    public static void teleport(PetBlock petBlock, Location location) {
        getEngineEntity(petBlock).teleport(location);
        getArmorstand(petBlock).teleport(location);
    }

    public static boolean isDead(PetBlock petBlock) {
        return (getEngineEntity(petBlock).isDead()
                || getArmorstand(petBlock).isDead())
                || (getEngineEntity(petBlock).getWorld().getName().equals(getArmorstand(petBlock).getWorld().getName())
                && getEngineEntity(petBlock).getLocation().distance(getArmorstand(petBlock).getLocation()) > 10);
    }

    public static void setDisplayName(PetBlock petBlock, String name) {
        getArmorstand(petBlock).setCustomName(name);
        getArmorstand(petBlock).setCustomNameVisible(true);
        final ItemStack itemStack = getArmorstand(petBlock).getHelmet();
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
        final Location location = ((Location) petBlock.getLocation()).clone();
        petBlock.remove();
        callBack.run(location);
    }

    public static void remove(PetBlock petBlock) {
        if (petBlock.getEngineEntity() != null && !((LivingEntity) petBlock.getEngineEntity()).isDead()) {
            ((LivingEntity) petBlock.getEngineEntity()).remove();
        }
        if (!((LivingEntity) petBlock.getArmorStand()).isDead()) {
            ((LivingEntity) petBlock.getArmorStand()).remove();
        }
    }

    private static ArmorStand getArmorstand(PetBlock petBlock) {
        return (ArmorStand) petBlock.getArmorStand();
    }

    private static LivingEntity getEngineEntity(PetBlock petBlock) {
        return (LivingEntity) petBlock.getEngineEntity();
    }

    @FunctionalInterface
    public interface TickCallBack {
        void run(Location location);
    }
}
