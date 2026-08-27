package net.village_taverns.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PoiHelper;
import net.fabricmc.fabric.api.registry.FabricPotionBrewingBuilder;
import net.minecraft.world.item.CreativeModeTabs;
import net.village_taverns.TavernBrewing;
import net.village_taverns.TavernVillagers;
import net.village_taverns.TavernsMod;
import net.village_taverns.block.TavernBlocks;

public final class FabricMod implements ModInitializer {
    @Override
    public void onInitialize() {
        TavernsMod.init();
        TavernsMod.registerBlocks();

        // Villager POI + schedule + profession — Fabric API (loader-specific).
        // 26.1: `TradeOfferHelper` is gone, trades are data (`data/village_taverns/villager_trade/**`).
        PoiHelper.register(TavernVillagers.PROFESSION_ID,
                TavernVillagers.POI_TICKET_COUNT, TavernVillagers.POI_SEARCH_DISTANCE,
                TavernVillagers.poiBlockStates());
        TavernVillagers.registerSchedule(); // the always-work EnvironmentAttribute<Activity>
        TavernVillagers.registerProfession(); // bartender profession + its per-level trade-set keys

        // Brewing recipes for the SpellPower / RangedWeaponAPI potions - Fabric API.
        // 26.1: `FabricBrewingRecipeRegistryBuilder` -> `FabricPotionBrewingBuilder` (same BUILD event,
        // same `PotionBrewing.Builder` callback argument).
        // Fired per-world when the registry is built, long after the Potions <clinit> mixin has
        // registered them, so lookups in TavernBrewing always resolve.
        FabricPotionBrewingBuilder.BUILD.register(TavernBrewing::register);

        // Creative-tab placement (vanilla Functional tab) — Fabric API.
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register((content) -> {
            for (var entry : TavernBlocks.all) {
                content.accept(entry.item());
            }
        });
    }
}
