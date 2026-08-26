package net.village_taverns.config;

import net.rpg_foundation.structure_pool_api.api.StructurePoolConfig;
import net.village_taverns.TavernsMod;

import java.util.ArrayList;
import java.util.List;

public class Defaults {
    public static final StructurePoolConfig villages;
    public static final BrewingConfig brewing;

    static {
        villages = new StructurePoolConfig();
        var weight = 10;
        var limit = 1;
        villages.entries = new ArrayList<>(List.of(
                new StructurePoolConfig.Entry("minecraft:village/desert/houses", TavernsMod.ID + ":village/desert/tavern", weight, limit),
                new StructurePoolConfig.Entry("minecraft:village/savanna/houses", TavernsMod.ID + ":village/savanna/tavern", weight, limit),
                new StructurePoolConfig.Entry("minecraft:village/plains/houses", TavernsMod.ID + ":village/plains/tavern", weight, limit),
                new StructurePoolConfig.Entry("minecraft:village/taiga/houses", TavernsMod.ID + ":village/taiga/tavern", weight, limit),
                new StructurePoolConfig.Entry("minecraft:village/snowy/houses", TavernsMod.ID + ":village/snowy/tavern", weight, limit)
        ));
    }

    /// The shipped brewing tree. Vanilla leaves two potions as dead ends — nothing brews *from*
    /// Thick or Mundane — so they serve as the two bases here, which keeps the whole tree off the
    /// crowded Awkward hub and gives one rule: **thick for casters, mundane for archers**.
    ///
    /// No `(base, ingredient)` pair below appears in `BrewingRecipeRegistry.registerDefaults`.
    /// Only Sugar is also a vanilla brewing ingredient, and only from Water and Awkward, so the
    /// pairs here stay clear. With one deliberate exception (amethyst, for Arcane) every reagent is
    /// a mob drop or a harvest, matching vanilla's palette, where only `stone` is a mineral flavour
    /// reagent.
    static {
        brewing = new BrewingConfig();
        brewing.setSchemaVersion(BrewingConfig.SCHEMA_VERSION);

        var thick = "minecraft:thick";      // Water + Glowstone Dust
        var mundane = "minecraft:mundane";  // Water + Redstone
        var fermentedSpiderEye = "minecraft:fermented_spider_eye";

        // Branch A — Thick, the caster's base.
        brew(thick, "minecraft:amethyst_shard", spellPower("arcane"), "Arcane Power");
        // Vanilla only ever consumes blaze *powder*, so the intact rod is free.
        brew(thick, "minecraft:blaze_rod", spellPower("fire"), "Fire Power");
        brew(thick, "minecraft:snowball", spellPower("frost"), "Frost Power");
        brew(thick, "minecraft:honeycomb", spellPower("healing"), "Healing Power");
        brew(thick, "minecraft:glow_ink_sac", spellPower("lightning"), "Lightning Power");
        brew(thick, "minecraft:rotten_flesh", spellPower("soul"), "Soul Power");
        brew(thick, "minecraft:glow_berries", spellPower("critical_chance"), "Spell Volatility");
        // Sugar is the one reagent here vanilla also uses (Water -> Mundane, Awkward -> Swiftness).
        // The pair is still free, since nothing vanilla brews from Thick. Chosen over Goat Horn,
        // which was maxCount(1) (one horn per batch, no stacking), barely renewable, and — because
        // matching is by item id — could consume a collected instrument variant by accident.
        brew(thick, "minecraft:sugar", spellPower("critical_damage"), "Amplify Spell");
        brew(thick, "minecraft:chorus_fruit", spellPower("haste"), "Spell Haste");

        // Branch B — Mundane, the marksman's base.
        brew(mundane, "minecraft:sweet_berries", rangedWeapon("damage"), "Ranged Damage");
        brew(mundane, "minecraft:feather", rangedWeapon("haste"), "Draw Speed");

        // Branch C — fermented spider eye school inversions. Vanilla's signature trick applied
        // across schools, so a player holding one reagent can reach the opposing school. Listed in
        // both directions on purpose: each flip still costs an eye, so cycling gains nothing.
        invert(fermentedSpiderEye, spellPower("fire"), spellPower("frost"));
        invert(fermentedSpiderEye, spellPower("healing"), spellPower("soul"));
        invert(fermentedSpiderEye, spellPower("arcane"), spellPower("lightning"));
    }

    /// Potion ids follow `<namespace>:<namespace>.<path>` — see `SpellPowerMod.potionIdFrom`.
    private static String spellPower(String path) {
        return "spell_power:spell_power." + path;
    }

    /// See `RangedWeaponMod.potionId`.
    private static String rangedWeapon(String path) {
        return "ranged_weapon:ranged_weapon." + path;
    }

    private static void brew(String base, String ingredient, String result, String note) {
        brewing.recipes.add(new BrewingConfig.Recipe(base, ingredient, result, note));
    }

    private static void invert(String ingredient, String one, String other) {
        brew(one, ingredient, other, "Inversion");
        brew(other, ingredient, one, "Inversion");
    }
}
