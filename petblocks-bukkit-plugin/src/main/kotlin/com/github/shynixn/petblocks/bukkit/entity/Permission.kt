package com.github.shynixn.petblocks.bukkit.entity

enum class Permission(val text: String) {
    CREATE("petblocks.pet.create"),
    RELOAD("petblocks.reload"),
    HELP("petblocks.help"),
    CALL("petblocks.pet.call"),
    AMOUNT("petblocks.pet.amount.1"),
    TEMPLATE("petblocks.pet.template.pet_hopping"),
    DELETE("petblocks.pet.delete"),
    DISPLAYNAME("petblocks.pet.displayName"),
    SPAWN("petblocks.pet.spawn"),
    DESPAWN("petblocks.pet.despawn"),
    VISIBILITY("petblocks.pet.visibility")
}
