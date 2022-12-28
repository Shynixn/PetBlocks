package com.github.shynixn.petblocks.bukkit.entity

enum class Permission(val text: String) {
    COMMAND("petblocks.commmand"),
    CREATE("petblocks.pet.create"),
    RELOAD("petblocks.reload"),
    HELP("petblocks.help"),
    CALL("petblocks.pet.call"),
    AMOUNT("petblocks.pet.amount.1"),
    LIST("petblocks.pet.list"),
    TEMPLATE("petblocks.pet.template.pet_hopping"),
    DELETE("petblocks.pet.delete"),
    DISPLAYNAME("petblocks.pet.displayName"),
    SPAWN("petblocks.pet.spawn"),
    DESPAWN("petblocks.pet.despawn"),
    VISIBILITY("petblocks.pet.visibility"),
    SKIN("petblocks.pet.skin")
}
