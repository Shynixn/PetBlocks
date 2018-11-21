package com.github.shynixn.petblocks.api.persistence.entity

import com.github.shynixn.petblocks.api.business.enumeration.ChatColor

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
interface ChatMessage {
    /**
     * Appends a text to the chatMessage and returns the same builder instance.
     */
    fun append(text: String): ChatMessage

    /**
     * Appends a chat color and returns the same builder instance.
     */
    fun append(chatColor: ChatColor): ChatMessage

    /**
     * Appends a new component to the chat message and returns the instance.
     */
    fun appendComponent(): ChatMessageComponent

    /**
     * Appends a text to this message with the last formatting type.
     */
    fun text(f: (ChatMessage) -> String)

    /**
     * Sets this component italic.
     */
    fun italic(f: (ChatMessage) -> Unit)

    /**
     * Creates a sub component.
     */
    fun component(f: ChatMessageComponent.() -> Unit)

    /**
     * Sets this component bold.
     */
    fun bold(f: (ChatMessage) -> Unit)

    /**
     * Sets this component underlined.
     */
    fun underline(f: (ChatMessage) -> Unit)

    /**
     * Sets this component strikeThrough.
     */
    fun strikeThrough(f: (ChatMessage) -> Unit)

    /**
     * Sets this component color.
     */
    fun color(color: ChatColor, f: (ChatMessage) -> Unit)

    /**
     * Returns all components of this message.
     * Can contain string, ChatColors or ChatMessages.
     */
    val components: List<Any>
}