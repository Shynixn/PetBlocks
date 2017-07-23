package com.github.shynixn.petblocks.business.logic.configuration;

import com.github.shynixn.petblocks.api.entities.ItemContainer;
import com.github.shynixn.petblocks.api.entities.PetType;
import com.github.shynixn.petblocks.business.Language;
import com.github.shynixn.petblocks.business.bukkit.nms.NMSRegistry;
import com.github.shynixn.petblocks.lib.BukkitUtilities;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Created by Shynixn
 */
public final class ConfigGUI {
    private static ConfigGUI instance;

    //Settings
    private boolean settings_copyskin = true;
    private boolean settings_allowLore = true;
    private boolean settings_clickemptyback = true;
    private boolean settings_onlyDisableItem;

    //General
    private ItemContainer general_myContainer;
    private ItemContainer general_defaultAppearanceContainer;
    private ItemContainer general_enablePetContainer;
    private ItemContainer general_disablePetContainer;
    private ItemContainer general_emptyslotContainer;
    private ItemContainer general_previouspageContainer;
    private ItemContainer general_nextpageContainer;
    //Item
    private ItemContainer items_defaultcostumeContainer;
    private ItemContainer items_colorcostumeContainer;
    private ItemContainer items_customcostumeContainer;
    private ItemContainer items_soundEnabledContainer;
    private ItemContainer items_soundDisabledContainer;
    private ItemContainer items_callpetContainer;
    private ItemContainer items_particlepetContainer;
    private ItemContainer items_cancelpetContainer;
    private ItemContainer items_cannonpetContainer;
    private ItemContainer items_skullNamingContainer;
    private ItemContainer items_namingContainer;
    private ItemContainer items_hatpetContainer;
    private ItemContainer items_ridingpetContainer;
    //Souls
    private final Map<PetType, ItemContainer> souls_Container = new HashMap<>();
    //Costumes
    private ItemStack[] costumes_default;
    private ItemStack[] costumes_color;
    private ItemStack[] costumes_custom;

    private ConfigGUI() {
        super();
    }

    public static ConfigGUI getInstance() {
        if (instance == null)
            instance = new ConfigGUI();
        return instance;
    }

    public void load(FileConfiguration c) {
        this.settings_copyskin = c.getBoolean("gui.settings.copy-skin");
        this.settings_allowLore = c.getBoolean("gui.settings.lore");
        this.settings_clickemptyback = c.getBoolean("gui.settings.click-empty-slot-back");
        this.settings_onlyDisableItem = c.getBoolean("gui.settings.use-only-disable-pet-item");

        this.general_myContainer = new CustomItemContainer(-253, -1, "none", c.getInt("gui.general." + "my-pet" + ".position"), c.getString("gui.general." + "my-pet" + ".lore"), true, "", null);
        this.general_defaultAppearanceContainer = CustomItemContainer.resolveItemContainer("gui.general.default-appearance", c);
        this.general_enablePetContainer = CustomItemContainer.resolveItemContainer("gui.general.enable-pet", c);
        this.general_disablePetContainer = CustomItemContainer.resolveItemContainer("gui.general.disable-pet", c);
        this.general_emptyslotContainer = CustomItemContainer.resolveItemContainer("gui.general.empty-slot", c);
        this.general_previouspageContainer = CustomItemContainer.resolveItemContainer("gui.general.previous-page", c);
        this.general_nextpageContainer = CustomItemContainer.resolveItemContainer("gui.general.next-page", c);

        this.items_soundDisabledContainer = CustomItemContainer.resolveItemContainer("gui.items.sounds-disabled-pet", c);
        this.items_soundEnabledContainer = CustomItemContainer.resolveItemContainer("gui.items.sounds-enabled-pet", c);

        this.items_defaultcostumeContainer = CustomItemContainer.resolveItemContainer("gui.items.default-costume", c);
        this.items_colorcostumeContainer = CustomItemContainer.resolveItemContainer("gui.items.color-costume", c);
        this.items_customcostumeContainer = CustomItemContainer.resolveItemContainer("gui.items.custom-costume", c);
        this.items_callpetContainer = CustomItemContainer.resolveItemContainer("gui.items.call-pet", c);
        this.items_particlepetContainer = CustomItemContainer.resolveItemContainer("gui.items.particle-pet", c);
        this.items_cancelpetContainer = CustomItemContainer.resolveItemContainer("gui.items.cancel-pet", c);
        this.items_cannonpetContainer = CustomItemContainer.resolveItemContainer("gui.items.cannon-pet", c);
        this.items_skullNamingContainer = CustomItemContainer.resolveItemContainer("gui.items.skullnaming-pet", c);
        this.items_namingContainer = CustomItemContainer.resolveItemContainer("gui.items.naming-pet", c);
        this.items_hatpetContainer = CustomItemContainer.resolveItemContainer("gui.items.hat-pet", c);
        this.items_ridingpetContainer = CustomItemContainer.resolveItemContainer("gui.items.riding-pet", c);

        this.souls_Container.put(PetType.PIG, CustomItemContainer.resolveItemContainer("gui.souls.pig-soul", c));
        this.souls_Container.put(PetType.CHICKEN, CustomItemContainer.resolveItemContainer("gui.souls.chicken-soul", c));
        this.souls_Container.put(PetType.DOG, CustomItemContainer.resolveItemContainer("gui.souls.dog-soul", c));
        this.souls_Container.put(PetType.CAT, CustomItemContainer.resolveItemContainer("gui.souls.cat-soul", c));
        this.souls_Container.put(PetType.BIRD, CustomItemContainer.resolveItemContainer("gui.souls.bird-soul", c));
        this.souls_Container.put(PetType.COW, CustomItemContainer.resolveItemContainer("gui.souls.cow-soul", c));
        this.souls_Container.put(PetType.SHEEP, CustomItemContainer.resolveItemContainer("gui.souls.sheep-soul", c));
        this.souls_Container.put(PetType.IRONGOLEM, CustomItemContainer.resolveItemContainer("gui.souls.irongolem-soul", c));
        this.souls_Container.put(PetType.DRAGON, CustomItemContainer.resolveItemContainer("gui.souls.dragon-soul", c));
        this.souls_Container.put(PetType.ZOMBIE, CustomItemContainer.resolveItemContainer("gui.souls.zombie-soul", c));
        this.souls_Container.put(PetType.CREEPER, CustomItemContainer.resolveItemContainer("gui.souls.creeper-soul", c));
        this.souls_Container.put(PetType.SKELETON, CustomItemContainer.resolveItemContainer("gui.souls.skeleton-soul", c));
        this.souls_Container.put(PetType.HORSE, CustomItemContainer.resolveItemContainer("gui.souls.horse-soul", c));
        this.souls_Container.put(PetType.SPIDER, CustomItemContainer.resolveItemContainer("gui.souls.spider-soul", c));
        this.souls_Container.put(PetType.VILLAGER, CustomItemContainer.resolveItemContainer("gui.souls.villager-soul", c));
        this.souls_Container.put(PetType.HUMAN, CustomItemContainer.resolveItemContainer("gui.souls.human-soul", c));
        this.souls_Container.put(PetType.SLIME, CustomItemContainer.resolveItemContainer("gui.souls.slime-soul", c));
        this.souls_Container.put(PetType.LAVASLIME, CustomItemContainer.resolveItemContainer("gui.souls.lavaslime-soul", c));
        this.souls_Container.put(PetType.PIGZOMBIE, CustomItemContainer.resolveItemContainer("gui.souls.pigzombie-soul", c));
        this.souls_Container.put(PetType.BAT, CustomItemContainer.resolveItemContainer("gui.souls.bat-soul", c));
        this.souls_Container.put(PetType.ENDERMAN, CustomItemContainer.resolveItemContainer("gui.souls.enderman-soul", c));
        this.souls_Container.put(PetType.SILVERFISH, CustomItemContainer.resolveItemContainer("gui.souls.silverfish-soul", c));
        this.souls_Container.put(PetType.GHAST, CustomItemContainer.resolveItemContainer("gui.souls.ghast-soul", c));
        this.souls_Container.put(PetType.BLAZE, CustomItemContainer.resolveItemContainer("gui.souls.blaze-soul", c));
        this.souls_Container.put(PetType.WITHER, CustomItemContainer.resolveItemContainer("gui.souls.wither-soul", c));

        this.general_defaultAppearanceContainer.setEnabled(true);
        this.costumes_default = this.resolveItems(c.getStringList("costumes.default"));
        this.costumes_color = this.resolveItems(c.getStringList("costumes.color"));
        this.costumes_custom = this.resolveItems(c.getStringList("costumes.custom"));
    }

    public ItemContainer getContainer(PetType petType) {
        if (this.souls_Container.containsKey(petType))
            return this.souls_Container.get(petType);
        throw new RuntimeException("Configuration is in an invalid state!");
    }

    public ItemContainer getItems_soundEnabledContainer() {
        return this.items_soundEnabledContainer;
    }

    public ItemContainer getItems_soundDisabledContainer() {
        return this.items_soundDisabledContainer;
    }

    public ItemStack[] getColoredItemStacks() {
        return this.costumes_color.clone();
    }

    public ItemStack[] getDefaultItemStacks() {
        return this.costumes_default.clone();
    }

    public ItemStack[] getCustomItemStacks() {
        return this.costumes_custom.clone();
    }

    public ItemContainer getGeneral_myContainer() {
        return this.general_myContainer;
    }

    public ItemContainer getGeneral_emptyslotContainer() {
        return this.general_emptyslotContainer;
    }

    public ItemContainer getGeneral_previouspageContainer() {
        return this.general_previouspageContainer;
    }

    public ItemContainer getGeneral_nextpageContainer() {
        return this.general_nextpageContainer;
    }

    public boolean isSettings_copyskin() {
        return this.settings_copyskin;
    }

    boolean isSettings_allowLore() {
        return this.settings_allowLore;
    }

    public boolean isSettings_clickemptyback() {
        return this.settings_clickemptyback;
    }

    public boolean isSettings_onlyDisableItem() {
        return this.settings_onlyDisableItem;
    }

    public ItemContainer getGeneral_defaultAppearanceContainer() {
        return this.general_defaultAppearanceContainer;
    }

    public ItemContainer getItems_defaultcostumeContainer() {
        return this.items_defaultcostumeContainer;
    }

    public ItemContainer getGeneral_enablePetContainer() {
        return this.general_enablePetContainer;
    }

    public ItemContainer getGeneral_disablePetContainer() {
        return this.general_disablePetContainer;
    }

    public ItemContainer getItems_colorcostumeContainer() {
        return this.items_colorcostumeContainer;
    }

    public ItemContainer getItems_customcostumeContainer() {
        return this.items_customcostumeContainer;
    }

    public ItemContainer getItems_callpetContainer() {
        return this.items_callpetContainer;
    }

    public ItemContainer getItems_particlepetContainer() {
        return this.items_particlepetContainer;
    }

    public ItemContainer getItems_cancelpetContainer() {
        return this.items_cancelpetContainer;
    }

    public ItemContainer getItems_cannonpetContainer() {
        return this.items_cannonpetContainer;
    }

    public ItemContainer getItems_skullNamingContainer() {
        return this.items_skullNamingContainer;
    }

    public ItemContainer getItems_namingContainer() {
        return this.items_namingContainer;
    }

    public ItemContainer getItems_hatpetContainer() {
        return this.items_hatpetContainer;
    }

    public ItemContainer getItems_ridingpetContainer() {
        return this.items_ridingpetContainer;
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
