# Api

PetBlocks offers a Developer Api, however it is not published to Maven Central or any other distribution system yet.
You need to directly reference the PetBlocks.jar file.

## Usage

Add a dependency in your plugin.yml

```yaml
softdepend: [ PetBlocks]
```

Take a look at the following example:
```java
public class YourPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        // Always gets the same instance of the PetService.
        PetService petService = Bukkit.getServicesManager().load(PetService.class);

        Player player = Bukkit.getPlayer("YourPlayerName");
        Plugin plugin = this;

        // GetPetsFromPlayerAsync may retrieve the pet from the Database or the InMemory cache.
        petService.getPetsFromPlayerAsync(player).thenAccept(pets -> {
            // Main Thread
            if (pets.size() > 0) {
                // Do not keep the pet instance in your plugin (e.g. in fields). Always retrieve it with getPetsFromPlayerAsync.
                // If you need to keep the pet instance in high performance scenarios, check if the pet has already been disposed before using it with pet.isDisposed().
                Pet pet = pets.get(0);

                // All pet methods are safe to be called regardless if the pet is currently spawned or not.
                pet.call();
                // Changes are automatically applied to the pet if it is spawned and automatically persisted.
                pet.setDisplayName("Hello World");
                pet.setLoop("idle");
            }
        }).exceptionally(error -> {
            plugin.getLogger().log(Level.SEVERE, "Failed to load pets.", error);
            return null;
        });
    }
}
```
