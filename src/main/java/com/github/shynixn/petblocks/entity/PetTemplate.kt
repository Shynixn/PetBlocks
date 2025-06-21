package com.github.shynixn.petblocks.entity

import com.github.shynixn.mcutils.common.repository.Element

class PetTemplate : Element {

    /**
     * Unique Identifier.
     */
    override var name: String = "template"

    /**
     * Wrapper.
     */
    var pet : PetTemplatePet = PetTemplatePet()

    /**
     * Event actions.
     */
    var events: HashMap<String, PetActionDefinition> = hashMapOf()

    /**
     * Loops.
     */
    var loops: HashMap<String, PetActionDefinition> = hashMapOf()
}


