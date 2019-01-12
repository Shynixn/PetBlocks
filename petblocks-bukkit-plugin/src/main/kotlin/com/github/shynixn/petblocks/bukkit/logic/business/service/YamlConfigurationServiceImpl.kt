package com.github.shynixn.petblocks.bukkit.logic.business.service

import api.business.service.YamlConfigurationService
import com.github.shynixn.petblocks.bukkit.logic.business.extension.deserializeToMap
import org.bukkit.configuration.file.YamlConfiguration

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
class YamlConfigurationServiceImpl : YamlConfigurationService {
    /**
     * Serializes the given [map] and [key] to a string.
     */
    override fun serializeToString(key: String, map: Map<String, Any?>): String {
        val yamlSerializer = YamlConfiguration()
        yamlSerializer.set(key, map)
        return yamlSerializer.saveToString()
    }

    /**
     * DeSerializes the given [data] and turns all memory sections into maps.
     */
    override fun deserializeToMap(key: String, data: String): Map<String, Any?> {
        val yamlSerializer = YamlConfiguration()
        yamlSerializer.loadFromString(data)
        return yamlSerializer.deserializeToMap(key)
    }
}