/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui;

import com.google.common.collect.Sets;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SingleSelectionModel;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Graphics;
import java.util.Set;

public class GameListPanel extends JPanel
{
    private JLabel listLabel;
    private JTextField gameEntryField;
    private JButton addButton;
    private JScrollPane scrollPane;
    private DefaultListModel<String> gameListModel;
    private JList<String> gameList;
    private JButton removeButton;
    
    public GameListPanel(Set<String> games, String label, String tooltip)
    {
        setLayout(new MigLayout("fill", "[grow][]", "[][][grow]"));
        setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        listLabel = new JLabel(label);
        listLabel.setToolTipText(tooltip);
        gameEntryField = new JTextField();
        addButton = new JButton("Add");
        scrollPane = new JScrollPane();
        gameListModel = new DefaultListModel<>();
        gameListModel.addAll(games);
        gameList = new JList<>(gameListModel);
        removeButton = new JButton("Remove");
        
        scrollPane.getViewport().add(gameList);
        
        add(listLabel, "cell 0 0");
        add(gameEntryField, "cell 0 1, growx");
        add(addButton, "cell 1 1, growx");
        add(scrollPane, "cell 0 2, grow");
        add(removeButton, "cell 1 2, top");
        
        addButton.addActionListener(e -> {
            if(!gameEntryField.getText().isBlank())
            {
                gameListModel.addElement(gameEntryField.getText());
                gameEntryField.setText("");
            }
        });
        
        removeButton.addActionListener(e -> {
            int selection = gameList.getSelectedIndex();
            while(selection != -1)
            {
                gameListModel.remove(selection);
                selection = gameList.getSelectedIndex();
            }
        });
    }
    
    public Set<String> getGames()
    {
        String[] temp = new String[gameListModel.getSize()];
        gameListModel.copyInto(temp);
        return Sets.newHashSet(temp);
    }
}
