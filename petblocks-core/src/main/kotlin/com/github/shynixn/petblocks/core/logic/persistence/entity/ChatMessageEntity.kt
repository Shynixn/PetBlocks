package com.github.shynixn.petblocks.core.logic.persistence.entity

import com.github.shynixn.petblocks.api.business.enumeration.ChatColor
import com.github.shynixn.petblocks.api.persistence.entity.ChatMessage
import com.github.shynixn.petblocks.api.persistence.entity.ChatMessageComponent

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
class ChatMessageEntity : ChatMessage {
    /**
     * Components.
     */
    override val components = ArrayList<Any>()

    /**
     * Appends a text to the chatMessage and returns the same builder instance.
     */
    override fun append(text: String): ChatMessage {
        this.components.add(text)
        return this
    }

    /**
     * Appends a chat color and returns the same builder instance.
     */
    override fun append(chatColor: ChatColor): ChatMessage {
        this.components.add(chatColor)
        return this
    }

    /**
     * Appends a new component to the chat message and returns the instance.
     */
    override fun appendComponent(): ChatMessageComponent {
        val component = ChatMessageComponentEntity(this)
        this.components.add(component)
        return component
    }

    /**
     * Appends a text to this message with the last formatting type.
     */
    override fun text(f: (ChatMessage) -> String) {
        this.append(f.invoke(this))
    }

    /**
     * Sets this component italic.
     */
    override fun italic(f: (ChatMessage) -> Unit) {
        this.append(ChatColor.ITALIC)
        f.invoke(this)
    }

    /**
     * Sets this component bold.
     */
    override fun bold(f: (ChatMessage) -> Unit) {
        this.append(ChatColor.BOLD)
        f.invoke(this)
    }

    /**
     * Sets this component underlined.
     */
    override fun underline(f: (ChatMessage) -> Unit) {
        this.append(ChatColor.UNDERLINE)
        f.invoke(this)
    }

    /**
     * Sets this component strikeThrough.
     */
    override fun strikeThrough(f: (ChatMessage) -> Unit) {
        this.append(ChatColor.STRIKETHROUGH)
        f.invoke(this)
    }

    /**
     * Sets this component color.
     */
    override fun color(color: ChatColor, f: (ChatMessage) -> Unit) {
        this.append(color)
        f.invoke(this)
    }

    /**
     * Creates a sub component.
     */
    override fun component(f: (ChatMessageComponent) -> Unit) {
        val component = ChatMessageComponentEntity(this)
        f.invoke(component)
        this.components.add(component)
    }
}