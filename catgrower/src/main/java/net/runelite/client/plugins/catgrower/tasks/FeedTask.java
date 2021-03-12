package net.runelite.client.plugins.catgrower.tasks;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.plugins.catgrower.CatGrowerPlugin;
import net.runelite.client.plugins.catgrower.Task;

import java.awt.*;

@Slf4j
public class FeedTask extends Task {
    Rectangle clickBounds;

    @Override
    public boolean validate()
    {
        return CatGrowerPlugin.needToFeed; //if this boolean defined in CatGrowerPlugin is true
    }

    @Override
    public String getTaskDescription() { return "Feeding Kitten: " + CatGrowerPlugin.timeout; }

    @Override
    public void onGameTick(GameTick event)
    {
        CatGrowerPlugin.timeout--;

        NPC kitten = npc.findNearestNpc("Kitten"); //Using NPC String name to avoid making list of all the Kittens
        WidgetItem catfood = inventory.getWidgetItem(config.catFood());
        Widget whistle = client.getWidget(387, 8);

        if (kitten != null)
        {
            if (client.getLocalPlayer().getWorldLocation().distanceTo(kitten.getWorldLocation()) > 2) {
                mouse.delayMouseClick(whistle.getBounds(), sleepDelay());
            } else {
                if (inventory.containsItem(config.catFood())) //If we have food
                {
                    if (kitten.getInteracting() == client.getLocalPlayer()) //If Kitten is visible + interacting with us
                    {
                        log.info("feeding cat 5");
                        targetMenu = new MenuEntry("", "", kitten.getIndex(), MenuAction.ITEM_USE.getId(), kitten.getIndex(), 9764864, false);
                        sleepDelay();
                        menu.setModifiedEntry(targetMenu, catfood.getId(), catfood.getIndex(), MenuAction.ITEM_USE_ON_NPC.getId());
                        mouse.delayMouseClick(new Point(0, 0), sleepDelay());
                        tickDelay();
                    }
//            CatGrowerPlugin.needToFeed = false;
                }
                CatGrowerPlugin.needToFeed = false;
            }
        }

        else if (!inventory.containsItem(config.catFood())) //If we don't have any food, pick up cat
        {
            log.info("No Food in Inventory.");
            targetMenu = new MenuEntry("", "", kitten.getIndex(), MenuAction.NPC_FIRST_OPTION.getId(), 0, 0, false);
            utils.doActionMsTime(targetMenu, new Point(0, 0),  sleepDelay());
            CatGrowerPlugin.timeout = 1 + tickDelay();
        }
        CatGrowerPlugin.needToFeed = false;
        log.info("false feed 3");
    }
}
