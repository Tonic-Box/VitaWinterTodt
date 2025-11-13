package com.tonic.vitawintertodt.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.coords.WorldPoint;

/**
 * Represents the possible positions in the Wintertodt minigame.
 */
@RequiredArgsConstructor
@Getter
public enum Position
{
    EAST(
            new WorldPoint(1638, 3996, 0),
            new WorldPoint(1638, 3988, 0),
            new WorldPoint(1649, 4006, 0)
    ),
    WEST(
            new WorldPoint(1622, 3996, 0),
            new WorldPoint(1622, 3988, 0),
            new WorldPoint(1611, 4006, 0)
    )
    ;

    private final WorldPoint brazier;
    private final WorldPoint tree;
    private final WorldPoint herbRoots;

    /**
     * Selects a new position based on the given position with a bias
     * towards staying in the same position.
     * @param position The current position, or null to select randomly.
     * @return The newly selected position.
     */
    public static Position selectNew(Position position) {
        return position == null
                ? (Math.random() < 0.5 ? EAST : WEST)
                : (Math.random() < 0.8 ? position : (position == EAST ? WEST : EAST));
    }
}
