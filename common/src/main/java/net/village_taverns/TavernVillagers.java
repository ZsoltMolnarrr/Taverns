package net.village_taverns;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.attribute.AttributeTypes;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.level.block.state.BlockState;
import net.village_taverns.block.TavernBlocks;
import org.jspecify.annotations.Nullable;

import java.util.Set;

public class TavernVillagers {
    public static final String BARTENDER = "bartender";
    public static final String ALWAYS_WORK = "always_work";

    /// The bartender's "schedule".
    ///
    /// 1.21.11 deleted `Schedule` / `ScheduleBuilder` and the `minecraft:schedule` registry entirely.
    /// `Brain#setSchedule` now takes an `EnvironmentAttribute<Activity>` whose value the brain samples
    /// from the world clock each time it refreshes its activities, and vanilla's day/night rhythm is a
    /// `Timeline` (`minecraft:villager_schedule`) writing keyframes into
    /// `EnvironmentAttributes.VILLAGER_ACTIVITY_GAMEPLAY`.
    ///
    /// An attribute that no timeline, biome or dimension type ever modifies simply reports its default
    /// value (26.1.2 `EnvironmentAttributeSystem#getValue` returns `attribute.defaultValue()` when there
    /// is no sampler for it), so a private attribute defaulting to `WORK` is exactly the old
    /// "always work" schedule — with no timeline, world clock or dimension-type data to ship.
    public static final EnvironmentAttribute<Activity> ALWAYS_WORK_ACTIVITY =
            EnvironmentAttribute.builder(AttributeTypes.ACTIVITY).defaultValue(Activity.WORK).build();

    public static final Identifier PROFESSION_ID = Identifier.fromNamespaceAndPath(TavernsMod.ID, BARTENDER);

    /// Registry key of the bartender profession. Since 1.21.2 `VillagerData#profession()` hands out a
    /// `Holder<VillagerProfession>` rather than the profession object, so the key is what everything
    /// (including our `VillagerMixin`) compares against.
    public static final ResourceKey<VillagerProfession> BARTENDER_PROFESSION_KEY =
            ResourceKey.create(Registries.VILLAGER_PROFESSION, PROFESSION_ID);

    public static final int POI_TICKET_COUNT = 1;
    public static final int POI_SEARCH_DISTANCE = 12;
    @Nullable public static VillagerProfession BAR_TENDER_PROFESSION;

    /// The bartender's workstation (brew-tap barrel) block states for the POI. Registration itself is
    /// loader-specific (Fabric: `PoiHelper`; NeoForge: a plain `Registry.register` of a
    /// `PointOfInterestType`) and lives in each platform's entrypoint; this only exposes the shared state set.
    public static Set<BlockState> poiBlockStates() {
        return ImmutableSet.copyOf(TavernBlocks.BARREL.block().getStateDefinition().getPossibleStates());
    }

    /// 26.1 made villager trades data driven: the offers themselves live in
    /// `data/village_taverns/villager_trade/bartender/<level>/*.json`, are grouped by
    /// `data/village_taverns/tags/villager_trade/bartender/level_<n>.json` and picked up by
    /// `data/village_taverns/trade_set/bartender/level_<n>.json`. The profession record only carries the
    /// trade-set key per merchant level — `VillagerTrades.ItemListing`, Fabric's `TradeOfferHelper` and
    /// NeoForge's `VillagerTradesEvent` are all gone, and with them the whole `TavernTrades` class.
    public static ResourceKey<TradeSet> tradeSet(int level) {
        return ResourceKey.create(Registries.TRADE_SET,
                Identifier.fromNamespaceAndPath(TavernsMod.ID, BARTENDER + "/level_" + level));
    }

    public static Int2ObjectMap<ResourceKey<TradeSet>> tradeSetsByLevel() {
        return Int2ObjectMap.ofEntries(
                Int2ObjectMap.entry(1, tradeSet(1)),
                Int2ObjectMap.entry(2, tradeSet(2)),
                Int2ObjectMap.entry(3, tradeSet(3)),
                Int2ObjectMap.entry(4, tradeSet(4)),
                Int2ObjectMap.entry(5, tradeSet(5))
        );
    }

    public static VillagerProfession createProfession(String name, ResourceKey<PoiType> workStation) {
        var id = Identifier.fromNamespaceAndPath(TavernsMod.ID, name);
        return new VillagerProfession(
                // 1.21.11: the record's first component is the displayed name as a `Text`, not the id
                // string vanilla used to build `entity.minecraft.villager.<id>` from. Pass the key the
                // existing translation files already carry (all 20 of them):
                // `entity.minecraft.villager.village_taverns.bartender`. Vanilla itself would derive
                // `entity.village_taverns.villager.bartender`; keeping the legacy key avoids re-keying
                // every lang file for no visible gain.
                Component.translatable("entity.minecraft.villager." + id.getNamespace() + "." + id.getPath()),
                (entry) -> {
                    return entry.is(workStation);
                },
                (entry) -> {
                    return entry.is(workStation);
                },
                ImmutableSet.of(),
                ImmutableSet.of(),
                SoundEvents.BOTTLE_FILL,
                tradeSetsByLevel());
    }

    /// Registers the bartender's always-work activity attribute. Loader-neutral vanilla registry
    /// insert; called from each platform's entrypoint (Fabric directly; NeoForge in the
    /// ENVIRONMENT_ATTRIBUTE `RegisterEvent` phase). Registration is not what makes the attribute
    /// work — it is only needed so the id round-trips through codecs/commands like any other.
    public static void registerSchedule() {
        Registry.register(BuiltInRegistries.ENVIRONMENT_ATTRIBUTE,
                Identifier.fromNamespaceAndPath(TavernsMod.ID, "gameplay/" + ALWAYS_WORK), ALWAYS_WORK_ACTIVITY);
    }

    /// Registers the bartender profession (with its per-level trade-set keys). Loader-neutral; there is
    /// no trade wiring left to do on either loader.
    public static void registerProfession() {
        var profession = createProfession(
                BARTENDER,
                ResourceKey.create(BuiltInRegistries.POINT_OF_INTEREST_TYPE.key(), PROFESSION_ID));
        Registry.register(BuiltInRegistries.VILLAGER_PROFESSION, PROFESSION_ID, profession);
        BAR_TENDER_PROFESSION = profession;
    }
}
