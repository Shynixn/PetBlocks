SELECT pet.id, pet.shy_player_id, pet.shy_particle_effect_id, pet.name, pet.engine, pet.material, pet.data, pet.skull, pet.enabled, pet.age, pet.unbreakable, pet.play_sounds FROM SHY_PETBLOCK pet, SHY_PLAYER play
WHERE pet.shy_player_id = play.id
AND play.uuid = ?;
