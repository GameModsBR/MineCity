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
PRIMARY KEY (`world_id`, `x`, `z`),
CONSTRAINT `chunk_world` FOREIGN KEY (`world_id`) REFERENCES `minecity_world` (`world_id`) ON DELETE CASCADE ON UPDATE CASCADE,
CONSTRAINT `chunk_island` FOREIGN KEY (`island_id`) REFERENCES `minecity_islands` (`island_id`) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE `minecity_setup` (
`property`  enum('version') NOT NULL ,
`value`  enum('1') NOT NULL DEFAULT '1' ,
PRIMARY KEY (`property`)
);

INSERT INTO `minecity_setup` VALUES('version', '1');