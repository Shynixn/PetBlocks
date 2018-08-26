package com.github.shynixn.petblocks.core.logic.persistence.entity

import com.github.shynixn.petblocks.api.business.enumeration.ChatClickAction
import com.github.shynixn.petblocks.api.business.enumeration.ChatColor
import com.github.shynixn.petblocks.api.persistence.entity.ChatMessage
import com.github.shynixn.petblocks.api.persistence.entity.ChatMessageComponent
import com.github.shynixn.petblocks.core.logic.business.extension.translateChatColors

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
class ChatMessageComponentEntity(private val parent: ChatMessage) : ChatMessageComponent {
    /**
     * Returns all components of this message.
     * Can contain string, ChatColors or ChatMessages.
     */
    override val components: List<Any> = ArrayList()
    private val text = StringBuilder()
    private var clickAction: ChatClickAction? = null
    private var clickActionData: String? = null
    private var hoverActionData: ChatMessageComponentEntity? = null

    /**
     * Appends a text to this message with the last formatting type.
     */
    override fun text(f: (ChatMessage) -> String) {
        text.append(f.invoke(this))
    }

    /**
     * Sets this component italic.
     */
    override fun italic(f: (ChatMessage) -> Unit) {
        this.text.append(ChatColor.ITALIC)
        f.invoke(this)
    }

    /**
     * Sets this component bold.
     */
    override fun bold(f: (ChatMessage) -> Unit) {
        this.text.append(ChatColor.BOLD)
        f.invoke(this)
    }

    /**
     * Sets this component underlined.
     */
    override fun underline(f: (ChatMessage) -> Unit) {
        this.text.append(ChatColor.UNDERLINE)
        f.invoke(this)
    }

    /**
     * Sets this component strikeThrough.
     */
    override fun strikeThrough(f: (ChatMessage) -> Unit) {
        this.text.append(ChatColor.STRIKETHROUGH)
        f.invoke(this)
    }

    /**
     * Sets this component color.
     */
    override fun color(color: ChatColor, f: (ChatMessage) -> Unit) {
        this.text.append(color)
        f.invoke(this)
    }

    /**
     * Creates a sub component.
     */
    override fun component(f: (ChatMessageComponent) -> Unit) {
        parent.component(f)
    }

    /**
     * Adds a clickable component.
     */
    override fun clickAction(f: (ChatMessageComponent) -> Pair<ChatClickAction, String>) {
        val data = f.invoke(this)
        this.clickAction = data.first
        this.clickActionData = data.second
    }

    /**
     * Sets the hover able component.
     */
    override fun hover(f: (ChatMessageComponent) -> Unit) {
        this.hoverActionData = ChatMessageComponentEntity(this)
        f.invoke(hoverActionData!!)
    }

    /**
     * String override.
     */
    override fun toString(): String {
        val builder = StringBuilder()

        builder.append("{ \"text\": \"")
        builder.append(this.text.toString().translateChatColors())
        builder.append('"')

        if (this.clickAction != null) {
            builder.append(", \"clickEvent\": {\"action\": \"")
            builder.append(this.clickAction!!.name.toLowerCase())
            builder.append("\" , \"value\" : \"")
            builder.append(this.clickActionData)
            builder.append("\"}")
        }

        if (this.hoverActionData != null) {
            builder.append(", \"hoverEvent\": {\"action\": \"")
            builder.append("show_text")
            builder.append("\" , \"value\" : ")
            builder.append(this.hoverActionData.toString())
            builder.append('}')
        }

        builder.append('}')
        return builder.toString()
    }
}