ALTER TABLE `minecity_city`
  ADD COLUMN `investment`  double NOT NULL DEFAULT 0 AFTER `perm_denial_message`;

ALTER TABLE `minecity_plots`
  ADD COLUMN `investment`  double NOT NULL DEFAULT 0 AFTER `perm_denial_message`;

ALTER TABLE `minecity_setup`
  MODIFY COLUMN `value`  enum('4', '5') NOT NULL DEFAULT '5';

UPDATE `minecity_setup` SET `value`='5';

ALTER TABLE `minecity_setup`
  MODIFY COLUMN `value`  enum('5') NOT NULL DEFAULT '5';
