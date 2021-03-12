package net.runelite.client.plugins.catgrower.tasks;

import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.plugins.catgrower.CatGrowerPlugin;
import net.runelite.client.plugins.catgrower.Task;

public class WaitingTask extends Task { ///////NOT USING THIS
    @Override
    public boolean validate()
    {

        return //Use this task if the follow is true
                (npc.findNearestNpc("Kitten") == null
                || !inventory.containsItem(config.catFood())
                );
    }

    @Override
    public String getTaskDescription() { return "Feeding Kitten: " + CatGrowerPlugin.timeout; }

    @Override
    public void onGameTick(GameTick event)
    {
        CatGrowerPlugin.timeout--;

        NPC kitten = npc.findNearestNpc("Kitten"); //Using NPC String name to avoid making list of all the Kittens
        WidgetItem catfood = inventory.getWidgetItem(config.catFood());

        if (inventory.containsItem(config.catFood()))
        {
            if (kitten != null && (kitten.getInteracting() == client.getLocalPlayer()))
            {
                MenuEntry targetMenu = new MenuEntry("", "", kitten.getId(), MenuAction.ITEM_USE_ON_NPC.getId(), 5, 9764864, false);
                utils.doModifiedActionGameTick(targetMenu, catfood.getId(), catfood.getIndex(), MenuAction.ITEM_USE_ON_NPC.getId(), new Point(0, 0), sleepDelay());
                tickDelay();
            } else { utils.sendGameMessage("no cat :/"); }
        }
        else if (!inventory.containsItem(config.catFood())) { utils.sendGameMessage("FOOD NOT FOUND"); }
    }
}
