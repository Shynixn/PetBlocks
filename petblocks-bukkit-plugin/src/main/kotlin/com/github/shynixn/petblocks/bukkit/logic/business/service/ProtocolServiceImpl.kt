@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.bukkit.logic.business.service

import com.github.shynixn.petblocks.api.business.service.ConcurrencyService
import com.github.shynixn.petblocks.api.business.service.LoggingService
import com.github.shynixn.petblocks.api.business.service.ProtocolService
import com.github.shynixn.petblocks.api.business.service.ProxyService
import com.github.shynixn.petblocks.bukkit.logic.business.extension.findClazz
import com.google.inject.Inject
import io.netty.channel.Channel
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import org.bukkit.entity.Player
import java.util.*
import java.util.function.Function
import kotlin.collections.HashMap
import kotlin.collections.HashSet

/**
 * Handles packet level manipulation.
 */
class ProtocolServiceImpl @Inject constructor(
    private val concurrencyService: ConcurrencyService,
    private val loggingService: LoggingService
) :
    ProtocolService {
    private val handlerName = "PetBlocks " + "-" + UUID.randomUUID().toString()
    private val cachedPlayerChannels = HashMap<Player, Channel>()
    private val listeners = HashMap<Class<*>, MutableSet<(Any) -> Unit>>()
    private val nmsPacketToInternalPacket = HashMap<Class<*>, Function<Pair<Any, Player>, Any?>>()
    private val internalPacketToNMSPacket = HashMap<Class<*>, Function<Any, Any?>>()
    private val playerToNmsPlayer = findClazz("org.bukkit.craftbukkit.VERSION.entity.CraftPlayer")
        .getDeclaredMethod("getHandle")
    private val playerConnectionField by lazy {
        try {
            findClazz("net.minecraft.server.level.EntityPlayer")
                .getDeclaredField("b")
        } catch (e: Exception) {
            findClazz("net.minecraft.server.VERSION.EntityPlayer")
                .getDeclaredField("playerConnection")
        }
    }
    private val sendPacketMethod by lazy{
        try {
            findClazz("net.minecraft.server.network.PlayerConnection")
                .getDeclaredMethod("sendPacket", findClazz("net.minecraft.network.protocol.Packet"))
        } catch (e: Exception) {
            findClazz("net.minecraft.server.VERSION.PlayerConnection")
                .getDeclaredMethod("sendPacket", findClazz("net.minecraft.server.VERSION.Packet"))
        }
    }
    private val networkManagerField by lazy {
        try {
            findClazz("net.minecraft.server.network.PlayerConnection")
                .getDeclaredField("a")
        } catch (e: Exception) {
           findClazz("net.minecraft.server.VERSION.PlayerConnection")
                .getDeclaredField("networkManager")
        }
    }
    private val channelField by lazy {
        try {
            findClazz("net.minecraft.network.NetworkManager")
                .getDeclaredField("k")
        } catch (e: Exception) {
            findClazz("net.minecraft.server.VERSION.NetworkManager")
                .getDeclaredField("channel")
        }
    }

    /**
     * Registers a player for incoming packets.
     * Does nothing if a player is already registered.
     */
    override fun <P> registerPlayer(player: P) {
        require(player is Player)

        if (cachedPlayerChannels.containsKey(player)) {
            return
        }

        val nmsPlayer = playerToNmsPlayer
            .invoke(player)
        val connection = playerConnectionField
            .get(nmsPlayer)
        val netWorkManager = networkManagerField.get(connection)
        val channel =  channelField
            .get(netWorkManager) as Channel

        val internalInterceptor = PacketInterceptor(player, this, loggingService)
        channel.pipeline().addBefore("packet_handler", handlerName, internalInterceptor)
        cachedPlayerChannels[player] = channel
    }

    /**
     * UnRegisters a player for incoming packets.
     * Does nothing if a player is already unregistered.
     */
    override fun <P> unRegisterPlayer(player: P) {
        require(player is Player)

        if (!cachedPlayerChannels.containsKey(player)) {
            return
        }

        val channel = cachedPlayerChannels[player]
        channel!!.eventLoop().execute {
            try {
                channel.pipeline().remove(handlerName)
            } catch (e: Exception) {
                // Can be ignored.
            }
        }
        cachedPlayerChannels.remove(player)
    }

    /**
     * Registers a listener for the given packet type.
     */
    override fun <T> registerListener(clazz: Class<T>, f: (T) -> Unit) {
        if (!listeners.containsKey(clazz)) {
            listeners[clazz] = HashSet()
        }

        listeners[clazz]!!.add(f as (Any) -> Unit)
    }

    /**
     * Closes all resources and connections.
     */
    override fun close() {
        for (player in cachedPlayerChannels.keys.toTypedArray()) {
            unRegisterPlayer(player)
        }

        cachedPlayerChannels.clear()
        listeners.clear()
        nmsPacketToInternalPacket.clear()
    }

    /**
     * On Message receive.
     */
    private fun onMessageReceive(player: Player, packet: Any) {
        if (!nmsPacketToInternalPacket.containsKey(packet.javaClass)) {
            return
        }

        val packetFunction = this.nmsPacketToInternalPacket[packet.javaClass]!!
        val commonPacket = packetFunction.apply(Pair(packet, player)) ?: return

        concurrencyService.runTaskSync {
            if (!this.listeners.containsKey(commonPacket.javaClass)) {
                return@runTaskSync
            }

            val listeners = this.listeners[commonPacket.javaClass]!!.toTypedArray()

            for (listener in listeners) {
                listener.invoke(commonPacket)
            }
        }
    }

    private class PacketInterceptor(
        private val player: Player,
        private val protocolService: ProtocolServiceImpl,
        private val loggingService: LoggingService
    ) :
        ChannelDuplexHandler() {
        /**
         * Incoming packet.
         */
        override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
            try {
                protocolService.onMessageReceive(player, msg)
            } catch (e: Exception) {
                loggingService.error("ChannelReadFailure.", e)
            }

            super.channelRead(ctx, msg)
        }
    }
}
