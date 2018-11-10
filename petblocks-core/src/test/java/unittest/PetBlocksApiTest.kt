@file:Suppress("UNCHECKED_CAST")

package unittest

import com.github.shynixn.petblocks.api.PetBlocksApi
import com.github.shynixn.petblocks.api.business.proxy.PluginProxy
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

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
class PetBlocksApiTest {
    /**
     * Given
     *      a valid pluginProxy and a valid service class.
     * When
     *      resolve is called
     * Then
     *     plugin Proxy resolve should be called.
     */
    @Test
    fun resolve_ValidPluginAndServiceClass_ShouldCallProxy() {
        // Arrange
        val proxy = MockedPluginProxy()
        val classUnderTest = createWithDependencies(proxy)
        throw

        // Act
        classUnderTest.resolve(String::class.java)

        // Assert
        Assertions.assertTrue(proxy.called)
    }

    /**
     * Given
     *      a valid pluginProxy and a valid entity class.
     * When
     *      create is called
     * Then
     *     plugin Proxy resolve should be called.
     */
    @Test
    fun create_ValidPluginAndEntityClass_ShouldCallProxy() {
        // Arrange
        val proxy = MockedPluginProxy()
        val classUnderTest = createWithDependencies(proxy)

        // Act
        classUnderTest.create(String::class.java)

        // Assert
        Assertions.assertTrue(proxy.called)
    }

    companion object {
        fun createWithDependencies(proxy: PluginProxy? = null): PetBlocksApi {
            var internalProxy = proxy
            if (internalProxy == null) {
                internalProxy = MockedPluginProxy()
            }

            val petblocksApi = PetBlocksApi
            val method = PetBlocksApi::class.java.getDeclaredMethod("initializePetBlocks", PluginProxy::class.java)
            method.isAccessible = true
            method.invoke(petblocksApi, internalProxy)
            return petblocksApi
        }
    }

    class MockedPluginProxy : PluginProxy {
        var called = false

        /**
         * Gets a business logic from the PetBlocks plugin.
         * All types in the service package can be accessed.
         * Throws a [IllegalArgumentException] if the service could not be found.
         * @param S the type of service class.
         */
        override fun <S> resolve(service: Class<S>): S {
            called = true
            return "" as S
        }

        /**
         * Creates a new entity from the given [entity].
         * Throws a [IllegalArgumentException] if the entity could not be found.
         * @param E the type of entity class.
         */
        override fun <E> create(entity: Class<E>): E {
            called = true
            return "" as E
        }
    }
}