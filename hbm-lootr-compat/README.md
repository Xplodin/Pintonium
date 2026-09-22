# HBM Lootr Compatibility

Small Forge 1.12.2 integration for HBM NTM-CE structure crates and Lootr.

## Why it is needed

Lootr 0.6.2 only performs its legacy world-generation conversion for
`TileEntityLockableLoot`. HBM crates use `TileEntityCrateBase`, so Lootr does not
see them even though HBM stores standard `LootTable` and `LootTableSeed` values.

## Safety rules

- Transfers unopened HBM loot tables and seeds without generating their contents.
- Respects Lootr's dimension and loot-table blacklists.
- Skips locked HBM crates by default.
- Only snapshots table-less, pre-filled crates during fresh chunk population.
- Suppresses item drops while replacing the original block.
- Restores the original block and NBT if loot transfer fails.
- Defaults to `dryRun=true` so the first launch only audits eligible crates.

## Test procedure

1. Place the built jar in the instance `mods` directory on both server and client.
2. Start with the generated `config/hbmlootrcompat.cfg` containing `dryRun=true`.
3. Visit an unexplored HBM structure and search `latest.log` for
   `Eligible HBM crate`.
4. Stop Minecraft, change `dryRun=false`, then revisit or reload the chunk.
5. Have two players open the converted Lootr container and verify independent loot.

Only unopened crates can retain true per-player random generation. Previously
opened crates no longer carry a loot table and are intentionally left unchanged.
