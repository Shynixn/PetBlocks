package com.github.shynixn.petblocks.lib;

import org.bukkit.Material;

public final class ParticleBuilder {
    private ParticleEffect effect;
    private double x;
    private double y;
    private double z;
    private double speed;
    private int amount;

    private Material material;
    private byte data;

    public ParticleBuilder() {
    }

    public ParticleBuilder(ParticleEffect effect, double x, double y, double z, double speed, int amount) {
        this.effect = effect;
        this.x = x;
        this.y = y;
        this.z = z;
        this.speed = speed;
    }

    public ParticleBuilder setRed(int red) {
        if (red >= 0)
            this.x = red;
        return this;
    }

    public ParticleBuilder setBlue(int blue) {
        if (blue >= 0)
            this.y = blue;
        return this;
    }

    public ParticleBuilder setGreen(int green) {
        if (green >= 0)
            this.z = green;
        return this;
    }

    public ParticleBuilder setColor(int red, int green, int blue) {
        setGreen(green);
        setBlue(blue);
        setRed(red);
        return this;
    }

    public int getRed() {
        return (int) x;
    }

    public int getBlue() {
        return (int) y;
    }

    public int getGreen() {
        return (int) z;
    }

    public ParticleBuilder setNoteColor(int color) {
        if (color > 20 || color < 0)
            color = 5;
        setRed(color);
        return this;
    }

    public ParticleEffect getEffect() {
        return effect;
    }

    public ParticleBuilder setEffect(ParticleEffect effect) {
        this.effect = effect;
        return this;
    }

    public int getAmount() {
        return amount;
    }

    public ParticleBuilder setAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public double getX() {
        return x;
    }

    public ParticleBuilder setX(double x) {
        this.x = x;
        return this;
    }

    public double getY() {
        return y;
    }

    public ParticleBuilder setY(double y) {
        this.y = y;
        return this;
    }

    public double getZ() {
        return z;
    }

    public ParticleBuilder setZ(double z) {
        this.z = z;
        return this;
    }

    public ParticleBuilder setOffset(double x, double y, double z) {
        setX(x);
        setY(y);
        setZ(z);
        return this;
    }

    public double getSpeed() {
        return speed;
    }

    public ParticleBuilder setSpeed(double speed) {
        this.speed = speed;
        return this;
    }

    public Material getMaterial() {
        return material;
    }

    public ParticleBuilder setMaterial(Material material) {
        this.material = material;
        return this;
    }

    public int getMaterialId() {
        if (material != null)
            return material.getId();
        return -1;
    }

    @SuppressWarnings("deprecation")
    public ParticleBuilder setMaterialId(int id) {
        if (id >= 0) {
            this.material = Material.getMaterial(id);
        }
        return this;
    }

    public Particle build() {
        return new Particle(this);
    }

    public byte getData() {
        return data;
    }

    public ParticleBuilder setData(byte data) {
        this.data = data;
        return this;
    }

    public boolean isColorParticleEffect() {
        if (effect == ParticleEffect.SPELL_MOB || effect == ParticleEffect.SPELL_MOB_AMBIENT || effect == ParticleEffect.REDSTONE || effect == ParticleEffect.NOTE)
            return true;
        return false;
    }

    public boolean isNoteParticleEffect() {
        if (effect == ParticleEffect.NOTE)
            return true;
        return false;
    }

    public boolean isMaterialParticleEffect() {
        if (effect == ParticleEffect.BLOCK_CRACK || effect == ParticleEffect.BLOCK_DUST || effect == ParticleEffect.ITEM_CRACK)
            return true;
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public String toString() {
        String sdefault = "Name:" + effect.getName() + " Amount:" + amount + " Speed:" + speed + " OffsetX: " + x + " OffsetY: " + y + " OffsetZ" + z;
        if (isColorParticleEffect())
            sdefault = "Name:" + effect.getName() + " Amount:" + amount + " Speed:" + speed + " Red:" + getRed() + " Green:" + getGreen() + " Blue:" + getBlue();
        else if (isNoteParticleEffect())
            sdefault = "Name:" + effect.getName() + " Amount:" + amount + " Speed:" + speed + " Color:" + getRed();
        else if (isMaterialParticleEffect() && material != null)
            sdefault += " Id:" + material.getId() + " Data:" + data;
        return sdefault;
    }
}
