package com.github.shynixn.petblocks.core.logic.persistence.entity

import com.fasterxml.jackson.annotation.JsonProperty

class PlayerDataEntity() {
    /**
     * Database Id.
     */
    var databaseId: Int = 0

    /**
     * Non Unique Username.
     */
    @JsonProperty("name")
    var name: String = ""

    /**
     * Unique Username.
     */
    @JsonProperty("uuid")
    var uuid: String = ""

    constructor(f: PlayerDataEntity.() -> Unit) : this() {
        f.invoke(this)
    }
}
