package com.github.shynixn.petblocks.business.logic.business.entity;

import com.github.shynixn.petblocks.api.business.enumeration.GUIPage;

public class GuiPageContainer {
    public GUIPage previousPage;
    public GUIPage page;
    public int startCount;
    public GuiPageContainer next;

    public GuiPageContainer(GUIPage page, GUIPage previousPage) {
        super();
        this.page = page;
        this.previousPage = previousPage;
    }
}