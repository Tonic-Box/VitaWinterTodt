package com.tonic.vitawintertodt.data;

import com.tonic.util.handler.StepHandler;
import com.tonic.vitawintertodt.api.WinterTodtBuilder;
import lombok.Getter;

import java.util.function.Function;

/**
 * Represents the different states of the Wintertodt activity.
 */
public enum State {
    /**
     * Initial state where the player starts the Wintertodt activity.
     */
    START(p -> WinterTodtBuilder.generateStart()),

    /**
     * Preparation state where the player readies themselves for the next round.
     */
    PREP(WinterTodtBuilder::generateSetup),

    /**
     * Active gameplay state where the player engages with the Wintertodt skilling boss.
     */
    GAME(WinterTodtBuilder::generateGameplay);

    private final Function<Position, StepHandler> supplier;
    @Getter
    private StepHandler handler;

    State(Function<Position, StepHandler> supplier) {
        this.supplier = supplier;
    }

    /**
     * Initializes a new run for the given position by creating a new StepHandler.
     *
     * @param position The current position in the Wintertodt activity.
     */
    private void newRun(Position position)
    {
        handler = supplier.apply(position);
    }

    /**
     * Recalculates the StepHandlers for all states based on the supplied position.
     *
     * @param position The current position in the Wintertodt activity.
     */
    public static void recalc(Position position)
    {
        for(State state : values())
        {
            state.newRun(position);
        }
    }

    /**
     * Transitions to the next state based on the current state.
     * @param state The current state.
     * @return The next state after transition.
     */
    public static State transition(State state)
    {
        switch(state)
        {
            case START:
                return State.PREP;
            case PREP:
                return State.GAME;
        }
        return state;
    }

    /**
     * Resets the StepHandler for the current state.
     */
    public void reset()
    {
        if(handler != null)
        {
            handler.reset();
        }
    }
}
