package com.github.shynixn.petblocks.lib;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created by Christoph on 18.07.2015.
 */
public class SpigotEvent extends Event {
    private final static HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
