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
    IRONGOLEM(new SoundBuilder("IRONGOLEM_WALK"), null, MoveType.WALKING),
    //
    // PACK 3
    VILLAGER(null, new SoundBuilder("VILLAGER_IDLE"), MoveType.WALKING),
    //

    // PACK 4
    HUMAN(null, null, MoveType.WALKING),
    //

    //Pack 5
    SILVERFISH(new SoundBuilder("SILVERFISH_WALK"), new SoundBuilder("SILVERFISH_IDLE"), MoveType.WALKING),
    //

    //Pack 6
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
                ((SoundBuilder)this.movingSound).apply(location);
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
                ((SoundBuilder)this.movingSound).apply(player);
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
                ((SoundBuilder)this.randomSound).apply(player);
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
                ((SoundBuilder)this.randomSound).apply(location);
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
