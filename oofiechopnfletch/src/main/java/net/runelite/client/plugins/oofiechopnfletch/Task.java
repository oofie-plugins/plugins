package net.runelite.client.plugins.oofiechopnfletch;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.Menu;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.plugins.iutils.*;
import net.runelite.client.plugins.iutils.bot.Bot;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public abstract class Task
{
    public Task()
    {
    }

    @Inject
    public Client client;

    @Inject
    public OofieChopnFletchConfig config;

    @Inject
    public iUtils utils;

    @Inject
    public MenuUtils menu;

    @Inject
    public MouseUtils mouse;

    @Inject
    public CalculationUtils calc;

    @Inject
    public PlayerUtils playerUtils;

    @Inject
    public InventoryUtils inventory;

    @Inject
    public ObjectUtils object;

    public MenuEntry entry;


    public int logsCut;

    private final Set<Integer> keepItems = new HashSet<>();

    public abstract boolean validate();

    public long sleepDelay()
    {
        OofieChopnFletchPlugin.sleepLength = calc.randomDelay(config.sleepWeightedDistribution(), config.sleepMin(), config.sleepMax(), config.sleepDeviation(), config.sleepTarget());
        return OofieChopnFletchPlugin.sleepLength;
    }

    public int tickDelay()
    {
        OofieChopnFletchPlugin.tickLength = (int) calc.randomDelay(config.tickDelayWeightedDistribution(), config.tickDelayMin(), config.tickDelayMax(), config.tickDelayDeviation(), config.tickDelayTarget());
        return OofieChopnFletchPlugin.tickLength;
    }

    public String getTaskDescription()
    {
        return this.getClass().getSimpleName();
    }

    public void onGameTick(GameTick event)
    {
        return;
    }



    public void knifeOnLog()
    {
        WidgetItem log = inventory.getWidgetItem(config.tree().getLogID());
        WidgetItem knife = inventory.getWidgetItem(ItemID.KNIFE);

        entry = new MenuEntry("", "", log.getId(), MenuAction.ITEM_USE_ON_WIDGET_ITEM.getId(), log.getIndex(), 9764864, false);
        utils.doModifiedActionMsTime(entry, knife.getId(), knife.getIndex(), MenuAction.ITEM_USE_ON_WIDGET_ITEM.getId(), log.getCanvasBounds(), sleepDelay());
        OofieChopnFletchPlugin.timeout = tickDelay();
    }

    public void chooseOption()
    {
        WidgetItem log = inventory.getWidgetItem(config.tree().getLogID());
        Widget option = client.getWidget(270, 0);

        if (option != null) //make sure the skill selection exists before clicking
        {
            entry = new MenuEntry("", "", 1, MenuAction.CC_OP.getId(), -1, 17694730+config.param(), false);

            utils.doActionMsTime(entry, option.getBounds(), sleepDelay());

            OofieChopnFletchPlugin.conditionTimeout = new TimeoutUntil( //We don't do anything until all the logs are fletched, if we still have logs --> fletch again.
                    () -> !inventory.containsItem(log.getId()),
                    () -> playerUtils.isAnimating(), //if we are animation --> extend timeout
                    5);

            OofieChopnFletchPlugin.timeout = tickDelay();
        }
    }

    public Set<Integer> getCraftedItems() { //ID's of all (u) bows
        return Set.of(48, 56, 58, 62, 66, 70, 50, 54, 60, 64, 68, 72);
    }

    public void dropWood()
    {
        keepItems.clear(); //Clean list incase it was changed
        keepItems.addAll(utils.stringToIntList(config.keep())); //Check list for items

        if (inventory.containsItem(getCraftedItems())) {
            inventory.dropAllExcept(keepItems, true, config.dropMin(), config.dropMax());
        }
    }


}