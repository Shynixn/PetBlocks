package com.github.shynixn.petblocks.bukkit.entity

enum class Permission(val text: String) {
    COMMAND("petblocks.commmand"),
    CREATE("petblocks.pet.create"),
    RELOAD("petblocks.reload"),
    HELP("petblocks.help"),
    CALL("petblocks.pet.call"),
    RIDE("petblocks.pet.ride"),
    FLY("petblocks.pet.fly"),
    HAT("petblocks.pet.hat"),
    UNMOUNT("petblocks.pet.unmount"),
    LIST("petblocks.pet.list"),
    DELETE("petblocks.pet.delete"),
    DISPLAYNAME("petblocks.pet.displayName"),
    SPAWN("petblocks.pet.spawn"),
    DESPAWN("petblocks.pet.despawn"),
    SKIN("petblocks.pet.skin"),


    VISIBILITY("petblocks.pet.visibility"),
    AMOUNT("petblocks.pet.amount.1"),
    TEMPLATE("petblocks.pet.template.pet_hopping"),
}
