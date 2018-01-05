package com.github.shynixn.petblocks.api.business.enumeration;

/**
 * List of permissions inside of the PetBlocks plugin.
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
public enum Permission {
    ALL_ENGINES("petblocks.pet.type.all", "petblocks.selection.engines.all"),
    SINGLE_ENGINE("petblocks.pet.type.$0", "petblocks.selection.engines.$0"),
    ALL_SIMPLEBLOCKCOSTUMES("petblocks.pet.defaultcostumes.all", "petblocks.selection.simpleblockcostumes.all"),
    SINGLE_SIMPLEBLOCKCOSTUME("petblocks.pet.defaultcostumes.$0", "petblocks.selection.simpleblockcostumes.$0"),
    ALL_COLOREDBLOCKCOSTUMES("petblocks.pet.colorcostumes.all", "petblocks.selection.coloredblockcostumes.all"),
    SINGLE_COLOREDBLOCKCOSTUME("petblocks.pet.colorcostumes.$0", "petblocks.selection.coloredblockcostumes.$0"),
    ALL_PLAYERHEADCOSTUMES("petblocks.pet.customcostumes.all", "petblocks.selection.playerheadcostumes.all"),
    SINGLE_PLAYERHEADCOSTUME("petblocks.pet.customcostumes.$0", "petblocks.selection.playerheadcostumes.$0"),
    ALL_MINECRAFTHEADCOSTUMES("petblocks.pet.minecraft-heads-costumes.all", "petblocks.selection.petcostumes.all"),
    SINGLE_MINECRAFTHEADCOSTUME("petblocks.pet.minecraft-heads-costumes.$0", "petblocks.selection.petcostumes.$0"),
    ALL_HEADDATABASECOSTUMES("petblocks.pet.head-database-costumes.all", "petblocks.selection.headdatabasecostumes.all"),
    ALL_PARTICLES("petblocks.pet.particles.all", "petblocks.selection.particles.all"),
    SINGLE_PARTICLE("petblocks.pet.particles.$0", "petblocks.selection.particles.$0"),

    ACTION_RENAME("petblocks.pet.rename", "petblocks.action.rename"),
    ACTION_CUSTOMSKULL("petblocks.pet.skin", "petblocks.action.customskin"),
    ACTION_CANNON("petblocks.pet.cannon", "petblocks.action.cannon"),
    ACTION_RIDE("petblocks.pet.ride", "petblocks.action.ride"),
    ACTION_WEAR("petblocks.pet.wear", "petblocks.action.wear");

    private final String[] perm;

    /**
     * Initializes a new permission.
     *
     * @param perm permission
     */
    Permission(String... perm) {
        this.perm = perm;
    }

    /**
     * Returns all permission in an array.
     *
     * @return permission
     */
    public String[] getPermission() {
        return this.perm.clone();
    }

    /**
     * Returns the permission string.
     *
     * @return permission
     */
    @Deprecated
    public String get() {
        return this.perm[0];
    }
}
