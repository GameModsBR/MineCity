ALTER TABLE `minecity_plots`
  ADD COLUMN `tax_accepted_flat`  double NOT NULL DEFAULT 0 AFTER `perm_denial_message`,
  ADD COLUMN `tax_accepted_percent`  double NOT NULL DEFAULT 0 AFTER `tax_accepted_flat`,
  ADD COLUMN `tax_applied_flat`  double NOT NULL DEFAULT 0 AFTER `tax_accepted_percent`,
  ADD COLUMN `tax_applied_percent`  double NOT NULL DEFAULT 0 AFTER `tax_applied_flat`;

ALTER TABLE `minecity_city`
  ADD COLUMN `tax_applied_flat`  double NOT NULL DEFAULT 0 AFTER `perm_denial_message`,
  ADD COLUMN `tax_applied_percent`  double NOT NULL DEFAULT 0 AFTER `tax_applied_flat`;
