package com.github.shynixn.petblocks.sponge.nms.v1_12_R1;

import net.minecraft.anchor.v1_12_mcpR1.entity.player.EntityPlayerMP;
import net.minecraft.anchor.v1_12_mcpR1.nbt.NBTTagCompound;
import net.minecraft.anchor.v1_12_mcpR1.nbt.NBTTagList;
import net.minecraft.anchor.v1_12_mcpR1.network.play.client.CPacketChatMessage;
import net.minecraft.anchor.v1_12_mcpR1.network.play.server.SPacketChat;
import net.minecraft.anchor.v1_12_mcpR1.util.text.ChatType;
import net.minecraft.anchor.v1_12_mcpR1.util.text.ITextComponent;
import net.minecraft.anchor.v1_12_mcpR1.util.text.TextComponentBase;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.property.ProfileProperty;

import java.awt.*;
import java.util.Base64;
import java.util.Iterator;
import java.util.UUID;

/**
 * Created by Shynixn 2018.
 * <p>
 * Version 1.2
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2018 by Shynixn
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
public class NMSUtils {

    /**
     * Sets the item damage of the given itemstack.
     *
     * @param itemStack itemstack
     * @param damage    damageValue
     */
    public static void setItemDamage(ItemStack itemStack, int damage) {
        ((net.minecraft.anchor.v1_12_mcpR1.item.ItemStack) (Object) itemStack).setItemDamage(damage);
    }

    /**
     * Sets the item skin by the given owner name.
     *
     * @param itemStack itemstakc
     * @param owner     name
     */
    public static void setItemOwner(ItemStack itemStack, String owner) {
        final net.minecraft.anchor.v1_12_mcpR1.item.ItemStack item = ((net.minecraft.anchor.v1_12_mcpR1.item.ItemStack) (Object) itemStack);

        NBTTagCompound compound = item.getTagCompound();
        if (compound == null) {
            compound = new NBTTagCompound();
        }

        compound.setString("SkullOwner", owner);
        item.setTagCompound(compound);
    }

    /**
     * Sends the given json message to the given players.
     * @param message message
     * @param players players
     */
    public static void sendJsonChatMessage(String message, Player[] players) {
        final ITextComponent component = ITextComponent.Serializer.jsonToComponent(message);
        final SPacketChat packetChatMessage = new SPacketChat(component, ChatType.CHAT);

        for (final Player player : players) {
            ((EntityPlayerMP) player).connection.sendPacket(packetChatMessage);
        }
    }

    /**
     * Updates the inventory of the player.
     *
     * @param player player
     */
    public static void updateInventoryFor(Player player) {
        ((EntityPlayerMP) player).sendContainerToPlayer(((EntityPlayerMP) player).openContainer);
    }

    /**
     * Sets the item skin by the given skinUrl.
     *
     * @param itemStack itemstack
     * @param skinUrl   skinUrl
     */
    public static void setItemSkin(ItemStack itemStack, String skinUrl) {
        final net.minecraft.anchor.v1_12_mcpR1.item.ItemStack item = ((net.minecraft.anchor.v1_12_mcpR1.item.ItemStack) (Object) itemStack);

        NBTTagCompound compound = item.getTagCompound();
        if (compound == null) {
            compound = new NBTTagCompound();
        }

        final GameProfile newSkinProfile = Sponge.getServer().getGameProfileManager().createProfile(UUID.randomUUID(), null);
        final ProfileProperty profileProperty = Sponge.getServer().getGameProfileManager().createProfileProperty("textures", Base64.getEncoder().encodeToString(("{textures:{SKIN:{url:\"" + skinUrl + "\"}}}").getBytes()), null);
        newSkinProfile.getPropertyMap().put("textures", profileProperty);

        NBTTagCompound nbttagcompound = new NBTTagCompound();
        nbttagcompound = writeGameProfile(nbttagcompound, newSkinProfile);
        compound.setTag("SkullOwner", nbttagcompound);
        item.setTagCompound(compound);
    }

    private static NBTTagCompound writeGameProfile(NBTTagCompound tagCompound, GameProfile profile) {
        if (profile.getName().isPresent()) {
            tagCompound.setString("Name", profile.getName().get());
        }

        if (profile.getUniqueId() != null) {
            tagCompound.setString("Id", profile.getUniqueId().toString());
        }

        if (!profile.getPropertyMap().isEmpty()) {
            final NBTTagCompound nbttagcompound = new NBTTagCompound();
            final Iterator var3 = profile.getPropertyMap().keySet().iterator();

            while (var3.hasNext()) {
                final String s = (String) var3.next();
                final NBTTagList nbttaglist = new NBTTagList();

                NBTTagCompound nbttagcompound1;
                for (final Iterator var6 = profile.getPropertyMap().get(s).iterator(); var6.hasNext(); nbttaglist.appendTag(nbttagcompound1)) {
                    final ProfileProperty property = (ProfileProperty) var6.next();
                    nbttagcompound1 = new NBTTagCompound();
                    nbttagcompound1.setString("Value", property.getValue());
                    if (property.hasSignature()) {
                        nbttagcompound1.setString("Signature", property.getSignature().get());
                    }
                }

                nbttagcompound.setTag(s, nbttaglist);
            }

            tagCompound.setTag("Properties", nbttagcompound);
        }

        return tagCompound;
    }
}
