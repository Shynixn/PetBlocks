@file:Suppress("MayBeConstant")

package com.github.shynixn.petblocks.api.business.localization

import com.github.shynixn.petblocks.api.business.annotation.Key

/**
 * Created by Shynixn 2020.
 * <p>
 * Version 1.5
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2020 by Shynixn
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
object Messages {
    /**
     * Message prefix.
     */
    @Key("prefix")
    var prefix: String = "prefix"
        private set

    /**
     * Gui title.
     */
    @Key("gui.title")
    var guiTitle: String = "gui.title"
        private set

    /**
     * No permission message.
     */
    @Key("messages.permissionNo")
    var noPermissionMessage = "messages.permissionNo"
        private set

    /**
     * Call success message.
     */
    @Key("messages.callPetSuccess")
    var callSuccessMessage = "messages.callPetSuccess"
        private set

    /**
     * Call error message.
     */
    @Key("messages.callPetError")
    var callErrorMessage = "messages.callPetError"
        private set

    /**
     * Despawn message.
     */
    @Key("messages.despawnPet")
    var despawnMessage = "messages.despawnPet"
        private set

    /**
     * Custom head suggest prefix.
     */
    @Key("messages.customHeadPrefix")
    var customHeadSuggestPrefix = "messages.customHeadPrefix"
        private set

    /**
     * Custom head suggest clickable.
     */
    @Key("messages.customHeadClickable")
    var customHeadSuggestClickable = "messages.customHeadClickable"
        private set

    /**
     * Custom head suggest suffix.
     */
    @Key("messages.customHeadSuffix")
    var customHeadSuggestSuffix = "messages.customHeadSuffix"
        private set

    /**
     * Custom head suggest hover.
     */
    @Key("messages.customHeadHover")
    var customHeadSuggestHover = "suggest-customHead-hover"
        private set

    /**
     * Custom head success message.
     */
    @Key("messages.customHeadSuccess")
    var customHeadSuccessMessage = "messages.customHeadSuccess"
        private set

    /**
     * Custom head success message.
     */
    @Key("messages.customHeadError")
    var customHeadErrorMessage = "messages.customHeadError"
        private set

    /**
     * Has permission selection yes.
     */
    @Key("messages.permissionYes")
    var permissionTranslationYes = "permission-translation-yes"
        private set

    /**
     * Has permission selection no.
     */
    @Key("messages.permissionNo")
    var permissionTranslationNo = "permission-translation-no"
        private set

    /**
     * Rename suggest prefix.
     */
    @Key("messages.renamePrefix")
    var renameSuggestPrefix = "suggest-rename-prefix"
        private set

    /**
     * Rename suggest clickable.
     */
    @Key("messages.renameClickable")
    var renameSuggestClickable = "suggest-rename-clickable"
        private set

    /**
     * Rename suggest suffix.
     */
    @Key("messages.renameSuffix")
    var renameSuggestSuffix = "suggest-rename-suffix"
        private set

    /**
     * Rename suggest hover.
     */
    @Key("messages.renameHover")
    var renameSuggestHover = "suggest-rename-hover"
        private set

    /**
     * Rename success message.
     */
    @Key("messages.changeNameSuccess")
    var renameSuccessMessage = "message-rename-success"
        private set

    /**
     * Rename error message.
     */
    @Key("messages.changeNameError")
    var renameErrorMessage = "message-rename-error"
        private set
}