package com.github.shynixn.petblocks.lib.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

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
public abstract class SQLProvider {
    private final Map<String, String> cache = new HashMap<>();

    /**
     * Returns a preparedStatement from the given statementName and connection
     * @param statementName statementName
     * @param connection connection
     * @return preparedStatement
     * @throws IOException exception
     * @throws SQLException exception
     */
    public PreparedStatement getStatement(String statementName, Connection connection) throws IOException, SQLException {
        if(connection == null)
            throw new IllegalArgumentException("Connection cannot be null!");
        if(connection.isClosed())
            throw new IllegalArgumentException("Connection is closed! Cannot create statement!");
        return connection.prepareStatement(this.getString(statementName));
    }

    /**
     * Returns a string from the given statementName
     * @param statementName statementName
     * @return string
     */
    public String getString(String statementName) throws IOException {
        if(statementName == null)
            throw new IllegalArgumentException("Statement cannot be null!");
        if (!this.cache.containsKey(statementName)) {
            this.cache.put(statementName, this.readStringFromFile(statementName));
        }
        return this.cache.get(statementName);
    }

    /**
     * Clears the statement cache
     */
    public void clear() {
        this.cache.clear();
    }

    /**
     * Reads a string from the given file
     * @param fileName fileName
     * @return string
     * @throws IOException exception
     */
    protected abstract String readStringFromFile(String fileName) throws IOException;
}