/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui;

import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.function.Function;

public class ValidatingTextField extends JTextField
{
    private Function<String, String> validator;
    private boolean isValid = true;
    
    public ValidatingTextField(Function<String, String> validator)
    {
        this("", validator);
    }
    
    public ValidatingTextField(String text, Function<String, String> validator)
    {
        super(text);
        
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
