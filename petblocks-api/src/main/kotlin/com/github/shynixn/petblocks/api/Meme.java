package com.github.shynixn.petblocks.api;

import com.github.shynixn.petblocks.api.entity.PetSpawnResultType;
import com.github.shynixn.petblocks.api.service.PetService;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;

public class Meme {


    public void data() {
        PetService petService = null;
        Player player = null;
        Plugin plugin = null;
        Location location = null;

        petService.getPetsFromPlayerAsync(player).thenAccept(pets -> {
            System.out.println("My pets: " + pets);
        }).exceptionally(e -> {
            plugin.getLogger().log(Level.SEVERE, "Failed to load pets of player " + player.getName() + ".", e);
            return null;
        });

        petService.addPetAsync(player, location, "memeOne").thenAccept(petSpawnResult -> {
            if(petSpawnResult.getType() == PetSpawnResultType.SUCCESS){
                Pet pet = petSpawnResult.getPet();

            }
        }).exceptionally(e -> {
            plugin.getLogger().log(Level.SEVERE, "Failed to spawn pet of player " + player.getName() + ".", e);
            return null;
        });







    }
}
