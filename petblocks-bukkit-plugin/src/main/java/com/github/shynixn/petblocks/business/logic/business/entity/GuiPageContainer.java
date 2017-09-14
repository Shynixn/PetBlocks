package com.github.shynixn.petblocks.business.logic.business.entity;

import com.github.shynixn.petblocks.api.business.enumeration.GUIPage;

public class GuiPageContainer {
    public GuiPageContainer previousPage;
    public GUIPage page;
    public int startCount;
    public GuiPageContainer next;
    public GuiPageContainer pre;

    public GuiPageContainer(GUIPage page, GuiPageContainer previousPage) {
        super();
        this.page = page;
        this.previousPage = previousPage;
    }
}