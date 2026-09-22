package com.bean.beanutils.compat.hbmlootr;

import net.minecraftforge.common.config.Configuration;

public final class CompatConfig {
   final boolean enabled;
   final boolean dryRun;
   final boolean convertFilledWorldgenCrates;
   final boolean skipLockedCrates;
   final int periodicScanTicks;

   private CompatConfig(boolean enabled, boolean dryRun, boolean convertFilledWorldgenCrates, boolean skipLockedCrates, int periodicScanTicks) {
      this.enabled = enabled;
      this.dryRun = dryRun;
      this.convertFilledWorldgenCrates = convertFilledWorldgenCrates;
      this.skipLockedCrates = skipLockedCrates;
      this.periodicScanTicks = periodicScanTicks;
   }

   public static CompatConfig load(Configuration config) {
      String category = "general";
      boolean enabled = config.getBoolean("enabled", category, true, "Master switch for HBM structure-crate conversion.");
      boolean dryRun = config.getBoolean("dryRun", category, true, "Log eligible HBM crates without replacing them. Set false after reviewing the log.");
      boolean convertFilled = config.getBoolean(
         "convertFilledWorldgenCrates", category, true, "Convert table-less, pre-filled HBM crates only during fresh chunk population."
      );
      boolean skipLocked = config.getBoolean(
         "skipLockedCrates", category, true, "Never replace locked HBM crates, preserving structure puzzles and access controls."
      );
      int scanTicks = config.getInt(
         "periodicScanTicks", category, 100, 20, 72000, "Interval for finding delayed HBM structure crates. Only unopened loot-table crates qualify."
      );
      return new CompatConfig(enabled, dryRun, convertFilled, skipLocked, scanTicks);
   }
}
