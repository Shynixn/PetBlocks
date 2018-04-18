package com.github.shynixn.petblocks.sponge.logic.persistence.entity

import com.flowpowered.math.vector.Vector3d
import com.github.shynixn.petblocks.core.logic.persistence.entity.LocationBuilder

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
class SpongeLocationBuilder : LocationBuilder {
    constructor() : super()
    constructor(worldName: String?, x: Double, y: Double, z: Double, yaw: Float, pitch: Float) : super(worldName, x, y, z, yaw, pitch)
    constructor(worldName: String?, x: Double, y: Double, z: Double, yaw: Double, pitch: Double) : super(worldName, x, y, z, yaw, pitch)

    /**
     * Returns the direction of the location.
     *
     * @return direction
     */
    fun getDirection(): Vector3d {
        val vector = SpongeLocationBuilder()
        val rotX = this.yaw
        val rotY = this.pitch
        vector.y = -Math.sin(Math.toRadians(rotY))
        val xz = Math.cos(Math.toRadians(rotY))
        vector.x = -xz * Math.sin(Math.toRadians(rotX))
        vector.z = xz * Math.cos(Math.toRadians(rotX))
        return vector.toVector()
    }

    fun toVector(): Vector3d {
        return Vector3d(x, y, z)
    }
}