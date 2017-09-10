ALTER TABLE SHY_PETBLOCK ADD COLUMN engine INTEGER;
BEGIN TRANSACTION;
CREATE TEMPORARY TABLE SHY_PETBLOCK_BACK
(
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  shy_player_id REFERENCES SHY_PLAYER(id),
  shy_particle_effect_id REFERENCES SHY_PARTICLE_EFFECT(id),
  name VARCHAR(32) NOT NULL,
  engine INTEGER,
  material VARCHAR(32),
  data CHAR(4),
  skull TEXT,
  enabled INTEGER,
  age INTEGER,
  unbreakable INTEGER,
  play_sounds INTEGER,
  CONSTRAINT foreignkey_player_id_cs FOREIGN KEY (shy_player_id) REFERENCES SHY_PLAYER(id),
  CONSTRAINT foreignkey_particle_id_cs FOREIGN KEY (shy_particle_effect_id) REFERENCES SHY_PARTICLE_EFFECT(id)
);
INSERT INTO SHY_PETBLOCK_BACK SELECT id,shy_player_id, shy_particle_effect_id,name,engine,material,data,skull,enabled,age,unbreakable,play_sounds FROM SHY_PETBLOCK;
DROP TABLE SHY_PETBLOCK;
CREATE TABLE SHY_PETBLOCK
(
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  shy_player_id REFERENCES SHY_PLAYER(id),
  shy_particle_effect_id REFERENCES SHY_PARTICLE_EFFECT(id),
  name VARCHAR(32) NOT NULL,
  engine INTEGER,
  material VARCHAR(32),
  data CHAR(4),
  skull TEXT,
  enabled INTEGER,
  age INTEGER,
  unbreakable INTEGER,
  play_sounds INTEGER,
  CONSTRAINT foreignkey_player_id_cs FOREIGN KEY (shy_player_id) REFERENCES SHY_PLAYER(id),
  CONSTRAINT foreignkey_particle_id_cs FOREIGN KEY (shy_particle_effect_id) REFERENCES SHY_PARTICLE_EFFECT(id)
);
INSERT INTO SHY_PETBLOCK SELECT id,shy_player_id, shy_particle_effect_id,name,engine,material,data,skull,enabled,age,unbreakable,play_sounds FROM SHY_PETBLOCK_BACK;
UPDATE SHY_PETBLOCK SET engine=1;
DROP TABLE SHY_PETBLOCK_BACK;
COMMIT;

