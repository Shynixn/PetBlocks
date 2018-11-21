package com.github.shynixn.petblocks.api.business.service

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
interface DependencyWorldGuardService {
    /**
     * Prepares the given [location] for spawning a pet inside of it. All worldguard regions get modified
     * to correctly adjust the spawning. Does nothing if already prepared.
     * @param L the type of the location.
     */
    fun <L> prepareSpawningRegion(location: L)

    /**
     * Resets the cached spawning adjustments which where made by [prepareSpawningRegion]. If no caches
     * are present it does nothing.
     * @param L the type of the location.
     */
    fun <L> resetSpawningRegion(location: L)

    /**
     * Returns all region names at the given [location].
     * @param L the type of the location.
     */
    fun <L> getRegionNames(location: L): List<String>
}