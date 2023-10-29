package com.github.shynixn.petblocks.enumeration

enum class Permission(val text: String) {
    // petblocks.command is the command permission.
    HELP("petblocks.help"),
    RELOAD("petblocks.reload"),
    CREATE("petblocks.pet.create"),
    CALL("petblocks.pet.call"),
    SPAWN("petblocks.pet.spawn"), // Minimum required to have an entity spawn.
    RIDE("petblocks.pet.ride"),
    HAT("petblocks.pet.hat"),
    UNMOUNT("petblocks.pet.unmount"),
    LIST("petblocks.pet.list"),
    DELETE("petblocks.pet.delete"),
    RENAME("petblocks.pet.displayName"),
    TOGGLE("petblocks.pet.toggle"),
    DESPAWN("petblocks.pet.despawn"),
    SKIN("petblocks.pet.skin"),
    LOOKAT("petblocks.pet.lookat"),
    LOOKATOWNER("petblocks.pet.lookatowner"),
    MOVETO("petblocks.pet.moveto"),
    MOVETOOWNER("petblocks.pet.movetoowner"),
    TELEPORT("petblocks.pet.teleport"),
    VELOCITY("petblocks.pet.velocity"),
    VISIBILITY("petblocks.pet.visibility"),
    LOOP("petblocks.pet.loop"),
    TEMPLATE("petblocks.pet.settemplate"),
    MANIPULATE_OTHER("petblocks.pet.manipulateOther"),
    // Dynamic
    DYN_AMOUNT("petblocks.pet.amount."),
    DYN_TEMPLATE("petblocks.pet.template."),
}
