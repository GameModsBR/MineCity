ALTER TABLE `minecity_city_perm_defaults`
MODIFY COLUMN `perm`  enum('ENTER','CLICK','PICKUP','HARVEST','OPEN','MODIFY','LEAVE','PVP','PVC','PVM','VEHICLE','RIDE', 'SPAWN_VEHICLES') NOT NULL;

ALTER TABLE `minecity_city_perm_player`
MODIFY COLUMN `perm`  enum('ENTER','CLICK','PICKUP','HARVEST','OPEN','MODIFY','LEAVE','PVP','PVC','PVM','VEHICLE','RIDE', 'SPAWN_VEHICLES') NOT NULL;

ALTER TABLE `minecity_city_perm_entity`
MODIFY COLUMN `perm`  enum('ENTER','CLICK','PICKUP','HARVEST','OPEN','MODIFY','LEAVE','PVP','PVC','PVM','VEHICLE','RIDE', 'SPAWN_VEHICLES') NOT NULL;

ALTER TABLE `minecity_city_perm_group`
MODIFY COLUMN `perm`  enum('ENTER','CLICK','PICKUP','HARVEST','OPEN','MODIFY','LEAVE','PVP','PVC','PVM','VEHICLE','RIDE', 'SPAWN_VEHICLES') NOT NULL;

ALTER TABLE `minecity_plot_perm_defaults`
MODIFY COLUMN `perm`  enum('ENTER','CLICK','PICKUP','HARVEST','OPEN','MODIFY','LEAVE','PVP','PVC','PVM','VEHICLE','RIDE', 'SPAWN_VEHICLES') NOT NULL;

ALTER TABLE `minecity_plot_perm_player`
MODIFY COLUMN `perm`  enum('ENTER','CLICK','PICKUP','HARVEST','OPEN','MODIFY','LEAVE','PVP','PVC','PVM','VEHICLE','RIDE', 'SPAWN_VEHICLES') NOT NULL;

ALTER TABLE `minecity_plot_perm_entity`
MODIFY COLUMN `perm`  enum('ENTER','CLICK','PICKUP','HARVEST','OPEN','MODIFY','LEAVE','PVP','PVC','PVM','VEHICLE','RIDE', 'SPAWN_VEHICLES') NOT NULL;

ALTER TABLE `minecity_plot_perm_group`
MODIFY COLUMN `perm`  enum('ENTER','CLICK','PICKUP','HARVEST','OPEN','MODIFY','LEAVE','PVP','PVC','PVM','VEHICLE','RIDE', 'SPAWN_VEHICLES') NOT NULL;

ALTER TABLE `minecity_world_perm_defaults`
MODIFY COLUMN `perm`  enum('ENTER','CLICK','PICKUP','HARVEST','OPEN','MODIFY','LEAVE','PVP','PVC','PVM','VEHICLE','RIDE', 'SPAWN_VEHICLES') NOT NULL;

UPDATE `minecity_city_perm_defaults` SET `perm`='VEHICLE' WHERE `perm`='SPAWN_VEHICLES';
UPDATE `minecity_city_perm_player` SET `perm`='VEHICLE' WHERE `perm`='SPAWN_VEHICLES';
UPDATE `minecity_city_perm_entity` SET `perm`='VEHICLE' WHERE `perm`='SPAWN_VEHICLES';
UPDATE `minecity_city_perm_group` SET `perm`='VEHICLE' WHERE `perm`='SPAWN_VEHICLES';
UPDATE `minecity_plot_perm_defaults` SET `perm`='VEHICLE' WHERE `perm`='SPAWN_VEHICLES';
UPDATE `minecity_plot_perm_player` SET `perm`='VEHICLE' WHERE `perm`='SPAWN_VEHICLES';
UPDATE `minecity_plot_perm_entity` SET `perm`='VEHICLE' WHERE `perm`='SPAWN_VEHICLES';
UPDATE `minecity_plot_perm_group` SET `perm`='VEHICLE' WHERE `perm`='SPAWN_VEHICLES';
UPDATE `minecity_world_perm_defaults` SET `perm`='VEHICLE' WHERE `perm`='SPAWN_VEHICLES';

ALTER TABLE `minecity_city_perm_defaults`
MODIFY COLUMN `perm`  enum('ENTER','CLICK','PICKUP','HARVEST','OPEN','MODIFY','LEAVE','PVP','PVC','PVM','VEHICLE','RIDE') NOT NULL;

ALTER TABLE `minecity_city_perm_player`
MODIFY COLUMN `perm`  enum('ENTER','CLICK','PICKUP','HARVEST','OPEN','MODIFY','LEAVE','PVP','PVC','PVM','VEHICLE','RIDE') NOT NULL;

ALTER TABLE `minecity_city_perm_entity`
MODIFY COLUMN `perm`  enum('ENTER','CLICK','PICKUP','HARVEST','OPEN','MODIFY','LEAVE','PVP','PVC','PVM','VEHICLE','RIDE') NOT NULL;

ALTER TABLE `minecity_city_perm_group`
MODIFY COLUMN `perm`  enum('ENTER','CLICK','PICKUP','HARVEST','OPEN','MODIFY','LEAVE','PVP','PVC','PVM','VEHICLE','RIDE') NOT NULL;

ALTER TABLE `minecity_plot_perm_defaults`
MODIFY COLUMN `perm`  enum('ENTER','CLICK','PICKUP','HARVEST','OPEN','MODIFY','LEAVE','PVP','PVC','PVM','VEHICLE','RIDE') NOT NULL;

ALTER TABLE `minecity_plot_perm_player`
MODIFY COLUMN `perm`  enum('ENTER','CLICK','PICKUP','HARVEST','OPEN','MODIFY','LEAVE','PVP','PVC','PVM','VEHICLE','RIDE') NOT NULL;

ALTER TABLE `minecity_plot_perm_entity`
MODIFY COLUMN `perm`  enum('ENTER','CLICK','PICKUP','HARVEST','OPEN','MODIFY','LEAVE','PVP','PVC','PVM','VEHICLE','RIDE') NOT NULL;

ALTER TABLE `minecity_plot_perm_group`
MODIFY COLUMN `perm`  enum('ENTER','CLICK','PICKUP','HARVEST','OPEN','MODIFY','LEAVE','PVP','PVC','PVM','VEHICLE','RIDE') NOT NULL;

ALTER TABLE `minecity_world_perm_defaults`
MODIFY COLUMN `perm`  enum('ENTER','CLICK','PICKUP','HARVEST','OPEN','MODIFY','LEAVE','PVP','PVC','PVM','VEHICLE','RIDE') NOT NULL;

ALTER TABLE `minecity_setup`
MODIFY COLUMN `value`  enum('1', '2') NOT NULL DEFAULT '2';

UPDATE `minecity_setup` SET `value`='2';

ALTER TABLE `minecity_setup`
MODIFY COLUMN `value`  enum('2') NOT NULL DEFAULT '2';
