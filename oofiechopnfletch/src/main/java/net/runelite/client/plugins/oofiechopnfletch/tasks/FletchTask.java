package net.runelite.client.plugins.oofiechopnfletch.tasks;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Player;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.iutils.TimeoutUntil;
import net.runelite.client.plugins.oofiechopnfletch.Task;
import net.runelite.client.plugins.oofiechopnfletch.OofieChopnFletchPlugin;

@Slf4j
public class FletchTask extends Task
{

    @Override
    public boolean validate()
    {
        int logs = config.tree().getLogID();
        return inventory.containsItem(logs);
    }

    @Override
    public String getTaskDescription()
    {
        return OofieChopnFletchPlugin.status = "Fletching";
    }

    @Override
    public void onGameTick(GameTick event)
    {
        int logs = config.tree().getLogID();
        Widget option = client.getWidget(270, 0);
        Player player = client.getLocalPlayer();

        if (logsCut == 0) {
            logsCut = inventory.getItemCount(config.tree().getLogID(), false);
        }

        if (player != null)
        {
            if (option == null && !playerUtils.isAnimating()) {
                knifeOnLog();
            } else if (option != null) {
                chooseOption();

                OofieChopnFletchPlugin.conditionTimeout = new TimeoutUntil(
                        ()-> !inventory.containsItem(logs),
                        ()-> playerUtils.isAnimating(),
                        5);
            }
        }
    }
}