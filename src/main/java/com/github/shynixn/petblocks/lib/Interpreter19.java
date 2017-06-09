package com.github.shynixn.petblocks.lib;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Level;

/**
 * Created by Shynixn
 */
public class Interpreter19 {
    public static Sound interPretSounds19(String sound) {
        try {
            if (!isAbove18())
                return Sound.valueOf(sound);
            if (sound.toUpperCase().equals("ENDERMAN_IDLE"))
                return Sound.valueOf("ENTITY_ENDERMEN_AMBIENT");
            if (sound.toUpperCase().equals("MAGMACUBE_WALK"))
                return Sound.valueOf("ENTITY_MAGMACUBE_JUMP");
            if (sound.toUpperCase().equals("SLIME_WALK"))
                return Sound.valueOf("ENTITY_SLIME_JUMP");
            if (sound.toUpperCase().equals("EXPLODE"))
                return Sound.valueOf("ENTITY_GENERIC_EXPLODE");
            if (sound.toUpperCase().equals("EAT"))
                return Sound.valueOf("ENTITY_GENERIC_EAT");
            if (sound.toUpperCase().contains("WALK"))
                return Sound.valueOf("ENTITY_" + sound.toUpperCase().split("_")[0] + "_STEP");
            if (sound.toUpperCase().contains("IDLE"))
                return Sound.valueOf("ENTITY_" + sound.toUpperCase().split("_")[0] + "_AMBIENT");
            if (sound.toUpperCase().equals("WOLF_GROWL"))
                return Sound.valueOf("ENTITY_WOLF_GROWL");
            if (sound.toUpperCase().equals("CAT_MEOW"))
                return Sound.valueOf("ENTITY_CAT_PURREOW");
            if (sound.toUpperCase().equals("HORSE_GALLOP"))
                return Sound.valueOf("ENTITY_HORSE_GALLOP");
            if (sound.toUpperCase().equals("BAT_LOOP"))
                return Sound.valueOf("ENTITY_BAT_LOOP");
            if (sound.toUpperCase().equals("GHAST_SCREAM"))
                return Sound.valueOf("ENTITY_GHAST_SCREAM");
            if (sound.toUpperCase().equals("BLAZE_BREATH"))
                return Sound.valueOf("ENTITY_BLAZE_AMBIENT");
            if (sound.toUpperCase().equals("ENDERDRAGON_WINGS"))
                return Sound.valueOf("ENTITY_ENDERDRAGON_FLAP");
            if (sound.toUpperCase().equals("ENDERDRAGON_GROWL"))
                return Sound.valueOf("ENTITY_ENDERDRAGON_GROWL");
            throw new RuntimeException("Sounds Betainterpreter19 cannot translate the sounds. " + sound + '.');
        } catch (final Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to interpret sounds above 1_9.", e);
        }
        return null;
    }

    public static ItemStack getItemInHand19(Player player, boolean offHand) {
        if (!isAbove18()) {
            return (ItemStack) ReflectionLib.invokeMethodByObject(player, "getItemInHand", new Object[0]);
        }
        if (offHand) {
            return (ItemStack) ReflectionLib.invokeMethodByObject(player.getInventory(), "getItemInOffHand", new Object[0]);
        }
        return (ItemStack) ReflectionLib.invokeMethodByObject(player.getInventory(), "getItemInMainHand", new Object[0]);
    }

    public static void setItemInHand19(Player player, ItemStack itemStack, boolean offHand) {
        if (!isAbove18()) {
            ReflectionLib.invokeMethodByObject(player, "setItemInHand", new Object[]{itemStack});
        } else if (offHand) {
            ReflectionLib.invokeMethodByObject(player.getInventory(), "setItemInOffHand", new Object[]{itemStack});
        } else {
            ReflectionLib.invokeMethodByObject(player.getInventory(), "setItemInMainHand", new Object[]{itemStack});
        }
    }

    private static boolean isAbove18() {
        try
        {
            BukkitUtilities.getServerVersion();
        }catch (final RuntimeException ex)
        {
            return false;
        }
        return  BukkitUtilities.getServerVersion().equals("v1_9_R1")
                || BukkitUtilities.getServerVersion().equals("v1_9_R2")
                || BukkitUtilities.getServerVersion().equals("v1_10_R1")
                || BukkitUtilities.getServerVersion().equals("v1_11_R1")
                || BukkitUtilities.getServerVersion().equals("v1_12_R1");
    }
}
