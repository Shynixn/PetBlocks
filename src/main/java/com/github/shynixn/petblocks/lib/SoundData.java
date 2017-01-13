package com.github.shynixn.petblocks.lib;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundData {
    private double pitch;
    private double volume;
    private Sound sound;

    public SoundData(Sound sound, double pitch, double volume) {
        super();
        this.pitch = pitch;
        this.sound = sound;
        this.volume = volume;
    }

    public SoundData(Sound sound, double pitch) {
        this(sound, pitch, 1.0);
    }

    public SoundData(Sound sound) {
        this(sound, 1.0);
    }

    public SoundData(String name, double pitch, double volume) {
        this(name);
        this.pitch = pitch;
        this.volume = volume;
    }

    public SoundData(String name) {
        this.pitch = 1.0;
        this.volume = 1.0;
        this.sound = Interpreter19.interPretSounds19(name);
    }

    public void playTo(Player player) {
        player.playSound(player.getLocation(), sound, (float) pitch, (float) volume);
    }

    public void play(Location location) {
        for (final Player player : location.getWorld().getPlayers()) {
            player.playSound(location, sound, (float) pitch, (float) volume);
        }
    }
}