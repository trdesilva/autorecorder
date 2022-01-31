package io.github.trdesilva.autorecorder.ui.gui;

import javax.swing.JTextArea;
import javax.swing.UIManager;

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
}
