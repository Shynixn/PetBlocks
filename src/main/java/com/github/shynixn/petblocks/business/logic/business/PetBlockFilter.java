package com.github.shynixn.petblocks.business.logic.business;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.message.Message;

import java.io.Closeable;

/**
 * Copyright 2017 Shynixn
 * <p>
 * Do not remove this header!
 * <p>
 * Version 1.0
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2017
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
class PetBlockFilter implements Filter, AutoCloseable {

    /**
     * Initializes a new filter
     */
    private PetBlockFilter() {

    }

    /**
     * Ignore
     *
     * @return result
     */
    @Override
    public Result getOnMismatch() {
        return null;
    }

    /**
     * Ignore
     *
     * @return result
     */
    @Override
    public Result getOnMatch() {
        return null;
    }

    /**
     * Ignore
     *
     * @return result
     */
    @Override
    public Result filter(Logger logger, Level level, Marker marker, String s, Object... objects) {
        return null;
    }

    /**
     * Ignore
     *
     * @return result
     */
    @Override
    public Result filter(Logger logger, Level level, Marker marker, Object o, Throwable throwable) {
        return null;
    }

    /**
     * Ignore
     *
     * @return result
     */
    @Override
    public Result filter(Logger logger, Level level, Marker marker, Message message, Throwable throwable) {
        return null;
    }

    /**
     * Result filtering as PetBlocks manages these error messages after it gets loaded. Bukkit unfortunately prints these
     * messages before PetBlocks can clean up the server so it's ok to filter this messages.
     *
     * @return result
     */
    @Override
    public Result filter(LogEvent event) {
        if (event.getMessage().toString().contains("Wrong location for CustomRabbit")
                || event.getMessage().toString().contains("Wrong location for CustomGroundArmorstand")
                || event.getMessage().toString().contains("but was stored in chunk")
                || event.getMessage().toString().contains("Attempted Double World add on CustomGroundArmorstand")
                || event.getMessage().toString().contains("Attempted Double World add on CustomRabbit")) {
            return Result.DENY;
        }
        return null;
    }

    /**
     * Creates a new logger
     *
     * @return logger
     */
    public static PetBlockFilter create() {
        final PetBlockFilter petBlockFilter = new PetBlockFilter();
        ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).addFilter(petBlockFilter);
        return petBlockFilter;
    }

    /**
     * Closes this resource, relinquishing any underlying resources.
     * This method is invoked automatically on objects managed by the
     * {@code try}-with-resources statement.
     * <p>
     * <p>While this interface method is declared to throw {@code
     * Exception}, implementers are <em>strongly</em> encouraged to
     * declare concrete implementations of the {@code close} method to
     * throw more specific exceptions, or to throw no exception at all
     * if the close operation cannot fail.
     * <p>
     * <p> Cases where the close operation may fail require careful
     * attention by implementers. It is strongly advised to relinquish
     * the underlying resources and to internally <em>mark</em> the
     * resource as closed, prior to throwing the exception. The {@code
     * close} method is unlikely to be invoked more than once and so
     * this ensures that the resources are released in a timely manner.
     * Furthermore it reduces problems that could arise when the resource
     * wraps, or is wrapped, by another resource.
     * <p>
     * <p><em>Implementers of this interface are also strongly advised
     * to not have the {@code close} method throw {@link
     * InterruptedException}.</em>
     * <p>
     * This exception interacts with a thread's interrupted status,
     * and runtime misbehavior is likely to occur if an {@code
     * InterruptedException} is {@linkplain Throwable#addSuppressed
     * suppressed}.
     * <p>
     * More generally, if it would cause problems for an
     * exception to be suppressed, the {@code AutoCloseable.close}
     * method should not throw it.
     * <p>
     * <p>Note that unlike the {@link Closeable#close close}
     * method of {@link Closeable}, this {@code close} method
     * is <em>not</em> required to be idempotent.  In other words,
     * calling this {@code close} method more than once may have some
     * visible side effect, unlike {@code Closeable.close} which is
     * required to have no effect if called more than once.
     * <p>
     * However, implementers of this interface are strongly encouraged
     * to make their {@code close} methods idempotent.
     *
     * @throws Exception if this resource cannot be closed
     */
    @Override
    public void close() throws Exception {
        ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).getContext().removeFilter(this);
    }
}
