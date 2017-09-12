package com.github.shynixn.petblocks.business.logic.persistence.controller;

import com.github.shynixn.petblocks.api.persistence.controller.ParticleEffectMetaController;
import com.github.shynixn.petblocks.api.persistence.controller.PetMetaController;
import com.github.shynixn.petblocks.api.persistence.controller.PlayerMetaController;
import com.github.shynixn.petblocks.api.persistence.entity.ParticleEffectMeta;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.api.persistence.entity.PlayerMeta;
import com.github.shynixn.petblocks.business.logic.persistence.Factory;
import com.github.shynixn.petblocks.business.logic.persistence.entity.PetData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PetMetaSQLiteControllerTest {

    private static Plugin mockPlugin() {
        final Server server = mock(Server.class);
        when(server.getLogger()).thenReturn(Logger.getGlobal());
        if(Bukkit.getServer() == null)
            Bukkit.setServer(server);
        final YamlConfiguration configuration = new YamlConfiguration();
        configuration.set("sql.enabled",false);
        configuration.set("sql.host", "localhost");
        configuration.set("sql.port", 3306);
        configuration.set("sql.database", "db");
        configuration.set("sql.username", "root");
        configuration.set("sql.password", "");
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

    @BeforeAll
    public static void disableFactory() {
        Factory.disable();
    }

    @Test
    public void insertSelectPetMetaTest() throws ClassNotFoundException {
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
                    meta.setDisplayName("Notch");
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

                    meta.setEngineId(4);
                    assertThrows(IllegalArgumentException.class, () -> controller.store(meta));

                    meta.setSkin(Material.STONE, (short)5, null);
                    controller.store(meta);

                    assertEquals(1, controller.size());
                    assertEquals("Notch", controller.getByPlayer(player).getDisplayName());
                }
            }
        } catch (final Exception e) {
            Logger.getLogger(this.getClass().getSimpleName()).log(Level.WARNING, "Failed to run test.", e);
            Assert.fail();
        }
    }

    @Test
    public void storeLoadPetMetaTest() throws ClassNotFoundException {
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
                    meta.setDisplayName("Me");
                    meta.setSkin(Material.BIRCH_DOOR_ITEM,(short)5 , "This is my long skin.");
                    meta.setEngineId(4);
                    meta.setEnabled(true);
                    meta.setAge(500);
                    meta.setUnbreakable(true);
                    meta.setSoundEnabled(true);
                    final ParticleEffectMeta particleEffectMeta = particleController.create();
                    particleEffectMeta.setEffectType(ParticleEffectMeta.ParticleEffectType.END_ROD);
                    particleController.store(particleEffectMeta);
                    meta.setParticleEffectMeta(particleEffectMeta);

                    final PlayerMeta playerMeta = playerController.create(player);
                    playerController.store(playerMeta);
                    meta.setPlayerMeta(playerMeta);
                    controller.store(meta);

                    assertEquals(1, controller.size());
                    meta = (PetData) controller.getById(meta.getId());
                    assertEquals("Me", meta.getDisplayName());
                    assertEquals(Material.BIRCH_DOOR_ITEM, meta.getSkinMaterial());
                    assertEquals((short)5, meta.getSkinDurability());
                    assertEquals("This is my long skin.", meta.getSkin());
                    assertEquals(4, meta.getEngineId());
                    assertEquals(true, meta.isEnabled());
                    assertEquals(500, meta.getAge());
                    assertEquals(true, meta.isUnbreakable());
                    assertEquals(true, meta.isSoundEnabled());

                    meta.setDisplayName("PikaPet");
                    meta.setSkin(Material.ARROW,(short)7 , "http://Skin.com");
                    meta.setEngineId(1);
                    meta.setEnabled(false);
                    meta.setAge(250);
                    meta.setUnbreakable(false);
                    meta.setSoundEnabled(false);
                    controller.store(meta);

                    assertEquals(1, controller.size());
                    meta = (PetData) controller.getById(meta.getId());
                    assertEquals("PikaPet", meta.getDisplayName());
                    assertEquals(Material.ARROW, meta.getSkinMaterial());
                    assertEquals((short)7, meta.getSkinDurability());
                    assertEquals("http://Skin.com", meta.getSkin());
                    assertEquals(1, meta.getEngineId());
                    assertEquals(false, meta.isEnabled());
                    assertEquals(250, meta.getAge());
                    assertEquals(false, meta.isUnbreakable());
                    assertEquals(false, meta.isSoundEnabled());
                }
            }
        } catch (final Exception e) {
            Logger.getLogger(this.getClass().getSimpleName()).log(Level.WARNING, "Failed to run test.", e);
            Assert.fail();
        }
    }
}
