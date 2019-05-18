package unittest

import com.github.shynixn.petblocks.api.business.enumeration.PluginDependency
import com.github.shynixn.petblocks.api.business.service.DependencyService
import com.github.shynixn.petblocks.bukkit.logic.business.service.DependencyServiceImpl
import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.conversations.Conversation
import org.bukkit.conversations.ConversationAbandonedEvent
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionAttachment
import org.bukkit.permissions.PermissionAttachmentInfo
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginDescriptionFile
import org.bukkit.plugin.PluginManager
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.logging.Logger

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
     *   2 installed plugin dependencies
     * When
     *    checkForInstalledDependencies is called
     * Then
     *    Message should be called once per dependency.
     */
    @Test
    fun checkForInstalledDependencies_TwoInstalledDependencies_ShouldCallMessageOncePerDependency() {
        // Arrange
        val consoleSender = MockedConsoleSender()
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
        val consoleSender = MockedConsoleSender()
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
        val dependency = PluginDependency.HEADDATABASE
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
        val dependency = PluginDependency.HEADDATABASE
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
        val dependency = PluginDependency.HEADDATABASE
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
        val dependency = PluginDependency.HEADDATABASE
        val classUnderTest = createWithDependencies(null, false)

        // Act
        val actual = classUnderTest.isInstalled(dependency)

        // Assert
        Assertions.assertFalse(actual)
    }

    companion object {
        fun createWithDependencies(consoleCommandSender: ConsoleCommandSender? = null, shouldInstallDependencies: Boolean = true): DependencyService {
            val plugin = Mockito.mock(Plugin::class.java)
            val server = Mockito.mock(Server::class.java)
            val pluginManager = Mockito.mock(PluginManager::class.java)

            Mockito.`when`(server.logger).thenReturn(Logger.getGlobal())

            if (Bukkit.getServer() == null) {
                Bukkit.setServer(server)
            }

            Mockito.`when`(plugin.server).thenReturn(server)
            Mockito.`when`(plugin.description).thenReturn(PluginDescriptionFile("Custom", "1.0", ""))
            Mockito.`when`(server.pluginManager).thenReturn(pluginManager)

            if (shouldInstallDependencies) {
                Mockito.`when`(pluginManager.getPlugin(Mockito.anyString())).thenReturn(plugin)
            }

            if (consoleCommandSender == null) {
                Mockito.`when`(server.consoleSender).thenReturn(MockedConsoleSender())
            } else {
                Mockito.`when`(server.consoleSender).thenReturn(consoleCommandSender)
            }

            return DependencyServiceImpl(plugin)
        }
    }

    private class MockedConsoleSender : ConsoleCommandSender {
        override fun acceptConversationInput(p0: String) {
            throw IllegalArgumentException()
        }

        override fun sendRawMessage(p0: String) {
            throw IllegalArgumentException()
        }

        override fun removeAttachment(p0: PermissionAttachment) {
            throw IllegalArgumentException()
        }

        override fun hasPermission(p0: String): Boolean {
            throw IllegalArgumentException()
        }

        override fun hasPermission(p0: Permission): Boolean {
            throw IllegalArgumentException()
        }

        override fun abandonConversation(p0: Conversation) {
            throw IllegalArgumentException()
        }

        override fun abandonConversation(p0: Conversation, p1: ConversationAbandonedEvent) {
            throw IllegalArgumentException()
        }

        override fun spigot(): CommandSender.Spigot {
            throw IllegalArgumentException()
        }

        var messageCounter = 0

        override fun sendMessage(p0: String) {
            messageCounter++
        }

        override fun sendMessage(p0: Array<out String>) {
            throw IllegalArgumentException()
        }

        override fun beginConversation(p0: Conversation): Boolean {
            throw IllegalArgumentException()
        }

        override fun isPermissionSet(p0: String): Boolean {
            throw IllegalArgumentException()
        }

        override fun isPermissionSet(p0: Permission): Boolean {
            throw IllegalArgumentException()
        }

        override fun addAttachment(p0: Plugin, p1: String, p2: Boolean): PermissionAttachment {
            throw IllegalArgumentException()
        }

        override fun addAttachment(p0: Plugin): PermissionAttachment {
            throw IllegalArgumentException()
        }

        override fun addAttachment(p0: Plugin, p1: String, p2: Boolean, p3: Int): PermissionAttachment {
            throw IllegalArgumentException()
        }

        override fun addAttachment(p0: Plugin, p1: Int): PermissionAttachment {
            throw IllegalArgumentException()
        }

        override fun getName(): String {
            throw IllegalArgumentException()
        }

        override fun isOp(): Boolean {
            throw IllegalArgumentException()
        }
        override fun getEffectivePermissions(): MutableSet<PermissionAttachmentInfo> {
            throw IllegalArgumentException()
        }

        override fun isConversing(): Boolean {
            throw IllegalArgumentException()
        }

        override fun getServer(): Server {
            throw IllegalArgumentException()
        }

        override fun recalculatePermissions() {
            throw IllegalArgumentException()
        }

        override fun setOp(p0: Boolean) {
            throw IllegalArgumentException()
        }
    }
}