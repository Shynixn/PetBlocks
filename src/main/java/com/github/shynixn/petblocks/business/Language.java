package com.github.shynixn.petblocks.business;

import java.io.File;
import java.lang.reflect.Field;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.shynixn.petblocks.business.bukkit.PetBlocksPlugin;
import com.github.shynixn.petblocks.lib.BukkitChatColor;
import com.github.shynixn.petblocks.lib.BukkitUtilities;

public final class Language {
    public static void reload(JavaPlugin plugin) {
        final File file = new File(plugin.getDataFolder(), "lang.yml");
        if (!file.exists())
            BukkitUtilities.copyFile(plugin.getResource("lang.yml"), file);
        final FileConfiguration config = new YamlConfiguration();
        try {
            config.load(file);
            GUI_TITLE = ChatColor.translateAlternateColorCodes('&', config.getString("gui-title"));
            DEFAULT_PETNAME = ChatColor.translateAlternateColorCodes('&', config.getString("default-petname"));

            PIG_DISPLAYNAME = ChatColor.translateAlternateColorCodes('&', config.getString("pig-name"));
            CHICKEN_DISPLAYNAME = ChatColor.translateAlternateColorCodes('&', config.getString("chicken-name"));
            DOG_DISPLAYNAME = ChatColor.translateAlternateColorCodes('&', config.getString("dog-name"));
            CAT_DISPLAYNAME = ChatColor.translateAlternateColorCodes('&', config.getString("cat-name"));
            BIRD_DISPLAYNAME = ChatColor.translateAlternateColorCodes('&', config.getString("bird-name"));
            COW_DISPLAYNAME = ChatColor.translateAlternateColorCodes('&', config.getString("cow-name"));
            SHEEP_DISPLAYNAME = ChatColor.translateAlternateColorCodes('&', config.getString("sheep-name"));
            IRONGOLEM_DISPLAYNAME = ChatColor.translateAlternateColorCodes('&', config.getString("irongolem-name"));
            DRAGON_DISPLAYNAME = ChatColor.translateAlternateColorCodes('&', config.getString("dragon-name"));
            ZOMBIE_DISPLAYNAME = ChatColor.translateAlternateColorCodes('&', config.getString("zombie-name"));
            CREEPER_DISPLAYNAME = ChatColor.translateAlternateColorCodes('&', config.getString("creeper-name"));
            SKELETON_DISPLAYNAME = ChatColor.translateAlternateColorCodes('&', config.getString("skeleton-name"));
            HORSE_DISPLAYNAME = ChatColor.translateAlternateColorCodes('&', config.getString("horse-name"));
            SPIDER_DISPLAYNAME = ChatColor.translateAlternateColorCodes('&', config.getString("spider-name"));
            VILLAGER_DISPLAYNAME = ChatColor.translateAlternateColorCodes('&', config.getString("villager-name"));
            HUMAN_DISPLAYNAME = ChatColor.translateAlternateColorCodes('&', config.getString("human-name"));
            BAT_DISPLAYNAME = ChatColor.translateAlternateColorCodes('&', config.getString("bat-name"));
            ENDERMAN_DISPLAYNAME = ChatColor.translateAlternateColorCodes('&', config.getString("enderman-name"));
            SILVERFISH_DISPLAYNAME = ChatColor.translateAlternateColorCodes('&', config.getString("silverfish-name"));
            PIGZOMBIE_DISPLAYNAME = ChatColor.translateAlternateColorCodes('&', config.getString("pigzombie-name"));
            SLIME_DISPLAYNAME = ChatColor.translateAlternateColorCodes('&', config.getString("slime-name"));
            LAVASLIME_DISPLAYNAME = ChatColor.translateAlternateColorCodes('&', config.getString("lavaslime-name"));
            GHAST_DISPLAYNAME = ChatColor.translateAlternateColorCodes('&', config.getString("ghast-name"));
            BLAZE_DISPLAYNAME = ChatColor.translateAlternateColorCodes('&', config.getString("blaze-name"));
            WITHER_DISPLAYNAME = ChatColor.translateAlternateColorCodes('&', config.getString("wither-name"));

            ENABLE_PET = ChatColor.translateAlternateColorCodes('&', config.getString("enable-pet"));
            DISABLE_PET = ChatColor.translateAlternateColorCodes('&', config.getString("disable-pet"));
            MY_PET = ChatColor.translateAlternateColorCodes('&', config.getString("my-pet"));

            COSTUME = ChatColor.translateAlternateColorCodes('&', config.getString("costumes"));
            COLOR_COSTUME = ChatColor.translateAlternateColorCodes('&', config.getString("color-costumes"));
            CUSTOM_COSTUME = ChatColor.translateAlternateColorCodes('&', config.getString("custom-costumes"));
            NAMING = ChatColor.translateAlternateColorCodes('&', config.getString("naming"));
            RIDING = ChatColor.translateAlternateColorCodes('&', config.getString("riding"));
            HAT = ChatColor.translateAlternateColorCodes('&', config.getString("hat"));

            NEXT = ChatColor.translateAlternateColorCodes('&', config.getString("next"));
            PREVIOUS = ChatColor.translateAlternateColorCodes('&', config.getString("previous"));

            MUTE = ChatColor.translateAlternateColorCodes('&', config.getString("disable-sound"));
            UNMUTE = ChatColor.translateAlternateColorCodes('&', config.getString("enable-sound"));

            NAME_MESSAGE = ChatColor.translateAlternateColorCodes('&', config.getString("naming-message"));
            NAME_SUCCES_MESSAGE = ChatColor.translateAlternateColorCodes('&', config.getString("naming-success"));
            NAME_ERROR_MESSAGE = ChatColor.translateAlternateColorCodes('&', config.getString("naming-error"));

            SNAME_MESSAGE = ChatColor.translateAlternateColorCodes('&', config.getString("skullnaming-message"));
            SNAME_SUCCES_MESSAGE = ChatColor.translateAlternateColorCodes('&', config.getString("skullnaming-success"));
            SNAME_ERROR_MESSAGE = ChatColor.translateAlternateColorCodes('&', config.getString("skullnaming-error"));

            CANNON = ChatColor.translateAlternateColorCodes('&', config.getString("cannon"));
            SKULL_NAMING = ChatColor.translateAlternateColorCodes('&', config.getString("skin"));
            CANCEL = ChatColor.translateAlternateColorCodes('&', config.getString("cancel"));
            CALL = ChatColor.translateAlternateColorCodes('&', config.getString("call"));

            PARTICLE = ChatColor.translateAlternateColorCodes('&', config.getString("particle"));
            EMPTY = ChatColor.translateAlternateColorCodes('&', config.getString("empty"));

            NO_PERMISSION = ChatColor.translateAlternateColorCodes('&', config.getString("no-perms"));

            ICO_PERMS_YES = ChatColor.translateAlternateColorCodes('&', config.getString("perms-ico-yes"));
            ICO_PERMS_NO = ChatColor.translateAlternateColorCodes('&', config.getString("perms-ico-no"));

            if (config.getString("prefix") != null) {
                PREFIX = ChatColor.translateAlternateColorCodes('&', config.getString("prefix"));
                NUMBER_PREFIX = ChatColor.translateAlternateColorCodes('&', config.getString("number-prefix"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            BukkitUtilities.sendColorMessage("Recreate your lang.yml!", ChatColor.RED, PetBlocksPlugin.PREFIX_CONSOLE);
        }
    }

    public static String getDisplayName(String petName) {
        for (Field field : Language.class.getDeclaredFields()) {
            if (field.getName().contains("DISPLAYNAME") && field.getName().toUpperCase().contains(petName.toUpperCase()))
                try {
                    return (String) field.get(null);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
        }
        throw new RuntimeException("Cannot find displaName");
    }

    public static String getDefaultPetName(Player player) {
        return DEFAULT_PETNAME.replace(":player", player.getName());
    }

    public static String GUI_TITLE = "PetBlocks";
    public static String PREFIX = BukkitChatColor.DARK_GREEN + "[" + BukkitChatColor.GREEN + "PetBlocks" + BukkitChatColor.DARK_GREEN + "] " + ChatColor.GREEN;
    public static String NUMBER_PREFIX = BukkitChatColor.BLUE.toString();

    public static String PIG_DISPLAYNAME = BukkitChatColor.LIGHT_PURPLE + "Pig";
    public static String CHICKEN_DISPLAYNAME = BukkitChatColor.WHITE + "Chicken";
    public static String DOG_DISPLAYNAME = BukkitChatColor.GOLD + "Dog";
    public static String CAT_DISPLAYNAME = BukkitChatColor.YELLOW + "Cat";
    public static String BIRD_DISPLAYNAME = BukkitChatColor.RED + "Bird";
    public static String COW_DISPLAYNAME = BukkitChatColor.WHITE + "Cow";
    public static String SHEEP_DISPLAYNAME = BukkitChatColor.GRAY + "Sheep";
    public static String IRONGOLEM_DISPLAYNAME = BukkitChatColor.DARK_GRAY + "Irongolem";
    public static String DRAGON_DISPLAYNAME = BukkitChatColor.DARK_PURPLE + "Dragon";
    public static String CREEPER_DISPLAYNAME = BukkitChatColor.GREEN + "Creeper";
    public static String ZOMBIE_DISPLAYNAME = BukkitChatColor.DARK_GREEN + "Zombie";
    public static String SKELETON_DISPLAYNAME = BukkitChatColor.GRAY + "Skeleton";
    public static String HORSE_DISPLAYNAME = BukkitChatColor.GOLD + "Horse";
    public static String VILLAGER_DISPLAYNAME = BukkitChatColor.GREEN + "Villager";
    public static String SPIDER_DISPLAYNAME = BukkitChatColor.DARK_GRAY + "Spider";
    public static String HUMAN_DISPLAYNAME = BukkitChatColor.AQUA + "Human";
    public static String BAT_DISPLAYNAME = BukkitChatColor.GRAY + "Bat";
    public static String ENDERMAN_DISPLAYNAME = BukkitChatColor.DARK_PURPLE + "Enderman";
    public static String SILVERFISH_DISPLAYNAME = BukkitChatColor.DARK_GRAY + "Silverfish";
    public static String PIGZOMBIE_DISPLAYNAME = BukkitChatColor.LIGHT_PURPLE + "Pigzombie";
    public static String SLIME_DISPLAYNAME = BukkitChatColor.GREEN + "Slime";
    public static String LAVASLIME_DISPLAYNAME = BukkitChatColor.DARK_RED + "Lavaslime";
    public static String GHAST_DISPLAYNAME = BukkitChatColor.WHITE + "Ghast";
    public static String BLAZE_DISPLAYNAME = BukkitChatColor.YELLOW + "Blaze";
    public static String WITHER_DISPLAYNAME = BukkitChatColor.DARK_GRAY + "Wither";

    public static String ENABLE_PET = BukkitChatColor.GREEN + "Enable Petblock";
    public static String DISABLE_PET = BukkitChatColor.RED + "Disable Petblock";
    public static String MY_PET = BukkitChatColor.DARK_GREEN + "My Pet";

    public static String COSTUME = BukkitChatColor.GREEN + "Costumes";
    public static String COLOR_COSTUME = BukkitChatColor.YELLOW + "Color Costumes";
    public static String CUSTOM_COSTUME = BukkitChatColor.BLUE + "Custom Costumes";
    public static String NAMING = BukkitChatColor.RED + "Change Name";
    public static String RIDING = BukkitChatColor.LIGHT_PURPLE + "Ride Pet";
    public static String HAT = BukkitChatColor.GOLD + "Wear your pet as hat";
    public static String CANNON = BukkitChatColor.WHITE + "Pet Cannon";
    public static String SKULL_NAMING = BukkitChatColor.YELLOW + "Change Skin";
    public static String CANCEL = BukkitChatColor.DARK_RED + "Cancel";
    public static String CALL = BukkitChatColor.DARK_GREEN + "Call";
    public static String PARTICLE = BukkitChatColor.DARK_RED + "Particles";
    public static String EMPTY = ChatColor.GRAY + "Empty";
    public static String NEXT = ChatColor.GREEN + "Next";
    public static String PREVIOUS = ChatColor.GREEN + "Previous";
    public static String MUTE = BukkitChatColor.YELLOW + "Mute Pet";
    public static String UNMUTE = BukkitChatColor.YELLOW + "Unmute Pet";

    public static String NAME_MESSAGE = ChatColor.YELLOW + "Enter the name the of your pet:";
    public static String NAME_SUCCES_MESSAGE = ChatColor.GREEN + "You changed the name of your pet.";
    public static String NAME_ERROR_MESSAGE = ChatColor.RED + "You cannot name your pet like that.";

    public static String SNAME_MESSAGE = ChatColor.YELLOW + "Enter the name the of your pet:";
    public static String SNAME_SUCCES_MESSAGE = ChatColor.GREEN + "You changed the name of your pet.";
    public static String SNAME_ERROR_MESSAGE = ChatColor.RED + "You cannot name your pet like that.";

    private static String DEFAULT_PETNAME = ":player's pet";

    public static String ICO_PERMS_YES = ChatColor.GREEN + "Yes";
    public static String ICO_PERMS_NO = ChatColor.RED + "No";
    public static String NO_PERMISSION = ChatColor.RED + "You don't have permissions.";
}
