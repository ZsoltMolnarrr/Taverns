package net.village_taverns;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.village_taverns.config.BrewingConfig;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// Applies the brewing recipes from [BrewingConfig] to the game's brewing registry.
///
/// Without these, the SpellPower / RangedWeaponAPI potions that
/// [net.village_taverns.mixin.PotionsMixin] causes to be registered are obtainable only from the
/// bartender's trade table — there is no survival brewing path for any of them.
///
/// Brewing in 1.21.1 is **code-registered, not datapack-driven**: no recipe type, no serializer, and
/// so nothing for datagen to emit. The recipe list therefore lives in a TinyConfig file
/// (`config/village_taverns/brewing.json`) rather than in `data/`, which is what gives pack authors
/// the reach a datapack would normally have. This class only resolves ids and forwards them.
///
/// Only vanilla API is used here, so this stays in `common` — each loader merely hands us its
/// builder, which is the *same vanilla class* on both:
/// - Fabric: `FabricBrewingRecipeRegistryBuilder.BUILD`
/// - NeoForge: `RegisterBrewingRecipesEvent` (game bus, posted after `registerDefaults` and before
///   `build()`, so our recipes append after vanilla's and vanilla wins any conflicting pair)
public class TavernBrewing {
    private static final Logger LOGGER = LoggerFactory.getLogger(TavernsMod.ID);

    public static void register(PotionBrewing.Builder builder) {
        // safeValue(), not value: this fires per world load rather than from init(), so it must not
        // depend on TavernsMod.init() having refreshed first. safeValue() double-checks the loaded
        // flag under a monitor and refreshes if needed, so a concurrent first access cannot observe
        // a half-loaded config.
        var config = TavernsMod.brewingConfig.safeValue();
        // Still null-checked: an empty or blank brewing.json makes Gson.fromJson return null, which
        // load() then stores as the value.
        if (config == null || !config.enabled || config.recipes == null) {
            return;
        }

        var registered = 0;
        // Recipes naming a potion nobody registered. Counted rather than logged per-line: this is the
        // expected, uninteresting case when SpellPower or RangedWeaponAPI simply is not installed.
        var unresolvedPotions = 0;

        for (var recipe : config.recipes) {
            if (recipe == null || recipe.base == null || recipe.ingredient == null || recipe.result == null) {
                LOGGER.warn("Brewing config: skipping entry with a missing base/ingredient/result field");
                continue;
            }

            var ingredient = item(recipe.ingredient);
            if (ingredient == null) {
                // Items are vanilla, so an unresolved one is a typo rather than a missing dependency.
                LOGGER.warn("Brewing config: unknown ingredient item '{}', skipping", recipe.ingredient);
                continue;
            }

            var base = potion(recipe.base);
            var result = potion(recipe.result);
            if (base == null || result == null) {
                unresolvedPotions++;
                continue;
            }

            builder.addMix(base, ingredient, result);
            registered++;
        }

        if (unresolvedPotions > 0) {
            LOGGER.info("Registered {} brewing recipes, skipped {} naming an unregistered potion "
                    + "(expected when SpellPower / RangedWeaponAPI are absent)", registered, unresolvedPotions);
        } else {
            LOGGER.info("Registered {} brewing recipes", registered);
        }
    }

    /// Null when the id is malformed or the potion is unregistered — the latter being the normal
    /// case when the owning mod is absent. Mirrors `TavernVillagers.createPotionStack`.
    @Nullable
    private static Holder<Potion> potion(String potionId) {
        var id = Identifier.tryParse(potionId);
        if (id == null) {
            LOGGER.warn("Brewing config: malformed potion id '{}', skipping", potionId);
            return null;
        }
        return BuiltInRegistries.POTION.get(id)
                .map(reference -> (Holder<Potion>) reference)
                .orElse(null);
    }

    @Nullable
    private static Item item(String itemId) {
        var id = Identifier.tryParse(itemId);
        if (id == null) {
            return null;
        }
        return BuiltInRegistries.ITEM.getOptional(id).orElse(null);
    }
}
