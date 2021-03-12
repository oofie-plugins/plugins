package net.runelite.client.plugins.catgrower.tasks;

import net.runelite.api.events.GameTick;
import net.runelite.client.plugins.catgrower.CatGrowerPlugin;
import net.runelite.client.plugins.catgrower.Task;

public class TimeoutTask extends Task
{
	@Override
	public boolean validate()
	{
		return CatGrowerPlugin.timeout > 0;
	}

	@Override
	public String getTaskDescription()
	{
		return "Timeout: " + CatGrowerPlugin.timeout;
	}

	@Override
	public void onGameTick(GameTick event)
	{
		CatGrowerPlugin.timeout--;
	}
}