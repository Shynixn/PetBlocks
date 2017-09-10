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
    SILVERFISH(new SoundBuilder(""), new SoundBuilder(""), MoveType.WALKING),
    //

    //Pack 6
    PIGZOMBIE(new SoundBuilder(), new SoundBuilder(""), MoveType.WALKING),
    //

    // Pack 7
    GHAST(null, new SoundBuilder(""), MoveType.FLYING),
    BLAZE(null, new SoundBuilder(""), MoveType.FLYING),
    WITHER(null, new SoundBuilder(""), MoveType.FLYING),

    // Pack 8 (1.9)
    SHULKER(null, new SoundBuilder(""), MoveType.WALKING, VersionSupport.VERSION_1_9_R1),


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


    - '35:12'
            - '35:1'
            - '35:2'
            - '35:3'
            - '35:4'
            - '35:5'
            - '35:6'
            - '35:7'
            - '35:8'
            - '35:9'
            - '35:10'
            - '35:11'
            - '35:12'
            - '35:13'
            - '35:14'
            - '35:15'
            - '95'
            - '95:1'
            - '95:2'
            - '95:3'
            - '95:4'
            - '95:5'
            - '95:6'
            - '95:7'
            - '95:8'
            - '95:9'
            - '95:10'
            - '95:11'
            - '95:12'
            - '95:13'
            - '95:14'
            - '95:15'
            - '159'
            - '159:1'
            - '159:2'
            - '159:3'
            - '159:4'
            - '159:5'
            - '159:6'
            - '159:7'
            - '159:8'
            - '159:9'
            - '159:10'
            - '159:11'
            - '159:12'
            - '159:13'
            - '159:14'
            - '159:15'




