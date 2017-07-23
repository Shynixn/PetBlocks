package com.github.shynixn.petblocks.lib;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

@Deprecated
public final class SkullMetaRegistry {
    private SkullMetaRegistry() {
        super();
    }

    public static ItemStack convertToSkinSkull(ItemStack itemStack, String skinUrl, String v) {
        if (itemStack.getType() == Material.SKULL_ITEM) {
            SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
            try {
                final Class<?> cls = Class.forName(v);
                final Object real = cls.cast(meta);
                final Field field = real.getClass().getDeclaredField("profile");
                field.setAccessible(true);
                field.set(real, getNonPlayerProfile(skinUrl));
                meta = SkullMeta.class.cast(real);
                itemStack.setItemMeta(meta);
                itemStack = setDisplayName(itemStack, "TMP");
            } catch (ClassNotFoundException | IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
                Bukkit.getLogger().log(Level.WARNING, "Failed to convert skin to skull.", e);
            }
        }
        return itemStack;
    }

    private static ItemStack setDisplayName(ItemStack itemStack, String name) {
        final ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(name);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public static String getLink(ItemStack itemStack, String v) {
        final SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
        try {
            final Class<?> cls = Class.forName(v);
            final Object real = cls.cast(meta);
            final Field field = real.getClass().getDeclaredField("profile");
            field.setAccessible(true);
            final GameProfile profile = (GameProfile) field.get(real);
            final Collection<Property> props = profile.getProperties().get("textures");
            for (final Property property : props) {
                if (property.getName().equals("textures")) {
                    final String text = Base64Coder.decodeString(property.getValue());
                    String s = "";
                    boolean start = false;
                    for (int i = 0; i < text.length(); i++) {
                        if (text.charAt(i) == '"') {
                            start = !start;
                        } else if (start) {
                            s += text.charAt(i);
                        }
                    }
                    return s;
                }
            }
        } catch (ClassNotFoundException | IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to obtain link from skull.", e);
        }
        return null;
    }

    private static GameProfile getNonPlayerProfile(String skinUrl) {
        final GameProfile newSkinProfile = new GameProfile(UUID.randomUUID(), null);
        newSkinProfile.getProperties().put("textures", new Property("textures", Base64Coder.encodeString("{textures:{SKIN:{url:\"" + skinUrl + "\"}}}")));
        return newSkinProfile;
    }
}
