package unittest

import com.github.shynixn.petblocks.api.business.enumeration.PluginDependency
import com.github.shynixn.petblocks.api.business.service.DependencyService
import com.github.shynixn.petblocks.api.business.service.MessageService
import com.github.shynixn.petblocks.api.persistence.entity.ChatMessage
import com.github.shynixn.petblocks.sponge.logic.business.service.DependencyServiceImpl
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.spongepowered.api.Game
import org.spongepowered.api.Server
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.command.source.ConsoleSource
import org.spongepowered.api.plugin.PluginContainer
import org.spongepowered.api.service.context.Context
import org.spongepowered.api.service.permission.SubjectCollection
import org.spongepowered.api.service.permission.SubjectData
import org.spongepowered.api.service.permission.SubjectReference
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.channel.MessageChannel
import org.spongepowered.api.util.Tristate
import java.util.*

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
class DependencyServiceTest {
    /**
     * Given
     *   one installed plugin dependency
     * When
     *    checkForInstalledDependencies is called
     * Then
     *    Message should be called twice per dependency.
     */
    @Test
    fun checkForInstalledDependencies_OneInstalledDependencies_ShouldCallMessageTwicePerDependency() {
        // Arrange
        val consoleSender = MockedMessageService()
        val classUnderTest = createWithDependencies(consoleSender)

        // Act
        classUnderTest.checkForInstalledDependencies()

        // Assert
        Assertions.assertEquals(2, consoleSender.messageCounter)
    }

    /**
     * Given
     *   no  installed plugin dependencies
     * When
     *    checkForInstalledDependencies is called
     * Then
     *    Message should not be called.
     */
    @Test
    fun checkForInstalledDependencies_NoInstalledDependencies_ShouldNotCallMessage() {
        // Arrange
        val consoleSender = MockedMessageService()
        val classUnderTest = createWithDependencies(consoleSender, false)

        // Act
        classUnderTest.checkForInstalledDependencies()

        // Assert
        Assertions.assertEquals(0, consoleSender.messageCounter)
    }

    /**
     * Given
     *   installed plugin dependencies
     * When
     *    getVersion is called
     * Then
     *    version should be returned.
     */
    @Test
    fun getVersion_InstalledDependencies_ShouldReturnVersion() {
        // Arrange
        val version = "1.0"
        val dependency = PluginDependency.WORLDGUARD
        val classUnderTest = createWithDependencies()

        // Act
        val actualDependency = classUnderTest.getVersion(dependency)

        // Assert
        Assertions.assertEquals(version, actualDependency)
    }

    /**
     * Given
     *   no installed plugin dependencies
     * When
     *    getVersion is called
     * Then
     *    Exception should be thrown.
     */
    @Test
    fun getVersion_NoInstalledDependencies_ShouldThrowException() {
        // Arrange
        val dependency = PluginDependency.WORLDGUARD
        val classUnderTest = createWithDependencies(null, false)

        // Act
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            classUnderTest.getVersion(dependency)
        }
    }

    /**
     * Given
     *   installed plugin dependencies
     * When
     *    isInstalled is called
     * Then
     *    True should be returned.
     */
    @Test
    fun isInstalled_InstalledDependencies_ShouldReturnTrue() {
        // Arrange
        val dependency = PluginDependency.WORLDGUARD
        val classUnderTest = createWithDependencies()

        // Act
        val actual = classUnderTest.isInstalled(dependency)

        // Assert
        Assertions.assertTrue(actual)
    }


    /**
     * Given
     *   no installed plugin dependencies
     * When
     *    isInstalled is called
     * Then
     *    False should be returned.
     */
    @Test
    fun isInstalled_InstalledDependencies_ShouldReturnFalse() {
        // Arrange
        val dependency = PluginDependency.WORLDGUARD
        val classUnderTest = createWithDependencies(null, false)

        // Act
        val actual = classUnderTest.isInstalled(dependency)

        // Assert
        Assertions.assertFalse(actual)
    }

    companion object {
        fun createWithDependencies(consoleCommandSender: MessageService? = null, shouldInstallDependencies: Boolean = true): DependencyService {
            val plugin = Mockito.mock(PluginContainer::class.java)
            val game = Mockito.mock(Game::class.java)
            val server = Mockito.mock(Server::class.java)

            Mockito.`when`(game.server).thenReturn(server)

            val field = Sponge::class.java.getDeclaredField("game")
            field.isAccessible = true
            field.set(null, game)

            if (shouldInstallDependencies) {
                Mockito.`when`(plugin.getDependency(Mockito.anyString()))
                        .thenReturn(Optional.of(org.spongepowered.plugin.meta.PluginDependency(org.spongepowered.plugin.meta.PluginDependency.LoadOrder.NONE, "custom", "1.0", false)))
            }

            return if (consoleCommandSender == null) {
                DependencyServiceImpl(plugin, MockedMessageService())
            } else {
                DependencyServiceImpl(plugin, consoleCommandSender)
            }
        }
    }

    private class MockedMessageService : MessageService {
        /**
         * Sends a message to the given source.
         */
        override fun <S> sendSourceMessage(source: S, message: String) {
            messageCounter++
        }

        var messageCounter = 0

        /**
         * Sends a colored console message.
         */
        override fun sendConsoleMessage(message: String) {
            messageCounter++
        }

        /**
         * Sends the given [chatMessage] to the given [player].
         */
        override fun <P> sendPlayerMessage(player: P, chatMessage: ChatMessage) {
            throw IllegalArgumentException()
        }
    }
}