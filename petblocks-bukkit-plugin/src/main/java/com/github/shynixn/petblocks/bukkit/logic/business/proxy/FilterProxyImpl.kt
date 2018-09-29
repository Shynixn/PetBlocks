package com.github.shynixn.petblocks.bukkit.logic.business.proxy

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Marker
import org.apache.logging.log4j.core.Filter
import org.apache.logging.log4j.core.LifeCycle
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.Logger
import org.apache.logging.log4j.message.Message

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
class FilterProxyImpl : Filter, AutoCloseable, LifeCycle {

    private var started: Boolean = false

    init {
        start()
        (LogManager.getRootLogger() as org.apache.logging.log4j.core.Logger).addFilter(this)
    }

    /**
     * Ignore.
     *
     * @return result
     */
    override fun getOnMismatch(): Filter.Result? {
        return null
    }

    /**
     * Ignore.
     *
     * @return result
     */
    override fun getOnMatch(): Filter.Result? {
        return null
    }

    /**
     * Ignore.
     *
     * @return result
     */
    override fun filter(logger: Logger, level: Level, marker: Marker, s: String, vararg objects: Any): Filter.Result? {
        return null
    }

    /**
     * Ignore.
     *
     * @return result
     */
    override fun filter(logger: Logger, level: Level, marker: Marker, o: Any, throwable: Throwable): Filter.Result? {
        return null
    }

    /**
     * Ignore.
     *
     * @return result
     */
    override fun filter(logger: Logger, level: Level, marker: Marker, message: Message, throwable: Throwable): Filter.Result? {
        return null
    }

    /**
     * Result filtering as PetBlocks manages these error messages after it gets loaded. Bukkit unfortunately prints these
     * messages before PetBlocks can clean up the server so it's ok to filter this messages.
     *
     * @return result
     */
    override fun filter(event: LogEvent): Filter.Result? {
        return if (event.message.toString().contains("Wrong location for CustomRabbit")
                || event.message.toString().contains("Wrong location for CustomGroundArmorstand")
                || event.message.toString().contains("but was stored in chunk")
                || event.message.toString().contains("Attempted Double World add on CustomGroundArmorstand")
                || event.message.toString().contains("Attempted Double World add on CustomRabbit")) {
            Filter.Result.DENY
        } else null
    }

    /**
     * LifeCycle start.
     */
    override fun start() {
        this.started = true
    }

    /**
     * LifeCycle stop.
     */
    override fun stop() {
        this.started = false
    }

    /**
     * Is started LifeCycle.
     *
     * @return started
     */
    override fun isStarted(): Boolean {
        return this.started
    }

    /**
     * Closes this resource, relinquishing any underlying resources.
     * This method is invoked automatically on objects managed by the
     * `try`-with-resources statement.
     * However, implementers of this interface are strongly encouraged
     * to make their `close` methods idempotent.
     *
     * @throws Exception if this resource cannot be closed
     */
    @Throws(Exception::class)
    override fun close() {
        (LogManager.getRootLogger() as org.apache.logging.log4j.core.Logger).context.removeFilter(this)
    }
}