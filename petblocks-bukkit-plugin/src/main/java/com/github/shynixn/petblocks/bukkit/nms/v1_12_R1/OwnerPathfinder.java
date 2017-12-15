package com.github.shynixn.petblocks.bukkit.nms.v1_12_R1;

import com.github.shynixn.petblocks.api.business.entity.PetBlock;
import com.github.shynixn.petblocks.bukkit.logic.business.configuration.ConfigPet;
import com.github.shynixn.petblocks.bukkit.nms.helper.PetBlockHelper;
import net.minecraft.server.v1_12_R1.EntityInsentient;
import net.minecraft.server.v1_12_R1.PathEntity;
import net.minecraft.server.v1_12_R1.PathfinderGoal;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Pathfinder for the PetBlock.
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
public final class OwnerPathfinder extends PathfinderGoal {
    private final EntityInsentient entity;
    private PathEntity path;
    private final Player player;
    private final PetBlock petBlock;

    private int counter2;
    private int counter;

    /**
     * Initializes a new petblock owner pathfinder.
     *
     * @param entityCreature creatureUsingPath
     * @param petBlock       petblock
     */
    public OwnerPathfinder(EntityInsentient entityCreature, PetBlock petBlock) {
        super();
        this.entity = entityCreature;
        this.player = (Player) petBlock.getPlayer();
        this.petBlock = petBlock;
    }

    /**
     * Calculates the navigation path and returns true if found.
     *
     * @return success
     */
    @Override
    public boolean a() {
        if (this.player == null) {
            return this.path != null;
        }
        if (!this.entity.getWorld().getWorldData().getName().equals(this.player.getWorld().getName())) {
            this.entity.getBukkitEntity().teleport(this.player.getLocation());
        } else if (this.entity.getBukkitEntity().getLocation().distance(this.player.getLocation()) > ConfigPet.getInstance().getBlocksAwayFromPlayer()) {
            this.counter2 = PetBlockHelper.afraidWaterEffect(this.petBlock, this.counter2);
            final Location targetLocation = this.player.getLocation();
            this.entity.getNavigation().o();
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
                this.entity.getBukkitEntity().setVelocity(new Vector(0.1, ConfigPet.getInstance().getModifier_petclimbing() * 0.1, 0.1));
                this.counter = 0;
            }
            if (this.path != null) {
                this.c();
            }
        }
        return this.path != null;
    }

    /**
     * Abstract navigation.
     */
    @Override
    public void c() {
        if (this.entity instanceof CustomRabbit) {
            this.entity.getNavigation().a(this.path, 2.5D);
        } else {
            this.entity.getNavigation().a(this.path, 1D);
        }
    }
}
