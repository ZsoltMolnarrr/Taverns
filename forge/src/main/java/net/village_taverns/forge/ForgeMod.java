package net.village_taverns.forge;

import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.RegisterEvent;
import net.village_taverns.Platform;
import net.village_taverns.TavernBrewing;
import net.village_taverns.TavernVillagers;
import net.village_taverns.TavernsMod;
import net.village_taverns.block.TavernBlocks;
import net.village_taverns.compat.RangedWeaponCompat;
import net.village_taverns.compat.SpellPowerCompat;
import net.village_taverns.forge.brewing.PotionBrewingRecipe;
import net.village_taverns.forge.client.ForgeClientMod;

@Mod(TavernsMod.ID)
public final class ForgeMod {

    // FMLJavaModLoadingContext.get() is flagged for removal by late 47.x builds, but the
    // constructor-injected replacement doesn't exist on early 47.x; get() works on all of [47,).
    @SuppressWarnings("removal")
    public ForgeMod() {
        TavernsMod.init();

        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        // Explicit event class: Forge 47's plain addListener(Consumer) infers the event type from the
        // lambda via TypeTools, which is fragile; the 4-arg overload takes it directly.
        modBus.addListener(EventPriority.NORMAL, false, RegisterEvent.class, ForgeMod::register);
        // Brewing: the recipe list is static and must be filled after the potions exist, i.e. after the
        // whole RegisterEvent phase. Common setup is the first hook that qualifies. Forge routes the
        // brewing stand through its own registry, so the recipes are IBrewingRecipes.
        modBus.addListener(EventPriority.NORMAL, false, FMLCommonSetupEvent.class,
                event -> event.enqueueWork(() -> TavernBrewing.register(
                        (base, ingredient, result) -> BrewingRecipeRegistry.addRecipe(
                                new PotionBrewingRecipe(base, ingredient, result)))));
        // Tavern blocks into the vanilla Functional tab — Forge mod-bus event (replaces ItemGroupEvents).
        modBus.addListener(EventPriority.NORMAL, false, BuildCreativeModeTabContentsEvent.class, ForgeMod::buildTabContents);
        // Villager trades — game-bus event (fired per profession); replaces Fabric API's TradeOfferHelper.
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, VillagerTradesEvent.class, ForgeMod::onVillagerTrades);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ForgeClientMod.register(modBus);
        }
    }

    /// Forge 47 unfreezes exactly one registry per `RegisterEvent` window, so every registry gets its own.
    public static void register(RegisterEvent event) {
        event.register(RegistryKeys.BLOCK, reg -> {
            TavernsMod.registerBlocks();
        });
        event.register(RegistryKeys.ITEM, reg -> {
            TavernsMod.registerBlockItems();
        });
        event.register(RegistryKeys.POTION, reg -> {
            // Replaces Fabric's `Potions.<clinit>` TAIL mixin: SpellPower and RangedWeaponAPI both
            // register their potions opt-in, and this is the only window in which they may.
            registerCompatPotions();
        });
        event.register(RegistryKeys.SCHEDULE, reg -> {
            TavernVillagers.registerSchedule();
        });
        event.register(RegistryKeys.POINT_OF_INTEREST_TYPE, reg -> {
            // POI registration — vanilla registry insert. Forge 47's PointOfInterestTypeCallbacks wires
            // the block-state -> POI mapping from the type's block states, so no helper is needed.
            Registry.register(Registries.POINT_OF_INTEREST_TYPE, TavernVillagers.PROFESSION_ID,
                    new PointOfInterestType(TavernVillagers.poiBlockStates(),
                            TavernVillagers.POI_TICKET_COUNT, TavernVillagers.POI_SEARCH_DISTANCE));
        });
        event.register(RegistryKeys.VILLAGER_PROFESSION, reg -> {
            TavernVillagers.registerProfession();
        });
    }

    private static void registerCompatPotions() {
        if (Platform.util().isModLoaded("spell_power")) {
            try {
                SpellPowerCompat.init();
            } catch (Throwable t) { }
        }
        if (Platform.util().isModLoaded("ranged_weapon_api")) {
            try {
                RangedWeaponCompat.init();
            } catch (Throwable t) { }
        }
    }

    private static void buildTabContents(BuildCreativeModeTabContentsEvent event) {
        if (!event.getTabKey().equals(ItemGroups.FUNCTIONAL)) {
            return;
        }
        for (var entry : TavernBlocks.all) {
            event.add(entry.item());
        }
    }

    private static void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() != TavernVillagers.BAR_TENDER_PROFESSION) {
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
