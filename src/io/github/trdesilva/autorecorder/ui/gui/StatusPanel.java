/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui;

import io.github.trdesilva.autorecorder.ui.status.StatusConsumer;
import io.github.trdesilva.autorecorder.ui.status.StatusMessage;
import io.github.trdesilva.autorecorder.ui.status.StatusType;
import net.miginfocom.swing.MigLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class StatusPanel extends JPanel implements StatusConsumer
{
    private JLabel messageLabel;
    private MouseListener mouseListener;
    
    public StatusPanel()
    {
        messageLabel = new JLabel("Welcome to Autorecorder");
        setLayout(new MigLayout("fill", "[grow]"));
        add(messageLabel, "growx");
    }
    
    @Override
    public synchronized void post(StatusMessage message)
    {
        messageLabel.setText(message.getMessage());
        messageLabel.setToolTipText(message.getTimestamp().toString());
        switch(message.getType())
        {
            case FAILURE:
                setBackground(Color.RED);
                break;
            case SUCCESS:
                setBackground(Color.GREEN);
                break;
            case WARNING:
                setBackground(Color.YELLOW);
                break;
            case INFO:
                setBackground(Color.CYAN);
                break;
            case DEBUG:
            default:
                setBackground(Color.LIGHT_GRAY);
        }
        String link = message.getLink();
        if(link != null)
        {
            mouseListener = new MouseAdapter()
            {
                @Override
                public void mouseClicked(MouseEvent e)
                {
                    try
                    {
                        Desktop.getDesktop().browse(new URI(link));
                    }
                    catch(URISyntaxException | IOException ex)
                    {
                        post(new StatusMessage(StatusType.DEBUG, "Link navigation failed: " + link));
                    }
                }
            };
            messageLabel.addMouseListener(mouseListener);
            messageLabel.setText(String.format("<html>%s (<a href='%s'>%s</a>)</html>", message.getMessage(), link, link));
            messageLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        else
        {
            messageLabel.removeMouseListener(mouseListener);
            messageLabel.setCursor(Cursor.getDefaultCursor());
        }
    }
}
