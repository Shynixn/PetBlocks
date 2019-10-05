@file:Suppress("UNCHECKED_CAST")

package unittest

import com.github.shynixn.petblocks.api.business.service.ConfigurationService
import com.github.shynixn.petblocks.api.business.service.GUIItemLoadService
import com.github.shynixn.petblocks.api.business.service.MessageService
import com.github.shynixn.petblocks.api.persistence.entity.ChatMessage
import com.github.shynixn.petblocks.api.persistence.entity.GuiItem
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta
import com.github.shynixn.petblocks.core.logic.business.commandexecutor.ReloadCommandExecutorImpl
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.io.InputStream
import java.nio.file.Path

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
class ReloadCommandExecutorTest {
    /**
     * Given
     *      a valid command source
     * When
     *      onExecuteCommand is called
     * Then
     *     refresh config should be called.
     */
    @Test
    fun onExecuteCommand_ValidCommandSource_ShouldRefreshConfig() {
        // Arrange
        val commandSource = "Unknown Command Source"
        val messageService = MockedMessageService()
        val configurationService = MockedConfigurationService()
        val classUnderTest = createWithDependencies(configurationService, messageService)

        // Act
        classUnderTest.onExecuteCommand(commandSource, arrayListOf<String>().toTypedArray())

        // Assert
        Assertions.assertTrue(messageService.sourceMessageCalled)
        Assertions.assertTrue(configurationService.refreshCalled)
    }

    companion object {
        fun createWithDependencies(
            configService: ConfigurationService = MockedConfigurationService(),
            messageService: MessageService = MockedMessageService()
        ): ReloadCommandExecutorImpl {
            return ReloadCommandExecutorImpl(configService, Mockito.mock(GUIItemLoadService::class.java), messageService)
        }
    }

    class MockedMessageService(var sourceMessageCalled: Boolean = false) : MessageService {
        /**
         * Sends a colored console message.
         */
        override fun sendConsoleMessage(message: String) {
            throw IllegalArgumentException()
        }

        /**
         * Sends a message to the given source.
         */
        override fun <S> sendSourceMessage(source: S, message: String, prefix: Boolean) {
            sourceMessageCalled = true
        }

        /**
         * Sends the given [chatMessage] to the given [player].
         * @param P the type of the player.
         */
        override fun <P> sendPlayerMessage(player: P, chatMessage: ChatMessage) {
            throw IllegalArgumentException()
        }
    }

    class MockedConfigurationService(var refreshCalled: Boolean = false) : ConfigurationService {
        /**
         * Gets the path to the folder where the application is allowed to store
         * save data.
         */
        override val applicationDir: Path
            get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

        /**
         * Reloads the config.
         */
        override fun reload() {
            refreshCalled = true
        }

        /**
         * Tries to load the config value from the given [path].
         * Throws a [IllegalArgumentException] if the path could not be correctly
         * loaded.
         */
        override fun <C> findValue(path: String): C {
            throw IllegalArgumentException()
        }

        /**
         * Opens an inputStream to the given resource name.
         */
        override fun openResource(name: String): InputStream {
            throw IllegalArgumentException()
        }

        /**
         * Checks if the given [path] contains a value.
         */
        override fun containsValue(path: String): Boolean {
            throw IllegalArgumentException()
        }
    }
}