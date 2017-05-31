package com.github.shynixn.petblocks.lib;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * Created by Shynixn
 */

public class Particle implements com.github.shynixn.petblocks.api.entities.Particle {
    private static final Long serialVersionUID = 1L;

    private ParticleEffect effect;

    private double x;
    private double y;
    private double z;
    private double speed;
    private int amount;

    private Material material;
    private byte data;

    public Particle(ParticleBuilder builder) {
        super();
        this.effect = builder.getEffect();
        this.x = builder.getX();
        this.y = builder.getY();
        this.z = builder.getZ();
        this.speed = builder.getSpeed();
        this.amount = builder.getAmount();
        this.material = builder.getMaterial();
        this.data = builder.getData();
    }

    public void setEffect(ParticleEffect effect) {
        this.effect = effect;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public void setData(byte data) {
        this.data = data;
    }

    public Particle() {
        super();
    }

    @Override
    public void play(Location location) {
        this.play(location, location.getWorld().getPlayers().toArray(new Player[location.getWorld().getPlayers().size()]));
    }

    @Override
    public void play(final Location location, final Player... players) {
        try {
            AsyncRunnable.toAsynchroneThread(new SendParticleRunnable(location, players));
        } catch (final Exception ignored) {

        }
    }

    @Override
    public ParticleEffect getEffect() {
        return this.effect;
    }

    @Override
    public double getX() {
        return this.x;
    }

    @Override
    public double getY() {
        return this.y;
    }

    @Override
    public double getZ() {
        return this.z;
    }

    @Override
    public double getSpeed() {
        return this.speed;
    }

    @Override
    public int getAmount() {
        return this.amount;
    }

    @Override
    public Material getMaterial() {
        return this.material;
    }

    @Override
    public int getMaterialId() {
        if (this.material == null)
            return -1;
        return this.material.getId();
    }

    @Override
    public byte getData() {
        return this.data;
    }

    private class SendParticleRunnable extends AsyncRunnable {
        private final Location location;
        private final Player[] players;

        SendParticleRunnable(Location location, Player... players) {
            super();
            this.location = location;
            this.players = players;
        }

        @Override
        public void run() {
            if (Particle.this.effect == ParticleEffect.SPELL_MOB || Particle.this.effect == ParticleEffect.SPELL_MOB_AMBIENT || Particle.this.effect == ParticleEffect.REDSTONE)
                Particle.this.effect.display(new ParticleEffect.OrdinaryColor((int) Particle.this.x, (int) Particle.this.z, (int) Particle.this.y), this.location, Arrays.asList(this.players));
            else if (Particle.this.effect == ParticleEffect.NOTE)
                Particle.this.effect.display(new ParticleEffect.NoteColor((int) Particle.this.x), this.location, Arrays.asList(this.players));
            else if (Particle.this.effect == ParticleEffect.BLOCK_CRACK || Particle.this.effect == ParticleEffect.BLOCK_DUST)
                Particle.this.effect.display(new ParticleEffect.BlockData(Particle.this.material, Particle.this.data), (float) Particle.this.x, (float) Particle.this.y, (float) Particle.this.z, (float) Particle.this.speed, Particle.this.amount, this.location, Arrays.asList(this.players));
            else if (Particle.this.effect == ParticleEffect.ITEM_CRACK)
                Particle.this.effect.display(new ParticleEffect.ItemData(Particle.this.material, Particle.this.data), (float) Particle.this.x, (float) Particle.this.y, (float) Particle.this.z, (float) Particle.this.speed, Particle.this.amount, this.location, Arrays.asList(this.players));
            else
                Particle.this.effect.display((float) Particle.this.x, (float) Particle.this.y, (float) Particle.this.z, (float) Particle.this.speed, Particle.this.amount, this.location, Arrays.asList(this.players));
        }
    }
}
