ALTER TABLE `minecity_city`
  ADD COLUMN `investment`  double NOT NULL DEFAULT 0 AFTER `perm_denial_message`;

ALTER TABLE `minecity_plots`
  ADD COLUMN `investment`  double NOT NULL DEFAULT 0 AFTER `perm_denial_message`;
