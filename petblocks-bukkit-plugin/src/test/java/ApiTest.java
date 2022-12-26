import com.github.shynixn.petblocks.bukkit.contract.Pet;
import com.github.shynixn.petblocks.bukkit.contract.PetBlocksApi;
import com.github.shynixn.petblocks.bukkit.contract.PetService;
import com.github.shynixn.petblocks.bukkit.entity.PetSpawnResultType;
import com.github.shynixn.petblocks.bukkit.entity.PetVisibility;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ApiTest {

    @Test
    public void onTest() {
        PetService petService = PetBlocksApi.INSTANCE.resolve(PetService.class);
        Player player = Mockito.mock(Player.class);

        petService.createPetAsync(player, player.getLocation(), "pet_hopping").thenAccept(petSpawnResult -> {
           if(petSpawnResult.getType() == PetSpawnResultType.SUCCESS){
               Pet pet = petSpawnResult.getPet();

               // Every change to the pet is automatically persisted and applied if it is currently spawned.
               pet.setVisibility(PetVisibility.ALL);

               // You can store the pet instance in your fields but check isDisposed if it is still a valid reference.
               if(!pet.isDisposed()){
                   pet.setDisplayName("&eMy amazing Pet");
               }
           }
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

    @Test
    public void onTest2(){
        PetService petService = PetBlocksApi.INSTANCE.resolve(PetService.class);
        Player player = Mockito.mock(Player.class);

        petService.getPetsFromPlayerAsync(player).thenAccept(pets -> {
            pets.stream().filter(e -> e.getName().equals("pikachu")).findFirst().ifPresent(pet -> {
                // Find the correct pet instance and call the pet to the player.
                pet.call();

                pet.setHeadItemStack(new ItemStack(Material.GOLD_BLOCK));
            });
        });
    }



}
