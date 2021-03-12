package net.runelite.client.plugins.catgrower.tasks;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Player;
import net.runelite.api.events.GameTick;
import net.runelite.client.plugins.catgrower.CatGrowerPlugin;
import net.runelite.client.plugins.catgrower.Task;

@Slf4j
public class MovingTask extends Task
{

	@Override
	public boolean validate()
	{
		return playerUtils.isMoving(CatGrowerPlugin.beforeLoc);
	}

	@Override
	public String getTaskDescription()
	{
		return CatGrowerPlugin.status;
	}

	@Override
	public void onGameTick(GameTick event)
	{
		Player player = client.getLocalPlayer();
		if (player != null)
		{
			playerUtils.handleRun(20, 30);
			CatGrowerPlugin.timeout = tickDelay();
		}
	}
}