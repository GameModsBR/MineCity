CREATE TABLE `minecity_world` (
`world_id`  int(11) NOT NULL AUTO_INCREMENT ,
`dim`  int(11) NOT NULL DEFAULT 0 ,
`world`  varchar(255) NOT NULL DEFAULT '' ,
`name`  varchar(255) NULL DEFAULT NULL ,
PRIMARY KEY (`world_id`),
UNIQUE INDEX `world_dir` (`dim`, `world`) USING BTREE
);

CREATE TABLE `minecity_players` (
`player_id`  int NOT NULL AUTO_INCREMENT ,
`player_uuid`  binary(16) NOT NULL ,
`player_name`  varchar(16) NOT NULL ,
PRIMARY KEY (`player_id`),
UNIQUE INDEX `player_uuid` (`player_uuid`)
);

CREATE TABLE `minecity_city` (
`city_id`  int(11) NOT NULL AUTO_INCREMENT ,
`name`  varchar(40) NOT NULL ,
`display_name`  varchar(40) NOT NULL ,
`owner`  int NULL ,
`spawn_world`  int(11) NOT NULL ,
`spawn_x`  int(11) NOT NULL ,
`spawn_y`  int(11) NOT NULL ,
`spawn_z`  int(11) NOT NULL ,
PRIMARY KEY (`city_id`),
CONSTRAINT `city_owner` FOREIGN KEY (`owner`) REFERENCES `minecity_players` (`player_id`) ON DELETE SET NULL ON UPDATE CASCADE,
CONSTRAINT `city_world_spawn` FOREIGN KEY (`spawn_world`) REFERENCES `minecity_world` (`world_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
UNIQUE INDEX `city_name` (`name`)
);

CREATE TABLE `minecity_islands` (
`island_id`  int NOT NULL AUTO_INCREMENT ,
`world_id`  int NOT NULL ,
`city_id`  int NOT NULL ,
PRIMARY KEY (`island_id`),
CONSTRAINT `island_world` FOREIGN KEY (`world_id`) REFERENCES `minecity_world` (`world_id`) ON DELETE CASCADE ON UPDATE CASCADE,
CONSTRAINT `island_city` FOREIGN KEY (`city_id`) REFERENCES `minecity_city` (`city_id`) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE `minecity_chunks` (
`world_id`  int NOT NULL ,
`x`  int NOT NULL ,
`z`  int NOT NULL ,
`island_id`  int NOT NULL ,
`reserve`  bit(1) NOT NULL DEFAULT b'0' ,
PRIMARY KEY (`world_id`, `x`, `z`),
CONSTRAINT `chunk_world` FOREIGN KEY (`world_id`) REFERENCES `minecity_world` (`world_id`) ON DELETE CASCADE ON UPDATE CASCADE,
CONSTRAINT `chunk_island` FOREIGN KEY (`island_id`) REFERENCES `minecity_islands` (`island_id`) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE `minecity_entities` (
`entity_id`  int NOT NULL AUTO_INCREMENT ,
`entity_uuid`  binary(16) NOT NULL ,
`entity_name`  varchar(16) NOT NULL ,
`entity_type`  enum('ITEM','STRUCTURE','STORAGE','VEHICLE','PROJECTILE','ANIMAL','MONSTER','UNCLASSIFIED') NOT NULL ,
`last_world`  int NULL ,
`last_x`  int NULL ,
`last_y`  int NULL ,
`last_z`  int NULL ,
`last_update`  timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ,
PRIMARY KEY (`entity_id`),
CONSTRAINT `entity_world` FOREIGN KEY (`last_world`) REFERENCES `minecity_world` (`world_id`) ON DELETE SET NULL ON UPDATE CASCADE,
UNIQUE INDEX `entity_uuid` (`entity_uuid`)
)
;

CREATE TABLE `minecity_groups` (
`group_id`  int NOT NULL AUTO_INCREMENT ,
`city_id`  int NOT NULL ,
`name`  varchar(40) NOT NULL ,
`display_name`  varchar(40) NOT NULL ,
PRIMARY KEY (`group_id`),
CONSTRAINT `group_city` FOREIGN KEY (`city_id`) REFERENCES `minecity_city` (`city_id`) ON DELETE CASCADE ON UPDATE CASCADE,
UNIQUE INDEX `group_name` (`city_id`, `name`)
)
;

CREATE TABLE `minecity_group_players` (
`group_id`  int NOT NULL ,
`player_id`  int NOT NULL ,
PRIMARY KEY (`group_id`, `player_id`),
CONSTRAINT `group_players_group` FOREIGN KEY (`group_id`) REFERENCES `minecity_groups` (`group_id`) ON DELETE CASCADE ON UPDATE CASCADE,
CONSTRAINT `group_players_player` FOREIGN KEY (`player_id`) REFERENCES `minecity_players` (`player_id`) ON DELETE CASCADE ON UPDATE CASCADE
)
;

CREATE TABLE `minecity_group_entities` (
`group_id`  int NOT NULL ,
`entity_id`  int NOT NULL ,
PRIMARY KEY (`group_id`, `entity_id`),
CONSTRAINT `group_entities_group` FOREIGN KEY (`group_id`) REFERENCES `minecity_groups` (`group_id`) ON DELETE CASCADE ON UPDATE CASCADE,
CONSTRAINT `group_entities_entity` FOREIGN KEY (`entity_id`) REFERENCES `minecity_entities` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
)
;

CREATE TABLE `minecity_city_perm_defaults` (
`city_id`  int NOT NULL ,
`perm`  enum('ENTER','CLICK','PICKUP','OPEN','LEAVE','PVP','PVC','PVM','SPAWN_VEHICLES','RIDE') NOT NULL ,
`allow`  bit(1) NOT NULL ,
`message`  varchar(100) NULL ,
PRIMARY KEY (`city_id`, `perm`),
CONSTRAINT `city_perm_defaults` FOREIGN KEY (`city_id`) REFERENCES `minecity_city` (`city_id`) ON DELETE CASCADE ON UPDATE CASCADE
)
;

CREATE TABLE `minecity_city_perm_player` (
`city_id`  int NOT NULL ,
`player_id`  int NOT NULL ,
`perm`  enum('ENTER','CLICK','PICKUP','OPEN','LEAVE','PVP','PVC','PVM','SPAWN_VEHICLES','RIDE') NOT NULL ,
`allow`  bit(1) NOT NULL DEFAULT b'0' ,
`message`  varchar(100) NULL ,
PRIMARY KEY (`city_id`, `player_id`, `perm`),
CONSTRAINT `city_perm_player_city` FOREIGN KEY (`city_id`) REFERENCES `minecity_city` (`city_id`) ON DELETE CASCADE ON UPDATE CASCADE,
CONSTRAINT `city_perm_player_player` FOREIGN KEY (`player_id`) REFERENCES `minecity_players` (`player_id`) ON DELETE CASCADE ON UPDATE CASCADE
)
;

CREATE TABLE `minecity_city_perm_entity` (
`city_id`  int NOT NULL ,
`entity_id`  int NOT NULL ,
`perm`  enum('ENTER','CLICK','PICKUP','OPEN','LEAVE','PVP','PVC','PVM','SPAWN_VEHICLES','RIDE') NOT NULL ,
`allow`  bit(1) NOT NULL DEFAULT b'0' ,
`message`  varchar(100) NULL ,
PRIMARY KEY (`city_id`, `entity_id`, `perm`),
CONSTRAINT `city_perm_entity_city` FOREIGN KEY (`city_id`) REFERENCES `minecity_city` (`city_id`) ON DELETE CASCADE ON UPDATE CASCADE,
CONSTRAINT `city_perm_entity_entity` FOREIGN KEY (`entity_id`) REFERENCES `minecity_entities` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
)
;

CREATE TABLE `minecity_city_perm_group` (
`city_id`  int NOT NULL ,
`group_id`  int NOT NULL ,
`perm`  enum('ENTER','CLICK','PICKUP','OPEN','LEAVE','PVP','PVC','PVM','SPAWN_VEHICLES','RIDE') NOT NULL ,
`allow`  bit(1) NOT NULL ,
`message`  varchar(100) NULL ,
PRIMARY KEY (`city_id`, `group_id`, `perm`),
CONSTRAINT `city_perm_group_city` FOREIGN KEY (`city_id`) REFERENCES `minecity_city` (`city_id`) ON DELETE CASCADE ON UPDATE CASCADE,
CONSTRAINT `city_perm_group_group` FOREIGN KEY (`group_id`) REFERENCES `minecity_groups` (`group_id`) ON DELETE CASCADE ON UPDATE CASCADE
)
;

CREATE TABLE `minecity_setup` (
`property`  enum('version') NOT NULL ,
`value`  enum('1') NOT NULL DEFAULT '1' ,
PRIMARY KEY (`property`)
);

INSERT INTO `minecity_setup` VALUES('version', '1');