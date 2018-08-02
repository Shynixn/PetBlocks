Particles
=========

It is possible to play specific particles in the PetBlocks plugin when a certain action appears.

The metadata of the particles are stored in the database to allow players to choose their own particle at any time.

Configuration and Samples
~~~~~~~~~~~~~~~~~~~~~~~~~

* Particles are played for every nearby player per default. This can be changed in the config.yml.

**config.yml**:
::
  pet:
    design:
       particles-other-players: true

* The configuration values for particles allow the 3 possible templates:

**Template for ordinary particles**:
::
  1:
    id: 95
    damage: 8
    skin: 'none'
    name: '&fExplosion'
    unbreakable: false
    lore:
        - 'none'
    effect:
        name: 'explode'
        speed: 0.01
        amount: 2
        offx: 0.5
        offy: 0.5
        offz: 0.5

**Template for colored particles**:
::
  3:
    id: 351
    damage: 12
    skin: 'none'
    name: '&9Blue-Trail'
    unbreakable: false
    lore:
       - 'none'
    effect:
        name: 'reddust'
        speed: 0.01
        amount: 20
        red: 0
        green: 0
        blue: 255

**Template for material particles (Supports only the legacy ids)**:
::
  2:
    id: 2
    damage: 0
    skin: 'none'
    name: '&eSand-Crack'
    unbreakable: false
    lore:
       - 'none'
    effect:
        name: 'blockdust'
        speed: 0.01
        amount: 2
        offx: 0.5
        offy: 0.5
        offz: 0.5
        id: 12
        damage: 0

**Name:** Name of the played particle. The names of the particles change frequently in minecraft, however
you don't have to be concerned about this as the PetBlocks plugin converts the names correctly to your
server version.

**Speed:** Speed of the particle.

**Amount:** Amount of the particle.

**Offset:** Size/Offset of the particle. Can be left out when using colored particles.

**red/green/blue:** RGB values for the colors. Can only be used when using colored particles.

**id/damage:** Id and damage value for materials. Can only be used for material particles.

ParticleList
~~~~~~~~~~~~

============================   ============================   ============================
Spigot 1.8.0 - 1.12.2          Spigot 1.13.0 - latest         Sponge 1.12.2
============================   ============================   ============================
none                           none                           none
explode                        poof                           explosion
largeexplode                   explosion                      large_explosion
hugeexplosion                  explosion_emitter              huge_explosion
fireworksSpark                 firework                       fireworks_spark
bubble                         bubble                         water_bubble
bubble_column_up               bubble_column_up               bubble_column_up
bubble_pop                     bubble_pop                     bubble_pop
splash                         splash                         water_splash
wake                           fishing                        water_wake
suspended                      underwater                     suspended
depthsuspend                   depthsuspend                   suspended_depth
crit                           crit                           critical_hit
magicCrit                      enchanted_hit                  magic_critical_hit
current_down                   current_down                   current_down
smoke                          smoke                          smoke
largesmoke                     large_smoke                    large_smoke
spell                          effect                         spell
instantSpell                   instant_effect                 instant_spell
mobSpell                       entity_effect                  instant_spell
mobSpellAmbient                mob_spell                      mob_spell
witchMagic                     witch                          witch_spell
dripWater                      dripping_water                 drip_water
dripLava                       dripping_lava                  drip_lava
angryVillager                  angry_villager                 angry_villager
happyVillager                  happy_villager                 happy_villager
townaura                       mycelium                       town_aura
note                           note                           note
portal                         portal                         portal
nautilus                       nautilus                       nautilus
enchantmenttable               enchant                        enchanting_glyphs
flame                          flame                          flame
lava                           lava                           lava
squid_ink                      squid_ink                      squid_ink
footstep                       footstep                       footstep
cloud                          cloud                          cloud
reddust                        dust                           redstone_dust
snowballpoof                   item_snowball                  snowball
snowshovel                     snowshovel                     snow_shovel
slime                          item_slime                     slime
heart                          heart                          heart
barrier                        barrier                        barrier
iconcrack                      item                           item_crack
blockcrack                     block                          block_crack
blockdust                      block                          block_dust
droplet                        rain                           water_drop
take                           take                           instant_spell
mobappearance                  elder_guardian                 guardian_appearance
dragonbreath                   dragon_breath                  dragon_breath
endRod                         end_rod                        end_rod
damageIndicator                damage_indicator               damage_indicator
sweepAttack                    sweep_attack                   sweep_attack
fallingdust                    falling_dust                   falling_dust
totem                          totem_of_undying               instant_spell
spit                           spit                           instant_spell
============================   ============================   ============================



