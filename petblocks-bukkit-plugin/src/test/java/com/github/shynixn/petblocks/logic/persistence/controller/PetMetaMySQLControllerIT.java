package com.github.shynixn.petblocks.logic.persistence.controller;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import com.github.shynixn.petblocks.api.persistence.controller.ParticleEffectMetaController;
import com.github.shynixn.petblocks.api.persistence.controller.PetMetaController;
import com.github.shynixn.petblocks.api.persistence.controller.PlayerMetaController;
import com.github.shynixn.petblocks.api.persistence.entity.ParticleEffectMeta;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.api.persistence.entity.PlayerMeta;
import com.github.shynixn.petblocks.bukkit.PetBlocksPlugin;
import com.github.shynixn.petblocks.bukkit.logic.Factory;
import com.github.shynixn.petblocks.bukkit.logic.persistence.entity.EngineData;
import com.github.shynixn.petblocks.bukkit.logic.persistence.entity.PetData;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PetMetaMySQLControllerIT {

    private static Plugin mockPlugin() {
        final YamlConfiguration configuration = new YamlConfiguration();
        configuration.set("sql.enabled", false);
        configuration.set("sql.host", "localhost");
        configuration.set("sql.port", 3306);
        configuration.set("sql.database", "db");
        configuration.set("sql.username", "root");
        configuration.set("sql.password", "");
        try {
            final Field field = PetBlocksPlugin.class.getDeclaredField("logger");
            field.setAccessible(true);
            field.set(null, Logger.getGlobal());
        } catch (IllegalAccessException | NoSuchFieldException e) {
           Assert.fail();
        }
        final Plugin plugin = mock(Plugin.class);
        new File("PetBlocks.db").delete();
        when(plugin.getDataFolder()).thenReturn(new File("PetBlocks"));
        when(plugin.getConfig()).thenReturn(configuration);
        when(plugin.getResource(any(String.class))).thenAnswer(invocationOnMock -> {
            final String file = invocationOnMock.getArgument(0);
            return Thread.currentThread().getContextClassLoader().getResourceAsStream(file);
        });
        return plugin;
    }


    private static DB database;

    @AfterAll
    public static void stopMariaDB()
    {
        try {
            database.stop();
        } catch (final ManagedProcessException e) {
            Logger.getLogger(PetMetaMySQLControllerIT.class.getSimpleName()).log(Level.WARNING, "Failed stop maria db.", e);
        }
    }

    @BeforeAll
    public static void startMariaDB() {
        try {
            Factory.disable();
            database = DB.newEmbeddedDB(3306);
            database.start();
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/?user=root&password=")) {
                try (Statement statement = conn.createStatement()) {
                    statement.executeUpdate("CREATE DATABASE db");
                }
            }
        } catch (SQLException | ManagedProcessException e) {
            Logger.getLogger(PetMetaMySQLControllerIT.class.getSimpleName()).log(Level.WARNING, "Failed start maria db.", e);
        }
    }

    @Test
    public void insertSelectPlayerMetaTest() throws ClassNotFoundException {
        final Plugin plugin = mockPlugin();
        plugin.getConfig().set("sql.enabled", true);
        Factory.initialize(mockPlugin());
        final UUID uuid = UUID.randomUUID();
        final Player player = mock(Player.class);
        when(player.getName()).thenReturn("Shynixn");
        when(player.getUniqueId()).thenReturn(uuid);
        try (PetMetaController controller = Factory.createPetDataController()) {
            try (ParticleEffectMetaController particleController = Factory.createParticleEffectController()) {
                try (PlayerMetaController playerController = Factory.createPlayerDataController()) {
                    for (final PetMeta item : controller.getAll()) {
                        controller.remove(item);
                    }
                    final PetData meta = new PetData();
                    meta.setPetDisplayName("Notch");
                    assertThrows(IllegalArgumentException.class, () -> controller.store(meta));
                    assertEquals(0, controller.size());

                    final ParticleEffectMeta particleEffectMeta = particleController.create();
                    particleEffectMeta.setEffectType(ParticleEffectMeta.ParticleEffectType.END_ROD);
                    particleController.store(particleEffectMeta);
                    meta.setParticleEffectMeta(particleEffectMeta);

                    assertThrows(IllegalArgumentException.class, () -> controller.store(meta));
                    assertEquals(0, controller.size());

                    final PlayerMeta playerMeta = playerController.create(player);
                    playerController.store(playerMeta);
                    meta.setPlayerMeta(playerMeta);
                    assertThrows(IllegalArgumentException.class, () -> controller.store(meta));

                    meta.setEngineId(1);
                    assertThrows(IllegalArgumentException.class, () -> controller.store(meta));

                    meta.setSkin(Material.STONE.getId(), (short) 5, null, false);
                    meta.setEngine(new EngineData(4));
                    controller.store(meta);

                    assertEquals(1, controller.size());
                }
            }
        } catch (final Exception e) {
            Logger.getLogger(this.getClass().getSimpleName()).log(Level.WARNING, "Failed to run test.", e);
            Assert.fail();
        }
    }


    @Test
    public void storeLoadPlayerMetaTest() throws ClassNotFoundException {
        final Plugin plugin = mockPlugin();
        plugin.getConfig().set("sql.enabled", true);
        Factory.initialize(mockPlugin());
        final UUID uuid = UUID.randomUUID();
        final Player player = mock(Player.class);
        when(player.getName()).thenReturn("Shynixn");
        when(player.getUniqueId()).thenReturn(uuid);
        try (PetMetaController controller = Factory.createPetDataController()) {
            try (ParticleEffectMetaController particleController = Factory.createParticleEffectController()) {
                try (PlayerMetaController playerController = Factory.createPlayerDataController()) {
                    for (final PetMeta item : controller.getAll()) {
                        controller.remove(item);
                    }
                    PetData meta = new PetData();
                    meta.setPetDisplayName("Me");
                    meta.setSkin(Material.BIRCH_DOOR_ITEM.getId(),5 , "This is my long skin.",true);
                    meta.setEnabled(true);
                    meta.setAge(500);
                    meta.setSoundEnabled(true);

                    final ParticleEffectMeta particleEffectMeta = particleController.create();
                    particleEffectMeta.setEffectType(ParticleEffectMeta.ParticleEffectType.END_ROD);
                    particleController.store(particleEffectMeta);
                    meta.setParticleEffectMeta(particleEffectMeta);

                    final PlayerMeta playerMeta = playerController.create(player);
                    playerController.store(playerMeta);
                    meta.setPlayerMeta(playerMeta);
                    meta.setEngine(new EngineData(0));
                    controller.store(meta);

                    assertEquals(1, controller.size());
                    meta = (PetData) controller.getById(meta.getId());
                    assertEquals("Me", meta.getPetDisplayName());
                    assertEquals(Material.BIRCH_DOOR_ITEM.getId(), meta.getItemId());
                    assertEquals((short)5, meta.getItemDamage());
                    assertEquals("This is my long skin.", meta.getSkin());
                    assertEquals(0, meta.getEngineId());
                    assertEquals(true, meta.isEnabled());
                    assertEquals(500, meta.getAge());
                    assertEquals(true, meta.isItemStackUnbreakable());
                    assertEquals(true, meta.isItemStackUnbreakable());

                    meta.setPetDisplayName("PikaPet");
                    meta.setSkin(Material.ARROW.getId(),(short)7 , "http://Skin.com", false);
                    meta.setEngineId(2);
                    meta.setEnabled(false);
                    meta.setAge(250);
                    meta.setSoundEnabled(false);
                    meta.setPlayerMeta(playerMeta);
                    meta.setParticleEffectMeta(particleEffectMeta);
                    meta.setEngine(new EngineData(2));
                    controller.store(meta);

                    assertEquals(1, controller.size());
                    meta = (PetData) controller.getById(meta.getId());
                    assertEquals("PikaPet", meta.getPetDisplayName());
                    assertEquals(Material.ARROW.getId(), meta.getItemId());
                    assertEquals((short)7, meta.getItemDamage());
                    assertEquals("http://Skin.com", meta.getSkin());
                    assertEquals(2, meta.getEngineId());
                    assertEquals(false, meta.isEnabled());
                    assertEquals(250, meta.getAge());
                    assertEquals(false, meta.isItemStackUnbreakable());
                    assertEquals(false, meta.isSoundEnabled());
                }
            }
        } catch (final Exception e) {
            Logger.getLogger(this.getClass().getSimpleName()).log(Level.WARNING, "Failed to run test.", e);
            Assert.fail();
        }
    }
}
