package com.github.shynixn.petblocks.integrationtests.persistence;

import com.github.shynixn.petblocks.bukkit.logic.compatibility.Factory;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeAll;

import java.io.File;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PlayerMetaSQLiteControllerIT {

    private static Plugin mockPlugin() {
        final YamlConfiguration configuration = new YamlConfiguration();
        configuration.set("sql.enabled", false);
        configuration.set("sql.host", "localhost");
        configuration.set("sql.port", 3306);
        configuration.set("sql.database", "db");
        configuration.set("sql.username", "root");
        configuration.set("sql.password", "");
        final Plugin plugin = mock(Plugin.class);
        if (Bukkit.getServer() == null) {
            final Server server = mock(Server.class);
            when(server.getLogger()).thenReturn(Logger.getGlobal());
            Bukkit.setServer(server);
        }
        new File("PetBlocks.db").delete();
        when(plugin.getDataFolder()).thenReturn(new File("PetBlocks"));
        when(plugin.getConfig()).thenReturn(configuration);
        when(plugin.getResource(any(String.class))).thenAnswer(invocationOnMock -> {
            final String file = invocationOnMock.getArgument(0);
            return Thread.currentThread().getContextClassLoader().getResourceAsStream(file);
        });
        return plugin;
    }

    @BeforeAll
    public static void disableFactory() {
        Factory.disable();
    }

 /*   @Test
    public void insertSelectPlayerMetaTest() throws ClassNotFoundException {
        Factory.initialize(mockPlugin());
        try (PlayerMetaController<Player> controller = Factory.createPlayerDataController()) {
            try (PetMetaController<Player> petController = Factory.createPetDataController()) {
                for (final PetMeta item : petController.getAll()) {
                    petController.remove(item);
                }
            }
            for (final PlayerMeta item : controller.getAll()) {
                controller.remove(item);
            }
            final UUID uniqueId = UUID.randomUUID();
            final PlayerMeta playerMeta = new PlayerData() {
                @Override
                public <T> T getPlayer() {
                    try {
                        return (T) Bukkit.getPlayer(this.getName());
                    } catch (final Exception ex) {
                        return null;
                    }
                }
            };
            assertThrows(IllegalArgumentException.class, () -> controller.store(playerMeta));
            assertEquals(0, controller.size());

            playerMeta.setUuid(uniqueId);
            controller.store(playerMeta);
            assertEquals(0, controller.size());

            playerMeta.setName("Sample");
            controller.store(playerMeta);
            assertEquals(1, controller.size());
            assertEquals(uniqueId, controller.getFromId(playerMeta.getId()).get().getUUID());
        } catch (final Exception e) {
            Logger.getLogger(this.getClass().getSimpleName()).log(Level.WARNING, "Failed to run test.", e);
            Assert.fail();
        }
    }


    @Test
    public void storeLoadPlayerMetaTest() throws ClassNotFoundException {
        Factory.initialize(mockPlugin());
        try (PlayerMetaController<Player> controller = Factory.createPlayerDataController()) {
            try (PetMetaController<Player> petController = Factory.createPetDataController()) {
                for (final PetMeta item : petController.getAll()) {
                    petController.remove(item);
                }
            }
            for (final PlayerMeta item : controller.getAll()) {
                controller.remove(item);
            }
            UUID uniqueId = UUID.randomUUID();
            PlayerMeta playerMeta = new PlayerData() {
                @Override
                public <T> T getPlayer() {
                    try {
                        return (T) Bukkit.getPlayer(this.getName());
                    } catch (final Exception ex) {
                        return null;
                    }
                }
            };
            playerMeta.setName("Second");
            playerMeta.setUuid(uniqueId);
            controller.store(playerMeta);

            assertEquals(1, controller.size());
            playerMeta = controller.getAll().get(0);
            assertEquals(uniqueId, playerMeta.getUUID());
            assertEquals("Second", playerMeta.getName());

            uniqueId = UUID.randomUUID();
            playerMeta.setName("Shynixn");
            playerMeta.setUuid(uniqueId);
            controller.store(playerMeta);

            playerMeta = controller.getAll().get(0);
            assertEquals(uniqueId, playerMeta.getUUID());
            assertEquals("Shynixn", playerMeta.getName());
        } catch (final Exception e) {
            Logger.getLogger(this.getClass().getSimpleName()).log(Level.WARNING, "Failed to run test.", e);
            Assert.fail();
        }
    }*/
}
