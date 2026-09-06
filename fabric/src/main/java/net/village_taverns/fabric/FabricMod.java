package net.village_taverns.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistry;
import net.minecraft.item.ItemGroups;
import net.minecraft.recipe.Ingredient;
import net.village_taverns.TavernBrewing;
import net.village_taverns.TavernVillagers;
import net.village_taverns.TavernsMod;
import net.village_taverns.block.TavernBlocks;
import net.village_taverns.fabric.village.FabricVillageStructures;

public final class FabricMod implements ModInitializer {
    @Override
    public void onInitialize() {
        // StructurePoolAPI is Fabric-only on 1.20.1; install the village injector (and load its config)
        // before TavernsMod.init() calls VillageStructures.injectIfAvailable().
        FabricVillageStructures.install();
        TavernsMod.init();
        TavernsMod.registerBlocks();
        TavernsMod.registerBlockItems();

        // Villager POI + schedule + profession + trades — Fabric API (loader-specific).
        PointOfInterestHelper.register(TavernVillagers.PROFESSION_ID,
                TavernVillagers.POI_TICKET_COUNT, TavernVillagers.POI_SEARCH_DISTANCE,
                TavernVillagers.poiBlockStates());
        TavernVillagers.registerSchedule();
        TavernVillagers.registerProfession(); // registers the bartender profession + builds TRADES
        TavernVillagers.TRADES.forEach((tier, factories) ->
                TradeOfferHelper.registerVillagerOffers(TavernVillagers.BAR_TENDER_PROFESSION, tier,
                        list -> list.addAll(factories)));

        // Brewing recipes for the SpellPower / RangedWeaponAPI potions. 1.20.1's brewing registry is a
        // static list filled once during bootstrap, and mod initializers run after `Bootstrap.initialize`
        // (so after the Potions <clinit> mixin has registered those potions).
        TavernBrewing.register((base, ingredient, result) ->
                FabricBrewingRecipeRegistry.registerPotionRecipe(base, Ingredient.ofItems(ingredient), result));

        // Creative-tab placement (vanilla Functional tab) — Fabric API.
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register((content) -> {
            for (var entry : TavernBlocks.all) {
                content.add(entry.item());
            }
        });
    }
}
