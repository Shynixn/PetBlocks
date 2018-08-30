package com.github.shynixn.petblocks.core.logic.business.service

import com.github.shynixn.petblocks.api.business.entity.ScriptResult
import com.github.shynixn.petblocks.api.business.enumeration.ScriptAction
import com.github.shynixn.petblocks.api.business.service.GUIScriptService
import com.github.shynixn.petblocks.core.logic.persistence.entity.ScriptResultImpl
import com.google.inject.Inject
import org.slf4j.Logger
import java.util.*

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
class GUIScriptServiceImpl @Inject constructor(private val logger: Logger) : GUIScriptService {
    /**
     * Executes the given [script] for the given [inventory].
     */
    override fun <I> executeScript(inventory: I, script: String): ScriptResult {
        val scriptResult = ScriptResultImpl()

        try {
            if (script.startsWith("binding collection ")) {
                val data = script.replace("binding collection ", "").split(" ")
                scriptResult.action = ScriptAction.LOAD_COLLECTION
                scriptResult.path = Optional.of(data[0])
                scriptResult.permission = Optional.of(data[1])
                return scriptResult
            } else if (script.startsWith("scrolling collection")) {
                val amount = script.replace("scrolling collection ", "").split(" ")[0].trim()
                scriptResult.action = ScriptAction.SCROLL_COLLECTION
                scriptResult.valueContainer = Optional.of(amount.toInt());
                return scriptResult
            } else if (script.startsWith("executing action")) {
                if (script.startsWith("executing action rename")) {
                    val permission = script.replace("executing action rename", "").trim()
                    scriptResult.action = ScriptAction.RENAME_PET
                    scriptResult.permission = Optional.of(permission)
                    return scriptResult
                }
                else if (script.startsWith("executing action customskin")) {
                    val permission = script.replace("executing action customskin", "").trim()
                    scriptResult.action = ScriptAction.CUSTOM_SKIN
                    scriptResult.permission = Optional.of(permission)
                    return scriptResult
                }
            }
        } catch (e: Exception) {
            logger.warn("Failed to execute script '$script'.")
        }

        return ScriptResultImpl()
    }
}