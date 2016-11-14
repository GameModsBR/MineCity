DROP PROCEDURE IF EXISTS PROC_DROP_FOREIGN_KEY;
DELIMITER $$
CREATE PROCEDURE PROC_DROP_FOREIGN_KEY(IN tableName VARCHAR(64), IN constraintName VARCHAR(64))
  BEGIN
    IF EXISTS(
        SELECT * FROM information_schema.table_constraints
        WHERE
          table_schema    = DATABASE()     AND
          table_name      = tableName      AND
          constraint_name = constraintName AND
          constraint_type = 'FOREIGN KEY')
    THEN
      SET @query = CONCAT('ALTER TABLE ', tableName, ' DROP FOREIGN KEY ', constraintName, ';');
      PREPARE stmt FROM @query;
      EXECUTE stmt;
      DEALLOCATE PREPARE stmt;
    END IF;
  END$$
DELIMITER ;

CALL PROC_DROP_FOREIGN_KEY('minecity_city', 'city_owner');
CALL PROC_DROP_FOREIGN_KEY('minecity_city', 'city_world_spawn');
CALL PROC_DROP_FOREIGN_KEY('minecity_islands', 'island_world');
CALL PROC_DROP_FOREIGN_KEY('minecity_islands', 'island_city');
CALL PROC_DROP_FOREIGN_KEY('minecity_chunks', 'chunk_world');
CALL PROC_DROP_FOREIGN_KEY('minecity_chunks', 'chunk_island');
CALL PROC_DROP_FOREIGN_KEY('minecity_entities', 'entity_world');
CALL PROC_DROP_FOREIGN_KEY('minecity_groups', 'group_city');
CALL PROC_DROP_FOREIGN_KEY('minecity_group_players', 'group_players_group');
CALL PROC_DROP_FOREIGN_KEY('minecity_group_players', 'group_players_player');
CALL PROC_DROP_FOREIGN_KEY('minecity_group_entities', 'group_entities_group');
CALL PROC_DROP_FOREIGN_KEY('minecity_group_entities', 'group_entities_entity');
CALL PROC_DROP_FOREIGN_KEY('minecity_city_perm_defaults', 'city_perm_defaults');
CALL PROC_DROP_FOREIGN_KEY('minecity_city_perm_player', 'city_perm_player_city');
CALL PROC_DROP_FOREIGN_KEY('minecity_city_perm_player', 'city_perm_player_player');
CALL PROC_DROP_FOREIGN_KEY('minecity_city_perm_entity', 'city_perm_entity_city');
CALL PROC_DROP_FOREIGN_KEY('minecity_city_perm_entity', 'city_perm_entity_entity');
CALL PROC_DROP_FOREIGN_KEY('minecity_city_perm_group', 'city_perm_group_city');
CALL PROC_DROP_FOREIGN_KEY('minecity_city_perm_group', 'city_perm_group_group');
CALL PROC_DROP_FOREIGN_KEY('minecity_group_managers', 'group_managers_group');
CALL PROC_DROP_FOREIGN_KEY('minecity_group_managers', 'group_managers_player');
CALL PROC_DROP_FOREIGN_KEY('minecity_plots', 'plot_island');
CALL PROC_DROP_FOREIGN_KEY('minecity_plots', 'plot_owner');
CALL PROC_DROP_FOREIGN_KEY('minecity_plot_perm_defaults', 'plot_perm_plot');
CALL PROC_DROP_FOREIGN_KEY('minecity_plot_perm_player', 'plot_perm_player_plot');
CALL PROC_DROP_FOREIGN_KEY('minecity_plot_perm_player', 'plot_perm_player_player');
CALL PROC_DROP_FOREIGN_KEY('minecity_plot_perm_entity', 'plot_perm_entity_plot');
CALL PROC_DROP_FOREIGN_KEY('minecity_plot_perm_entity', 'plot_perm_entity_entity');
CALL PROC_DROP_FOREIGN_KEY('minecity_plot_perm_group', 'plot_perm_group_plot');
CALL PROC_DROP_FOREIGN_KEY('minecity_plot_perm_group', 'plot_perm_group_entity');
CALL PROC_DROP_FOREIGN_KEY('minecity_world_perm_defaults', 'world_perm_world');

DROP PROCEDURE PROC_DROP_FOREIGN_KEY;

ALTER TABLE `minecity_setup` ENGINE=MyISAM;

DELETE FROM `minecity_city` WHERE (SELECT 1 FROM `minecity_players` tbl WHERE tbl.player_id = minecity_city.owner) IS NULL ;
DELETE FROM `minecity_group_players` WHERE (SELECT 1 FROM `minecity_players` tbl WHERE tbl.player_id = minecity_group_players.player_id) IS NULL ;
DELETE FROM `minecity_city_perm_player` WHERE (SELECT 1 FROM `minecity_players` tbl WHERE tbl.player_id = minecity_city_perm_player.player_id) IS NULL ;
DELETE FROM `minecity_group_managers` WHERE (SELECT 1 FROM `minecity_players` tbl WHERE tbl.player_id = minecity_group_managers.player_id) IS NULL ;
DELETE FROM `minecity_plots` WHERE (SELECT 1 FROM `minecity_players` tbl WHERE tbl.player_id = minecity_plots.owner) IS NULL ;
DELETE FROM `minecity_plot_perm_player` WHERE (SELECT 1 FROM `minecity_players` tbl WHERE tbl.player_id = minecity_plot_perm_player.player_id) IS NULL ;

DELETE FROM `minecity_city` WHERE (SELECT 1 FROM `minecity_world` tbl WHERE tbl.world_id = minecity_city.spawn_world) IS NULL ;
DELETE FROM `minecity_islands` WHERE (SELECT 1 FROM `minecity_world` tbl WHERE tbl.world_id = minecity_islands.world_id) IS NULL ;
DELETE FROM `minecity_chunks` WHERE (SELECT 1 FROM `minecity_world` tbl WHERE tbl.world_id = minecity_chunks.world_id) IS NULL ;
DELETE FROM `minecity_entities` WHERE (SELECT 1 FROM `minecity_world` tbl WHERE tbl.world_id = minecity_entities.last_world) IS NULL ;
DELETE FROM `minecity_world_perm_defaults` WHERE (SELECT 1 FROM `minecity_world` tbl WHERE tbl.world_id = minecity_world_perm_defaults.world_id) IS NULL ;

DELETE FROM `minecity_islands` WHERE (SELECT 1 FROM `minecity_city` tbl WHERE tbl.city_id = minecity_islands.city_id) IS NULL ;
DELETE FROM `minecity_groups` WHERE (SELECT 1 FROM `minecity_city` tbl WHERE tbl.city_id = minecity_groups.city_id) IS NULL ;
DELETE FROM `minecity_city_perm_defaults` WHERE (SELECT 1 FROM `minecity_city` tbl WHERE tbl.city_id = minecity_city_perm_defaults.city_id) IS NULL ;
DELETE FROM `minecity_city_perm_player` WHERE (SELECT 1 FROM `minecity_city` tbl WHERE tbl.city_id = minecity_city_perm_player.city_id) IS NULL ;
DELETE FROM `minecity_city_perm_entity` WHERE (SELECT 1 FROM `minecity_city` tbl WHERE tbl.city_id = minecity_city_perm_entity.city_id) IS NULL ;
DELETE FROM `minecity_city_perm_group` WHERE (SELECT 1 FROM `minecity_city` tbl WHERE tbl.city_id = minecity_city_perm_group.city_id) IS NULL ;

DELETE FROM `minecity_chunks` WHERE (SELECT 1 FROM `minecity_islands` tbl WHERE tbl.island_id = minecity_chunks.island_id) IS NULL ;
DELETE FROM `minecity_plots` WHERE (SELECT 1 FROM `minecity_islands` tbl WHERE tbl.island_id = minecity_plots.island_id) IS NULL ;

DELETE FROM `minecity_group_entities` WHERE (SELECT 1 FROM `minecity_entities` tbl WHERE tbl.entity_id = minecity_group_entities.entity_id) IS NULL ;
DELETE FROM `minecity_city_perm_entity` WHERE (SELECT 1 FROM `minecity_entities` tbl WHERE tbl.entity_id = minecity_city_perm_entity.entity_id) IS NULL ;
DELETE FROM `minecity_plot_perm_entity` WHERE (SELECT 1 FROM `minecity_entities` tbl WHERE tbl.entity_id = minecity_plot_perm_entity.entity_id) IS NULL ;

DELETE FROM `minecity_group_players` WHERE (SELECT 1 FROM `minecity_groups` tbl WHERE tbl.group_id = minecity_group_players.group_id) IS NULL ;
DELETE FROM `minecity_group_entities` WHERE (SELECT 1 FROM `minecity_groups` tbl WHERE tbl.group_id = minecity_group_entities.group_id) IS NULL ;
DELETE FROM `minecity_city_perm_group` WHERE (SELECT 1 FROM `minecity_groups` tbl WHERE tbl.group_id = minecity_city_perm_group.group_id) IS NULL ;
DELETE FROM `minecity_group_managers` WHERE (SELECT 1 FROM `minecity_groups` tbl WHERE tbl.group_id = minecity_group_managers.group_id) IS NULL ;
DELETE FROM `minecity_plot_perm_group` WHERE (SELECT 1 FROM `minecity_groups` tbl WHERE tbl.group_id = minecity_plot_perm_group.group_id) IS NULL ;

DELETE FROM `minecity_plot_perm_defaults` WHERE (SELECT 1 FROM `minecity_plots` tbl WHERE tbl.plot_id = minecity_plot_perm_defaults.plot_id) IS NULL ;
DELETE FROM `minecity_plot_perm_player` WHERE (SELECT 1 FROM `minecity_plots` tbl WHERE tbl.plot_id = minecity_plot_perm_player.plot_id) IS NULL ;
DELETE FROM `minecity_plot_perm_entity` WHERE (SELECT 1 FROM `minecity_plots` tbl WHERE tbl.plot_id = minecity_plot_perm_entity.plot_id) IS NULL ;
DELETE FROM `minecity_plot_perm_group` WHERE (SELECT 1 FROM `minecity_plots` tbl WHERE tbl.plot_id = minecity_plot_perm_group.plot_id) IS NULL ;

ALTER TABLE `minecity_world` ENGINE=InnoDB;
ALTER TABLE `minecity_players` ENGINE=InnoDB;
ALTER TABLE `minecity_city` ENGINE=InnoDB;
ALTER TABLE `minecity_islands` ENGINE=InnoDB;
ALTER TABLE `minecity_chunks` ENGINE=InnoDB;
ALTER TABLE `minecity_entities` ENGINE=InnoDB;
ALTER TABLE `minecity_groups` ENGINE=InnoDB;
ALTER TABLE `minecity_group_players` ENGINE=InnoDB;
ALTER TABLE `minecity_group_entities` ENGINE=InnoDB;
ALTER TABLE `minecity_city_perm_defaults` ENGINE=InnoDB;
ALTER TABLE `minecity_city_perm_player` ENGINE=InnoDB;
ALTER TABLE `minecity_city_perm_entity` ENGINE=InnoDB;
ALTER TABLE `minecity_city_perm_group` ENGINE=InnoDB;
ALTER TABLE `minecity_group_managers` ENGINE=InnoDB;
ALTER TABLE `minecity_plots` ENGINE=InnoDB;
ALTER TABLE `minecity_plot_perm_defaults` ENGINE=InnoDB;
ALTER TABLE `minecity_plot_perm_player` ENGINE=InnoDB;
ALTER TABLE `minecity_plot_perm_entity` ENGINE=InnoDB;
ALTER TABLE `minecity_plot_perm_group` ENGINE=InnoDB;
ALTER TABLE `minecity_world_perm_defaults` ENGINE=InnoDB;

ALTER TABLE `minecity_city`
  ADD CONSTRAINT `city_owner` FOREIGN KEY (`owner`) REFERENCES `minecity_players` (`player_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `city_world_spawn` FOREIGN KEY (`spawn_world`) REFERENCES `minecity_world` (`world_id`) ON DELETE RESTRICT ON UPDATE CASCADE
;

ALTER TABLE `minecity_islands`
  ADD CONSTRAINT `island_world` FOREIGN KEY (`world_id`) REFERENCES `minecity_world` (`world_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `island_city` FOREIGN KEY (`city_id`) REFERENCES `minecity_city` (`city_id`) ON DELETE CASCADE ON UPDATE CASCADE
;

ALTER TABLE `minecity_chunks`
  ADD CONSTRAINT `chunk_world` FOREIGN KEY (`world_id`) REFERENCES `minecity_world` (`world_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `chunk_island` FOREIGN KEY (`island_id`) REFERENCES `minecity_islands` (`island_id`) ON DELETE CASCADE ON UPDATE CASCADE
;

ALTER TABLE `minecity_entities`
  ADD CONSTRAINT `entity_world` FOREIGN KEY (`last_world`) REFERENCES `minecity_world` (`world_id`) ON DELETE SET NULL ON UPDATE CASCADE
;

ALTER TABLE `minecity_groups`
  ADD CONSTRAINT `group_city` FOREIGN KEY (`city_id`) REFERENCES `minecity_city` (`city_id`) ON DELETE CASCADE ON UPDATE CASCADE
;

ALTER TABLE `minecity_group_players`
  ADD CONSTRAINT `group_players_group` FOREIGN KEY (`group_id`) REFERENCES `minecity_groups` (`group_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `group_players_player` FOREIGN KEY (`player_id`) REFERENCES `minecity_players` (`player_id`) ON DELETE CASCADE ON UPDATE CASCADE
;

ALTER TABLE `minecity_group_entities`
  ADD CONSTRAINT `group_entities_group` FOREIGN KEY (`group_id`) REFERENCES `minecity_groups` (`group_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `group_entities_entity` FOREIGN KEY (`entity_id`) REFERENCES `minecity_entities` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
;

ALTER TABLE `minecity_city_perm_defaults`
  ADD CONSTRAINT `city_perm_defaults` FOREIGN KEY (`city_id`) REFERENCES `minecity_city` (`city_id`) ON DELETE CASCADE ON UPDATE CASCADE
;

ALTER TABLE `minecity_city_perm_player`
  ADD CONSTRAINT `city_perm_player_city` FOREIGN KEY (`city_id`) REFERENCES `minecity_city` (`city_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `city_perm_player_player` FOREIGN KEY (`player_id`) REFERENCES `minecity_players` (`player_id`) ON DELETE CASCADE ON UPDATE CASCADE
;

ALTER TABLE `minecity_city_perm_entity`
  ADD CONSTRAINT `city_perm_entity_city` FOREIGN KEY (`city_id`) REFERENCES `minecity_city` (`city_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `city_perm_entity_entity` FOREIGN KEY (`entity_id`) REFERENCES `minecity_entities` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
;

ALTER TABLE `minecity_city_perm_group`
  ADD CONSTRAINT `city_perm_group_city` FOREIGN KEY (`city_id`) REFERENCES `minecity_city` (`city_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `city_perm_group_group` FOREIGN KEY (`group_id`) REFERENCES `minecity_groups` (`group_id`) ON DELETE CASCADE ON UPDATE CASCADE
;

ALTER TABLE `minecity_group_managers`
  ADD CONSTRAINT `group_managers_group` FOREIGN KEY (`group_id`) REFERENCES `minecity_groups` (`group_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `group_managers_player` FOREIGN KEY (`player_id`) REFERENCES `minecity_players` (`player_id`) ON DELETE CASCADE ON UPDATE CASCADE
;

ALTER TABLE `minecity_plots`
  ADD CONSTRAINT `plot_island` FOREIGN KEY (`island_id`) REFERENCES `minecity_islands` (`island_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `plot_owner` FOREIGN KEY (`owner`) REFERENCES `minecity_players` (`player_id`) ON DELETE SET NULL ON UPDATE CASCADE
;

ALTER TABLE `minecity_plot_perm_defaults`
  ADD CONSTRAINT `plot_perm_plot` FOREIGN KEY (`plot_id`) REFERENCES `minecity_plots` (`plot_id`) ON DELETE CASCADE ON UPDATE CASCADE
;

ALTER TABLE `minecity_plot_perm_player`
  ADD CONSTRAINT `plot_perm_player_plot` FOREIGN KEY (`plot_id`) REFERENCES `minecity_plots` (`plot_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `plot_perm_player_player` FOREIGN KEY (`player_id`) REFERENCES `minecity_players` (`player_id`) ON DELETE CASCADE ON UPDATE CASCADE
;

ALTER TABLE `minecity_plot_perm_entity`
  ADD CONSTRAINT `plot_perm_entity_plot` FOREIGN KEY (`plot_id`) REFERENCES `minecity_plots` (`plot_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `plot_perm_entity_entity` FOREIGN KEY (`entity_id`) REFERENCES `minecity_entities` (`entity_id`) ON DELETE CASCADE ON UPDATE CASCADE
;

ALTER TABLE `minecity_plot_perm_group`
  ADD CONSTRAINT `plot_perm_group_plot` FOREIGN KEY (`plot_id`) REFERENCES `minecity_plots` (`plot_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `plot_perm_group_entity` FOREIGN KEY (`group_id`) REFERENCES `minecity_groups` (`group_id`) ON DELETE CASCADE ON UPDATE CASCADE
;

ALTER TABLE `minecity_world_perm_defaults`
  ADD CONSTRAINT `world_perm_world` FOREIGN KEY (`world_id`) REFERENCES `minecity_world` (`world_id`) ON DELETE CASCADE ON UPDATE CASCADE
;

ALTER TABLE `minecity_setup`
  MODIFY COLUMN `value`  enum('6', '7') NOT NULL DEFAULT '7';

DELETE FROM `minecity_setup`;
INSERT INTO `minecity_setup` VALUES ('version', '7');

ALTER TABLE `minecity_setup`
  MODIFY COLUMN `value`  enum('7') NOT NULL DEFAULT '7';
