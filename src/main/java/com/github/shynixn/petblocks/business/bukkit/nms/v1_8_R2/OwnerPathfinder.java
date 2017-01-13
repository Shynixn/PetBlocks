package com.github.shynixn.petblocks.business.bukkit.nms.v1_8_R2;

import com.github.shynixn.petblocks.business.logic.configuration.ConfigPet;
import com.github.shynixn.petblocks.business.bukkit.nms.helper.PetBlockHelper;
import net.minecraft.server.v1_8_R2.EntityInsentient;
import net.minecraft.server.v1_8_R2.NBTTagCompound;
import net.minecraft.server.v1_8_R2.PathEntity;
import net.minecraft.server.v1_8_R2.PathfinderGoal;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Map;

public final class OwnerPathfinder extends PathfinderGoal {
    private final EntityInsentient entity;
    private PathEntity path;
    private final Player player;
    private int counter;
    private int counter2;

    public OwnerPathfinder(EntityInsentient entitycreature, Player player) {
        this.entity = entitycreature;
        this.player = player;
    }

    @Override
    public boolean a() {
        if (this.player == null) {
            return this.path != null;
        }
        if (!this.entity.getWorld().getWorldData().getName().equals(this.player.getWorld().getName())) {
            this.entity.getBukkitEntity().teleport(this.player.getLocation());
        } else if (this.entity.getBukkitEntity().getLocation().distance(this.player.getLocation()) > 2) {
            this.counter2 = PetBlockHelper.afraidWaterEffect(this.entity.getBukkitEntity(), this.counter2);
            Location targetLocation = this.player.getLocation();
            this.entity.getNavigation().m();
            this.entity.getNavigation();
            this.path = this.entity.getNavigation().a(targetLocation.getX() + 1, targetLocation.getY(), targetLocation.getZ() + 1);
            this.entity.getNavigation();
            if (this.entity.getBukkitEntity().getLocation().distance(this.player.getLocation()) > ConfigPet.getInstance().getFollow_maxRangeTeleport())
                this.entity.getBukkitEntity().teleport(this.player.getLocation());
            if (Math.abs(this.entity.getBukkitEntity().getLocation().getY() - targetLocation.getY()) >= 2) {
                this.counter++;
            } else {
                this.counter = 0;
            }
            if (this.counter > 5) {
                this.entity.getBukkitEntity().setVelocity(new Vector(0.1, (float) ConfigPet.getInstance().getModifier_petclimbing() * 0.1, 0.1));
                this.counter = 0;
            }
            if (this.path != null) {
                this.c();
            }
        }
        return this.path != null;
    }

    @Override
    public void c() {
        this.entity.getNavigation().a(this.path, 1D);
    }

    public static boolean isUnbreakable(ItemStack itemStack) {
        net.minecraft.server.v1_8_R2.ItemStack stack = CraftItemStack.asNMSCopy(itemStack);
        return stack.getTag() != null && stack.getTag().hasKey("Unbreakable") && stack.getTag().getBoolean("Unbreakable");
    }

    public static ItemStack setItemstackTag(ItemStack itemStack, Map<String, Object> tags) {
        net.minecraft.server.v1_8_R2.ItemStack stack = CraftItemStack.asNMSCopy(itemStack);
        for (String tag : tags.keySet()) {
            NBTTagCompound nbtTagCompound;
            if (stack.getTag() == null)
                nbtTagCompound = new NBTTagCompound();
            else
                nbtTagCompound = stack.getTag();
            if (tags.get(tag) instanceof String)
                nbtTagCompound.setString(tag, (String) tags.get(tag));
            else if (tags.get(tag) instanceof Boolean)
                nbtTagCompound.setBoolean(tag, (Boolean) tags.get(tag));
            else if (tags.get(tag) instanceof Integer)
                nbtTagCompound.setInt(tag, (Integer) tags.get(tag));
            else if (tags.get(tag) instanceof Float)
                nbtTagCompound.setFloat(tag, (Float) tags.get(tag));
            else if (tags.get(tag) instanceof Double)
                nbtTagCompound.setDouble(tag, (Double) tags.get(tag));
            else if (tags.get(tag) instanceof Long)
                nbtTagCompound.setLong(tag, (Long) tags.get(tag));
            else if (tags.get(tag) instanceof Byte)
                nbtTagCompound.setByte(tag, (Byte) tags.get(tag));
            stack.setTag(nbtTagCompound);
        }
        return CraftItemStack.asCraftMirror(stack);
    }
}
