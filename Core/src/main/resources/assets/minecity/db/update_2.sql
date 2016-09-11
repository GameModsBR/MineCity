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
MODIFY COLUMN `value`  enum('2', '3') NOT NULL DEFAULT '3';

UPDATE `minecity_setup` SET `value`='3';

ALTER TABLE `minecity_setup`
MODIFY COLUMN `value`  enum('3') NOT NULL DEFAULT '3';
