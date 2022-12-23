package com.github.shynixn.petblocks.api

import com.github.shynixn.petblocks.api.entity.PetSpawnResultType
import com.github.shynixn.petblocks.api.service.PetService
import kotlinx.coroutines.runBlocking
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player

class KotlinMeme {

    fun meme() {
        val petService: PetService? = null
        val player: Player? = null

        petService!!
        player!!

        runBlocking {
            val pets = petService.getPetsFromPlayer(player)


            val result = petService.addPet(player, Location(Bukkit.getWorld("world"), 30.0, 30.0, 30.0), "123456")

            if(result.type == PetSpawnResultType.SUCCESS){

            }

            pets[0].call()

        }


    }
}
