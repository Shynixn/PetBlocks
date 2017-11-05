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
    ALLPETTYPES("petblocks.pet.type.all"),
    SINGLEPETTYPE("petblocks.pet.type."),
    RENAMEPET("petblocks.pet.rename"),
    RENAMESKULL("petblocks.pet.skin"),
    CANNON("petblocks.pet.cannon"),
    RIDEPET("petblocks.pet.ride"),
    WEARPET("petblocks.pet.wear"),
    ALLDEFAULTCOSTUMES("petblocks.pet.defaultcostumes.all"),
    SINGLEDEFAULTCOSTUME("petblocks.pet.defaultcostumes."),
    ALLCOLORCOSTUMES("petblocks.pet.colorcostumes.all"),
    SINGLECOLORCOSTUME("petblocks.pet.colorcostumes."),
    ALLCUSTOMCOSTUMES("petblocks.pet.customcostumes.all"),
    SINGLECUSTOMCOSTUME("petblocks.pet.customcostumes."),
    ALLMINECRAFTHEADSCOSTUMES("petblocks.pet.minecraft-heads-costumes.all"),
    SINGLEMINECRAFTHEADSCOSTUME("petblocks.pet.minecraft-heads-costumes."),
    ALLHEADATABASECOSTUMES("petblocks.pet.head-database-costumes.all"),
    ALLPARTICLES("petblocks.pet.particles.all"),
    SINGLEPARTICLE("petblocks.pet.particles."),
    OWNINGAMECOSTUMES("petblocks.inventory.costume");

    private final String perm;

    /**
     * Initializes a new permission.
     *
     * @param perm permission
     */
    Permission(String perm) {
        this.perm = perm;
    }

    /**
     * Returns the permission string.
     *
     * @return permission
     */
    public String get() {
        return this.perm;
    }
}
