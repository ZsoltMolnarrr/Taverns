package net.village_taverns;

import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.village_taverns.config.BrewingConfig;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// Applies the brewing recipes from [BrewingConfig] to the game's brewing registry.
///
/// Without these, the SpellPower / RangedWeaponAPI potions Taverns asks those mods to register are
/// obtainable only from the bartender's trade table — there is no survival brewing path for any of them.
///
/// Brewing is **code-registered, not datapack-driven**: no recipe type, no serializer, and so nothing
/// for datagen to emit. The recipe list therefore lives in a TinyConfig file
/// (`config/village_taverns/brewing.json`) rather than in `data/`, which is what gives pack authors
/// the reach a datapack would normally have. This class only resolves ids and forwards them.
///
/// **1.20.1 delta:** there is no `BrewingRecipeRegistry.Builder` and no per-world rebuild. 1.20.1's
/// brewing recipes are a static list filled once during bootstrap, and vanilla's
/// `registerPotionRecipe` is private — so each loader supplies its own [Registrar] and this class
/// only resolves ids:
/// - Fabric: `FabricBrewingRecipeRegistry.registerPotionRecipe`, called from the mod initializer
///   (which runs after `Bootstrap.initialize`, hence after the potions exist).
/// - Forge: a `net.minecraftforge.common.brewing.IBrewingRecipe`, added from `FMLCommonSetupEvent`
///   (after the `RegisterEvent` phase that registers the potions). Forge routes the brewing stand
///   through its own registry, so the vanilla list is not an option there anyway.
///
/// Registration happens after vanilla's, so a `base` + `ingredient` pair vanilla also defines keeps
/// vanilla's result (both loaders return the first match).
public class TavernBrewing {
    private static final Logger LOGGER = LoggerFactory.getLogger(TavernsMod.ID);

    /// What a loader does with one resolved recipe. Fabric hands this to Fabric API; Forge wraps it
    /// in an `IBrewingRecipe`.
    @FunctionalInterface
    public interface Registrar {
        void register(Potion base, Item ingredient, Potion result);
    }

    private static boolean registered = false;

    /// Idempotent: the recipe list is static and survives world reloads, so a second call would only
    /// duplicate entries.
    public static void register(Registrar registrar) {
        if (registered) {
            return;
        }
        registered = true;

        // safeValue(), not value: double-checks the loaded flag under a monitor and refreshes if
        // needed, so this cannot observe a half-loaded config even if init() has not run yet.
        var config = TavernsMod.brewingConfig.safeValue();
        // Still null-checked: an empty or blank brewing.json makes Gson.fromJson return null, which
        // load() then stores as the value.
        if (config == null || !config.enabled || config.recipes == null) {
            return;
        }

        var count = 0;
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

            registrar.register(base, ingredient, result);
            count++;
        }

        if (unresolvedPotions > 0) {
            LOGGER.info("Registered {} brewing recipes, skipped {} naming an unregistered potion "
                    + "(expected when SpellPower / RangedWeaponAPI are absent)", count, unresolvedPotions);
        } else {
            LOGGER.info("Registered {} brewing recipes", count);
        }
    }

    /// Null when the id is malformed or the potion is unregistered — the latter being the normal
    /// case when the owning mod is absent. Mirrors `TavernVillagers.createPotionStack`.
    @Nullable
    private static Potion potion(String potionId) {
        var id = Identifier.tryParse(potionId);
        if (id == null) {
            LOGGER.warn("Brewing config: malformed potion id '{}', skipping", potionId);
            return null;
        }
        return Registries.POTION.getOrEmpty(id).orElse(null);
    }

    @Nullable
    private static Item item(String itemId) {
        var id = Identifier.tryParse(itemId);
        if (id == null) {
            return null;
        }
        return Registries.ITEM.getOrEmpty(id).orElse(null);
    }
}
