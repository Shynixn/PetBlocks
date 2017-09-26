package com.github.shynixn.petblocks.bukkit.lib;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

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
