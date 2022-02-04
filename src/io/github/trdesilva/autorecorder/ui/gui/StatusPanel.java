package io.github.trdesilva.autorecorder.ui.gui;

import io.github.trdesilva.autorecorder.ui.status.StatusConsumer;
import io.github.trdesilva.autorecorder.ui.status.StatusMessage;
import io.github.trdesilva.autorecorder.ui.status.StatusType;
import net.miginfocom.swing.MigLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import java.awt.Color;

public class StatusPanel extends JPanel implements StatusConsumer
{
    private JLabel messageLabel;
    
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
    }
}
