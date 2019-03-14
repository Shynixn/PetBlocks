@file:Suppress("UNCHECKED_CAST")

package unittest

import com.github.shynixn.petblocks.api.business.service.ConfigurationService
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
            return ReloadCommandExecutorImpl(configService, messageService)
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
         * Gets the [Path] to the configuration folder.
         */
        override val dataFolder: Path
            get() = Mockito.mock(Path::class.java)

        /**
         * Opens a new inputStream to the given [resource].
         */
        override fun openResourceInputStream(resource: String): InputStream {
            throw IllegalArgumentException()
        }

        /**
         * Tries to load the config value from the given [path].
         * Throws a [IllegalArgumentException] if the path could not be correctly
         * loaded.
         * @param C the type of the returned value.
         */
        override fun <C> findValue(path: String): C {
            throw IllegalArgumentException()
        }

        /**
         * Checks if the given path is containing in the config.yml.
         */
        override fun contains(path: String): Boolean {
            throw IllegalArgumentException()
        }

        /**
         * Tries to return a [GuiItem] matching the displayName and the lore of the given [item].
         * Can be called asynchronly. Uses the [path] parameter for faster fetching.
         * @param I the type of the itemstack.
         */
        override fun <I> findClickedGUIItem(path: String, item: I): GuiItem? {
            throw IllegalArgumentException()
        }

        /**
         * Tries to return a list of [GuiItem] matching the given path from the config.
         * Can be called asynchronly.
         */
        override fun findGUIItemCollection(path: String): List<GuiItem>? {
            throw IllegalArgumentException()
        }

        /**
         * Generates the default pet meta.
         */
        override fun generateDefaultPetMeta(uuid: String, name: String): PetMeta {
            throw IllegalArgumentException()
        }

        /**
         * Clears cached resources and refreshes the used configuration.
         */
        override fun refresh() {
            refreshCalled = true
        }
    }
}