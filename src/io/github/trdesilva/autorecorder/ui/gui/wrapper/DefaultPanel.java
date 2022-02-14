/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui.wrapper;

import javax.swing.JPanel;
import javax.swing.UIManager;

public class DefaultPanel extends JPanel
{
    public DefaultPanel()
    {
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch(Exception e)
        {
            // don't really care
        }
    }
}
