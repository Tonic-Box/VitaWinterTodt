package com.tonic.vitawintertodt.api;

import com.tonic.Static;
import com.tonic.api.game.SkillAPI;
import com.tonic.api.game.VarAPI;
import com.tonic.api.widgets.BankAPI;
import com.tonic.api.widgets.InventoryAPI;
import com.tonic.api.widgets.WidgetAPI;
import com.tonic.data.ItemEx;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarbitID;
import java.util.HashMap;
import java.util.Map;

/**
 * A collection of util methods and one-off constants for
 * interacting with the Wintertodt minigame.
 */
public class WinterAPI
{
    public static final WorldPoint BANK = new WorldPoint(1640, 3944, 0);
    public static final WorldPoint WAITING_AREA = new WorldPoint(1631, 3981, 0);
    public  static final Map<Integer,Integer> AXES = new HashMap<>() {{
        put(ItemID.BRONZE_AXE, 1);
        put(ItemID.IRON_AXE, 1);
        put(ItemID.STEEL_AXE, 6);
        put(ItemID.BLACK_AXE, 11);
        put(ItemID.MITHRIL_AXE, 21);
        put(ItemID.ADAMANT_AXE, 31);
        put(ItemID.RUNE_AXE, 41);
        put(ItemID.DRAGON_AXE, 61);
        put(ItemID.INFERNAL_AXE, 61);
        put(ItemID.CRYSTAL_AXE, 71);
    }};

    public static int[] rejuvenationPotionIds = {
            ItemID.WINT_POTION1,
            ItemID.WINT_POTION2,
            ItemID.WINT_POTION3,
            ItemID.WINT_POTION4
    };

    /**
     * Gets the current warmth level of the player in Wintertodt.
     *
     * @return The current warmth level, or Integer.MAX_VALUE if it cannot be determined.
     */
    public static int getWarmth()
    {
        try
        {
            String text = WidgetAPI.getText(InterfaceID.WintStatus.WARMTH_TITLE);
            if (text == null || text.isBlank())
            {
                return Integer.MAX_VALUE;
            }
            return Integer.parseInt(text.replaceAll("\\D+", ""));
        }
        catch (Exception e)
        {
            return Integer.MAX_VALUE;
        }
    }

    /**
     * Gets the respawn timer for Wintertodt.
     *
     * @return The respawn timer in ticks.
     */
    public static int getTimer()
    {
        return VarAPI.getVar(VarbitID.WINT_TRANSMIT_RESPAWNDELAY);
    }

    /**
     * Handles the player's warmth by consuming a Rejuvenation potion if the warmth is below 65.
     *
     * @return true if a potion was consumed, false otherwise.
     */
    public static boolean handleWarmth()
    {
        if(getWarmth() < 65)
        {
            ItemEx pot = InventoryAPI.getItem("Rejuvenation");
            if(pot != null)
            {
                InventoryAPI.interact(pot, 2);
                return true;
            }
        }
        return false;
    }

    /**
     * Decides the best axe the player can use based on their woodcutting level and the axes they have in their bank.
     *
     * @return The item ID of the best axe the player can use, or -1 if no suitable axe is found.
     */
    public static int decideBestAxe() {
        return Static.invoke(() -> {
            int bestAxe = -1;
            int bestAxeLevel = 0;
            int woodcuttingLevel = SkillAPI.getLevel(Skill.WOODCUTTING);

            for (Map.Entry<Integer, Integer> entry : AXES.entrySet()) {
                int axeId = entry.getKey();
                int requiredLevel = entry.getValue();
                if (woodcuttingLevel >= requiredLevel && BankAPI.contains(axeId)) {
                    if (requiredLevel > bestAxeLevel) {
                        bestAxe = axeId;
                        bestAxeLevel = requiredLevel;
                    }
                }
            }

            return bestAxe;
        });
    }
}
