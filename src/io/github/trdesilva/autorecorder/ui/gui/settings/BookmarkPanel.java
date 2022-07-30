/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui.settings;

import io.github.trdesilva.autorecorder.Settings;
import io.github.trdesilva.autorecorder.video.Hotkey;
import net.miginfocom.swing.MigLayout;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BookmarkPanel extends JPanel
{
    private final JCheckBox bookmarksEnabledCheckbox;
    private Hotkey bookmarkKey;
    private final JCheckBox consumeWindowsKeyEnabledCheckbox;
    private boolean fieldHasFocus = false;
    
    public BookmarkPanel(Settings settings)
    {
        bookmarkKey = settings.getBookmarkKey();
        
        setLayout(new MigLayout("fill", "[][120!]", "[][]"));
    
        bookmarksEnabledCheckbox = new JCheckBox();
        bookmarksEnabledCheckbox.setSelected(settings.areBookmarksEnabled());
        bookmarksEnabledCheckbox.setText("Save bookmarks in recordings by pressing");
        
        JTextField bookmarkKeyField = new JTextField(settings.getBookmarkKey().toString());
        bookmarkKeyField.setText(settings.getBookmarkKey().toString());
        bookmarkKeyField.setEditable(false);
        bookmarkKeyField.setToolTipText("Click to set bookmark hotkey (CTRL, SHIFT, and ALT modifiers allowed)");
        
        consumeWindowsKeyEnabledCheckbox = new JCheckBox();
        consumeWindowsKeyEnabledCheckbox.setSelected(settings.isConsumeWindowsKeyEnabled());
        consumeWindowsKeyEnabledCheckbox.setEnabled(bookmarksEnabledCheckbox.isSelected());
        consumeWindowsKeyEnabledCheckbox.setText("Disable Windows key while recording (requires bookmarks to be enabled)");
    
        add(bookmarksEnabledCheckbox, "cell 0 0");
        add(bookmarkKeyField, "cell 1 0, grow");
        add(consumeWindowsKeyEnabledCheckbox, "cell 0 1");
        
        bookmarksEnabledCheckbox.addActionListener(e ->
                                                   {
                                                       if(bookmarksEnabledCheckbox.isSelected())
                                                       {
                                                           consumeWindowsKeyEnabledCheckbox.setEnabled(true);
                                                       }
                                                       else
                                                       {
                                                           consumeWindowsKeyEnabledCheckbox.setEnabled(false);
                                                           consumeWindowsKeyEnabledCheckbox.setSelected(false);
                                                       }
                                                   });
        
        bookmarkKeyField.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if(fieldHasFocus && e.getKeyCode() != KeyEvent.VK_SHIFT
                && e.getKeyCode() != KeyEvent.VK_CONTROL && e.getKeyCode() != KeyEvent.VK_ALT)
                {
                    Hotkey hotkey;
                    if(e.getKeyCode() != KeyEvent.VK_ESCAPE)
                    {
                        hotkey = new Hotkey(e);
                    }
                    else
                    {
                        hotkey = new Hotkey();
                    }
                    
                    bookmarkKey = hotkey;
                    bookmarkKeyField.setText(bookmarkKey.toString());
                    fieldHasFocus = false;
                }
            }
        });
        
        bookmarkKeyField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if(bookmarksEnabledCheckbox.isSelected())
                {
                    bookmarkKey = new Hotkey();
                    bookmarkKeyField.setText(bookmarkKey.toString());
                    fieldHasFocus = true;
                }
            }
        });
    }
    
    public boolean areBookmarksEnabled()
    {
        return bookmarksEnabledCheckbox.isSelected();
    }
    
    public Hotkey getBookmarkKey()
    {
        return bookmarkKey;
    }
    
    public boolean isConsumeWindowsKeyEnabled()
    {
        return consumeWindowsKeyEnabledCheckbox.isSelected();
    }
}
