package net.village_taverns.neoforge;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.village_taverns.TavernsMod;
import net.village_taverns.TavernBrewing;
import net.village_taverns.TavernVillagers;
import net.village_taverns.block.TavernBlocks;

@Mod(TavernsMod.ID)
public final class NeoForgeMod {
    public NeoForgeMod(IEventBus modBus) {
        TavernsMod.init();
        modBus.addListener(RegisterEvent.class, NeoForgeMod::register);
        // Tavern blocks into the vanilla Functional tab — NeoForge mod-bus event (replaces ItemGroupEvents).
        modBus.addListener(BuildCreativeModeTabContentsEvent.class, NeoForgeMod::buildTabContents);
        // Villager trades — game-bus event (fired per profession); replaces Fabric API's TradeOfferHelper.
        NeoForge.EVENT_BUS.addListener(VillagerTradesEvent.class, NeoForgeMod::onVillagerTrades);
        // Brewing recipes - game-bus event, fired after BrewingRecipeRegistry.registerDefaults and
        // before build(), so our recipes append after vanilla's. Replaces Fabric API's BUILD event.
        NeoForge.EVENT_BUS.addListener(RegisterBrewingRecipesEvent.class, NeoForgeMod::onRegisterBrewingRecipes);
    }

    public static void register(RegisterEvent event) {
        event.register(Registries.BLOCK, reg -> {
            TavernsMod.registerBlocks();
        });
        // 1.21.11: the `minecraft:schedule` registry is gone; the bartender's always-work schedule is
        // an `EnvironmentAttribute<Activity>` in the `minecraft:environment_attribute` registry.
        event.register(Registries.ENVIRONMENT_ATTRIBUTE, reg -> {
            TavernVillagers.registerSchedule();
        });
        event.register(Registries.POINT_OF_INTEREST_TYPE, reg -> {
            // POI registration — vanilla registry insert. NeoForge's POI registry callback wires the
            // block-state -> POI mapping from the type's block states, so no Fabric API helper is needed.
            try {
                Registry.register(BuiltInRegistries.POINT_OF_INTEREST_TYPE, TavernVillagers.PROFESSION_ID,
                        new PoiType(TavernVillagers.poiBlockStates(),
                                TavernVillagers.POI_TICKET_COUNT, TavernVillagers.POI_SEARCH_DISTANCE));
            } catch (Exception e) {
            }
        });
        event.register(Registries.VILLAGER_PROFESSION, reg -> {
            TavernVillagers.registerProfession();
        });
    }

    private static void onRegisterBrewingRecipes(RegisterBrewingRecipesEvent event) {
        TavernBrewing.register(event.getBuilder());
    }

    private static void buildTabContents(BuildCreativeModeTabContentsEvent event) {
        if (!event.getTabKey().equals(CreativeModeTabs.FUNCTIONAL_BLOCKS)) {
            return;
        }
        for (var entry : TavernBlocks.all) {
            event.accept(entry.item());
        }
    }

    private static void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() != TavernVillagers.BARTENDER_PROFESSION_KEY) {
            return;
        }
        TavernVillagers.TRADES.forEach((tier, factories) -> {
            var tierList = event.getTrades().get(tier.intValue());
            if (tierList != null) {
                tierList.addAll(factories);
            }
        });
    }
}
