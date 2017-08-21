package com.github.shynixn.petblocks.business.logic.persistence.controller;

import com.github.shynixn.petblocks.api.persistence.controller.ParticleEffectMetaController;
import com.github.shynixn.petblocks.api.persistence.controller.PetMetaController;
import com.github.shynixn.petblocks.api.persistence.entity.ParticleEffectMeta;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.business.logic.persistence.Factory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ParticleEffectMetaSQLiteControllerTest {

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
    public void insertSelectParticleEffectMetaTest() throws ClassNotFoundException {
        Factory.initialize(mockPlugin());
        try (ParticleEffectMetaController controller = Factory.createParticleEffectController()) {
            try (PetMetaController petController = Factory.createPetDataController()) {
                for (final PetMeta item : petController.getAll()) {
                    petController.remove(item);
                }
            }
            for (final ParticleEffectMeta item : controller.getAll()) {
                controller.remove(item);
            }
            final ParticleEffectMeta meta = controller.create();
            controller.store(meta);
            assertEquals(0, controller.size());
            meta.setEffectType(ParticleEffectMeta.ParticleEffectType.CLOUD);
            controller.store(meta);
            assertEquals(1, controller.size());
            assertEquals(ParticleEffectMeta.ParticleEffectType.CLOUD, controller.getById(meta.getId()).getEffectType());
        } catch (final Exception e) {
            Logger.getLogger(this.getClass().getSimpleName()).log(Level.WARNING, "Failed to run test.", e);
            Assert.fail();
        }
    }


    @Test
    public void storeLoadParticleEffectMetaTest() throws ClassNotFoundException {
        Factory.initialize(mockPlugin());
        try (ParticleEffectMetaController controller = Factory.createParticleEffectController()) {
            try (PetMetaController petController = Factory.createPetDataController()) {
                for (final PetMeta item : petController.getAll()) {
                    petController.remove(item);
                }
            }
            for (final ParticleEffectMeta item : controller.getAll()) {
                controller.remove(item);
            }
            ParticleEffectMeta meta = controller.create();
            meta.setEffectType(ParticleEffectMeta.ParticleEffectType.DAMAGE_INDICATOR);
            meta.setAmount(5)
                    .setX(2.25)
                    .setY(3.75)
                    .setZ(11.24)
                    .setSpeed(0.0001)
                    .setMaterial(Material.BONE)
                    .setData((byte)5);
            controller.store(meta);
            assertEquals(1, controller.size());

            meta = controller.getById(meta.getId());
            assertEquals(ParticleEffectMeta.ParticleEffectType.DAMAGE_INDICATOR, meta.getEffectType());
            assertEquals(5, meta.getAmount());
            assertEquals(2.25, meta.getX());
            assertEquals(3.75, meta.getY());
            assertEquals(11.24, meta.getZ());
            assertEquals(Material.BONE, meta.getMaterial());
            assertEquals((byte)5, (byte)meta.getData());

            meta.setAmount(7)
                    .setEffectType(ParticleEffectMeta.ParticleEffectType.BARRIER)
                    .setX(4.25)
                    .setY(7.75)
                    .setZ(5.24)
                    .setSpeed(0.002)
                    .setMaterial(Material.BARRIER)
                    .setData((byte)7);
            controller.store(meta);

            meta = controller.getById(meta.getId());
            assertEquals(ParticleEffectMeta.ParticleEffectType.BARRIER, meta.getEffectType());
            assertEquals(7, meta.getAmount());
            assertEquals(4.25, meta.getX());
            assertEquals(7.75, meta.getY());
            assertEquals(5.24, meta.getZ());
            assertEquals(Material.BARRIER, meta.getMaterial());
            assertEquals((byte)7, (byte)meta.getData());
        } catch (final Exception e) {
            Logger.getLogger(this.getClass().getSimpleName()).log(Level.WARNING, "Failed to run test.", e);
            Assert.fail();
        }
    }
}
