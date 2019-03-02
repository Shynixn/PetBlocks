@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.core.logic.business.extension

import com.github.shynixn.petblocks.api.PetBlocksApi
import com.github.shynixn.petblocks.api.business.enumeration.ChatColor
import com.github.shynixn.petblocks.api.business.service.ConcurrencyService
import com.github.shynixn.petblocks.api.business.service.LoggingService
import com.github.shynixn.petblocks.api.persistence.entity.ChatMessage
import com.github.shynixn.petblocks.api.persistence.entity.Position
import com.github.shynixn.petblocks.api.persistence.entity.PropertyTrackable
import com.github.shynixn.petblocks.core.logic.persistence.entity.ChatMessageEntity
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KProperty

/**
 * Created by Shynixn 2018.
 * <p>
 * Version 1.2
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2018 by Shynixn
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

/**
 * Creates a new chat message.
 */
fun chatMessage(f: ChatMessage.() -> Unit): ChatMessage {
    val chatMessage = ChatMessageEntity()
    f.invoke(chatMessage)
    return chatMessage
}

/**
 * Changes the position to it's yaw front by the given amount.
 */
fun Position.relativeFront(amount: Double): Position {
    this.x = x + amount * Math.cos(Math.toRadians(yaw + 90))
    this.z = z + amount * Math.sin(Math.toRadians(yaw + 90))
    return this
}

/**
 * Gets the column value.
 */
inline fun <reified V> Map<String, Any>.getItem(key: String): V {
    val data = this[key]

    if (data is Int && V::class == Boolean::class) {
        return (this[key] == 1) as V
    }

    return this[key] as V
}

/**
 * Merges the args after the first parameter.
 *
 * @param args args
 * @return merged.
 */
fun mergeArgs(args: Array<out String>): String {
    val builder = StringBuilder()
    for (i in 1 until args.size) {
        if (builder.isNotEmpty()) {
            builder.append(' ')
        }

        builder.append(args[i])
    }
    return builder.toString()
}

/**
 * Executes the given [f] via the [concurrencyService] synchronized with the server tick.
 */
inline fun sync(
    concurrencyService: ConcurrencyService,
    delayTicks: Long = 0L,
    repeatingTicks: Long = 0L,
    crossinline f: () -> Unit
) {
    concurrencyService.runTaskSync(delayTicks, repeatingTicks) {
        f.invoke()
    }
}

/**
 * Gets if the given property has changed.
 */
fun <R> KProperty<R>.hasChanged(instance: PropertyTrackable): Boolean {
    val hasChanged = instance.propertyTracker.hasChanged(this)
    instance.propertyTracker.onPropertyChanged(this, false)
    return hasChanged
}

/**
 * Executes the given [f] via the [concurrencyService] asynchronous.
 */
inline fun async(
    concurrencyService: ConcurrencyService,
    delayTicks: Long = 0L,
    repeatingTicks: Long = 0L,
    crossinline f: () -> Unit
) {
    concurrencyService.runTaskAsync(delayTicks, repeatingTicks) {
        f.invoke()
    }
}

/**
 * Accepts the action safely.
 */
fun <T> CompletableFuture<T>.thenAcceptSafely(f: (T) -> Unit) {
    this.thenAccept(f).exceptionally { e ->
        PetBlocksApi.resolve<LoggingService>(LoggingService::class.java).error("Failed to execute Task.", e)
        throw RuntimeException(e)
    }
}

/**
 * Translates the given chatColor.
 */
fun String.translateChatColors(): String {
    return ChatColor.translateChatColorCodes('&', this)
}