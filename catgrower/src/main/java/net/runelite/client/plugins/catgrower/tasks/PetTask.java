package net.runelite.client.plugins.catgrower.tasks;


import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.catgrower.CatGrowerPlugin;
import net.runelite.client.plugins.catgrower.Task;

import java.awt.*;
import java.util.Arrays;

@Slf4j
public class PetTask extends Task {
    @Override
    public boolean validate()
    {
        return
                CatGrowerPlugin.needToPet; //if this boolean defined in CatGrowerPlugin is true
    }

    @Override
    public String getTaskDescription()
    {
        return "Giving Cat Some Scritches: " + CatGrowerPlugin.timeout;
    }

    @Override
    public void onGameTick(GameTick event) {
        CatGrowerPlugin.timeout--;


        Widget dialog = client.getWidget(219,1);
        NPC kitten = npc.findNearestNpc("Kitten");

        if (kitten != null && kitten.getInteracting() == client.getLocalPlayer())
        {
            if ( dialog == null )
            {
                //!REMOVED MenuEntry - Does it change anything?
                targetMenu = new MenuEntry("", "", kitten.getIndex(), 13, 0, 0, false);
                utils.doActionMsTime(targetMenu, new Point(0, 0), sleepDelay());
                log.info("clicked on pet");
                tickDelay();
                return;
            }
            if (dialog.getChildren()[1].getText().toLowerCase().contains("stroke") ) {
                MenuEntry targetMenu = new MenuEntry("", "", 0, 30, 1, 14352385, false);
                utils.doActionMsTime(targetMenu, new Point(0, 0), sleepDelay());
                log.info("Selected stroke pet");
                tickDelay();
            }
            CatGrowerPlugin.needToPet = false;
            log.info("false pet 1");
        }
        CatGrowerPlugin.needToPet = false;
        log.info("false pet 2");
    }

}
