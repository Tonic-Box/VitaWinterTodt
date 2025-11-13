package com.tonic.vitawintertodt;

import com.tonic.vitawintertodt.api.WinterAPI;
import com.tonic.vitawintertodt.data.Position;
import com.tonic.vitawintertodt.data.State;
import net.runelite.api.Client;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;
import javax.inject.Inject;

@PluginDescriptor(
        name = "Vita Auto Wintertodt",
        description = "Plays Wintertodt minigame for you",
        tags = {"vita", "wintertodt", "minigame", "auto"}
)
public class WinterTodtPlugin extends Plugin
{
    @Inject
    private ClientToolbar clientToolbar;
    @Inject
    public OverlayManager overlayManager;
    @Inject
    private WinterOverlay overlay;
    private SidePanel panel;
    private NavigationButton navButton;
    private State state;
    private Position position;

    @Override
    protected void startUp()
    {
        panel = injector.getInstance(SidePanel.class);

        navButton = NavigationButton.builder()
                .tooltip("Vita Auto Wintertodt")
                .icon(ImageUtil.loadImageResource(getClass(), "icon.png"))
                .panel(panel)
                .build();

        clientToolbar.addNavigation(navButton);
        overlayManager.add(overlay);
        reset();
    }

    @Override
    protected void shutDown()
    {
        clientToolbar.removeNavigation(navButton);
        overlayManager.remove(overlay);
        reset();
    }

    /**
     * Resets the plugin state.
     */
    private void reset()
    {
        State.PREP.reset();
        State.GAME.reset();
        State.START.reset();
        state = null;
        position = null;
    }

    /**
     * Starts a new round with the given state.
     * @param newState The state to start the new round with.
     */
    private void newRound(State newState)
    {
        position = Position.selectNew(position);
        State.recalc(position);
        state = newState;
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        // Shutdown if stopped and still running
        if(!panel.isRunning() && state != null)
        {
            reset();
            return;
        }

        // Start new round if started but not yet running
        if(panel.isRunning() && state == null)
        {
            newRound(State.START);
        }

        // Stay ALIVE
        if(state == null || WinterAPI.handleWarmth())
        {
            return;
        }

        // Transition to prep when round ends
        if(WinterAPI.getTimer() != 0 && state == State.GAME)
        {
            newRound(State.PREP);
        }

        // Execute current state and transition if needed
        if(!state.getHandler().step())
        {
            state.getHandler().reset();
            state = State.transition(state);
        }
    }

    @Subscribe
    public void onClientTick(ClientTick event)
    {
        overlay.update(state);
    }
}
