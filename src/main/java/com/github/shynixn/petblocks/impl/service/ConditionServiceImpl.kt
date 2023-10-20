package com.github.shynixn.petblocks.impl.service

import com.github.shynixn.petblocks.contract.ConditionService
import com.github.shynixn.petblocks.contract.Pet
import com.google.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bukkit.plugin.Plugin
import org.openjdk.nashorn.api.scripting.AbstractJSObject
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory
import java.util.logging.Level
import javax.script.Compilable
import javax.script.ScriptEngine


class ConditionServiceImpl @Inject constructor(private val plugin: Plugin) : ConditionService {
    private val scriptEngine: ScriptEngine
    private val compileAbleScriptEngine: Compilable

    init {
        val factory = NashornScriptEngineFactory()
        this.scriptEngine = factory.scriptEngine
        this.compileAbleScriptEngine = this.scriptEngine as Compilable
    }

    /**
     * Evaluates a Javascript expression to a boolean expression.
     */
    override suspend fun evaluate(expression: String): Boolean {
        try {
            return withContext(Dispatchers.IO) {
                synchronized(scriptEngine) {
                    scriptEngine.eval(expression)
                    true
                }
            }
        } catch (e: Exception) {
            plugin.logger.log(Level.SEVERE, "Cannot evaluate expression '$expression'.", e)
            return false
        }
    }
}
