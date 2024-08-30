# Template

PetBlocks allows you to create templates, how a pet is going to look like and how it is going to behave. You can fully
customize the behaviour and program the pets to perform
certain actions.

PetBlocks provides you with a starting pet template called ``classic``, which can be found in
the ``plugins/PetBlocks/pets/`` folder. You can copy this template and start desigining your own pets.
Make sure to give it a new unique template identifier.

Modify a template and execute ``/petblocks reload`` to apply the changes to your pet.

## Static Values

Most of the static values are explained in the config file itself. Set them according to your needs.

Example for static values are ``name``, ``version`` and all initial pet settings under ``pet:``.

## Events

Events define what actions to execute on certain events. 

The following events are supported:

* leftClick
* rightClick
* ridingSneak

For example the ``leftClick`` event gets executed when a player left-clicks on the pet. However, it may not be the owner, who clicks on a pet, therefore you need
to check if the player is the owner first as a condition.

Example:

```yaml
events:
  rightClick:
    actions:  # You can add/remove actions as you want here.
      - name: "Open the pet GUI" 
        condition:
          type: STRING_EQUALS
          left: "%petblocks_eventPlayer_name%"
          right: "%petblocks_owner_name%"
        type: "COMMAND"
        level: "PLAYER"
        run:
          - "/petblocks select %petblocks_pet_name%"
          - "/petblock"
```

If you want to execute nothing when the player rightClicks on the pet, remove all actions.

```yaml
events:
  rightClick:
    actions: []
```

## Building a new action

!!! note "PlaceHolders"
    You can use placeholders almost anywhere when building actions. You can even use PlaceHolderApi based placeholders.

Create a new action and give it an arbitrary name.

```yaml
events:
  rightClick:
    actions:  # You can add/remove actions as you want here.
      - name: "My new action" # Required arbitrary name.
```

Select the action type. Support actions are ``COMMAND``, ``DELAY``, ``JAVASCRIPT``.

#### COMMAND

Executes one or multiple commands as Console or Player.

!!! note "Vanilla Commands"
      A vanilla command may automatically log to the console and to the chat of every op player. You can disable the output for op players using vanilla game rules such as ``/gamerule sendCommandFeedback false``, ``/gamerule logAdminCommands false``, ``/gamerule commandBlockOutput false``. If you want to hide the output in your console (not recommend), you need to install a LogFilter plugin.

```yaml
events:
  rightClick:
    actions:  # You can add/remove actions as you want here.
      - name: "My new action" # Required arbitrary name.
        type: "COMMAND" # Required action type.
        level: "SERVER" # Required for type COMMAND. Possible values are PLAYER (player level permission), SERVER (console level permission)
```

```yaml
events:
  rightClick:
    actions:  # You can add/remove actions as you want here.
      - name: "My new action" # Required arbitrary name.
        type: "COMMAND" # Required action type.
        level: "SERVER" # Required for type COMMAND. Possible values are PLAYER (player level permission), SERVER (console level permission)
        run: 
          - "/say Hello %petblocks_owner_name%" # Required for type COMMAND. One or more commands are allowed.
```

#### DELAY

Delays the next action for a certain amount of ticks.

```yaml
events:
  rightClick:
    actions:  # You can add/remove actions as you want here.
      - name: "Delay Action" # Required arbitrary name.
        type: "DELAY" # Required action type.
        ticks: 60 # Required for type DELAY. 60 Ticks delay.
```

#### JAVASCRIPT

Executes JavaScript based Code for value calculation

```yaml
events:
  rightClick:
    actions:  # You can add/remove actions as you want here.
      - name: "JavaScript action" # Required arbitrary name.
        type: "JAVASCRIPT" # Required action type.
        initial: "Cool" # Required for type JAVASCRIPT. The initial value of the result variable.
        variable: "myVariable" # Required for type JAVASCRIPT. The name of the result variable, which can be read using PlaceHolders.
        js: |                  # Required for type JAVASCRIPT. Actual multi line JavaScript code.
          function createText() {
             var text = "%petblocks_js_myVariable%" + " Plugin"
             return text
          }
          createText();
```

### Debugging Actions

When you start creating actions, it is very helpful to know, which action is currently being executed and how
variables are evaluated. Every action can be logged to your server console by setting the optional ``debug: true`` property of an action.

```yaml
events:
  rightClick:
    actions:  # You can add/remove actions as you want here.
      - name: "Delay Action" # Required arbitrary name.
        type: "DELAY" # Required action type.
        ticks: 60 # Required for type DELAY. 60 Ticks delay.
        debug: true # Optional flag to log this action to the console.
```

### Restricting Actions

#### Permission

Actions can optionally have the permission tag:

```yaml
events:
  rightClick:
    actions:  # You can add/remove actions as you want here.
      - name: "My new action" # Required arbitrary name.
        permission: "mycustom.permission"
        type: "COMMAND" # Required action type.
        level: "SERVER" # Required for type COMMAND. Possible values are PLAYER (player level permission), SERVER (console level permission)
        run: 
          - "/say Hello %petblocks_owner_name%" # Required for type COMMAND. One or more commands are allowed.
```

#### Conditions

Actions can optionally have conditions, which support the following types:

* ``STRING_EQUALS``
* ``STRING_NOT_EQUALS``
* ``STRING_CONTAINS``
* ``STRING_NOT_CONTAINS``
* ``STRING_EQUALS_IGNORE_CASE``
* ``STRING_NOT_EQUALS_IGNORE_CASE``
* ``NUMBER_GREATER_THAN``
* ``NUMBER_GREATER_THAN_OR_EQUAL``
* ``NUMBER_LESS_THAN``
* ``NUMBER_LESS_THAN_OR_EQUAL``
* ``JAVASCRIPT``

Try to avoid using ``JAVASCRIPT`` because it requires more computation time. It should only be used if you want to create complex boolean expressions.

```yaml
events:
  rightClick:
    actions:  # You can add/remove actions as you want here.
      - name: "My new action" # Required arbitrary name.
        condition: # Optional condition tag.
          type: STRING_EQUALS # Required condition type.
          left: "%petblocks_eventPlayer_name%" 
          right: "%petblocks_owner_name%"
        type: "COMMAND" # Required action type.
        level: "SERVER" # Required for type COMMAND. Possible values are PLAYER (player level permission), SERVER (console level permission)
        run: 
          - "/say Hello %petblocks_owner_name%" # Required for type COMMAND. One or more commands are allowed.
```

```yaml
events:
  rightClick:
    actions:  # You can add/remove actions as you want here.
      - name: "My new action" # Required arbitrary name.
        condition: # Optional condition tag.
          type: JAVASCRIPT # Required condition type.
          js: "Math.floor(Math.random() * 100) <= 50" # 50% chance to execute this command.
        type: "COMMAND" # Required action type.
        level: "SERVER" # Required for type COMMAND. Possible values are PLAYER (player level permission), SERVER (console level permission)
        run: 
          - "/say Hello %petblocks_owner_name%" # Required for type COMMAND. One or more commands are allowed.
```

## Loops

Loops define, what the pet should repeatedly do. You can customize and define your own loops using actions.

!!! note "Programmable Pets"
    You can freely build new loops, delete loops and customize the behavior of pets. The actions allow full freedom to 
    design your pets. 

### Idle Loop

The idle loop explained:

1. Delay Action
    1. We wait for 20 ticks.
2. Look at owner with a 90% change.
    1. The JavaScript condition is evaluated, which returns true 90% of the time
    2. The console executes the command ``/petblocks lookatowner %petblocks_pet_name% %petblocks_owner_name%``
3. Switch to moveToOwner if pet is too far away
    1. The JavaScript condition is evaluated, if the distance of the pet to the owner is bigger than 7 blocks.
    2. If true, the console executes the command ``/petblocks loop %petblocks_pet_name% moveToOwner %petblocks_owner_name%`` , which switches the loop ``idle`` to the loop called ``moveToOwner``.
    3. If false, this loop continous and starts again from the top with ``Delay Action``


```yaml
loops:
  idle: 
    actions:
      - name: "Delay Action"
        type: "DELAY"
        ticks: 20
      - name: "Look at owner with a 90% change."
        condition:
          type: JAVASCRIPT
          js: "Math.floor(Math.random() * 100) <= 90" # Calculate chance in JavaScript.
        type: "COMMAND"
        level: "SERVER"
        run:
          - "/petblocks lookatowner %petblocks_pet_name% %petblocks_owner_name%"
      - name: "Switch to moveToOwner if pet is too far away"
        condition:
          type: JAVASCRIPT
          js: "%petblocks_pet_distanceToOwner% > 7"
        type: "COMMAND"
        level: "SERVER"
        run:
          - "/petblocks loop %petblocks_pet_name% moveToOwner %petblocks_owner_name%"
```

### MoveToOwner Loop

As we noticed that the loop changes to the loop ``moveToOwner``, we take a look at this loop too.

1. Recalculate path and start moving to owner.
    1. The console executes the command ``/petblocks moveToOwner %petblocks_pet_name% 0.2 %petblocks_owner_name%``
2. Switch to idle if the pet is beside the player
    1. The JavaScript condition is evaluated, if the distance of the pet to the owner is smaller than 4 blocks.
    2. If true, the console executes the command ``/petblocks loop %petblocks_pet_name% moveToOwner %petblocks_owner_name%`` , which switches the loop ``moveToOwner`` to the loop called ``idle``.
    3. If false, the next action is executed.
3. Teleport pet to player if the pet is too far away for the pathfinder.
    1. The JavaScript condition is evaluated, if the distance of the pet to the owner is bigger than 20 blocks.
    2. If true, the command is executed to teleport the pet to the owner location.
    3. If false, the next action is executed.
4. Delay Action
    1. We wait for 20 ticks.
    2. The loop continous and starts again from the top with ``Recalculate path and start moving to owner``

```yaml
loops:
  moveToOwner:
    actions:
      - name: "Recalculate path and start moving to owner."
        type: "COMMAND"
        level: "SERVER"
        run:
          - "/petblocks moveToOwner %petblocks_pet_name% 0.2 %petblocks_owner_name%"
      - name: "Switch to idle if the pet is beside the player"
        condition:
          type: JAVASCRIPT
          js: "%petblocks_pet_distanceToOwner% < 4"
        type: "COMMAND"
        level: "SERVER"
        run:
          - "/petblocks loop %petblocks_pet_name% idle %petblocks_owner_name%"
      - name: "Teleport pet to player if the pet is too far away for the pathfinder."
        condition:
          type: JAVASCRIPT
          js: "%petblocks_pet_distanceToOwner% > 20"
        type: "COMMAND"
        level: "SERVER"
        run:
          - "/petblocks teleport %petblocks_pet_name% %petblocks_owner_locationWorld% %petblocks_owner_locationX% %petblocks_owner_locationY% %petblocks_owner_locationZ% %petblocks_owner_locationYaw% %petblocks_owner_locationPitch% %petblocks_owner_name%"
      - name: "Delay Action"
        type: "DELAY"
        ticks: 20
```
