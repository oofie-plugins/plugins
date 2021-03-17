package net.runelite.client.plugins.oofiechopnfletch;

import com.google.inject.Injector;
import com.google.inject.Provides;
import java.time.Duration;
import java.time.Instant;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuEntry;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import com.owain.chinbreakhandler.ChinBreakHandler;
import net.runelite.client.plugins.iutils.ConditionTimeout;
import net.runelite.client.plugins.iutils.InventoryUtils;
import net.runelite.client.plugins.oofiechopnfletch.tasks.*;
import net.runelite.client.plugins.iutils.iUtils;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;


@Extension
@PluginDependency(iUtils.class)
@PluginDescriptor(
    name = "Oofie's Chop n Fletch",
    enabledByDefault = false,
    description = "Chops n Fletch n Drop",
    tags = {"chop", "n", "fletch", "oofie"}
)
@Slf4j
public class OofieChopnFletchPlugin extends Plugin
{
    @Inject
    private Injector injector;

    @Inject
    private Client client;

    @Inject
    private OofieChopnFletchConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private OofieChopnFletchOverlay overlay;

    @Inject
    private iUtils utils;

    @Inject
    private InventoryUtils inventory;

    @Inject
    public ChinBreakHandler chinBreakHandler;

    @Inject
    private ConfigManager configManager;

    private TaskSet tasks = new TaskSet();
    public static LocalPoint beforeLoc = new LocalPoint(0, 0);

    MenuEntry targetMenu;
    Instant botTimer;
    Player player;

    public static boolean startBot;
    public static long sleepLength;
    public static int tickLength;
    public static int timeout;
    public static String status = "starting...";
    public static ConditionTimeout conditionTimeout;
    public static boolean timeoutFinished;


    @Provides
    OofieChopnFletchConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(OofieChopnFletchConfig.class);
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
            injector.getInstance(CutTask.class),
            injector.getInstance(FletchTask.class),
            injector.getInstance(DropTask.class)
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
        if (!configButtonClicked.getGroup().equalsIgnoreCase("OofieChopnFletch"))
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

                if (timeoutFinished)
                {
                    if (timeout > 0)
                    {
                        return;
                    }

                    Task newTask = tasks.getValidTask();
                    if (newTask != null)
                    {
                        newTask.onGameTick(event);
                        status = task.getTaskDescription();
                    } else
                    {
                        status = "Idle";
                    }

                    timeoutFinished = false;
                }
            }
            else
            {
                status = "Task not found";
                log.debug(status);
            }
            beforeLoc = player.getLocalLocation();
        }
    }
}