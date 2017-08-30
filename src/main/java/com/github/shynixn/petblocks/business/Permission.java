package com.github.shynixn.petblocks.business;

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

    Permission(String perm) {
        this.perm = perm;
    }

    public String get() {
        return this.perm;
    }
}
