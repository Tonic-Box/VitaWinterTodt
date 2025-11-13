package com.tonic.vitawintertodt;

import com.tonic.model.ui.components.FancyButton;
import com.tonic.model.ui.components.FancyCard;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;

/**
 * The side panel for the Wintertodt plugin.
 */
public class SidePanel extends PluginPanel
{
    private final JButton startStopButton;
    private boolean isRunning = false;

    @Inject
    public SidePanel()
    {
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.insets = new Insets(0, 0, 10, 0);

        FancyCard card = new FancyCard("Wintertodt", "Plays Wintertodt minigame for you.");
        add(card, c);
        c.gridy++;

        startStopButton = new FancyButton("Start");
        startStopButton.setFocusable(false);
        startStopButton.addActionListener(e -> {
            isRunning = !isRunning;
            startStopButton.setBackground(isRunning ? Color.RED : new Color(64, 169, 211));
            startStopButton.setText(isRunning ? "Stop" : "Start");
        });

        add(startStopButton, c);
    }

    /**
     * Returns whether the bot is currently running.
     *
     * @return true if the bot is running, false otherwise
     */
    public boolean isRunning()
    {
        return isRunning;
    }
}
