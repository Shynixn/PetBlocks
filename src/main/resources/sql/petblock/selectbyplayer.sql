SELECT pet.id, pet.shy_player_id, pet.shy_particle_effect_id, pet.name, pet.type, pet.material, pet.data, pet.skull, pet.enabled, pet.age, pet.unbreakable, pet.play_sounds, pet.moving_type, pet.movement_type FROM SHY_PETBLOCK pet, SHY_PLAYER play
WHERE pet.shy_player_id = play.id
AND play.uuid = ?;
