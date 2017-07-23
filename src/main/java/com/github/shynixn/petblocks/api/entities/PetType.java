package com.github.shynixn.petblocks.api.entities;

import com.github.shynixn.petblocks.business.bukkit.nms.VersionSupport;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.github.shynixn.petblocks.lib.SoundData;

public enum PetType {
    PIG(new SoundData("PIG_WALK"), new SoundData("PIG_IDLE"), MoveType.WALKING),
    CHICKEN(new SoundData("CHICKEN_WALK"), new SoundData("CHICKEN_IDLE"), MoveType.WALKING),
    DOG(new SoundData("WOLF_WALK"), new SoundData("WOLF_GROWL"), MoveType.WALKING),
    CAT(new SoundData("WOLF_WALK"), new SoundData("CAT_MEOW"), MoveType.WALKING),
    BIRD(new SoundData("CHICKEN_WALK", 0.3, 1), new SoundData("BAT_IDLE", 1, 0), MoveType.FLYING),

    // PACK 1
    COW(new SoundData("COW_WALK"), new SoundData("COW_IDLE"), MoveType.WALKING),
    SHEEP(new SoundData("SHEEP_WALK"), new SoundData("SHEEP_IDLE"), MoveType.WALKING),
    IRONGOLEM(new SoundData("IRONGOLEM_WALK"), null, MoveType.WALKING),
    //
    // PACK 2
    ZOMBIE(new SoundData("ZOMBIE_WALK"), new SoundData("ZOMBIE_IDLE"), MoveType.WALKING),
    SKELETON(new SoundData("SKELETON_WALK"), new SoundData("SKELETON_IDLE"), MoveType.WALKING),
    CREEPER(null, null, MoveType.WALKING),
    //
    // PACK 3
    SPIDER(new SoundData("SPIDER_WALK"), new SoundData("SPIDER_IDLE"), MoveType.WALKING),
    VILLAGER(null, new SoundData("VILLAGER_IDLE"), MoveType.WALKING),
    HORSE(new SoundData("HORSE_GALLOP"), new SoundData("HORSE_IDLE"), MoveType.WALKING),
    //

    // PACK 4
    HUMAN(null, null, MoveType.WALKING),
    //

    //Pack 5
    ENDERMAN(null, new SoundData("ENDERMAN_IDLE"), MoveType.WALKING),
    SILVERFISH(new SoundData("SILVERFISH_WALK"), new SoundData("SILVERFISH_IDLE"), MoveType.WALKING),
    BAT(new SoundData("BAT_LOOP"), new SoundData("BAT_IDLE"), MoveType.FLYING),
    //

    //Pack 6
    SLIME(new SoundData("SLIME_WALK"), null, MoveType.WALKING),
    LAVASLIME(new SoundData("MAGMACUBE_WALK"), null, MoveType.WALKING),
    PIGZOMBIE(new SoundData("PIG_WALK"), new SoundData("PIG_IDLE"), MoveType.WALKING),
    //

    // Pack 7
    GHAST(null, new SoundData("GHAST_SCREAM"), MoveType.FLYING),
    BLAZE(null, new SoundData("BLAZE_BREATH"), MoveType.FLYING),
    WITHER(null, new SoundData("WITHER_IDLE"), MoveType.FLYING),

    // Pack 8 (1.9)
    SHULKER(new SoundData("SHULKER_WELK"), new SoundData("SHULKER_IDLE"), MoveType.WALKING, VersionSupport.VERSION_1_9_R1),

    DRAGON(new SoundData("ENDERDRAGON_WINGS"), new SoundData("ENDERDRAGON_GROWL"), MoveType.FLYING);

    private final SoundData movingSound;
    private final SoundData randomSound;
    private final MoveType type;
    private final VersionSupport version;

    PetType(SoundData movingSound, SoundData randomSound, MoveType type) {
        this.movingSound = movingSound;
        this.randomSound = randomSound;
        this.type = type;
        this.version = VersionSupport.VERSION_1_8_R1;
    }

    PetType(SoundData movingSound, SoundData randomSound, MoveType type,VersionSupport version) {
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
        if (this.movingSound != null)
            this.movingSound.play(location);
    }

    public void playMovingSound(Player player) {
        if (this.movingSound != null)
            this.movingSound.playTo(player);
    }

    public void playRandomSound(Player player) {
        if (this.randomSound != null)
            this.randomSound.playTo(player);
    }

    public void playRandomSound(Location location) {
        if (this.randomSound != null)
            this.randomSound.play(location);
    }

    public static String[] getNames() {
        final String[] names = new String[PetType.values().length];
        for (int i = 0; i < names.length; i++) {
            names[i] = PetType.values()[i].name();
        }
        return names;
    }

    public static PetType getPetTypeFromName(String name) {
        for (final PetType pet : PetType.values())
        {
            if (pet.name().equalsIgnoreCase(name))
                return pet;
        }
        return null;
    }
}
