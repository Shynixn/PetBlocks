package com.github.shynixn.petblocks.lib;

import org.bukkit.Material;

@Deprecated
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
        super();
    }

    public ParticleBuilder(ParticleEffect effect, double x, double y, double z, double speed, int amount) {
        super();
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
        this.setGreen(green);
        this.setBlue(blue);
        this.setRed(red);
        return this;
    }

    public int getRed() {
        return (int) this.x;
    }

    public int getBlue() {
        return (int) this.y;
    }

    public int getGreen() {
        return (int) this.z;
    }

    public ParticleBuilder setNoteColor(int color) {
        if (color > 20 || color < 0)
            color = 5;
        this.setRed(color);
        return this;
    }

    public ParticleEffect getEffect() {
        return this.effect;
    }

    public ParticleBuilder setEffect(ParticleEffect effect) {
        this.effect = effect;
        return this;
    }

    public int getAmount() {
        return this.amount;
    }

    public ParticleBuilder setAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public double getX() {
        return this.x;
    }

    public ParticleBuilder setX(double x) {
        this.x = x;
        return this;
    }

    public double getY() {
        return this.y;
    }

    public ParticleBuilder setY(double y) {
        this.y = y;
        return this;
    }

    public double getZ() {
        return this.z;
    }

    public ParticleBuilder setZ(double z) {
        this.z = z;
        return this;
    }

    public ParticleBuilder setOffset(double x, double y, double z) {
        this.setX(x);
        this.setY(y);
        this.setZ(z);
        return this;
    }

    public double getSpeed() {
        return this.speed;
    }

    public ParticleBuilder setSpeed(double speed) {
        this.speed = speed;
        return this;
    }

    public Material getMaterial() {
        return this.material;
    }

    public ParticleBuilder setMaterial(Material material) {
        this.material = material;
        return this;
    }

    public int getMaterialId() {
        if (this.material != null)
            return this.material.getId();
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
        return this.data;
    }

    public ParticleBuilder setData(byte data) {
        this.data = data;
        return this;
    }

    public boolean isColorParticleEffect() {
        return this.effect == ParticleEffect.SPELL_MOB || this.effect == ParticleEffect.SPELL_MOB_AMBIENT || this.effect == ParticleEffect.REDSTONE || this.effect == ParticleEffect.NOTE;
    }

    public boolean isNoteParticleEffect() {
        return this.effect == ParticleEffect.NOTE;
    }

    public boolean isMaterialParticleEffect() {
        return this.effect == ParticleEffect.BLOCK_CRACK || this.effect == ParticleEffect.BLOCK_DUST || this.effect == ParticleEffect.ITEM_CRACK;
    }

    @SuppressWarnings("deprecation")
    @Override
    public String toString() {
        String sdefault = "Name:" + this.effect.getName() + " Amount:" + this.amount + " Speed:" + this.speed + " OffsetX: " + this.x + " OffsetY: " + this.y + " OffsetZ" + this.z;
        if (this.isColorParticleEffect())
            sdefault = "Name:" + this.effect.getName() + " Amount:" + this.amount + " Speed:" + this.speed + " Red:" + this.getRed() + " Green:" + this.getGreen() + " Blue:" + this.getBlue();
        else if (this.isNoteParticleEffect())
            sdefault = "Name:" + this.effect.getName() + " Amount:" + this.amount + " Speed:" + this.speed + " Color:" + this.getRed();
        else if (this.isMaterialParticleEffect() && this.material != null)
            sdefault += " Id:" + this.material.getId() + " Data:" + this.data;
        return sdefault;
    }
}
