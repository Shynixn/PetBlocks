package com.github.shynixn.petblocks.bukkit.logic.business.helper;

import org.slf4j.Logger;
import org.slf4j.Marker;

import java.util.logging.Level;

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
public class LoggingBridge implements Logger {

    private final java.util.logging.Logger logger;

    public LoggingBridge(java.util.logging.Logger logger) {
        super();
        this.logger = logger;
    }

    @Override
    public String getName() {
        return this.logger.getName();
    }

    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public void trace(String s) {
        throw new RuntimeException(s);
    }

    @Override
    public void trace(String s, Object o) {
        throw new RuntimeException(s);
    }

    @Override
    public void trace(String s, Object o, Object o1) {
        throw new RuntimeException(s);
    }

    @Override
    public void trace(String s, Object... objects) {
        throw new RuntimeException(s);
    }

    @Override
    public void trace(String s, Throwable throwable) {
        throw new RuntimeException(s);
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return false;
    }

    @Override
    public void trace(Marker marker, String s) {
        throw new RuntimeException(s);
    }

    @Override
    public void trace(Marker marker, String s, Object o) {
        throw new RuntimeException(s);
    }

    @Override
    public void trace(Marker marker, String s, Object o, Object o1) {
        throw new RuntimeException(s);
    }

    @Override
    public void trace(Marker marker, String s, Object... objects) {
        throw new RuntimeException(s);
    }

    @Override
    public void trace(Marker marker, String s, Throwable throwable) {
        throw new RuntimeException(s);
    }

    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    @Override
    public void debug(String s) {
        this.logger.log(Level.SEVERE, s);
    }

    @Override
    public void debug(String s, Object o) {
        this.logger.log(Level.SEVERE, s, o);
    }

    @Override
    public void debug(String s, Object o, Object o1) {
        this.logger.log(Level.SEVERE, s, new Object[]{o, o1});
    }

    @Override
    public void debug(String s, Object... objects) {
        this.logger.log(Level.SEVERE, s, objects);
    }

    @Override
    public void debug(String s, Throwable throwable) {
        this.logger.log(Level.SEVERE, s, throwable);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return false;
    }

    @Override
    public void debug(Marker marker, String s) {
        throw new RuntimeException(s);
    }

    @Override
    public void debug(Marker marker, String s, Object o) {
        throw new RuntimeException(s);
    }

    @Override
    public void debug(Marker marker, String s, Object o, Object o1) {
        throw new RuntimeException(s);
    }

    @Override
    public void debug(Marker marker, String s, Object... objects) {
        throw new RuntimeException(s);
    }

    @Override
    public void debug(Marker marker, String s, Throwable throwable) {
        throw new RuntimeException(s);
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public void info(String s) {
        this.logger.log(Level.INFO, s);
    }

    @Override
    public void info(String s, Object o) {
        this.logger.log(Level.INFO, s, o);
    }

    @Override
    public void info(String s, Object o, Object o1) {
        this.logger.log(Level.INFO, s, new Object[]{o, o1});
    }

    @Override
    public void info(String s, Object... objects) {
        this.logger.log(Level.INFO, s, objects);
    }

    @Override
    public void info(String s, Throwable throwable) {
        this.logger.log(Level.INFO, s, throwable);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return false;
    }

    @Override
    public void info(Marker marker, String s) {
        throw new RuntimeException(s);
    }

    @Override
    public void info(Marker marker, String s, Object o) {
        throw new RuntimeException(s);
    }

    @Override
    public void info(Marker marker, String s, Object o, Object o1) {
        throw new RuntimeException(s);
    }

    @Override
    public void info(Marker marker, String s, Object... objects) {
        throw new RuntimeException(s);
    }

    @Override
    public void info(Marker marker, String s, Throwable throwable) {
        throw new RuntimeException(s);
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public void warn(String s) {
        this.logger.log(Level.WARNING, s);
    }

    @Override
    public void warn(String s, Object o) {
        this.logger.log(Level.WARNING, s, o);
    }

    @Override
    public void warn(String s, Object... objects) {
        this.logger.log(Level.WARNING, s, objects);
    }

    @Override
    public void warn(String s, Object o, Object o1) {
        this.logger.log(Level.WARNING, s, new Object[]{o, o1});
    }

    @Override
    public void warn(String s, Throwable throwable) {
        this.logger.log(Level.WARNING, s, throwable);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return false;
    }

    @Override
    public void warn(Marker marker, String s) {
        throw new RuntimeException(s);
    }

    @Override
    public void warn(Marker marker, String s, Object o) {
        throw new RuntimeException(s);
    }

    @Override
    public void warn(Marker marker, String s, Object o, Object o1) {
        throw new RuntimeException(s);
    }

    @Override
    public void warn(Marker marker, String s, Object... objects) {
        throw new RuntimeException(s);
    }

    @Override
    public void warn(Marker marker, String s, Throwable throwable) {
        throw new RuntimeException(s);
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public void error(String s) {
        this.logger.log(Level.WARNING, s);
    }

    @Override
    public void error(String s, Object o) {
        this.logger.log(Level.WARNING, s, o);
    }

    @Override
    public void error(String s, Object o, Object o1) {
        this.logger.log(Level.WARNING, s, new Object[]{o, o1});
    }

    @Override
    public void error(String s, Object... objects) {
        this.logger.log(Level.WARNING, s, objects);
    }

    @Override
    public void error(String s, Throwable throwable) {
        this.logger.log(Level.WARNING, s, throwable);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return false;
    }

    @Override
    public void error(Marker marker, String s) {
        throw new RuntimeException(s);
    }

    @Override
    public void error(Marker marker, String s, Object o) {
        throw new RuntimeException(s);
    }

    @Override
    public void error(Marker marker, String s, Object o, Object o1) {
        throw new RuntimeException(s);
    }

    @Override
    public void error(Marker marker, String s, Object... objects) {
        throw new RuntimeException(s);
    }

    @Override
    public void error(Marker marker, String s, Throwable throwable) {
        throw new RuntimeException(s);
    }
}
