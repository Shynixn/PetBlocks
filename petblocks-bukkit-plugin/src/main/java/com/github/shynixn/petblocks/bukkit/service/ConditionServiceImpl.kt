package com.github.shynixn.petblocks.bukkit.service

import com.google.inject.Inject
import org.bukkit.plugin.Plugin
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory
import java.util.logging.Level
import javax.script.ScriptEngine


class ConditionServiceImpl @Inject constructor(private val plugin: Plugin) : ConditionService {
    private val scriptEngine: ScriptEngine

    init {
        val factory = NashornScriptEngineFactory()
        this.scriptEngine = factory.scriptEngine
    }

    /**
     * Evaluates a Javascript expression to a boolean expression.
     * Returns null if not parseable.
     */
    override suspend fun evaluate(expression: String): Boolean {
        try {
            val result = this.scriptEngine.eval(expression)
            return result as Boolean
        } catch (e: Exception) {
            plugin.logger.log(Level.SEVERE, "Cannot evaluate expression '$expression'.", e)
            return false
        }
    }
}
