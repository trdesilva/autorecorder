/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui;

import com.google.inject.Inject;
import io.github.trdesilva.autorecorder.Settings;
import io.github.trdesilva.autorecorder.ui.gui.wrapper.DefaultPanel;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.io.IOUtils;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import java.io.IOException;
import java.nio.charset.Charset;

public class LicensePanel extends DefaultPanel
{
    @Inject
    public LicensePanel(Settings settings, Navigator navigator)
    {
        setLayout(new MigLayout("fill", "[]", "[grow][]"));
        
        JScrollPane scrollPane = new JScrollPane();
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(UIManager.getFont("Label.font"));
        JButton okButton = new JButton("OK");
        
        String terms;
        try
        {
            terms = IOUtils.toString(ClassLoader.getSystemClassLoader().getResourceAsStream("terms.txt"),
                                     Charset.defaultCharset());
        }
        catch(IOException e)
        {
            terms = "Failed to load ToU";
            okButton.setEnabled(false);
        }
        textArea.setText(terms);
        
        scrollPane.getViewport().add(textArea);
        
        add(scrollPane, "cell 0 0, grow");
        add(okButton, "cell 0 1, right, tag ok");
        
        okButton.addActionListener(e -> {
            settings.setTermsAccepted(true);
            settings.save();
            navigator.showMainView();
        });
    }
}
