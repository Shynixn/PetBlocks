package com.github.shynixn.petblocks.bukkit.entity

class PetTemplate {
    /**
     * Unique Identifier.
     */
    var id: String = "template"

    /**
     * DisplayName of the pet.
     */
    var displayName: String = ""

    /**
     * All RightClick actions.
     */
    var rightClickDefinition: PetActionDefinition = PetActionDefinition()

    /**
     * All loop actions.
     */
    var loopDefinition: PetActionDefinition = PetActionDefinition()
}
