package com.github.shynixn.petblocks.business.logic.configuration;

import com.github.shynixn.petblocks.api.entities.Particle;
import com.github.shynixn.petblocks.business.Language;
import com.github.shynixn.petblocks.lib.BukkitUtilities;
import com.github.shynixn.petblocks.lib.ParticleBuilder;
import com.github.shynixn.petblocks.lib.ParticleEffect;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by Shynixn
 */
public class ConfigParticle {
    private static ConfigParticle instance;

    private ItemStack[] particles;
    private CustomItemContainer.ParticleItemContainer[] particleItemContainers;

    private ConfigParticle() {
        super();
    }

    public static ConfigParticle getInstance() {
        if (instance == null) {
            instance = new ConfigParticle();
        }
        return instance;
    }

    public ItemStack[] getParticleItemStacks() {
        return this.particles.clone();
    }

    public void load(FileConfiguration c) {
        final List<CustomItemContainer.ParticleItemContainer> containers = new ArrayList<>();
        for (int i = 0; i < 54; i++) {
            if (c.getString("particles." + i + ".owner") != null) {
                containers.add(this.getParticleItemContainer(i, c));
            }
        }
        final ItemStack[] itemStacks = new ItemStack[54];
        for (int i = 0; i < itemStacks.length; i++) {
            for (final CustomItemContainer.ParticleItemContainer container : containers) {
                if (container.getPosition() == i) {
                    itemStacks[i] = container.generate();
                    BukkitUtilities.nameItemDisplay(itemStacks[i], Language.NUMBER_PREFIX + "" + i + "");
                }
            }
        }
        this.particles = itemStacks;
        this.particleItemContainers = containers.toArray(new CustomItemContainer.ParticleItemContainer[containers.size()]);
    }

    public Particle getParticle(int slot) {
        for (final CustomItemContainer.ParticleItemContainer container : this.particleItemContainers) {
            if (container.getPosition() == slot)
                return container.getParticle().build();
        }
        return new ParticleBuilder().setEffect(null).build();
    }

    private CustomItemContainer.ParticleItemContainer getParticleItemContainer(int number, FileConfiguration c) {
        try {
            final String s = "particles." + number + '.';
            final String a = s + "effect.";
            return new CustomItemContainer.ParticleItemContainer(c.getInt(s + "id"), c.getInt(s + "damage"), c.getString(s + "owner"), number, c.getString(s + "lore"), true, "", this.getParticleForContainer(a, c));
        } catch (final Exception e) {
            Bukkit.getLogger().log(Level.WARNING,"Cannot create particle effect from config!. Please recreate your config file or reconfigure your particle!", e);
        }
        return null;
    }

    private ParticleBuilder getParticleForContainer(String p, FileConfiguration c) {
        if (c.contains(p + "id")) {
            return new ParticleBuilder().setEffect(ParticleEffect.getParticleEffectFromName(c.getString(p + "name")))
                    .setSpeed(c.getDouble(p + "speed"))
                    .setAmount(c.getInt(p + "amount"))
                    .setOffset(c.getDouble(p + "offx"), c.getDouble(p + "offy"), c.getDouble(p + "offz"))
                    .setMaterialId(c.getInt(p + "id"))
                    .setData((byte) c.getInt(p + "damage"));
        } else if (c.contains(p + "red")) {
            return new ParticleBuilder().setEffect(ParticleEffect.getParticleEffectFromName(c.getString(p + "name")))
                    .setSpeed(c.getDouble(p + "speed"))
                    .setAmount(c.getInt(p + "amount"))
                    .setColor(c.getInt(p + "red"), c.getInt(p + "green"), c.getInt(p + "blue"));

        } else {
            return new ParticleBuilder().setEffect(ParticleEffect.getParticleEffectFromName(c.getString(p + "name")))
                    .setSpeed(c.getDouble(p + "speed"))
                    .setAmount(c.getInt(p + "amount"))
                    .setOffset(c.getDouble(p + "offx"), c.getDouble(p + "offy"), c.getDouble(p + "offz"));
        }
    }
}
