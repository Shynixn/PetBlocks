package com.github.shynixn.petblocks.bukkit.nms.v1_9_R2;

import com.github.shynixn.petblocks.api.business.entity.PetBlock;
import com.github.shynixn.petblocks.bukkit.nms.helper.PetBlockHelper;
import com.github.shynixn.petblocks.bukkit.logic.business.configuration.ConfigPet;
import net.minecraft.server.v1_9_R2.EntityInsentient;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import net.minecraft.server.v1_9_R2.PathEntity;
import net.minecraft.server.v1_9_R2.PathfinderGoal;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
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
    private final PetBlock petBlock;

    public OwnerPathfinder(EntityInsentient entitycreature, PetBlock petBlock) {
        super();
        this.entity = entitycreature;
        this.player = (Player) petBlock.getPlayer();
        this.petBlock = petBlock;
    }

    @Override
    public boolean a() {
        if (this.player == null) {
            return this.path != null;
        }
        if (!this.entity.getWorld().getWorldData().getName().equals(this.player.getWorld().getName())) {
            this.entity.getBukkitEntity().teleport(this.player.getLocation());
        } else if (this.entity.getBukkitEntity().getLocation().distance(this.player.getLocation()) > ConfigPet.getInstance().getBlocksAwayFromPlayer()) {
            this.counter2 = PetBlockHelper.afraidWaterEffect(petBlock, this.counter2);
            final Location targetLocation = this.player.getLocation();
            this.entity.getNavigation().n();
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
        if (this.entity instanceof CustomRabbit)
            this.entity.getNavigation().a(this.path, 2.5D);
        else
            this.entity.getNavigation().a(this.path, 1D);
    }
}
