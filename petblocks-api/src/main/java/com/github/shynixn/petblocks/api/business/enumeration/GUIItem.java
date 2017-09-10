package com.github.shynixn.petblocks.api.business.enumeration;

import com.github.shynixn.petblocks.api.business.entity.GUIItemContainer;

import java.util.Optional;

/**
 * Copyright 2017 Shynixn
 * <p>
 * Do not remove this header!
 * <p>
 * Version 1.0
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2017
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
public enum GUIItem {
    MYPET("gui.general.my-pet"),
    CANCEL("gui.items.cancel-pet"),
    ENABLEPET("gui.general.enable-pet"),
    DISABLEPET("gui.general.disable-pet"),
    WARDROBE("gui.general.wardrobe"),
    CALL("gui.items.call-pet"),
    CANNON("gui.items.cannon-pet"),
    SKULLNAMING("gui.items.skullnaming-pet"),
    NAMING("gui.items.naming-pet"),
    HAT("gui.items.hat-pet"),
    RIDING("gui.items.riding-pet"),
    SOUNDENABLED("gui.items.sounds-enabled-pet"),
    SOUNDDISABLED("gui.items.sounds-disabled-pet"),

    ORDINARYCOSTUMES("gui.items.default-costume"),
    COLORCOSTUMES("gui.items.color-costume"),
    RARECOSTUMES("gui.items.custom-costume"),
    EXCLUSIVECOSTUMES("gui.items.minecraft-heads-costume"),
    SUGGESTHEADS("gui.items.suggest-heads"),
    HEADDATABASE("gui.items.head-database-costume"),
    PARTICLEEFFECTS("gui.items.particle-pet"),

    NEXTPAGE("gui.general.next-page"),
    PREVIOUSPAGE("gui.general.previous-page"),
    DEFAULTAPPEARANCE("gui.general.default-appearance"),
    EMPTYSLOT("gui.general.empty-slot")
    ;

    private GUIItemContainer container;
    private final String path;

    GUIItem(String path) {
        this.path = path;
    }

    public void setContainer(GUIItemContainer container) {
        this.container = container;
    }

    public Optional<GUIItemContainer> getContainer() {
        return Optional.ofNullable(this.container);
    }

    public String getPath() {
        return this.path;
    }
}
