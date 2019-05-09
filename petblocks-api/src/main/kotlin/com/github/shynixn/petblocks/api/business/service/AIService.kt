package com.github.shynixn.petblocks.api.business.service

import com.github.shynixn.petblocks.api.business.proxy.AICreationProxy
import com.github.shynixn.petblocks.api.persistence.entity.AIBase
import java.util.function.Function

interface AIService {
    /**
     * Registers a custom ai type with unique [type] and a proxy to create required AI actions.
     * Existing types can be overwritten if the given [type] already exists.
     */
    fun <A : AIBase> registerSerializationProxy(type: String, creator: AICreationProxy<A>)

    /**
     * Registers a custom ai type with unique [type] which gets created by the [onCreate]
     * function, serialized by [onSerialize] and deSerialized by [nDeserialize].
     * Existing types can be overwritten if the given [type] already exists.
     */
    fun <A : AIBase> registerSerializationProxy(
        type: String,
        onCreate: Function<A, Any?>,
        onSerialize: Function<A, Map<String, Any?>>,
        onDeserialize: Function<Map<String, Any?>, A>
    )
}