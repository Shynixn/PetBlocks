package com.github.shynixn.petblocks.bukkit.logic.business.entity;

import com.github.shynixn.petblocks.api.business.enumeration.GUIPage;

public class GuiPageContainer {
    public final GuiPageContainer previousPage;
    public final GUIPage page;
    public int startCount;
    public int currentCount;

    public GuiPageContainer(GUIPage page, GuiPageContainer previousPage) {
        super();
        this.page = page;
        this.previousPage = previousPage;
    }
}