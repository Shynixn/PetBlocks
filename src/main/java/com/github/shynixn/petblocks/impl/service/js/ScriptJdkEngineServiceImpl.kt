package com.github.shynixn.petblocks.impl.service.js

import com.github.shynixn.petblocks.contract.JavaScriptService
import javax.script.Compilable
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

class ScriptJdkEngineServiceImpl(options: List<String>) : JavaScriptService {
    private val scriptEngine: ScriptEngine = ScriptEngineManager().getEngineByName("nashorn")
    private val compileAbleScriptEngine: Compilable

    init {;
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