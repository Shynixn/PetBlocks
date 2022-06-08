@file:Suppress("unused")

package com.github.shynixn.petblocks.api.business.enumeration

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
enum class Version(
    /**
     * Id of the bukkit versions.
     */
    val bukkitId: String,
    /**
     * General id.
     */
    val id: String,
    /**
     * Numeric Id for calculations.
     */
    val numericId: Double
) {
    /**
     * Unknown version.
     */
    VERSION_UNKNOWN("", "", 0.0),

    /**
     * Version 1.8.0-1.8.2.
     */
    VERSION_1_8_R1("v1_8_R1", "1.8.0", 1.081),

    /**
     * Version 1.8.3-1.8.4.
     */
    VERSION_1_8_R2("v1_8_R2", "1.8.3", 1.082),

    /**
     * Version 1.8.5-1.8.9.
     */
    VERSION_1_8_R3("v1_8_R3", "1.8.8", 1.083),

    /**
     * Version 1.9.0-1.9.1.
     */
    VERSION_1_9_R1("v1_9_R1", "1.9.0", 1.091),

    /**
     * Version 1.9.2-1.9.4
     */
    VERSION_1_9_R2("v1_9_R2", "1.9.4", 1.092),

    /**
     * Version 1.10.0 - 1.10.2.
     */
    VERSION_1_10_R1("v1_10_R1", "1.10.2", 1.10),

    /**
     * Version 1.11.0 - 1.11.2.
     */
    VERSION_1_11_R1("v1_11_R1", "1.11.2", 1.11),

    /**
     * Version 1.12.0 - 1.12.2.
     */
    VERSION_1_12_R1("v1_12_R1", "1.12.2", 1.12),

    /**
     * Version 1.13.0 - 1.13.0.
     */
    VERSION_1_13_R1("v1_13_R1", "1.13.0", 1.13),

    /**
     * Version 1.13.1 - 1.13.2.
     */
    VERSION_1_13_R2("v1_13_R2", "1.13.2", 1.132),

    /**
     * Version 1.14.0 - 1.14.4.
     */
    VERSION_1_14_R1("v1_14_R1", "1.14.4", 1.144),

    /**
     * Version 1.15.0 - 1.15.2.
     */
    VERSION_1_15_R1("v1_15_R1", "1.15.2", 1.150),

    /**
     * Version 1.16.0 - 1.16.1.
     */
    VERSION_1_16_R1("v1_16_R1", "1.16.1", 1.160),

    /**
     * Version 1.16.2 - 1.16.3.
     */
    VERSION_1_16_R2("v1_16_R2", "1.16.2", 1.162),

    /**
     * Version 1.16.4 - 1.16.4.
     */
    VERSION_1_16_R3("v1_16_R3", "1.16.4", 1.164),

    /**
     * Version 1.17.0 - 1.17.1.
     */
    VERSION_1_17_R1("v1_17_R1", "1.17.0", 1.170),

    /**
     * Version 1.18.0 - 1.18.1.
     */
    VERSION_1_18_R1("v1_18_R1", "1.18.0", 1.180),

    /**
     * Version 1.18.2 - 1.18.2.
     */
    VERSION_1_18_R2("v1_18_R2", "1.18.2", 1.182),

    /**
     * Version 1.19.0 - 1.19.0.
     */
    VERSION_1_19_R1("v1_19_R1", "1.19.0", 1.190);

    /**
     * Checks if this version is same or greater than the given version by parameter.
     *
     * @param version version.
     * @return isSameOrGreater.
     */
    fun isVersionSameOrGreaterThan(version: Version): Boolean {
        val result = this.numericId.compareTo(version.numericId)
        return result == 0 || result == 1
    }

    /**
     * Checks if this version is lower than the given version by parameter.
     *
     * @param version version
     * @return isLower
     */
    fun isVersionLowerThan(version: Version): Boolean {
        val result = this.numericId.compareTo(version.numericId)
        return result == -1
    }

    /**
     * Gets if this version is compatible to the versions given as parameter.
     */
    fun isCompatible(versions: Array<Version>): Boolean {
        return versions.singleOrNull { v -> v.bukkitId == this.bukkitId } != null
    }
}
