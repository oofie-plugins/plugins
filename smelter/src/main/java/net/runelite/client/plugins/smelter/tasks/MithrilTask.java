package net.runelite.client.plugins.smelter.tasks;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Player;
import net.runelite.api.Varbits;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.smelter.BarType;
import net.runelite.client.plugins.smelter.Task;
import net.runelite.client.plugins.smelter.SmelterPlugin;
import net.runelite.client.plugins.smelter.TimeoutUntil;

import static net.runelite.client.plugins.smelter.SmelterPlugin.conditionTimeout;

@Slf4j
public class MithrilTask extends Task
{

    @Override
    public boolean validate()
    {
        return config.BarType().equals(BarType.MITHRIL);
    }

    @Override
    public String getTaskDescription()
    {
        return SmelterPlugin.status;
    }

    @Override
    public void onGameTick(GameTick event)
    {
        Widget lvlup = client.getWidget(WidgetInfo.LEVEL_UP_SKILL);

        if (playerUtils.isAnimating()) {
            SmelterPlugin.status = "Smelting";
        }

        if ( !inv.containsItem(meth) && !bank.isOpen() ) {
            SmelterPlugin.timeout = tickDelay();
            openBank();
            SmelterPlugin.timeout = tickDelay();
        }

        if ( bank.isOpen() ) {

            if ( inv.containsItem(methbar) ) {
                bank.depositAll();

            } else if ( !inv.containsItem(meth) ) {
                if ( bank.contains(meth, 5) ) {
//                    if ( client.getVar(Varbits.WITHDRAW_X_AMOUNT) == 5 )
                    bank.withdrawItemAmount(meth, 5);
                    timeout(meth);
                    SmelterPlugin.timeout = tickDelay();
                } else {
                    utils.sendGameMessage("out of item 1");
                }

            } else if ( !inv.containsItem(coal) && inv.containsItem(meth) ) {
                if ( bank.contains(coal, 23) ) {
                    bank.withdrawAllItem(coal);
                    SmelterPlugin.timeout = tickDelay();
                } else {
                    utils.sendGameMessage("out of item 2");
                }

            } else if (inv.isFull()) {
                usefurnce = true;
                useFurnace();
                SmelterPlugin.timeout = tickDelay();
            }
        }
        //handle lvl up
        if (lvlup != null ) {
            handleLvlUp(steel);
//            if ( inv.containsItem(steel) ) {
//                usefurnce = true;
//                useFurnace();
//                SmelterPlugin.timeout = tickDelay();
//            } else {
//                openBank();
//                SmelterPlugin.timeout = tickDelay();
//            }
        }
        // click furnace
        Widget optionMenu = client.getWidget(270, 4);
        if (usefurnce && optionMenu == null) {
            useFurnace();
            SmelterPlugin.timeout = tickDelay();
        }
        // select bar
        if (optionMenu != null) {
            entry = new MenuEntry("", "", 1, MenuAction.CC_OP.getId(), -1, config.BarType().getParam1(), false );
            utils.doActionMsTime(entry, optionMenu.getBounds(), sleepDelay());
            conditionTimeout = new TimeoutUntil(
                    ()-> !inv.containsItem(adam),
                    ()-> playerUtils.isAnimating(),
                    5);
        }
//        if (isIdle()) {
//            handleIdle(meth);
//        }
    }
}