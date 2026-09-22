import loottweaker.LootTweaker;

// New Crimson Revelations 1.6.0 creates tower chests with this exact ID,
// but ships no corresponding loot table. Keep the loot_tables/ prefix:
// existing vanilla and Lootr chests already have it saved in their NBT.
// Lootr handles per-player generation using this table without another mod.
val tower = LootTweaker.newTable("crimsonrevelations:loot_tables/structures/thaumaturge_tower");

// Ordinary supplies from a village thaumaturge's study.
val supplies = tower.addPool("beanpack_tower_supplies", 3, 5, 0, 0);
supplies.addItemEntry(<minecraft:paper> * 4, 25, "paper");
supplies.addItemEntry(<minecraft:book> * 2, 15, "books");
supplies.addItemEntry(<minecraft:glass_bottle> * 3, 15, "bottles");
supplies.addItemEntry(<minecraft:dye:0> * 2, 10, "ink");
supplies.addItemEntry(<minecraft:gold_nugget> * 6, 10, "gold");

// Modest early/mid-game Thaumcraft materials; no progression-skip gear.
val materials = tower.addPool("beanpack_tower_materials", 2, 3, 0, 0);
materials.addItemEntry(<thaumcraft:amber> * 3, 30, "amber");
materials.addItemEntry(<thaumcraft:quicksilver> * 2, 25, "quicksilver");
materials.addItemEntry(<thaumcraft:ingot:0> * 2, 10, "thaumium");

// One treasure bag, usually common; its contents use Thaumcraft's own loot.
val treasure = tower.addPool("beanpack_tower_treasure", 1, 1, 0, 0);
treasure.addItemEntry(<thaumcraft:loot_bag:0>, 9, "common_bag");
treasure.addItemEntry(<thaumcraft:loot_bag:1>, 1, "uncommon_bag");
