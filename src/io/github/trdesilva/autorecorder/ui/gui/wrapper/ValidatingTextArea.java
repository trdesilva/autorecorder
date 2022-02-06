/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui.wrapper;

import javax.swing.BorderFactory;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.function.Function;

public class ValidatingTextArea extends JTextArea
{
    private Function<String, String> validator;
    private boolean isValid = true;
    
    public ValidatingTextArea(Function<String, String> validator)
    {
        this("", validator);
    }
    
    public ValidatingTextArea(String text, Function<String, String> validator)
    {
        super(text);
    
        setFont(UIManager.getFont("Label.font"));
        setLineWrap(true);
        setBorder(UIManager.getLookAndFeel().getDefaults().getBorder("TextField.border"));
        
        this.validator = validator;
        
        addKeyListener(new KeyListener()
        {
            @Override
            public void keyTyped(KeyEvent e)
            {
                // action listener is invoked before text is updated for normal characters
                String text = getText();
                if(!e.isActionKey() && e.getKeyChar() != '\b')
                {
                    text += e.getKeyChar();
                }
                String error = validator.apply(text);
                if(error != null)
                {
                    setBorder(BorderFactory.createLineBorder(Color.RED));
                    setToolTipText(error);
                    isValid = false;
                }
                else
                {
                    setBorder(UIManager.getLookAndFeel().getDefaults().getBorder("TextField.border"));
                    setToolTipText(null);
                    isValid = true;
                }
            }
            
            @Override
            public void keyPressed(KeyEvent e)
            {
            
            }
            
            @Override
            public void keyReleased(KeyEvent e)
            {
            
            }
        });
    }
    
    @Override
    public boolean isValid()
    {
        return isValid;
    }
}
