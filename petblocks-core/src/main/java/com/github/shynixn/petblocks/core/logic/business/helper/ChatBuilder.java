package com.github.shynixn.petblocks.core.logic.business.helper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

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
public class ChatBuilder {

    private final List<Object> components = new ArrayList<>();

    /**
     * Adds a text
     *
     * @param text text
     * @return instance
     */
    public ChatBuilder text(String text) {
        if (text == null)
            throw new IllegalArgumentException("Text cannot be null!");
        this.components.add(text);
        return this;
    }

    /**
     * Creates a component for complex editing from the text and returns it for editing
     *
     * @param text text
     * @return created component
     */
    public Component component(String text) {
        if (text == null)
            throw new IllegalArgumentException("Text cannot be null!");
        final Component component = new Component(this, text);
        this.components.add(component);
        return component;
    }

    /**
     * Sets the text bold
     *
     * @return instance
     */
    public ChatBuilder bold() {
        this.components.add(ChatColor.BOLD);
        return this;
    }

    /**
     * Sets the text italic
     *
     * @return instance
     */
    public ChatBuilder italic() {
        this.components.add(ChatColor.ITALIC);
        return this;
    }

    /**
     * Sets the text underlined
     *
     * @return instance
     */
    public ChatBuilder underline() {
        this.components.add(ChatColor.UNDERLINE);
        return this;
    }

    /**
     * Sets the text strikeThrough
     *
     * @return instance
     */
    public ChatBuilder strikeThrough() {
        this.components.add(ChatColor.STRIKETHROUGH);
        return this;
    }

    /**
     * Sets the current color
     *
     * @param chatColor color
     * @return instance
     */
    public ChatBuilder color(ChatColor chatColor) {
        if (chatColor == null)
            throw new IllegalArgumentException("ChatColor cannot be null");
        this.components.add(chatColor);
        return this;
    }

    /**
     * Resets the color
     *
     * @return instance
     */
    public ChatBuilder reset() {
        this.components.add(ChatColor.RESET);
        return this;
    }

    public List<Object> getComponents() {
        return Collections.unmodifiableList(this.components);
    }

    /**
     * Types of ClickAction
     */
    public enum ClickAction {
        OPEN_URL,
        OPEN_FILE,
        RUN_COMMAND,
        SUGGEST_COMMAND,
        CHANGE_PAGE,
    }

    public static class Component {
        private final StringBuilder text = new StringBuilder();
        private ChatColor color;
        private boolean bold;
        private boolean italic;
        private boolean underlined;
        private boolean strikethrough;
        private final ChatBuilder builder;

        private ClickAction clickAction;
        private String clickActionData;
        private Component hoverActionData;
        private final Component parentComponent;

        /**
         * Initializes a new component with the given builder and text
         *
         * @param builder builder
         * @param text    text
         */
        Component(ChatBuilder builder, String text) {
            this(builder, text, null);
        }

        /**
         * Initializes a new component with the given builder text and parentComponent
         *
         * @param builder   builder
         * @param text      text
         * @param component parent
         */
        Component(ChatBuilder builder, String text, Component component) {
            super();
            if (builder == null)
                throw new IllegalArgumentException("Builder cannot be null!");
            if (text == null)
                throw new IllegalArgumentException("Text cannot be null!");
            this.builder = builder;
            this.text.append(text);
            this.parentComponent = component;
        }

        /**
         * Returns the parent component. Returns null if there is no parent
         *
         * @return componend
         */
        public Component getParentComponent() {
            return this.parentComponent;
        }

        /**
         * Returns the builder of the component
         *
         * @return builder
         */
        public ChatBuilder builder() {
            return this.builder;
        }

        /**
         * Sets the text of the component
         *
         * @param text text
         * @return instance
         */
        public Component setText(String text) {
            if (text == null)
                throw new IllegalArgumentException("Text cannot be null!");
            this.text.setLength(0);
            this.text.append(text);
            return this;
        }

        /**
         * Sets the click Action of the component
         *
         * @param action action
         * @param text   text
         * @return instance
         */
        public Component setClickAction(ClickAction action, String text) {
            if (text == null)
                throw new IllegalArgumentException("Text cannot be null!");
            this.clickAction = action;
            this.clickActionData = text;
            return this;
        }

        /**
         * Sets the hover text of the component and returns the component for the hover-text
         *
         * @param text text
         * @return childComponent
         */
        public Component setHoverText(String text) {
            if (text == null)
                throw new IllegalArgumentException("Text cannot be null!");
            this.hoverActionData = new Component(this.builder, text, this);
            return this.hoverActionData;
        }

        /**
         * Returns the text
         *
         * @return text
         */
        public String getText() {
            return ChatColor.translateAlternateColorCodes('&', this.text.toString());
        }

        /**
         * Appends a text to the component
         *
         * @param text text
         * @return instance
         */
        public Component appendText(String text) {
            if (text == null)
                throw new IllegalArgumentException("Text cannot be null!");
            this.text.append(text);
            return this;
        }

        /**
         * Sets the component color
         *
         * @param color color
         * @return instance
         */
        public Component setColor(ChatColor color) {
            this.color = color;
            return this;
        }

        /**
         * Returns if the component is bold
         *
         * @return bold
         */
        public boolean isBold() {
            return this.bold;
        }

        /**
         * Sets the component bold
         *
         * @param bold bold
         * @return isBold
         */
        public Component setBold(boolean bold) {
            this.bold = bold;
            return this;
        }

        /**
         * Returns if the component is italic
         *
         * @return isItalic
         */
        public boolean isItalic() {
            return this.italic;
        }

        /**
         * Sets the component italic
         *
         * @param italic italic
         * @return instance
         */
        public Component setItalic(boolean italic) {
            this.italic = italic;
            return this;
        }

        /**
         * Returns if the component isUnderlined
         *
         * @return isUnderLined
         */
        public boolean isUnderlined() {
            return this.underlined;
        }

        /**
         * Sets the component underLined
         *
         * @param underlined underLines
         * @return instance
         */
        public Component setUnderlined(boolean underlined) {
            this.underlined = underlined;
            return this;
        }

        /**
         * Returns if the component isStrikeThrough
         *
         * @return isStrikeTrough
         */
        public boolean isStrikethrough() {
            return this.strikethrough;
        }

        /**
         * Sets the component strikeThrough
         *
         * @param strikethrough strikeThrough
         * @return instance
         */
        public Component setStrikethrough(boolean strikethrough) {
            this.strikethrough = strikethrough;
            return this;
        }

        /**
         * Returns a string representation of the object. In general, the
         * {@code toString} method returns a string that
         * "textually represents" this object. The result should
         * be a concise but informative representation that is easy for a
         * person to read.
         * It is recommended that all subclasses override this method.
         *
         * @return a string representation of the object.
         */
        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append("{ \"text\": \"");
            builder.append(ChatColor.translateAlternateColorCodes('&', this.text.toString()));
            builder.append('"');
            if (this.color != null) {
                builder.append(", \"color\": \"");
                builder.append(this.color.name().toLowerCase());
                builder.append('"');
            }
            if (this.bold) {
                builder.append(", \"bold\": \"");
                builder.append(this.bold);
                builder.append('"');
            }
            if (this.italic) {
                builder.append(", \"italic\": \"");
                builder.append(this.italic);
                builder.append('"');
            }
            if (this.underlined) {
                builder.append(", \"underlined\": \"");
                builder.append(this.underlined);
                builder.append('"');
            }
            if (this.strikethrough) {
                builder.append(", \"strikethrough\": \"");
                builder.append(this.strikethrough);
                builder.append('"');
            }
            if (this.clickAction != null) {
                builder.append(", \"clickEvent\": {\"action\": \"");
                builder.append(this.clickAction.name().toLowerCase());
                builder.append("\" , \"value\" : \"");
                builder.append(this.clickActionData);
                builder.append("\"}");
            }
            if (this.hoverActionData != null) {
                builder.append(", \"hoverEvent\": {\"action\": \"");
                builder.append("show_text");
                builder.append("\" , \"value\" : ");
                builder.append(this.hoverActionData.toString());
                builder.append('}');
            }
            builder.append('}');
            return builder.toString();
        }
    }
}
