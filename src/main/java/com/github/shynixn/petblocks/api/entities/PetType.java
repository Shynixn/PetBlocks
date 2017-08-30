package com.github.shynixn.petblocks.api.entities;

import com.github.shynixn.petblocks.api.persistence.entity.SoundMeta;
import com.github.shynixn.petblocks.business.bukkit.nms.VersionSupport;
import com.github.shynixn.petblocks.business.logic.persistence.entity.SoundBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.logging.Level;

@Deprecated
public enum PetType {
    PIG(new SoundBuilder("PIG_WALK"), new SoundBuilder("PIG_IDLE"), MoveType.WALKING),
    CHICKEN(new SoundBuilder("CHICKEN_WALK"), new SoundBuilder("CHICKEN_IDLE"), MoveType.WALKING),
    DOG(new SoundBuilder("WOLF_WALK"), new SoundBuilder("WOLF_GROWL"), MoveType.WALKING),
    CAT(new SoundBuilder("WOLF_WALK"), new SoundBuilder("CAT_MEOW"), MoveType.WALKING),
    BIRD(new SoundBuilder("CHICKEN_WALK", 0.3, 1), new SoundBuilder("BAT_IDLE", 1, 0), MoveType.FLYING),

    // PACK 1
    COW(new SoundBuilder("COW_WALK"), new SoundBuilder("COW_IDLE"), MoveType.WALKING),
    SHEEP(new SoundBuilder("SHEEP_WALK"), new SoundBuilder("SHEEP_IDLE"), MoveType.WALKING),
    IRONGOLEM(new SoundBuilder("IRONGOLEM_WALK"), null, MoveType.WALKING),
    //
    // PACK 2
    ZOMBIE(new SoundBuilder("ZOMBIE_WALK"), new SoundBuilder("ZOMBIE_IDLE"), MoveType.WALKING),
    SKELETON(new SoundBuilder("SKELETON_WALK"), new SoundBuilder("SKELETON_IDLE"), MoveType.WALKING),
    CREEPER(null, null, MoveType.WALKING),
    //
    // PACK 3
    SPIDER(new SoundBuilder("SPIDER_WALK"), new SoundBuilder("SPIDER_IDLE"), MoveType.WALKING),
    VILLAGER(null, new SoundBuilder("VILLAGER_IDLE"), MoveType.WALKING),
    HORSE(new SoundBuilder("HORSE_GALLOP"), new SoundBuilder("HORSE_IDLE"), MoveType.WALKING),
    //

    // PACK 4
    HUMAN(null, null, MoveType.WALKING),
    //

    //Pack 5
    ENDERMAN(null, new SoundBuilder("ENDERMAN_IDLE"), MoveType.WALKING),
    SILVERFISH(new SoundBuilder("SILVERFISH_WALK"), new SoundBuilder("SILVERFISH_IDLE"), MoveType.WALKING),
    BAT(new SoundBuilder("BAT_LOOP"), new SoundBuilder("BAT_IDLE"), MoveType.FLYING),
    //

    //Pack 6
    SLIME(new SoundBuilder("SLIME_WALK"), null, MoveType.WALKING),
    LAVASLIME(new SoundBuilder("MAGMACUBE_WALK"), null, MoveType.WALKING),
    PIGZOMBIE(new SoundBuilder("PIG_WALK"), new SoundBuilder("PIG_IDLE"), MoveType.WALKING),
    //

    // Pack 7
    GHAST(null, new SoundBuilder("GHAST_SCREAM"), MoveType.FLYING),
    BLAZE(null, new SoundBuilder("BLAZE_BREATH"), MoveType.FLYING),
    WITHER(null, new SoundBuilder("WITHER_IDLE"), MoveType.FLYING),

    // Pack 8 (1.9)
    SHULKER(null, new SoundBuilder("SHULKER_IDLE"), MoveType.WALKING, VersionSupport.VERSION_1_9_R1),

    DRAGON(new SoundBuilder("ENDERDRAGON_WINGS"), new SoundBuilder("ENDERDRAGON_GROWL"), MoveType.FLYING);

    private final SoundMeta movingSound;
    private final SoundMeta randomSound;
    private final MoveType type;
    private final VersionSupport version;

    PetType(SoundMeta movingSound, SoundMeta randomSound, MoveType type) {
        this.movingSound = movingSound;
        this.randomSound = randomSound;
        this.type = type;
        this.version = VersionSupport.VERSION_1_8_R1;
    }

    PetType(SoundMeta movingSound, SoundMeta randomSound, MoveType type, VersionSupport version) {
        this.movingSound = movingSound;
        this.randomSound = randomSound;
        this.type = type;
        this.version = version;
    }

    public VersionSupport getVersion() {
        return this.version;
    }

    @Deprecated
    public MoveType getMoveType() {
        return this.type;
    }

    public void playMovingSound(Location location) {
        if (this.movingSound != null) {
            if(!this.canPlaySpecificSounds())
                return;
            try {
                this.movingSound.apply(location);
            } catch (final Exception e) {
                Bukkit.getLogger().log(Level.WARNING, "Failed to play sound.", e);
            }
        }
    }

    public void playMovingSound(Player player) {
        if (this.movingSound != null) {
            if(!this.canPlaySpecificSounds())
                return;
            try {
                this.movingSound.apply(player);
            } catch (final Exception e) {
                Bukkit.getLogger().log(Level.WARNING, "Failed to play sound.", e);
            }
        }
    }

    public void playRandomSound(Player player) {
        if (this.randomSound != null) {
            if(!this.canPlaySpecificSounds())
                return;
            try {
                this.randomSound.apply(player);
            } catch (final Exception e) {
                Bukkit.getLogger().log(Level.WARNING, "Failed to play sound.", e);
            }
        }
    }

    public void playRandomSound(Location location) {
        if (this.randomSound != null) {
            if(!this.canPlaySpecificSounds())
                return;
            try {
                this.randomSound.apply(location);
            } catch (final Exception e) {
                Bukkit.getLogger().log(Level.WARNING, "Failed to play sound.", e);
            }
        }
    }

    private boolean canPlaySpecificSounds() {
        return this.movingSound != null && !(this.movingSound.getName().contains("SHULKER") && VersionSupport.getServerVersion().isVersionLowerThan(VersionSupport.VERSION_1_9_R1));
    }

    public static String[] getNames() {
        final String[] names = new String[PetType.values().length];
        for (int i = 0; i < names.length; i++) {
            names[i] = PetType.values()[i].name();
        }
        return names;
    }

    public static PetType getPetTypeFromName(String name) {
        for (final PetType pet : PetType.values()) {
            if (pet.name().equalsIgnoreCase(name))
                return pet;
        }
        return null;
    }
}
