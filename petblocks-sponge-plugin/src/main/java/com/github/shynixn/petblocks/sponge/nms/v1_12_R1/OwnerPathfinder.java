package com.github.shynixn.petblocks.sponge.nms.v1_12_R1;

import com.flowpowered.math.vector.Vector3d;
import com.github.shynixn.petblocks.api.business.entity.PetBlock;
import com.github.shynixn.petblocks.sponge.logic.compatibility.PetBlockExtensionsKt;
import com.github.shynixn.petblocks.sponge.logic.compatibility.Config;
import net.minecraft.anchor.v1_12_mcpR1.entity.EntityLiving;
import net.minecraft.anchor.v1_12_mcpR1.entity.ai.EntityAIBase;
import net.minecraft.anchor.v1_12_mcpR1.pathfinding.Path;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;

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
public final class OwnerPathfinder extends EntityAIBase {
    private final EntityLiving entity;
    private Path path;
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
    public OwnerPathfinder(EntityLiving entityCreature, PetBlock petBlock) {
        super();
        this.entity = entityCreature;
        this.player = (Player) petBlock.getPlayer();
        this.petBlock = petBlock;
    }

    /**
     * Abstract navigation.
     */
    @Override
    public void startExecuting() {
        if (this.entity instanceof CustomRabbit) {
            this.entity.getNavigator().setPath(this.path, 2.5D);
        } else {
            this.entity.getNavigator().setPath(this.path, 1D);
        }
    }

    /**
     * Calculates the navigation path and returns true if found.
     *
     * @return success
     */
    @Override
    public boolean shouldExecute() {
        if (this.player == null) {
            return this.path != null;
        }

        if (!this.entity.getEntityWorld().getWorldInfo().getWorldName().equals(this.player.getWorld().getName())) {
            ((Living) this.entity).setLocation(this.player.getLocation());
        } else if (this.isAway()) {
            this.counter2 =    PetBlockExtensionsKt.playAfraidOfWaterEffect(this.petBlock, this.counter2);
            final Location targetLocation = this.player.getLocation();
            this.entity.getNavigator().noPath();
            this.entity.getNavigator();
            this.path = this.entity.getNavigator().getPathToXYZ(targetLocation.getX() + 1, targetLocation.getY(), targetLocation.getZ() + 1);
            this.entity.getNavigator();
            if (this.isFarWay()) {
                ((Living) this.entity).setLocation(this.player.getLocation());
            }
            if (Math.abs(((Living) this.entity).getLocation().getY() - targetLocation.getY()) >= 2) {
                this.counter++;
            } else {
                this.counter = 0;
            }
            if (this.counter > 5) {
                ((Living) this.entity).setVelocity(new Vector3d(0.1, Config.INSTANCE.getModifier_petclimbing() * 0.1, 0.1));
                this.counter = 0;
            }
            if (this.path != null) {
                this.startExecuting();
            }
        }
        return this.path != null;
    }

    private boolean isFarWay() {
        final Living living = (Living) this.entity;
        return living.getLocation().getPosition().distance(this.player.getLocation().getPosition()) > Config.INSTANCE.getFollow_maxRangeTeleport();
    }

    private boolean isAway() {
        final Living living = (Living) this.entity;
        return living.getLocation().getPosition().distance(this.player.getLocation().getPosition()) > Config.INSTANCE.getBlocksAwayFromPlayer();
    }
}
