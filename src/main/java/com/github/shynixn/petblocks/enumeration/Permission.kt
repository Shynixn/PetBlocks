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
    SKIN_HEADDATABASE("petblocks.pet.skin.headDataBase"),
    LOOKAT("petblocks.pet.lookat"),
    LOOKATOWNER("petblocks.pet.lookatOwner"),
    MOVETO("petblocks.pet.moveto"),
    MOVETOOWNER("petblocks.pet.movetoOwner"),
    TELEPORT("petblocks.pet.teleport"),
    VELOCITY("petblocks.pet.velocity"),
    VISIBILITY("petblocks.pet.visibility"),
    LOOP("petblocks.pet.loop"),
    TEMPLATE("petblocks.pet.setTemplate"),
    MANIPULATE_OTHER("petblocks.pet.manipulateOther"),
    SELECT("petblocks.pet.select"),
    OPEN_HEADDATABSE("petblocks.pet.openHeadDatabase"),
    BREAK_BLOCK("petblocks.pet.breakBlock"),
    // Dynamic
    DYN_AMOUNT("petblocks.pet.amount."),
    DYN_TEMPLATE("petblocks.pet.template."),
}
