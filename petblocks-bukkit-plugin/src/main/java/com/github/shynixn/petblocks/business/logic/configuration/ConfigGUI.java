package com.github.shynixn.petblocks.business.logic.configuration;

import com.github.shynixn.petblocks.api.business.enumeration.GUIItem;
import com.github.shynixn.petblocks.api.entities.PetType;
import com.github.shynixn.petblocks.api.persistence.controller.IController;
import com.github.shynixn.petblocks.api.persistence.controller.IFileController;
import com.github.shynixn.petblocks.api.persistence.entity.EngineContainer;
import com.github.shynixn.petblocks.business.Language;
import com.github.shynixn.petblocks.business.bukkit.PetBlocksPlugin;
import com.github.shynixn.petblocks.business.bukkit.nms.NMSRegistry;
import com.github.shynixn.petblocks.business.logic.persistence.Factory;
import com.github.shynixn.petblocks.lib.BukkitUtilities;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * Created by Shynixn
 */
public final class ConfigGUI {
    private static final ConfigGUI instance = new ConfigGUI();
    private final IFileController<EngineContainer> engineController = Factory.createEngineController();
    private ItemStack[] costumes_default;
    private ItemStack[] costumes_color;
    private ItemStack[] costumes_custom;
    private ItemStack[] costumes_minecraftHeads;
    private Plugin plugin;

    /**
     * Initializes a new configGUI
     */
    private ConfigGUI() {
        super();
    }

    /**
     * Returns the instance of the configGUI
     *
     * @return instance
     */
    public static ConfigGUI getInstance() {
        return instance;
    }

    /**
     * Reloads the config
     */
    public void reload() {
        this.plugin = JavaPlugin.getPlugin(PetBlocksPlugin.class);
        this.plugin.reloadConfig();
        final FileConfiguration c = this.plugin.getConfig();
        this.costumes_default = this.resolveItems(c.getStringList("costumes.default"));
        this.costumes_color = this.resolveItems(c.getStringList("costumes.color"));
        this.costumes_custom = this.resolveItems(c.getStringList("costumes.custom"));
        this.costumes_minecraftHeads = this.resolveMinecraftHeadItems();
        for (final GUIItem item : GUIItem.values()) {
            item.setContainer(CustomItemContainer.resolveItemContainer(item.getPath(), c));
        }
        GUIItem.DEFAULTAPPEARANCE.getContainer().get().setEnabled(true);
        this.engineController.reload();
    }

    /**
     * Returns the engineController to manage loaded engines
     *
     * @return engines
     */
    public IFileController<EngineContainer> getEngineController() {
        return this.engineController;
    }

    /**
     * Returns all itemStacks for the ordinary category
     *
     * @return itemStacks
     */
    public ItemStack[] getDefaultItemStacks() {
        return this.costumes_default.clone();
    }

    /**
     * Returns all itemStacks for the colored category
     *
     * @return itemStacks
     */
    public ItemStack[] getColoredItemStacks() {
        return this.costumes_color.clone();
    }

    /**
     * Returns all itemStacks for the custom category
     *
     * @return itemStacks
     */
    public ItemStack[] getCustomItemStacks() {
        return this.costumes_custom.clone();
    }

    /**
     * Returns the itemStacks for the minecraftHeads
     *
     * @return itemStacks
     */
    public ItemStack[] getMinecraftHeadsItemStacks() {
        return this.costumes_minecraftHeads.clone();
    }

    /**
     * Returns if copySkin is enabled
     *
     * @return copySkin
     */
    public boolean isCopySkinEnabled() {
        return this.plugin.getConfig().getBoolean("gui.settings.copy-skin");
    }

    /**
     * Returns if lore is enabled
     *
     * @return lore
     */
    boolean isLoreEnabled() {
        return this.plugin.getConfig().getBoolean("gui.settings.lore");
    }

    /**
     * Returns if emptyClickBack is enabled
     *
     * @return enabled
     */
    public boolean isEmptyClickBackEnabled() {
        return this.plugin.getConfig().getBoolean("gui.settings.click-empty-slot-back");
    }

    /**
     * Returns if disable item is enabled
     *
     * @return displayItem
     */
    public boolean isOnlyDisableItemEnabled() {
        return this.plugin.getConfig().getBoolean("gui.settings.use-only-disable-pet-item");
    }

    private ItemStack[] resolveMinecraftHeadItems() {
        final List<ItemStack> itemStacks = new ArrayList<>();
        try {
            final Cipher decipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            decipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(Base64Coder.decode("vcnhus0kpQAIokFsEoT+0g=="), "AES"), new IvParameterSpec("RandomInitVector".getBytes("UTF-8")));
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new CipherInputStream(JavaPlugin.getPlugin(PetBlocksPlugin.class).getResource("minecraftheads.db"), decipher)))) {
                String s;
                final String splitter = Pattern.quote(",");
                while ((s = reader.readLine()) != null) {
                    final String[] tags = s.split(splitter);
                    if (tags.length == 3 && tags[2].length() % 4 == 0) {
                        ItemStack itemStack = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
                        final String line = Base64Coder.decodeString(tags[2]).replace("{\"textures\":{\"SKIN\":{\"url\":\"", "");
                        final String url = line.substring(0, line.indexOf("\""));
                        itemStack = NMSRegistry.changeSkullSkin(itemStack, url);
                        itemStacks.add(itemStack);
                    }
                }
            }
        } catch (IOException | InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to read minecraft-heads.com skins.");
        }
        for (int i = 0; i < itemStacks.size(); i++) {
            BukkitUtilities.nameItemDisplay(itemStacks.get(i), Language.NUMBER_PREFIX + "" + i + "");
        }
        return itemStacks.toArray(new ItemStack[itemStacks.size()]);
    }

    private ItemStack[] resolveItems(List<String> texts) {
        final List<ItemStack> itemStacks = new ArrayList<>();
        for (final String s : texts) {
            try {
                int i = 0;
                ItemStack itemStack = null;
                if (BukkitUtilities.tryParseInt(s)) {
                    itemStack = new ItemStack(Material.getMaterial(Integer.parseInt(s)));
                } else if (s.contains(":")) {
                    final String[] parts = s.split(":");
                    boolean skull = false;
                    if (parts[0].equalsIgnoreCase("skull")) {
                        skull = true;
                        itemStack = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
                        i = 2;
                    } else if (BukkitUtilities.tryParseInt(parts[0]) && BukkitUtilities.tryParseInt(parts[1])) {
                        itemStack = new ItemStack(Material.getMaterial(Integer.parseInt(parts[0])), 1, (short) Integer.parseInt(parts[1]));
                        i = 2;
                    } else if (BukkitUtilities.tryParseInt(parts[0])) {
                        itemStack = new ItemStack(Material.getMaterial(Integer.parseInt(parts[0])));
                        i = 1;
                    }
                    for (; i < parts.length; i++) {
                        if (parts[i].equalsIgnoreCase("unbreakable")) {
                            final Map<String, Object> data = new HashMap<>();
                            data.put("Unbreakable", true);
                            itemStack = NMSRegistry.setItemStackTag(itemStack, data);
                        }
                        if (parts[i].equalsIgnoreCase("lore")) {
                            i++;
                            final List<String> lore = new ArrayList<>();
                            for (; i < parts.length; i++) {
                                if (parts[i].equalsIgnoreCase("unbreakable")) {
                                    i--;
                                    break;
                                } else {
                                    lore.add(ChatColor.translateAlternateColorCodes('&', parts[i]));
                                }
                            }
                            if (itemStack != null) {
                                final ItemMeta meta = itemStack.getItemMeta();
                                meta.setLore(lore);
                                itemStack.setItemMeta(meta);
                            }
                        }
                    }
                    if (skull) {
                        if (parts[1].contains("textures.minecraft.net")) {
                            if (!parts[1].startsWith("http://"))
                                parts[1] = "http://" + parts[1];
                            itemStack = NMSRegistry.changeSkullSkin(itemStack, parts[1]);
                        } else {
                            itemStack = BukkitUtilities.activateHead(parts[1], itemStack);
                        }
                    }
                }
                itemStacks.add(itemStack);
            } catch (final Exception e) {
                Bukkit.getLogger().log(Level.WARNING, "Failed to enable registry.", e);
            }
        }
        for (int i = 0; i < itemStacks.size(); i++) {
            BukkitUtilities.nameItemDisplay(itemStacks.get(i), Language.NUMBER_PREFIX + "" + i + "");
        }
        return itemStacks.toArray(new ItemStack[itemStacks.size()]);
    }
}
