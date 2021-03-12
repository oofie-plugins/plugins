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
package net.runelite.client.plugins.khazardminer;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.*;
import net.runelite.client.plugins.iutils.*;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static net.runelite.client.plugins.khazardminer.khazardminerState.*;

//PLUGINS

@Extension
@PluginDependency(iUtils.class)
@PluginDescriptor(
	name = "Oofie's Khazardminer",
	enabledByDefault = false,
	description = "Mines ore near fishing trawler and banks",
	tags = {"ore, mining, oofiedoofie"}
)
@Slf4j
public class khazardminerPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private khazardminerConfiguration config;

	@Inject
	private iUtils utils;

	@Inject
	private MouseUtils mouse;

	@Inject
	private PlayerUtils playerUtils;

	@Inject
	private InventoryUtils inventory;

	@Inject
	private InterfaceUtils interfaceUtils;

	@Inject
	private CalculationUtils calc;

	@Inject
	private MenuUtils menu;

	@Inject
	private ObjectUtils object;

	@Inject
	private BankUtils bank;

	@Inject
	private NPCUtils npc;

	@Inject
	private WalkUtils walk;

	@Inject
	private ConfigManager configManager;

	@Inject
	PluginManager pluginManager;

	@Inject
	OverlayManager overlayManager;

	@Inject
	private khazardminerOverlay overlay;


	khazardminerState state;
	GameObject targetObject;
	NPC targetNPC;
	MenuEntry targetMenu;
	WorldPoint skillLocation;
	Instant botTimer;
	LocalPoint beforeLoc;
	Player player;

	WorldArea BANK = new WorldArea(new WorldPoint(2666,3164,0),new WorldPoint(2658,3156,0));
	WorldArea MIDDLE = new WorldArea(new WorldPoint(2664, 3161,0), new WorldPoint(2638, 3166,0));
	WorldArea IRON_ORE = new WorldArea(new WorldPoint(2622,3146,0),new WorldPoint(2629,3153,0));

	int timeout = 0;
	long sleepLength;
	boolean startKhazardMiner;
	private final Set<Integer> itemIds = new HashSet<>();
	private final Set<Integer> requiredIds = new HashSet<>();
	GameObject ironOre;


	@Provides
	khazardminerConfiguration provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(khazardminerConfiguration.class);
	}

	private void resetVals() {
		overlayManager.remove(overlay);
		state = null;
		timeout = 0;
		botTimer = null;
		skillLocation = null;
		startKhazardMiner = false;
	}

	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) {
		if (!configButtonClicked.getGroup().equalsIgnoreCase("khazardminer")) {
			return;
		}
		log.info("button {} pressed!", configButtonClicked.getKey());
		if (configButtonClicked.getKey().equals("startButton")) {
			if (!startKhazardMiner) {
				startKhazardMiner = true;
				state = null;
				targetMenu = null;
				botTimer = Instant.now();
				setLocation();
				overlayManager.add(overlay);
			}
			else
			{
				resetVals();
			}
		}
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("khazardminer"))
		{
			return;
		}
		startKhazardMiner = false;
	}
	public void setLocation()
	{
		if (client != null && client.getLocalPlayer() != null && client.getGameState().equals(GameState.LOGGED_IN))
		{
			skillLocation = client.getLocalPlayer().getWorldLocation();
			beforeLoc = client.getLocalPlayer().getLocalLocation();
		}
		else
		{
			log.debug("Tried to start bot before being logged in");
			skillLocation = null;
			resetVals();
		}
	}
	private long sleepDelay()
	{
		sleepLength = calc.randomDelay(config.sleepWeightedDistribution(), config.sleepMin(), config.sleepMax(), config.sleepDeviation(), config.sleepTarget());
		return sleepLength;
	}

	private int tickDelay()
	{
		int tickLength = (int) calc.randomDelay(config.tickDelayWeightedDistribution(), config.tickDelayMin(), config.tickDelayMax(), config.tickDelayDeviation(), config.tickDelayTarget());
		log.debug("tick delay for {} ticks", tickLength);
		return tickLength;
	}

	private void interactOre()
	{
		ironOre = object.findNearestGameObject(11365,11364);
		utils.doGameObjectActionMsTime(ironOre,MenuAction.GAME_OBJECT_FIRST_OPTION.getId(),sleepDelay());
	}
	private void openBank()
	{
		targetObject = object.findNearestGameObject(10529);
		if (targetObject != null)
		{
			targetMenu = new MenuEntry("", "", targetObject.getId(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), targetObject.getSceneMinLocation().getX(), targetObject.getSceneMinLocation().getY(), false);
			menu.setEntry(targetMenu);
			mouse.delayMouseClick(targetObject.getConvexHull().getBounds(), sleepDelay());
		}
		else
		{
			log.info("Bank is null");
			startKhazardMiner = false;
		}
	}

	public khazardminerState getState() {
		if (timeout > 0) {
			return TIMEOUT;
		}
		if (playerUtils.isMoving(beforeLoc)) {
			timeout = 2 + tickDelay();
			return MOVING;
		}
		if (bank.isDepositBoxOpen()) {
			return getBankState();
		}
		if (inventory.isFull()) {
			return FIND_BANK;
		}
		if (!inventory.isFull() && !client.getLocalPlayer().getWorldArea().intersectsWith(IRON_ORE)) {
			return WALKING_TO_ORE;
		}
		if (!inventory.isFull() && client.getLocalPlayer().getWorldArea().intersectsWith(IRON_ORE)){
			return FIND_ORE;
		}
		return UNHANDLED_STATE;
	}

	@Subscribe
	private void onGameTick(GameTick tick)
	{
		if (!startKhazardMiner)
		{
			return;
		}
		player = client.getLocalPlayer();
		if (client != null && player != null && skillLocation != null)
		{
			if (!client.isResized())
			{
				utils.sendGameMessage("Client must be set to resizable");
				startKhazardMiner = false;
				return;
			}
			state = getState();
			beforeLoc = player.getLocalLocation();
			switch (state)
			{
				case TIMEOUT:
					playerUtils.handleRun(30, 20);
					timeout--;
					break;
				case FIND_ORE:
					interactOre();
					timeout = tickDelay();
					break;
				case ANIMATING:
				case MOVING:
					playerUtils.handleRun(30, 20);
					break;
				case FIND_BANK:
					openBank();
					timeout = tickDelay();
					break;
				case DEPOSIT_ITEMS:
					bank.depositAllOfItem(440);
					break;
				case WALKING_TO_ORE:
					walk.sceneWalk((new WorldPoint(2626 + calc.getRandomIntBetweenRange(0, 1), 3151 + calc.getRandomIntBetweenRange(0, 2), 0)), 1, sleepDelay());
					break;
			}
		}
	}

	private khazardminerState getBankState()
	{
		if(inventory.isFull()){
			return DEPOSIT_ITEMS;
		}
		if(!inventory.isFull()){
			return WALKING_TO_ORE;
		}
		return UNHANDLED_STATE;
	}
	@Subscribe
	private void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN && startKhazardMiner)
		{
			state = TIMEOUT;
			timeout = 2;
		}
	}

	@Subscribe
	private void onMenuOptionClicked(MenuOptionClicked event)
		{
			if(targetMenu!=null){
				event.consume();
				client.invokeMenuAction(targetMenu.getOption(), targetMenu.getTarget(), targetMenu.getIdentifier(), targetMenu.getOpcode(),
						targetMenu.getParam0(), targetMenu.getParam1());
				targetMenu = null;
			}
		log.info(event.toString());
	}
}