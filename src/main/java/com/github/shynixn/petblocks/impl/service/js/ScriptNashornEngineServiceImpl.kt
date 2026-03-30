package com.github.shynixn.petblocks.impl.service.js

import com.github.shynixn.petblocks.contract.JavaScriptService
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory
import javax.script.Compilable
import javax.script.ScriptEngine

class ScriptNashornEngineServiceImpl(options: List<String>) : JavaScriptService {
    private val scriptEngine: ScriptEngine
    private val compileAbleScriptEngine: Compilable

    init {
        val factory = NashornScriptEngineFactory()
        this.scriptEngine = factory.scriptEngine
        this.scriptEngine.put(ScriptEngine.ARGV, options.toTypedArray())
        this.compileAbleScriptEngine = this.scriptEngine as Compilable
    }

    /**
     * Evaluates a Javascript expression.
     */
    override fun evaluate(expression: String): Any? {
        // Script Engine is thread safe.
        return scriptEngine.eval(expression)
    }
}