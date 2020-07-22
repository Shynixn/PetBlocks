Creating AIS
============

There are 2 different types of AIS in PetBlocks:

* Pathfinder AIS
* Event-based AIS

Pathfinder AIS are only getting executed when a pet is fully spawned and has got at least one movement ai.
They are built on top of the vanilla NMS pathfinders. One example of them would be Zombies following and attacking
nearby players. Also, directly injecting vanilla pathfinders is possible.

Event-based AIS are always getting executed when an event on the server appears. Which can be events, commands or
even schedulers.

Decide for pathfinder based AIS if you only want to execute something once a pet is spawned, use a event-based AIS if you
want to manage interactions with the pet.

Example
~~~~~~~

.. note::  **RandomJumpAI** - In this example we are creating a new pathfinder ai which lets the pet randomly jump
 depending on a certain configurable chance in the config.yml.

0. Include the PetBlocks API as mentioned in the getting started section.

1. Define our data holding entity with a custom field chance and set it to 10 percent.

.. code-block:: java

    public class RandomJumpAI implements AIBase {
        public static final String TYPENAME = "randomjump";

        private long id;
        private String type = TYPENAME;
        private double chance = 0.1;

        public double getChance() {
            return this.chance;
        }

        public void setChance(double chance) {
            this.chance = chance;
        }

        @Override
        public long getId() {
            return this.id;
        }

        @Override
        public void setId(long id) {
            this.id = id;
        }

        @Override
        public String getType() {
            return this.type;
        }

        @Override
        public void setType(String type) {
            this.type = type;
        }

        @Override
        public String getUserId() {
            return null;
        }

        @Override
        public void setUserId(String s) {
        }
    }

2. Create our pathfinder. This guide will not go into detail how pathfinders are executed, so researching on
various minecraft forums is useful to get familiar with this.

.. code-block:: java

    public class RandomJumpAIPathfinder implements PathfinderProxy {
        private final PetProxy pet;
        private final RandomJumpAI ai;

        private long lastTimeExecuted;

        public RandomJumpAIPathfinder(PetProxy pet, RandomJumpAI ai) {
            this.pet = pet;
            this.ai = ai;
        }

        @Override
        public void onExecute() {
            final long currentMilliseconds = System.currentTimeMillis();

            // All methods in this class get not executed every tick so we need to make our own time calculation.
            // Skip chance calculation if less than one second has passed since the last time.
            if (currentMilliseconds - this.lastTimeExecuted < 1000) {
                return;
            }

            final double random = Math.random();

            // A chance of 10% only adds a jump vector 10 percent of the time.
            if (random <= this.ai.getChance()) {
                final Vector vector = new Vector(0, 1, 0);

                this.pet.setVelocity(vector);
            }

            this.lastTimeExecuted = currentMilliseconds;
        }

        @Override
        public AIBase getAiBase() {
            // Always return the ai data here.
            return this.ai;
        }

        @Override
        public boolean isInteruptible() {
            // Can execution be cancelled? Almost always you want to return false here.
            return false;
        }

        @Override
        public void setInteruptible(boolean b) {
            // Can be ignored.
        }

        @Override
        public boolean shouldGoalBeExecuted() {
            // Once this function returns true, OnStartExecuting and OnExecute will be called.
            // In this case we return true as the condition always gets checked in onExecute.
            return true;
        }

        @Override
        public boolean shouldGoalContinueExecuting() {
            // Should continuously OnExecute be called?
            // We want to restart the cycle after one time calling OnExecute, so return false.
            return false;
        }

        @Override
        public void onStartExecuting() {
            // Will be called once shouldGoalBExecuted returns true.
        }

        @Override
        public void onStopExecuting() {
            // Will be called once shouldGoalContinueExecuting returns false.
        }
    }


3. Register our ai on startup. This should always be executed on plugin start up otherwise
   PetBlocks will not be able to work with the custom ai anymore.

.. code-block:: java

   @Override
    public void onEnable() {
        final AIService aiService = PetBlocksApi.INSTANCE.resolve(AIService.class);

        aiService.registerAI(RandomJumpAI.TYPENAME, new AICreationProxy<RandomJumpAI>() {
            /**
             * Gets called once the given aiBase has to be serialized. This ensures ais get saved
             * into the PetBlocks database without having to deal with saving them on your own.
             * @param aiBase aiBase getting serialized.
             * @return serializedContent.
             */
            @Override
            public Map<String, Object> onSerialization(RandomJumpAI aiBase) {
                final Map<String, Object> serializedContent = new HashMap<>();

                // Id and type are automatically serialized. You only need to set your custom fields.
                serializedContent.put("chance", aiBase.getChance());

                return serializedContent;
            }

            /**
             * Gets called once the given aiBase has to be de Serialized. This ensures ais can get restored
             * from the PetBlocks database without having to deal with saving them on your own.
             * @param source serializedContent.
             * @return ai instance.
             */
            @Override
            public RandomJumpAI onDeserialization(Map<String, ?> source) {
                final RandomJumpAI randomJumpAI = new RandomJumpAI();

                randomJumpAI.setChance((Double) source.get("chance"));

                return randomJumpAI;
            }

            /**
             * Gets called once the hitbox entity requests a new pathfinders in order to work.
             * @param pet Pet requesting the pathfinder.
             * @param aiBase AI being offered to create this pathfinder.
             * @return Null when no pathfinder should be added or a
             * instance of a class implementation PathfinderProxy or a
             * instance of a vanilla NMS pathfinder.
             */
            @Override
            public Object onPathfinderCreation(PetProxy pet, RandomJumpAI aiBase) {
                return new RandomJumpAIPathfinder(pet, aiBase);
            }
        });
    }

4. Use your created ai like any other ai in the config.yml.

config.yml
::
     type: 'randomjump'
     chance: 0.1



