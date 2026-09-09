package net.village_taverns.forge;

import net.minecraft.item.ItemGroups;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(TavernsMod.ID)
public final class ForgeMod {

    private static final Logger LOGGER = LoggerFactory.getLogger(TavernsMod.ID);

    // FMLJavaModLoadingContext.get() is flagged for removal by late 47.x builds, but the
    // constructor-injected replacement doesn't exist on early 47.x; get() works on all of [47,).
    @SuppressWarnings("removal")
    public ForgeMod() {
        TavernsMod.init();

        // RangedWeaponAPI's potions are opt-in, and this only *files* the request -- RangedWeaponAPI
        // registers them itself, from its own RegisterEvent(POTIONS) window. Filing the request from our
        // own POTION block below would race that: Forge posts the single POTION event to each mod's bus in
        // turn and mods.toml declares `ordering = "NONE"`, so RangedWeaponAPI's block may already have run
        // (and read an unset flag) by the time ours does, registering nothing at all with no error. The mod
        // constructor runs before any RegisterEvent is posted to any bus, so it cannot lose that race.
        // Before RangedWeaponAPI 2.3.4.008 the same call registered directly and threw "Can not register to
        // a locked registry" straight into a bare `catch (Throwable t) {}` here, which is what hid it.
        if (Platform.util().isModLoaded("ranged_weapon_api")) {
            try {
                RangedWeaponCompat.init();
            } catch (Throwable t) {
                LOGGER.error("Failed to request RangedWeaponAPI potions -- the brewing recipes and "
                        + "bartender trades naming them will be skipped", t);
            }
        }

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

    /// Registration is duplicated here rather than delegated to `common`'s registerX() methods, because a
    /// plain `Registry.register` is not usable on this loader: Forge only clears the vanilla registry's own
    /// lock from 47.4.0 onwards, so on 47.0-47.3 and NeoForge 1.20.1 it throws "Can not register to a locked
    /// registry" even inside the correct `RegisterEvent` window. Our mods.toml declares
    /// `loaderVersion = "[47,)"`, so those are supported configurations. The helper this event hands out is
    /// the API every build of [47,) sanctions, so Forge iterates the same content `common` exposes and
    /// registers it itself. `common` keeps its own vanilla-shaped registration for Fabric.
    ///
    /// `event.register` is a no-op unless its key matches the event's registry, so all six blocks are
    /// declared unconditionally; Forge posts one event per registry and each block runs in exactly its own.
    ///
    /// There is no ITEM_GROUP block: the tavern blocks go into the vanilla Functional tab, wired from
    /// `BuildCreativeModeTabContentsEvent` (see #buildTabContents).
    public static void register(RegisterEvent event) {
        event.register(RegistryKeys.BLOCK, helper -> {
            for (var entry : TavernBlocks.all) {
                helper.register(entry.id(), entry.block());
            }
        });

        event.register(RegistryKeys.ITEM, helper -> {
            for (var entry : TavernBlocks.all) {
                helper.register(entry.id(), entry.item());
            }
        });

        event.register(RegistryKeys.POTION, helper -> {
            // Replaces Fabric's `Potions.<clinit>` TAIL mixin. RangedWeaponAPI is not handled here -- its
            // potions are requested from the mod constructor and it registers them itself; see above.
            registerSpellPowerPotions(helper);
        });

        event.register(RegistryKeys.SCHEDULE, helper ->
                helper.register(TavernVillagers.SCHEDULE_ID, TavernVillagers.ALWAYS_WORK_SCHEDULE));

        event.register(RegistryKeys.POINT_OF_INTEREST_TYPE, helper -> {
            // Forge 47's PointOfInterestTypeCallbacks wires the block-state -> POI mapping from the type's
            // block states as the entry is added, so nothing else is needed here.
            helper.register(TavernVillagers.PROFESSION_ID,
                    new PointOfInterestType(TavernVillagers.poiBlockStates(),
                            TavernVillagers.POI_TICKET_COUNT, TavernVillagers.POI_SEARCH_DISTANCE));
        });

        event.register(RegistryKeys.VILLAGER_PROFESSION, helper -> {
            helper.register(TavernVillagers.PROFESSION_ID, TavernVillagers.professionToRegister());
            // The helper returns void, so the field `VillagerTradesEvent` filters on is filled in afterwards.
            TavernVillagers.linkProfessionEntry();
            // Reads the POTION registry, which drained at event 11; villager_profession is event 28.
            TavernVillagers.setupTrades();
        });
    }

    /// Spell Power registers its own potions only when its `register_potions` config is on, so Taverns
    /// writes them here instead -- iterating the creation-only map, exactly as `SpellPowerCompat.init()`
    /// does on Fabric. Skips ids already present, so a player who *has* turned that config on does not get
    /// a duplicate-key crash whichever mod's block ran first.
    private static void registerSpellPowerPotions(RegisterEvent.RegisterHelper<Potion> helper) {
        if (!Platform.util().isModLoaded("spell_power")) {
            return;
        }
        try {
            SpellPowerCompat.potionsToRegister().forEach((id, potion) -> {
                if (!Registries.POTION.containsId(id)) {
                    helper.register(id, potion);
                }
            });
        } catch (Throwable t) {
            // Swallowed rather than fatal: a missing potion costs some brewing recipes and trades, not the
            // server. Logged, though -- a bare catch here is what hid a broken RangedWeaponAPI request.
            LOGGER.error("Failed to register Spell Power potions -- the brewing recipes and bartender "
                    + "trades naming them will be skipped", t);
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
