package com.github.shynixn.petblocks.entity

import com.fasterxml.jackson.annotation.JsonProperty
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
    @JsonProperty("events")
    var events: HashMap<String, PetActionDefinition> = hashMapOf()

    /**
     * Loops.
     */
    @JsonProperty("loops")
    var loops: HashMap<String, PetActionDefinition> = hashMapOf()
}


