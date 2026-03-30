package com.github.shynixn.petblocks.impl.service.js

import com.github.shynixn.petblocks.contract.JavaScriptService
import org.bukkit.plugin.Plugin
import java.util.logging.Level

class JavaScriptServiceImpl(plugin: Plugin, options: List<String>) : JavaScriptService {
    private var handle: JavaScriptService

    init {
        try {
            // Try Load Nashorn Implementation
            this.handle = ScriptNashornEngineServiceImpl(options)
            plugin.logger.log(Level.INFO, "Loaded embedded NashornScriptEngine.")
        } catch (e: Error) {
            try {
                // Try Load JDK Implementation
                this.handle = ScriptJdkEngineServiceImpl(options)
                plugin.logger.log(Level.INFO, "Loaded JDK NashornScriptEngine.")
            } catch (ex: Exception) {
                throw RuntimeException("Cannot find NashornScriptEngine implementation.", ex)
            }
        }
    }

    /**
     * Evaluates a Javascript expression.
     */
    override fun evaluate(expression: String): Any? {
        return handle.evaluate(expression)
    }
}