package com.github.shynixn.petblocks.bukkit.logic.business.helper;

import com.github.shynixn.petblocks.api.business.entity.GUIItemContainer;
import com.github.shynixn.petblocks.api.business.entity.PetBlock;
import com.github.shynixn.petblocks.api.persistence.entity.EngineContainer;
import com.github.shynixn.petblocks.api.persistence.entity.ParticleEffectMeta;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.bukkit.logic.business.configuration.Config;
import com.github.shynixn.petblocks.bukkit.nms.v1_12_R1.MaterialCompatibility12;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

/**
 * Created by Shynixn 2017.
 * <p>
 * Version 1.1
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2017 by Shynixn
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class PetBlockModifyHelper {

    public static ItemStack setItemStackNBTTag(ItemStack itemStack, Map<String, Object> nbtTags) {
        try {
            final Method nmsCopyMethod = createClass("org.bukkit.craftbukkit.VERSION.inventory.CraftItemStack").getDeclaredMethod("asNMSCopy", ItemStack.class);

            final Class<?> nbtTagClass = createClass("net.minecraft.server.VERSION.NBTTagCompound");
            final Class<?> nmsItemStackClass = createClass("net.minecraft.server.VERSION.ItemStack");
            final Method bukkitCopyMethod = createClass("org.bukkit.craftbukkit.VERSION.inventory.CraftItemStack").getDeclaredMethod("asBukkitCopy", nmsItemStackClass);
            final Method getNBTTag = nmsItemStackClass.getDeclaredMethod("getTag");
            final Method setNBTTag = nmsItemStackClass.getDeclaredMethod("setTag", nbtTagClass);
            final Object nmsItemStack = nmsCopyMethod.invoke(null, itemStack);

            final Method nbtSetString = nbtTagClass.getDeclaredMethod("setString", String.class, String.class);
            final Method nbtSetBoolean = nbtTagClass.getDeclaredMethod("setBoolean", String.class, boolean.class);
            final Method nbtSetInteger = nbtTagClass.getDeclaredMethod("setInt", String.class, int.class);

            for (final String key : nbtTags.keySet()) {
                final Object value = nbtTags.get(key);
                Object nbtTag;
                if ((nbtTag = getNBTTag.invoke(nmsItemStack)) == null) {
                    nbtTag = nbtTagClass.newInstance();
                }

                if (value instanceof String) {
                    final String dataValue = (String) value;
                    nbtSetString.invoke(nbtTag, key, dataValue);
                } else if (value instanceof Integer) {
                    final int dataValue = (int) value;
                    nbtSetInteger.invoke(nbtTag, key, dataValue);
                } else if (value instanceof Boolean) {
                    final boolean dataValue = (boolean) value;
                    nbtSetBoolean.invoke(nbtTag, key, dataValue);
                }
                setNBTTag.invoke(nmsItemStack, nbtTag);
            }
            return (ItemStack) bukkitCopyMethod.invoke(null, nmsItemStack);
        } catch (NoSuchMethodException | ClassNotFoundException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to set nbt tag.", e);
        }
        return null;
    }

    private static Class<?> createClass(String path) throws ClassNotFoundException {
        final String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
        return Class.forName(path.replace("VERSION", version));
    }

    /**
     * Sets the engine for the given petMeta and petblock.
     *
     * @param petMeta         petMeta
     * @param petBlock        petblock
     * @param engineContainer engineData
     */
    public static void setEngine(PetMeta petMeta, PetBlock petBlock, EngineContainer engineContainer) {
        if (engineContainer == null)
            return;
        petMeta.setEngine(engineContainer);
        if (Config.getInstance().isCopySkinEnabled()) {
            final GUIItemContainer container = engineContainer.getGUIItem();
            petMeta.setSkin(container.getItemId(), container.getItemDamage(), container.getSkin(), container.isItemUnbreakable());
        }
        if (petBlock != null) {
            petBlock.respawn();
        }
    }

    /**
     * Sets the costume for the given petMeta and petblock.
     *
     * @param petMeta   petMeta
     * @param petBlock  petblock
     * @param container container
     */
    public static void setCostume(PetMeta petMeta, PetBlock petBlock, GUIItemContainer container) {
        if (container == null)
            return;
        petMeta.setSkin(container.getItemId(), container.getItemDamage(), container.getSkin(), container.isItemUnbreakable());
        if (petBlock != null) {
            petBlock.respawn();
        }
    }

    /**
     * Renames the petblock with the given name.
     *
     * @param petMeta  petMeta
     * @param petBlock petBlock
     * @param name     name
     */
    public static void rename(PetMeta petMeta, PetBlock petBlock, String name) {
        petMeta.setPetDisplayName(name);
        if (petBlock != null) {
            petBlock.respawn();
        }
    }

    /**
     * Sets the particleEffect for the given petMeta and petblock.
     *
     * @param petMeta   petMeta
     * @param petBlock  petblock
     * @param container container
     */
    public static void setParticleEffect(PetMeta petMeta, PetBlock petBlock, GUIItemContainer container) {
        if (container == null)
            return;
        final Optional<ParticleEffectMeta> transferOpt = Config.getInstance().getParticleController().getFromItem(container);
        if (!transferOpt.isPresent())
            return;
        final ParticleEffectMeta transfer = transferOpt.get();
        petMeta.getParticleEffectMeta().setEffectType(transfer.getEffectType());
        petMeta.getParticleEffectMeta().setSpeed(transfer.getSpeed());
        petMeta.getParticleEffectMeta().setAmount(transfer.getAmount());
        petMeta.getParticleEffectMeta().setOffset(transfer.getOffsetX(), transfer.getOffsetY(), transfer.getOffsetZ());
        petMeta.getParticleEffectMeta().setMaterial(transfer.getMaterial());
        petMeta.getParticleEffectMeta().setData(transfer.getData());
        if (petBlock != null) {
            petBlock.respawn();
        }
    }

    /**
     * Sets the skin for the given petMeta and petblock.
     *
     * @param petMeta  petMeta
     * @param petBlock petBlock
     * @param skin     skin
     */
    public static void setSkin(PetMeta petMeta, PetBlock petBlock, String skin) {
        String petSkin = skin;
        if (petSkin.contains("textures.minecraft") && !petSkin.contains("http://")) {
            petSkin = "http://" + skin;
        }
        petMeta.setSkin(MaterialCompatibility12.getIdFromMaterial(Material.SKULL_ITEM), (short) 3, petSkin, false);
        if (petBlock != null) {
            petBlock.respawn();
        }
    }
}
