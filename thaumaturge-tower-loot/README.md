# Thaumaturge tower loot repair

NewCrimsonRevelations-1.6.0.jar assigns its tower chest the ID
`crimsonrevelations:loot_tables/structures/thaumaturge_tower` but contains no
table for it. The 1.6.0 release notes explicitly describe tower loot as WIP.
The local log confirms that Lootr converted the chest and then failed to
resolve that table (reported chest: -371, 76, 192).

Copy `crimson_tower_loot.zs` into the instance/server `scripts` directory and
fully restart Minecraft or the dedicated server. Requires the already installed
CraftTweaker and LootTweaker. No mod JAR modifications are needed.

The table provides 3-5 study-supply rolls, 2-3 Thaumcraft-material rolls, and
one treasure bag (90% common / 10% uncommon). Lootr retains its normal
per-player inventories. Existing unopened chests using the same ID can use
the table; no new terrain is required for those chests.

## Verification after restart

1. Check `crafttweaker.log` for errors mentioning this script.
2. In a test world, run
   `/lootr chest crimsonrevelations:loot_tables/structures/thaumaturge_tower`
   at a clear test location, then open the resulting chest.
3. Check that supplies, Thaumcraft materials and one bag appear, and that
   `latest.log` no longer reports an unresolved tower loot table.
4. A second player should receive their own inventory.

Previously opened chests can retain an empty saved per-player inventory.
This script intentionally does not erase any saved inventories. In creative,
sneak-break only an affected empty chest and replace it with the test command
above at the intended position (the command places a chest at the player's
block position). Replacing it creates a new Lootr identity for all players;
first confirm no player is storing items in that chest. Do not use
`/lootr clear <player>` for this repair: that command clears the player's
inventories across all Lootr chests.

Static verification: exact table ID checked against installed tower bytecode;
LootTweaker signatures checked against installed 0.5.1; Thaumcraft item IDs
and metadata checked against installed BETA26. In-game verification still
requires restarting and loading the pack.

Upstream release notes:
https://www.curseforge.com/minecraft/mc-mods/new-crimson-revelations/files/8802619
LootTweaker API:
https://loottweaker-docs.readthedocs.io/en/latest/type-docs/loottweaker.html

Remove this script to stop creating the table on future starts. LootTweaker
also exports created tables into each world's data/loot_tables directory;
check that directory when intentionally removing or replacing the repair.
