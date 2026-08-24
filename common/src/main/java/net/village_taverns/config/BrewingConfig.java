package net.village_taverns.config;

import net.tiny_config.versioning.VersionableConfig;

import java.util.ArrayList;

/// User-editable brewing recipes, written to `config/village_taverns/brewing.json`.
///
/// Brewing is the one part of Minecraft content that has **no datapack path** in 1.21.1 — there is
/// no `RecipeType` for it, no serializer, and so nothing datagen can emit. Recipes only exist as
/// code handed to a `BrewingRecipeRegistry.Builder`. This config is the stand-in: pack authors get
/// the same reach a datapack would have given them, through a flat JSON list.
///
/// ### Format
/// ```json
/// {
///   "schema_version": 2,
///   "enabled": true,
///   "recipes": [
///     { "base": "minecraft:thick", "ingredient": "minecraft:amethyst_shard",
///       "result": "spell_power:spell_power.arcane", "note": "Arcane Power" }
///   ]
/// }
/// ```
/// - `base` / `result` — potion ids. Ours follow `<namespace>:<namespace>.<path>`
///   (e.g. `spell_power:spell_power.fire`, `ranged_weapon:ranged_weapon.damage`); vanilla ones are
///   plain (`minecraft:thick`, `minecraft:awkward`).
/// - `ingredient` — any item id.
/// - `note` — free text, ignored by the loader. Present only so the generated file reads as
///   documentation; delete it freely.
///
/// Nothing here is restricted to this mod's potions: `base` and `result` accept **any** registered
/// potion, so a pack can bolt on purely vanilla recipes (say `minecraft:awkward` + `minecraft:kelp`
/// → `minecraft:water_breathing`) through the same list.
///
/// ### Behaviour
/// - Entries whose potion is unregistered are skipped — that is the normal case when SpellPower or
///   RangedWeaponAPI is not installed, so it is counted rather than logged per-line.
/// - Entries with a malformed id or an unknown *item* are warned about individually: items are
///   vanilla, so failing to resolve one is a typo, not a missing dependency.
/// - `enabled: false` turns the whole feature off without emptying the list.
/// - Order matters only for conflicts: `BrewingRecipeRegistry.craft` returns the first match, and
///   vanilla's recipes are already registered by the time ours are appended, so a `base`+`ingredient`
///   pair that vanilla also defines will keep vanilla's result.
///
/// Bump [#SCHEMA_VERSION] whenever the shipped defaults change — TinyConfig discards any file
/// written against an older version and regenerates it, so players pick up new recipes instead of
/// silently keeping a stale list.
public class BrewingConfig extends VersionableConfig {
    public static final int SCHEMA_VERSION = 2;

    /// Master switch. `false` registers nothing, leaving `recipes` intact for later.
    public boolean enabled = true;

    public ArrayList<Recipe> recipes = new ArrayList<>();

    public static class Recipe {
        public Recipe() { }

        /// Potion id of the bottle in the brewing stand's lower slots.
        public String base;
        /// Item id of the reagent in the top slot.
        public String ingredient;
        /// Potion id the base is converted into.
        public String result;
        /// Free text for the reader's benefit; never parsed.
        public String note;

        public Recipe(String base, String ingredient, String result, String note) {
            this.base = base;
            this.ingredient = ingredient;
            this.result = result;
            this.note = note;
        }
    }
}
