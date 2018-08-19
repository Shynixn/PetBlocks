package helper

import org.slf4j.Logger
import org.slf4j.Marker
import java.util.logging.Level

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
class LoggingHelper : Logger {

    private val logger = java.util.logging.Logger.getLogger("TestLogger")

    override fun getName(): String {
        return this.logger.name
    }

    override fun isTraceEnabled(): Boolean {
        return false
    }

    override fun trace(s: String) {
        throw RuntimeException(s)
    }

    override fun trace(s: String, o: Any) {
        throw RuntimeException(s)
    }

    override fun trace(s: String, o: Any, o1: Any) {
        throw RuntimeException(s)
    }

    override fun trace(s: String, vararg objects: Any) {
        throw RuntimeException(s)
    }

    override fun trace(s: String, throwable: Throwable) {
        throw RuntimeException(s)
    }

    override fun isTraceEnabled(marker: Marker): Boolean {
        return false
    }

    override fun trace(marker: Marker, s: String) {
        throw RuntimeException(s)
    }

    override fun trace(marker: Marker, s: String, o: Any) {
        throw RuntimeException(s)
    }

    override fun trace(marker: Marker, s: String, o: Any, o1: Any) {
        throw RuntimeException(s)
    }

    override fun trace(marker: Marker, s: String, vararg objects: Any) {
        throw RuntimeException(s)
    }

    override fun trace(marker: Marker, s: String, throwable: Throwable) {
        throw RuntimeException(s)
    }

    override fun isDebugEnabled(): Boolean {
        return true
    }

    override fun debug(s: String) {
        this.logger.log(Level.SEVERE, s)
    }

    override fun debug(s: String, o: Any) {
        this.logger.log(Level.SEVERE, s, o)
    }

    override fun debug(s: String, o: Any, o1: Any) {
        this.logger.log(Level.SEVERE, s, arrayOf(o, o1))
    }

    override fun debug(s: String, vararg objects: Any) {
        this.logger.log(Level.SEVERE, s, objects)
    }

    override fun debug(s: String, throwable: Throwable) {
        this.logger.log(Level.SEVERE, s, throwable)
    }

    override fun isDebugEnabled(marker: Marker): Boolean {
        return false
    }

    override fun debug(marker: Marker, s: String) {
        throw RuntimeException(s)
    }

    override fun debug(marker: Marker, s: String, o: Any) {
        throw RuntimeException(s)
    }

    override fun debug(marker: Marker, s: String, o: Any, o1: Any) {
        throw RuntimeException(s)
    }

    override fun debug(marker: Marker, s: String, vararg objects: Any) {
        throw RuntimeException(s)
    }

    override fun debug(marker: Marker, s: String, throwable: Throwable) {
        throw RuntimeException(s)
    }

    override fun isInfoEnabled(): Boolean {
        return true
    }

    override fun info(s: String) {
        this.logger.log(Level.INFO, s)
    }

    override fun info(s: String, o: Any) {
        this.logger.log(Level.INFO, s, o)
    }

    override fun info(s: String, o: Any, o1: Any) {
        this.logger.log(Level.INFO, s, arrayOf(o, o1))
    }

    override fun info(s: String, vararg objects: Any) {
        this.logger.log(Level.INFO, s, objects)
    }

    override fun info(s: String, throwable: Throwable) {
        this.logger.log(Level.INFO, s, throwable)
    }

    override fun isInfoEnabled(marker: Marker): Boolean {
        return false
    }

    override fun info(marker: Marker, s: String) {
        throw RuntimeException(s)
    }

    override fun info(marker: Marker, s: String, o: Any) {
        throw RuntimeException(s)
    }

    override fun info(marker: Marker, s: String, o: Any, o1: Any) {
        throw RuntimeException(s)
    }

    override fun info(marker: Marker, s: String, vararg objects: Any) {
        throw RuntimeException(s)
    }

    override fun info(marker: Marker, s: String, throwable: Throwable) {
        throw RuntimeException(s)
    }

    override fun isWarnEnabled(): Boolean {
        return true
    }

    override fun warn(s: String) {
        this.logger.log(Level.WARNING, s)
    }

    override fun warn(s: String, o: Any) {
        this.logger.log(Level.WARNING, s, o)
    }

    override fun warn(s: String, vararg objects: Any) {
        this.logger.log(Level.WARNING, s, objects)
    }

    override fun warn(s: String, o: Any, o1: Any) {
        this.logger.log(Level.WARNING, s, arrayOf(o, o1))
    }

    override fun warn(s: String, throwable: Throwable) {
        this.logger.log(Level.WARNING, s, throwable)
    }

    override fun isWarnEnabled(marker: Marker): Boolean {
        return false
    }

    override fun warn(marker: Marker, s: String) {
        throw RuntimeException(s)
    }

    override fun warn(marker: Marker, s: String, o: Any) {
        throw RuntimeException(s)
    }

    override fun warn(marker: Marker, s: String, o: Any, o1: Any) {
        throw RuntimeException(s)
    }

    override fun warn(marker: Marker, s: String, vararg objects: Any) {
        throw RuntimeException(s)
    }

    override fun warn(marker: Marker, s: String, throwable: Throwable) {
        throw RuntimeException(s)
    }

    override fun isErrorEnabled(): Boolean {
        return true
    }

    override fun error(s: String) {
        this.logger.log(Level.WARNING, s)
    }

    override fun error(s: String, o: Any) {
        this.logger.log(Level.WARNING, s, o)
    }

    override fun error(s: String, o: Any, o1: Any) {
        this.logger.log(Level.WARNING, s, arrayOf(o, o1))
    }

    override fun error(s: String, vararg objects: Any) {
        this.logger.log(Level.WARNING, s, objects)
    }

    override fun error(s: String, throwable: Throwable) {
        this.logger.log(Level.WARNING, s, throwable)
    }

    override fun isErrorEnabled(marker: Marker): Boolean {
        return false
    }

    override fun error(marker: Marker, s: String) {
        throw RuntimeException(s)
    }

    override fun error(marker: Marker, s: String, o: Any) {
        throw RuntimeException(s)
    }

    override fun error(marker: Marker, s: String, o: Any, o1: Any) {
        throw RuntimeException(s)
    }

    override fun error(marker: Marker, s: String, vararg objects: Any) {
        throw RuntimeException(s)
    }

    override fun error(marker: Marker, s: String, throwable: Throwable) {
        throw RuntimeException(s)
    }
}