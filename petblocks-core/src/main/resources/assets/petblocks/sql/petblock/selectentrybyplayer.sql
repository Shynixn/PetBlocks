SELECT 1 FROM SHY_PETBLOCK pet, SHY_PLAYER play
WHERE pet.shy_player_id = play.id
AND play.uuid = ?;