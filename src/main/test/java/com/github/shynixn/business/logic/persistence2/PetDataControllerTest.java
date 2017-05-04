package com.github.shynixn.business.logic.persistence2;

import com.github.shynixn.petblocks.business.logic.persistence2.Factory;
import com.github.shynixn.petblocks.business.logic.persistence2.IPetDataController;
import com.github.shynixn.petblocks.business.logic.persistence2.PetData;
import com.github.shynixn.petblocks.business.logic.persistence2.PlayerData;
import com.github.shynixn.petblocks.lib.util.IController;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.InputStream;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Shynixn
 */
public class PetDataControllerTest {

    private static Plugin mockPlugin() {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.set("sql.local", true);
        configuration.set("sql.host", "localhost");
        configuration.set("sql.port", 3306);
        configuration.set("sql.database", "db");
        configuration.set("sql.username", "root");
        configuration.set("sql.password", "");
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
        return plugin;
    }

    @Test
    public void getPetDataByPlayerTest() throws ClassNotFoundException {
        Factory.enable(mockPlugin());
        final PetData petData = new PetData();
        petData.setName("Maier");
        try (IPetDataController petDataIController = Factory.createPetDataController()) {
            PlayerData playerData = new PlayerData();
            try (IController<PlayerData> playerDataIController = Factory.createPlayerDataController()) {
                playerData.setName("Christoph");
                playerData.setUuid(UUID.randomUUID());
                playerDataIController.store(playerData);
                petData.setPlayerId(playerData.getId());
            }
            for (final PetData pet : petDataIController.getAll()) {
                petDataIController.remove(pet);
            }
            petDataIController.store(petData);
            Player player = mock(Player.class);
            when(player.getUniqueId()).thenReturn(playerData.getUUID());
            PetData petData1 = petDataIController.getByPlayer(player);
            Assert.assertEquals(petData1.getName(), petData.getName());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}
