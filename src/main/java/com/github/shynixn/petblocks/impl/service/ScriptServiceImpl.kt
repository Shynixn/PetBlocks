package com.github.shynixn.petblocks.impl.service

import com.github.shynixn.mcutils.common.ConfigurationService
import com.github.shynixn.petblocks.contract.ScriptService
import com.google.inject.Inject
import org.bukkit.plugin.Plugin
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory
import java.util.logging.Level
import javax.script.Compilable
import javax.script.ScriptEngine


class ScriptServiceImpl @Inject constructor(
    private val plugin: Plugin,
    configurationService: ConfigurationService
) : ScriptService {
    private val scriptEngine: ScriptEngine
    private val compileAbleScriptEngine: Compilable

    init {
        val factory = NashornScriptEngineFactory()
        val options = configurationService.findValue<List<String>>("scriptEngine.options")
        this.scriptEngine = factory.getScriptEngine()
        this.scriptEngine.put(ScriptEngine.ARGV, options.toTypedArray())
        this.compileAbleScriptEngine = this.scriptEngine as Compilable
    }

    /**
     * Evaluates a Javascript expression.
     */
    override fun evaluate(expression: String): Any? {
        return try {
            // Script Engine is thread safe.
            scriptEngine.eval(expression)
        } catch (e: Exception) {
            plugin.logger.log(Level.SEVERE, "Cannot evaluate expression '$expression'.", e)
            false
        }
    }
}
