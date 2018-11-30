@file:Suppress("UNCHECKED_CAST")

package unittest

import com.github.shynixn.petblocks.api.PetBlocksApi
import com.github.shynixn.petblocks.api.business.enumeration.ChatColor
import com.github.shynixn.petblocks.api.business.proxy.CompletableFutureProxy
import com.github.shynixn.petblocks.api.business.proxy.PluginProxy
import com.github.shynixn.petblocks.api.business.service.ConcurrencyService
import com.github.shynixn.petblocks.api.business.service.LoggingService
import com.github.shynixn.petblocks.core.jvm.logic.business.proxy.CompletableFutureProxyImpl
import com.github.shynixn.petblocks.core.logic.business.extension.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.IOException
import java.util.concurrent.CompletableFuture

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
class ExtensionMethodTest {
    /**
     * Given
     *      a chatColors enum text
     * When
     *      translateChatColors is called.
     * Then
     *     the returned string should be correctly formatted.
     */
    @Test
    fun translateChatColors_ChatColorsEnumString_ShouldReturnCorrectFormattedString() {
        // Arrange
        val someColoredString = ChatColor.RED.toString() + "This is a a fancy colored text."
        val expectedResult = "§cThis is a a fancy colored text."

        // Act
        val actual = someColoredString.translateChatColors()

        // Assert
        Assertions.assertEquals(expectedResult, actual)
    }

    /**
     * Inline Function do not get detected by code coverage.
     *
     * Given
     *      synchronized call
     * When
     *      sync function is executed.
     * Then
     *     the concurrency service should run a sync task.
     */
    @Test
    fun sync_SyncFunctionCall_ShouldExecuteSyncTask() {
        // Arrange
        val concurrenceService = MockedConcurrencyService()
        var called = false

        // Act
        sync(concurrenceService, 1L, 2L) {
            called = true
        }

        // Assert
        Assertions.assertTrue(called)
        Assertions.assertTrue(concurrenceService.calledSync)
        Assertions.assertFalse(concurrenceService.calledASync)
    }

    /**
     * Inline Function do not get detected by code coverage.
     *
     * Given
     *      asynchronous call
     * When
     *      async function is executed.
     * Then
     *     the concurrency service should run a async task.
     */
    @Test
    fun async_ASyncFunctionCall_ShouldExecuteASyncTask() {
        // Arrange
        val concurrenceService = MockedConcurrencyService()
        var called = false

        // Act
        async(concurrenceService, 1L, 2L) {
            called = true
        }

        // Assert
        Assertions.assertTrue(called)
        Assertions.assertTrue(concurrenceService.calledASync)
        Assertions.assertFalse(concurrenceService.calledSync)
    }

    /**
     * Given
     *      a chatMessage function call
     * When
     *      the function is executed
     * Then
     *     a action should be called.
     */
    @Test
    fun chatMessage_FunctionCall_ShouldBeCalled() {
        // Arrange
        var called = false

        // Act
        chatMessage {
            called = true
        }

        // Assert
        Assertions.assertTrue(called)
    }

    /**
     * Given
     *      a list of arguments
     * When
     *      mergeArgs is executed
     * Then
     *     the arguments from index 1 to the end should be returned.
     */
    @Test
    fun mergeArgs_ListOfArguments_ShouldCorrectlyMerge() {
        // Arrange
        val arguments = arrayOf("execute", "pettemplate", "supertemplate", "1")
        val expected = "pettemplate supertemplate 1"

        // Act
        val actual = mergeArgs(arguments)

        // Assert
        Assertions.assertEquals(expected, actual)
    }

    class MockedConcurrencyService : ConcurrencyService {
        var calledSync = false
        var calledASync = false
        /**
         * Creates a new completable future.
         */
        override fun <T> createCompletableFuture(): CompletableFutureProxy<T> {
            return CompletableFutureProxyImpl()
        }

        /**
         * Runs the given [function] synchronised with the given [delayTicks] and [repeatingTicks].
         */
        override fun runTaskSync(delayTicks: Long, repeatingTicks: Long, function: () -> Unit) {
            calledSync = true
            function.invoke()
        }

        /**
         * Runs the given [function] asynchronous with the given [delayTicks] and [repeatingTicks].
         */
        override fun runTaskAsync(delayTicks: Long, repeatingTicks: Long, function: () -> Unit) {
            calledASync = true
            function.invoke()
        }
    }

    class MockedLogger : LoggingService {
        var errorCalled = false

        /**
         * Logs an info text.
         */
        override fun info(text: String, e: Throwable?) {
        }

        /**
         * Logs an warning text.
         */
        override fun warn(text: String, e: Throwable?) {
        }

        /**
         * Logs an error text.
         */
        override fun error(text: String, e: Throwable?) {
            errorCalled = true
        }
    }

    class MockedPluginProxy(private val logger: LoggingService) : PluginProxy {
        /**
         * Gets a business logic from the PetBlocks plugin.
         * All types in the service package can be accessed.
         * Throws a [IllegalArgumentException] if the service could not be found.
         * @param S the type of service class.
         */
        override fun <S, C> resolve(service: C): S {
            return logger as S
        }

        /**
         * Creates a new entity from the given [entity].
         * Throws a [IllegalArgumentException] if the entity could not be found.
         * @param E the type of entity class.
         */
        override fun <E, C> create(entity: C): E {
            return "" as E
        }
    }
}