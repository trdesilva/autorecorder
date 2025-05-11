/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui.wrapper;

import javax.swing.JTextArea;
import javax.swing.UIManager;
import java.awt.Dimension;

public class WrappingLabel extends JTextArea
{
    public WrappingLabel(String text)
    {
        super(text);
        setText(text);
        setWrapStyleWord(true);
        setLineWrap(true);
        setOpaque(false);
        setEditable(false);
        setFocusable(false);
        setBackground(UIManager.getColor("Label.background"));
        setFont(UIManager.getFont("Label.font"));
        setBorder(UIManager.getBorder("Label.border"));
    }
    
    public WrappingLabel(String text, int maxX, int maxY)
    {
        super(text, maxX, maxY);
        setText(text);
        setWrapStyleWord(true);
        setLineWrap(true);
        setOpaque(false);
        setEditable(false);
        setFocusable(false);
        setBackground(UIManager.getColor("Label.background"));
        setFont(UIManager.getFont("Label.font"));
        setBorder(UIManager.getBorder("Label.border"));
    }
}
