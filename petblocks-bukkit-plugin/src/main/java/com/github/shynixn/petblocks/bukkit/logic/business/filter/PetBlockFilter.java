package com.github.shynixn.petblocks.bukkit.logic.business.filter;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LifeCycle;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.message.Message;

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
public class PetBlockFilter implements Filter, AutoCloseable, LifeCycle {

    private boolean started;

    /**
     * Initializes a new filter.
     */
    private PetBlockFilter() {
        super();
    }

    /**
     * Ignore.
     *
     * @return result
     */
    @Override
    public Result getOnMismatch() {
        return null;
    }

    /**
     * Ignore.
     *
     * @return result
     */
    @Override
    public Result getOnMatch() {
        return null;
    }

    /**
     * Ignore.
     *
     * @return result
     */
    @Override
    public Result filter(Logger logger, Level level, Marker marker, String s, Object... objects) {
        return null;
    }

    /**
     * Ignore.
     *
     * @return result
     */
    @Override
    public Result filter(Logger logger, Level level, Marker marker, Object o, Throwable throwable) {
        return null;
    }

    /**
     * Ignore.
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
     * LifeCycle start.
     */
    @Override
    public void start() {
        this.started = true;
    }

    /**
     * LifeCycle stop.
     */
    @Override
    public void stop() {
        this.started = false;
    }

    /**
     * Is started LifeCycle.
     *
     * @return started
     */
    @Override
    public boolean isStarted() {
        return this.started;
    }

    /**
     * Creates a new logger.
     *
     * @return logger
     */
    public static PetBlockFilter create() {
        final PetBlockFilter petBlockFilter = new PetBlockFilter();
        petBlockFilter.start();
        ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).addFilter(petBlockFilter);
        return petBlockFilter;
    }

    /**
     * Closes this resource, relinquishing any underlying resources.
     * This method is invoked automatically on objects managed by the
     * {@code try}-with-resources statement.
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
