package com.github.shynixn.petblocks.api.business.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

interface CoroutineSessionService {
    /**
     * Launch.
     */
    fun launch(dispatcher: CoroutineContext, f: suspend CoroutineScope.() -> Unit): Job

    /**
     * Minecraft.
     */
    val minecraftDispatcher: CoroutineContext

    /**
     * Async.
     */
    val asyncDispatcher: CoroutineContext
}
