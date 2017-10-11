package com.github.shynixn.petblocks.api.bukkit.event;

import com.github.shynixn.petblocks.api.business.entity.PetBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Sub instance of all PetBlock events.
 * <p>
 * Version 1.1
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2017 by Shynixn
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
public class PetBlockEvent extends Event {

    private final static HandlerList handlers = new HandlerList();
    private final PetBlock petBlock;

    /**
     * Initializes a new petblock event.
     *
     * @param petBlock petblock
     */
    public PetBlockEvent(PetBlock petBlock) {
        super();
        if (petBlock == null)
            throw new IllegalArgumentException("PetBlock cannot be null!");
        this.petBlock = petBlock;
    }

    /**
     * Returns the petblock which triggered the event.
     *
     * @return petblock
     */
    public PetBlock getPetBlock() {
        return this.petBlock;
    }

    /**
     * Returns the player who owns the petblock.
     *
     * @return player
     */
    public Player getPlayer() {
        return (Player) this.petBlock.getPlayer();
    }

    /**
     * Bukkit implementation.
     *
     * @return handler
     */
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Bukkit implementation.
     *
     * @return handlerList
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
