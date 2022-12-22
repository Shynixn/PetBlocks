package com.github.shynixn.petblocks.bukkit.event

import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

open class PetBlocksEvent : Event(), Cancellable {
    private var cancelledFlag = false

    /**
     * Event.
     */
    companion object {
        private var handlers = HandlerList()

        /**
         * Handlerlist.
         */
        @JvmStatic
        fun getHandlerList(): HandlerList {
            return handlers
        }
    }

    /**
     * Returns all handles.
     */
    override fun getHandlers(): HandlerList {
        return PetBlocksEvent.handlers
    }

    override fun isCancelled(): Boolean {
        return cancelledFlag
    }

    override fun setCancelled(flag: Boolean) {
        this.cancelledFlag = flag
    }
}
