ALTER TABLE `minecity_city`
  ADD COLUMN `price`  double NULL DEFAULT NULL AFTER `perm_denial_message`;

ALTER TABLE `minecity_plots`
  ADD COLUMN `price`  double NULL DEFAULT NULL AFTER `perm_denial_message`;

ALTER TABLE `minecity_setup`
  MODIFY COLUMN `value`  enum('5', '6') NOT NULL DEFAULT '6';

UPDATE `minecity_setup` SET `value`='6';

ALTER TABLE `minecity_setup`
  MODIFY COLUMN `value`  enum('6') NOT NULL DEFAULT '6';
