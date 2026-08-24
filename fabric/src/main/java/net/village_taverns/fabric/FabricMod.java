package net.village_taverns.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.item.ItemGroups;
import net.village_taverns.TavernBrewing;
import net.village_taverns.TavernVillagers;
import net.village_taverns.TavernsMod;
import net.village_taverns.block.TavernBlocks;

public final class FabricMod implements ModInitializer {
    @Override
    public void onInitialize() {
        TavernsMod.init();
        TavernsMod.registerBlocks();

        // Villager POI + schedule + profession + trades — Fabric API (loader-specific).
        PointOfInterestHelper.register(TavernVillagers.PROFESSION_ID,
                TavernVillagers.POI_TICKET_COUNT, TavernVillagers.POI_SEARCH_DISTANCE,
                TavernVillagers.poiBlockStates());
        TavernVillagers.registerSchedule();
        TavernVillagers.registerProfession(); // registers the bartender profession + builds TRADES
        TavernVillagers.TRADES.forEach((tier, factories) ->
                TradeOfferHelper.registerVillagerOffers(TavernVillagers.BAR_TENDER_PROFESSION, tier,
                        list -> list.addAll(factories)));

        // Brewing recipes for the SpellPower / RangedWeaponAPI potions - Fabric API.
        // Fired per-world when the registry is built, long after the Potions <clinit> mixin has
        // registered them, so lookups in TavernBrewing always resolve.
        FabricBrewingRecipeRegistryBuilder.BUILD.register(TavernBrewing::register);

        // Creative-tab placement (vanilla Functional tab) — Fabric API.
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register((content) -> {
            for (var entry : TavernBlocks.all) {
                content.add(entry.item());
            }
        });
    }
}
