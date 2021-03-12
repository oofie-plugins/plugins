package net.runelite.client.plugins.catgrower;

import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.iutils.*;

@Slf4j
public abstract class Task
{
	public Task()
	{
	}

	@Inject
	public Client client;

	@Inject
	public ItemManager itemManager;

	@Inject
	public CatGrowerConfig config;

	@Inject
	public iUtils utils;

	@Inject
	public NPCUtils npc;

	@Inject
	public InventoryUtils inventory;

	@Inject
	public MenuUtils menu;

	@Inject
	public MouseUtils mouse;

	@Inject
	public CalculationUtils calc;

	@Inject
	public PlayerUtils playerUtils;

	@Inject
	public ObjectUtils object;

	public MenuEntry entry;

	public WidgetItem targetItem;

	public MenuEntry targetMenu;




	public abstract boolean validate();

	public long sleepDelay()
	{
		CatGrowerPlugin.sleepLength = calc.randomDelay(config.sleepWeightedDistribution(), config.sleepMin(), config.sleepMax(), config.sleepDeviation(), config.sleepTarget());
		return CatGrowerPlugin.sleepLength;
	}

	public int tickDelay()
	{
		CatGrowerPlugin.tickLength = (int) calc.randomDelay(config.tickDelayWeightedDistribution(), config.tickDelayMin(), config.tickDelayMax(), config.tickDelayDeviation(), config.tickDelayTarget());
		return CatGrowerPlugin.tickLength;
	}

	public String getTaskDescription()
	{
		return this.getClass().getSimpleName();
	}

	public void onGameTick(GameTick event)
	{
		return;
	}

	public void onChatMessage(ChatMessage event) { return; }

}
