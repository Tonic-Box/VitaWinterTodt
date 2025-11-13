package com.tonic.vitawintertodt.api;

import com.tonic.api.entities.PlayerAPI;
import com.tonic.api.entities.TileObjectAPI;
import com.tonic.api.game.MovementAPI;
import com.tonic.api.game.SkillAPI;
import com.tonic.api.handlers.BankBuilder;
import com.tonic.api.widgets.BankAPI;
import com.tonic.api.widgets.InventoryAPI;
import com.tonic.data.ItemEx;
import com.tonic.data.LayoutView;
import com.tonic.data.TileObjectEx;
import com.tonic.services.GameManager;
import com.tonic.util.ClickManagerUtil;
import com.tonic.util.handler.AbstractHandlerBuilder;
import com.tonic.util.handler.StepHandler;
import com.tonic.vitawintertodt.data.BrazierState;
import com.tonic.vitawintertodt.data.Position;
import net.runelite.api.Skill;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.ObjectID;

/**
 * WinterTodtBuilder class.
 */
public class WinterTodtBuilder extends AbstractHandlerBuilder<WinterTodtBuilder>
{
    /**
     * Generates the starting handler for Wintertodt.
     * @return The starting StepHandler.
     */
    public static StepHandler generateStart()
    {
        return new WinterTodtBuilder()
                .bank()
                .walkWaitingArea()
                .waitForReset()
                .build();
    }

    /**
     * Generates the setup handler for Wintertodt.
     * @param position The position to use.
     * @return The setup StepHandler.
     */
    public static StepHandler generateSetup(Position position)
    {
        return new WinterTodtBuilder()
                .waitForReset()
                .prep(position)
                .walkToBrazier(position)
                .waitForGame()
                .build();
    }

    /**
     * Generates the gameplay handler for Wintertodt.
     * @param position The position to use.
     * @return The gameplay StepHandler.
     */
    public static StepHandler generateGameplay(Position position)
    {
        return new WinterTodtBuilder()
                .walkToTree(position)
                .chopTree()
                .fletch(position)
                .offerWood()
                .build();
    }

    private WinterTodtBuilder walkToBrazier(Position position)
    {
        walkTo(position.getBrazier());
        return this;
    }

    private WinterTodtBuilder walkToTree(Position position)
    {
        walkTo(position.getTree());
        return this;
    }

    private WinterTodtBuilder walkWaitingArea()
    {
        walkTo(WinterAPI.WAITING_AREA);
        return this;
    }

    private WinterTodtBuilder waitForReset()
    {
        addDelayUntil(() -> WinterAPI.getTimer() > 0);
        return this;
    }

    private WinterTodtBuilder waitForGame()
    {
        add(context -> {
            context.put("fmxp", SkillAPI.getExperience(Skill.FIREMAKING));
        });
        addDelayUntil(() -> WinterAPI.getTimer() == 0);
        addDelayUntil(context -> {
            BrazierState brazier = BrazierState.getState();
            if(brazier == BrazierState.LIT)
            {
                context.remove("fmxp");
                return true;
            }
            int oldFmxp = context.get("fmxp");
            int newFmxp = SkillAPI.getExperience(Skill.FIREMAKING);
            if(newFmxp > oldFmxp)
            {
                context.remove("fmxp");
                return true;
            }
            brazier.interact();
            return false;
        });
        return this;
    }

    private WinterTodtBuilder offerWood()
    {
        addDelayUntil(context -> {
            if(!InventoryAPI.containsAny(ItemID.WINT_BRUMA_KINDLING, ItemID.WINT_BRUMA_ROOT))
            {
                context.remove("last_tick");
                context.remove("fmxp");
                return true;
            }

            if(context.get("fmxp") == null)
            {
                context.put("last_tick", GameManager.getTickCount());
                context.put("fmxp", SkillAPI.getExperience(Skill.FIREMAKING));
            }

            BrazierState state = BrazierState.getState();
            switch(state)
            {
                case LIT:
                    int lastFmxp = context.get("fmxp");
                    int currentFmxp = SkillAPI.getExperience(Skill.FIREMAKING);
                    if(currentFmxp > lastFmxp)
                    {
                        context.put("last_tick", GameManager.getTickCount());
                        context.put("fmxp", currentFmxp);
                        break;
                    }

                    if(GameManager.getTickCount() - (int) context.get("last_tick") < 4)
                    {
                        break;
                    }

                    context.put("last_tick", GameManager.getTickCount());
                    context.put("fmxp", currentFmxp);
                    state.interact();
                    break;
                case UNLIT:
                case DESTROYED:
                    state.interact();
                    break;
            }
            return false;
        });
        return this;
    }

    private WinterTodtBuilder chopTree()
    {
        addDelayUntil(() -> {
            if(InventoryAPI.count(ItemID.WINT_BRUMA_ROOT) >= 10)
                return true;

            if(PlayerAPI.isIdle())
            {
                TileObjectEx tree = TileObjectAPI.search()
                        .withName("Bruma roots")
                        .nearest();
                ClickManagerUtil.queueClickBox(tree);
                TileObjectAPI.interact(tree, "Chop");
            }
            return false;
        });
        return this;
    }

    private WinterTodtBuilder fletch(Position position)
    {
        add(context -> {
            MovementAPI.walkToWorldPoint(position.getBrazier());
            context.put("last_tick", 0);
            context.put("fxp", SkillAPI.getExperience(Skill.FLETCHING));
        });
        addDelayUntil(context -> {
            if(!InventoryAPI.contains(ItemID.WINT_BRUMA_ROOT))
            {
                context.remove("last_tick");
                context.remove("fxp");
                return true;
            }

            if(!MovementAPI.isMoving() && !PlayerAPI.getLocal().getWorldLocation().equals(position.getBrazier()))
                MovementAPI.walkToWorldPoint(position.getBrazier());

            int lastFxp = context.get("fxp");
            int currentFxp = SkillAPI.getExperience(Skill.FLETCHING);

            if(currentFxp > lastFxp)
            {
                context.put("last_tick", GameManager.getTickCount());
                context.put("fxp", currentFxp);
                return false;
            }

            if(GameManager.getTickCount() - (int) context.get("last_tick") < 4)
            {
                return false;
            }

            context.put("last_tick", GameManager.getTickCount());
            ItemEx knife = InventoryAPI.getItem(ItemID.KNIFE);
            ItemEx root = InventoryAPI.getItem(ItemID.WINT_BRUMA_ROOT);
            ClickManagerUtil.queueClickBox(LayoutView.SIDE_MENU.getWidget());
            InventoryAPI.useOn(knife, root);
            return false;
        });
        return this;
    }

    private WinterTodtBuilder prep(Position position)
    {
        add("start", context -> {
            if(!InventoryAPI.contains(ItemID.TINDERBOX))
            {
                return jump("tinderbox", context);
            }
            if(!InventoryAPI.contains(ItemID.KNIFE))
            {
                return jump("knife", context);
            }
            if(!InventoryAPI.contains(ItemID.HAMMER))
            {
                return jump("hammer", context);
            }
            if(InventoryAPI.getItem(" axe") == null)
            {
                return jump("axe", context);
            }
            if(InventoryAPI.count(WinterAPI.rejuvenationPotionIds) < 2)
                return jump("pots", context);
            return jump("end", context);
        });
        add("tinderbox", context -> {
            if(InventoryAPI.contains(ItemID.TINDERBOX))
                return jump("start", context);
            TileObjectEx crate = TileObjectAPI.search()
                    .withId(ObjectID.WINT_CHEST_TINDERBOX)
                    .nearest();
            ClickManagerUtil.queueClickBox(crate);
            TileObjectAPI.interact(crate, 0);
            return jump("start", context);
        });
        add("knife", context -> {
            if(InventoryAPI.contains(ItemID.KNIFE))
                return jump("start", context);
            TileObjectEx crate = TileObjectAPI.search()
                    .withId(ObjectID.WINT_CHEST_KNIFE)
                    .nearest();
            ClickManagerUtil.queueClickBox(crate);
            TileObjectAPI.interact(crate, 0);
            return jump("start", context);
        });
        add("hammer", context -> {
            if(InventoryAPI.contains(ItemID.HAMMER))
                return jump("start", context);
            TileObjectEx crate = TileObjectAPI.search()
                    .withId(ObjectID.WINT_CHEST_HAMMER)
                    .nearest();
            ClickManagerUtil.queueClickBox(crate);
            TileObjectAPI.interact(crate, 0);
            return jump("start", context);
        });
        add("axe", context -> {
            if(InventoryAPI.contains(ItemID.BRONZE_AXE))
                return jump("start", context);
            TileObjectEx crate = TileObjectAPI.search()
                    .withId(ObjectID.WINT_CHEST_AXE)
                    .nearest();
            ClickManagerUtil.queueClickBox(crate);
            TileObjectAPI.interact(crate, 0);
            return jump("start", context);
        });
        addDelayUntil("pots", () -> {
            if(InventoryAPI.count(ItemID.WINT_VIAL) >= 5)
                return true;
            TileObjectEx crate = TileObjectAPI.search()
                    .withId(ObjectID.WINT_CHEST_VIAL)
                    .nearest();
            ClickManagerUtil.queueClickBox(crate);
            TileObjectAPI.interact(crate, 1);
            return false;
        });
        walkTo(position.getHerbRoots());
        add(() -> {
            TileObjectEx roots = TileObjectAPI.search()
                    .withId(ObjectID.WINT_HERB_ROOTS)
                    .nearest();
            ClickManagerUtil.queueClickBox(roots);
            TileObjectAPI.interact(roots, 0);
        });
        addDelayUntil(() -> InventoryAPI.count(ItemID.WINT_HERB) >= 5);
        addDelayUntil("end", () -> {
            boolean atBrazier = PlayerAPI.getLocal().getWorldLocation().equals(position.getBrazier());

            if (!atBrazier && !MovementAPI.isMoving())
                MovementAPI.walkToWorldPoint(position.getBrazier());

            if (!InventoryAPI.contains(ItemID.WINT_HERB))
                return atBrazier;

            ClickManagerUtil.queueClickBox(LayoutView.SIDE_MENU.getWidget());
            InventoryAPI.useOn(
                    InventoryAPI.getItem(ItemID.WINT_HERB),
                    InventoryAPI.getItem(ItemID.WINT_VIAL)
            );
            return false;
        });
        addDelayUntil(() -> !MovementAPI.isMoving());
        return this;
    }

    private WinterTodtBuilder bank()
    {
        BankBuilder builder = BankBuilder.get()
                .open()
                .depositInventory()
                .add(context -> {
                    int bestAxe = WinterAPI.decideBestAxe();
                    BankAPI.withdraw(bestAxe, 1, false);
                    speedUp(context);
                })
                .withdraw(
                        false,
                        BankBuilder.BankItem.of(
                                ItemID.KNIFE, 1,
                                ItemID.TINDERBOX, 1,
                                ItemID.HAMMER, 1
                        )
                );
        walkTo(WinterAPI.BANK);
        append(builder);
        return this;
    }
}
