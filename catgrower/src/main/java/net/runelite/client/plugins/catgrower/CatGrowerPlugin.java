/*
 * Copyright (c) 2018, SomeoneWithAnInternetConnection
 * Copyright (c) 2018, oplosthee <https://github.com/oplosthee>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.catgrower;

import com.google.inject.Injector;
import com.google.inject.Provides;
import com.owain.chinbreakhandler.ChinBreakHandler;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.catgrower.tasks.*;
import net.runelite.client.plugins.iutils.*;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.time.Instant;


@Extension
@PluginDependency(iUtils.class)
@PluginDescriptor(
	name = "Oofie's Cat Grower",
	enabledByDefault = false,
	description = "Oofies Cat Grower",
	tags = {"oofie", "doofie", "cat", "grower"}
)


@Slf4j
public class CatGrowerPlugin extends Plugin
{
	@Inject
	private Injector injector;

	@Inject
	private MenuUtils menu;

	@Inject
	private Client client;

	@Inject
	private MouseUtils mouse;

	@Inject
	private CatGrowerConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private CatGrowerOverlay overlay;

	@Inject
	public ChinBreakHandler chinBreakHandler;

	@Inject
	private ConfigManager configManager;

	@Inject
	private NPCUtils npc;

	@Inject
	private InventoryUtils inventory;

	@Inject
	private iUtils utils;

	private TaskSet tasks = new TaskSet();
	public static LocalPoint beforeLoc = new LocalPoint(0, 0);

	MenuEntry targetMenu;
	Instant botTimer;
	Player player;
	public static boolean needToFeed = false;
	public static boolean needToPet = false;
	public static boolean startBot;
	public static long sleepLength;
	public static int tickLength;
	public static int timeout;
	public static String status = "starting...";

	@Provides
    CatGrowerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CatGrowerConfig.class);
	}

	@Override
	protected void startUp()
	{
		chinBreakHandler.registerPlugin(this);
	}

	@Override
	protected void shutDown()
	{
		resetVals();
		chinBreakHandler.unregisterPlugin(this);
	}


	private void loadTasks()
	{
		tasks.clear();
		tasks.addAll(
			injector.getInstance(TimeoutTask.class),
			injector.getInstance(MovingTask.class),
//			injector.getInstance(WaitingTask.class),
				injector.getInstance(FeedTask.class),
				injector.getInstance(PetTask.class)
		);
	}

	public void resetVals()
	{
		log.debug("stopping Task Template plugin");
		overlayManager.remove(overlay);
		chinBreakHandler.stopPlugin(this);
		startBot = false;
		botTimer = null;
		tasks.clear();
	}

	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked)
	{
		if (!configButtonClicked.getGroup().equalsIgnoreCase("OofiesCatGrower"))
		{
			return;
		}
		log.debug("button {} pressed!", configButtonClicked.getKey());
		if (configButtonClicked.getKey().equals("startButton"))
		{
			if (!startBot)
			{
				Player player = client.getLocalPlayer();
				if (client != null && player != null && client.getGameState() == GameState.LOGGED_IN)
				{
					log.info("starting Task Template plugin");
					loadTasks();
					startBot = true;
					chinBreakHandler.startPlugin(this);
					timeout = 0;
					targetMenu = null;
					botTimer = Instant.now();
					overlayManager.add(overlay);
					beforeLoc = client.getLocalPlayer().getLocalLocation();
				}
				else
				{
					log.info("Start logged in");
				}
			}
			else
			{
				resetVals();
			}
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getMessage().equals("Your kitten is very hungry.") || event.getMessage().equals("Your kitten is hungry.") || event.getMessage().toLowerCase().contains("hungry")) {
			needToFeed = true;
			log.info("need to feed  is true");
			return;
		}
		if (event.getMessage().equals("Your kitten wants attention.") || event.getMessage().contains("attention") || event.getMessage().toLowerCase().contains("attention")) {
			needToPet = true;
			log.info("need to pet is true");
			return;
		}
		Task task = tasks.getValidTask();
		if (task != null)
		{
			status = task.getTaskDescription();
			task.onChatMessage(event);
		}
	}

	@Subscribe
	private void onGameTick(GameTick event)
	{
		if (!startBot || chinBreakHandler.isBreakActive(this))
		{
			return;
		}
		player = client.getLocalPlayer();
		if (client != null && player != null && client.getGameState() == GameState.LOGGED_IN)
		{
			if (chinBreakHandler.shouldBreak(this))
			{
				status = "Taking a break";
				chinBreakHandler.startBreak(this);
				timeout = 5;
			}
			Task task = tasks.getValidTask();
			if (task != null)
			{
				status = task.getTaskDescription();
				task.onGameTick(event);
			}
			else
			{
				status = "Task not found";
				log.debug(status);
			}
			beforeLoc = player.getLocalLocation();
		}
	}

	@Subscribe
	public void onCommandExecuted(CommandExecuted event)
	{
		NPC kitten = npc.findNearestNpc("Kitten");
		Widget whistle = client.getWidget(387, 8);

		if (event.getCommand().equalsIgnoreCase("testpet")) needToPet = true;
		if (event.getCommand().equalsIgnoreCase("testfeed")) needToFeed = true;
		if (event.getCommand().equalsIgnoreCase("testpickup"))
		{
			targetMenu = new MenuEntry("", "", kitten.getIndex(), MenuAction.NPC_FIRST_OPTION.getId(), 0, 0, false);
			utils.doActionGameTick(targetMenu, new Point(0, 0), timeout = 2);
		}
		if (event.getCommand().equalsIgnoreCase("testcall"))
		{
			mouse.delayMouseClick(whistle.getBounds(), 300);
		}
	}
}
//MenuOption=Call follower MenuTarget= Id=1 Opcode=57 Param0=-1 Param1=25362439 CanvasX=698 CanvasY=451 Authentic=true