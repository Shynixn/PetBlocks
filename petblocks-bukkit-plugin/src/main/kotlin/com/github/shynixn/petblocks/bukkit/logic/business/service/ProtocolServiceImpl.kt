@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.bukkit.logic.business.service

import com.github.shynixn.petblocks.api.business.service.ConcurrencyService
import com.github.shynixn.petblocks.api.business.service.LoggingService
import com.github.shynixn.petblocks.api.business.service.ProtocolService
import com.github.shynixn.petblocks.api.business.service.ProxyService
import com.github.shynixn.petblocks.bukkit.logic.business.extension.findClazz
import com.github.shynixn.petblocks.core.logic.business.extension.accessible
import com.github.shynixn.petblocks.core.logic.persistence.entity.PacketPlayInPosition
import com.github.shynixn.petblocks.core.logic.persistence.entity.PacketPlayInSteerVehicle
import com.github.shynixn.petblocks.core.logic.persistence.entity.PacketPlayOutMount
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
    private val loggingService: LoggingService,
    private val proxyService: ProxyService
) :
    ProtocolService {
    private val handlerName = "PetBlocks " + "-" + UUID.randomUUID().toString()
    private val cachedPlayerChannels = HashMap<Player, Channel>()
    private val listeners = HashMap<Class<*>, MutableSet<(Any) -> Unit>>()
    private val nmsPacketToInternalPacket = HashMap<Class<*>, Function<Pair<Any, Player>, Any?>>()
    private val internalPacketToNMSPacket = HashMap<Class<*>, Function<Any, Any?>>()
    private val playerToNmsPlayer = findClazz("org.bukkit.craftbukkit.VERSION.entity.CraftPlayer")
        .getDeclaredMethod("getHandle")
    private val playerConnectionField = findClazz("net.minecraft.server.VERSION.EntityPlayer")
        .getDeclaredField("playerConnection")
    private val sendPacketMethod = findClazz("net.minecraft.server.VERSION.PlayerConnection")
        .getDeclaredMethod("sendPacket", findClazz("net.minecraft.server.VERSION.Packet"))

    /**
     * Init.
     */
    init {
        val packetPlayInPosition = object : Function<Pair<Any, Player>, Any?> {
            private val upperClazz = findClazz("net.minecraft.server.VERSION.PacketPlayInFlying")
            private val x = upperClazz.getDeclaredField("x").accessible(true)
            private val y = upperClazz.getDeclaredField("y").accessible(true)
            private val z = upperClazz.getDeclaredField("z").accessible(true)
            val clazz = findClazz("net.minecraft.server.VERSION.PacketPlayInFlying\$PacketPlayInPosition")
            override fun apply(t: Pair<Any, Player>): Any? {
                val packet = t.first
                val source = t.second
                return PacketPlayInPosition(
                    source,
                    x.getDouble(packet),
                    y.getDouble(packet),
                    z.getDouble(packet)
                )
            }
        }
        nmsPacketToInternalPacket[packetPlayInPosition.clazz] = packetPlayInPosition

        val packetPlayInSteerVehicle = object : Function<Pair<Any, Player>, Any?> {
            val clazz = findClazz("net.minecraft.server.VERSION.PacketPlayInSteerVehicle")
            private val sideWays = clazz.getDeclaredField("a").accessible(true)
            private val forward = clazz.getDeclaredField("b").accessible(true)
            private val jump = clazz.getDeclaredField("c").accessible(true)
            private val unMount = clazz.getDeclaredField("d").accessible(true)

            override fun apply(t: Pair<Any, Player>): Any? {
                val packet = t.first
                val source = t.second
                return PacketPlayInSteerVehicle(
                    source,
                    forward.getFloat(packet).toDouble(),
                    sideWays.getFloat(packet).toDouble(),
                    jump.getBoolean(packet),
                    unMount.getBoolean(packet)
                )
            }
        }
        nmsPacketToInternalPacket[packetPlayInSteerVehicle.clazz] = packetPlayInSteerVehicle

        internalPacketToNMSPacket[PacketPlayOutMount::class.java] = object : Function<Any, Any?> {
            val clazz = findClazz("net.minecraft.server.VERSION.PacketPlayOutMount")
            private val entityId = clazz.getDeclaredField("a").accessible(true)
            private val passengerIds = clazz.getDeclaredField("b").accessible(true)

            override fun apply(t: Any): Any? {
                require(t is PacketPlayOutMount)
                val packet = clazz.getDeclaredConstructor().newInstance()
                entityId.set(packet, proxyService.getEntityId(t.entity))
                passengerIds.set(
                    packet,
                    t.passengers.map { p -> proxyService.getEntityId(p) }.toTypedArray().toIntArray()
                )
                return packet
            }
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
        val netWorkManager = findClazz("net.minecraft.server.VERSION.PlayerConnection")
            .getDeclaredField("networkManager")
            .get(connection)
        val channel = findClazz("net.minecraft.server.VERSION.NetworkManager")
            .getDeclaredField("channel")
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
     * Sends a packet to the given player.
     */
    override fun <T, P> sendPacket(packet: T, player: P) {
        require(player is Player)
        require(packet is Any)

        if (!internalPacketToNMSPacket.containsKey(packet.javaClass)) {
            throw IllegalArgumentException("Packet '$packet' does not have a valid mapping!")
        }

        val nmsPacket = internalPacketToNMSPacket[packet.javaClass]!!.apply(packet)

        val nmsPlayer = playerToNmsPlayer
            .invoke(player)
        val connection = playerConnectionField
            .get(nmsPlayer)
        sendPacketMethod.invoke(connection, nmsPacket)
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
                listener.invoke(packet)
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
