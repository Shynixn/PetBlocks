package com.github.shynixn.business.logic.persistence2;

import com.github.shynixn.petblocks.business.Config;
import com.github.shynixn.petblocks.business.logic.configuration.ConfigCommands;
import com.github.shynixn.petblocks.business.logic.persistence2.Factory;
import com.github.shynixn.petblocks.business.logic.persistence2.IPetDataController;
import com.github.shynixn.petblocks.business.logic.persistence2.PetData;
import com.github.shynixn.petblocks.business.logic.persistence2.PlayerData;
import com.github.shynixn.petblocks.business.logic.persistence2.commandexecutor.PetDataCommandExecutor;
import com.github.shynixn.petblocks.lib.util.IDatabaseController;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.logging.Logger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Shynixn
 */
public class PetCommandExecutorTest {

    @Before
    public void prepareServer() {
        BukkitScheduler scheduler = mock(BukkitScheduler.class);
        when(scheduler.runTaskAsynchronously(any(Plugin.class), any(Runnable.class))).thenAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                System.out.println("GOT YOU");
                Runnable runnable = invocationOnMock.getArgument(1);
                runnable.run();
                return null;
            }
        });
        Server server = mock(Server.class);
        Logger logger = Logger.getLogger("TestLogger");
        when(server.getLogger()).thenReturn(logger);
        when(server.getScheduler()).thenReturn(scheduler);
        try {
            Bukkit.setServer(server);
        } catch (Exception ex) {

        }
    }

    @Test
    public void renamePetTest() {
        Plugin plugin = mockPlugin();
        Factory.enable(plugin);
        Player player = mockPlayer();
        try (IPetDataController controller = Factory.createPetDataController();
             IDatabaseController<PlayerData> playerController = Factory.createPlayerDataController()) {
            for (PetData petData : controller.getAll()) {
                controller.remove(petData);
            }
            for (PlayerData playerData : playerController.getAll()) {
                playerController.remove(playerData);
            }

            PlayerData playerData = PlayerData.from(player);
            playerController.store(playerData);
            PetData petData = PetData.from(playerData);
            petData.setName("Maier");
            controller.store(petData);

            PetDataCommandExecutor petDataCommandExecutor = new PetDataCommandExecutor(plugin);
            petDataCommandExecutor.onCommandSend(player, new String[]{"rename", "Gerhard"});
            petDataCommandExecutor.onCommandSend(player, new String[]{"rename", "22222222222222222222222222222222222222222"});
            petDataCommandExecutor.onCommandSend(player, new String[]{null,null});
            petDataCommandExecutor.onCommandSend(player, new String[]{"rename",null});

            Assert.assertNotNull(controller.getByPlayer(player));
            Assert.assertEquals(controller.getByPlayer(player).getName(), "Gerhard");
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    private static Player mockPlayer() {
        UUID uuid = UUID.randomUUID();
        Player player = mock(Player.class);
        when(player.getName()).thenReturn("Shynixn");
        when(player.getUniqueId()).thenReturn(uuid);
        when(player.hasPermission(any(String.class))).thenReturn(true);
        return player;
    }

    private static Plugin mockPlugin() {
        YamlConfiguration configuration = new YamlConfiguration();
        try {
            configuration.load(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("config.yml")));
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        Plugin plugin = mock(Plugin.class);
        when(plugin.getDataFolder()).thenReturn(new File("PetBlocks"));
        when(plugin.getConfig()).thenReturn(configuration);
        when(plugin.getResource(any(String.class))).thenAnswer(new Answer<InputStream>() {
            @Override
            public InputStream answer(InvocationOnMock invocationOnMock) throws Throwable {
                String file = invocationOnMock.getArgument(0);
                return Thread.currentThread().getContextClassLoader().getResourceAsStream(file);
            }
        });
        Config.initiliaze(plugin);
        ConfigCommands.getInstance().load(plugin.getConfig());
        return plugin;
    }
}
