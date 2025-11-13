package com.tonic.vitawintertodt.data;

import com.tonic.api.entities.TileObjectAPI;
import com.tonic.data.TileObjectEx;
import com.tonic.services.ClickManager;
import com.tonic.util.ClickManagerUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.gameval.ObjectID;

/**
 * BrazierState enum.
 */
@Getter
@RequiredArgsConstructor
public enum BrazierState
{
    NONE(-1, "None", "None"),
    UNLIT(ObjectID.WINT_BRAZIER, "Brazier", "Light"),
    LIT(ObjectID.WINT_BRAZIER_LIT, "Burning brazier", "Feed"),
    DESTROYED(ObjectID.WINT_BRAZIER_BROKEN, "Brazier", "Fix")
    ;

    private final int id;
    private final String name;
    private final String option;

    /**
     * Get the brazier tile object.
     * @return The brazier tile object.
     */
    public TileObjectEx get()
    {
        return TileObjectAPI.search()
                .withAction(option)
                .sortNearest()
                .first();
    }

    /**
     * Interact with the brazier.
     */
    public void interact()
    {
        TileObjectEx brazier = get();
        if(brazier == null)
        {
            return;
        }
        ClickManagerUtil.queueClickBox(brazier);
        TileObjectAPI.interact(brazier, option);
    }

    /**
     * Get the current brazier state.
     * @return The brazier state.
     */
    public static BrazierState getState()
    {
        String option = TileObjectAPI.search()
                .withNames(UNLIT.getName(), LIT.getName(), DESTROYED.getName())
                .sortNearest()
                .first()
                .getActions()[0];

        for (BrazierState state : values())
        {
            if (state.getOption().equals(option))
            {
                return state;
            }
        }
        return NONE;
    }
}