@file:Suppress("UnstableApiUsage")

package com.github.shynixn.petblocks.sponge.logic.business.serializer

import com.github.shynixn.petblocks.api.business.serializer.ItemStackSerializer
import com.google.common.reflect.TypeToken
import ninja.leaping.configurate.hocon.HoconConfigurationLoader
import org.spongepowered.api.item.inventory.ItemStack
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.StringReader
import java.io.StringWriter
import java.lang.RuntimeException

/**
 * Created by Shynixn 2019.
 * <p>
 * Version 1.2
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2019 by Shynixn
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
class ItemStackSerializerImpl : ItemStackSerializer {
    /**
     * Gets called on serialization.
     */
    override fun onSerialization(item: Any): Any {
        try {
            val sink = StringWriter()
            val loader = HoconConfigurationLoader.builder().setSink { BufferedWriter(sink) }.build()
            val node = loader.createEmptyNode()
            node.setValue<ItemStack>(TypeToken.of(ItemStack::class.java), item as ItemStack)
            loader.save(node)
            val map = HashMap<String, Any>()
            map["item"] = sink.toString()
            return map
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException(e)
        }
    }

    /**
     * Gets called on Deserialization.
     */
    override fun onDeserialization(item: Any): Any {
        require(item is Map<*, *>)
        val source = StringReader(item["item"] as String)
        val loader = HoconConfigurationLoader.builder().setSource { BufferedReader(source) }.build()
        val node = loader.load()
        return node.getValue(TypeToken.of(ItemStack::class.java))!!
    }
}