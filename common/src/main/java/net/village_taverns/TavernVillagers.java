package net.village_taverns;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.Holder;
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
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.state.BlockState;
import net.village_taverns.block.TavernBlocks;
import net.village_taverns.village.TavernTrades;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
    /// value (`WorldEnvironmentAttributeAccess#getAttributeValue` returns `attribute.getDefaultValue()`
    /// when there is no entry), so a private attribute defaulting to `WORK` is exactly the old
    /// "always work" schedule — with no timeline or dimension-type data to ship.
    public static final EnvironmentAttribute<Activity> ALWAYS_WORK_ACTIVITY =
            EnvironmentAttribute.builder(AttributeTypes.ACTIVITY).defaultValue(Activity.WORK).build();

    public static final Identifier PROFESSION_ID = Identifier.fromNamespaceAndPath(TavernsMod.ID, BARTENDER);

    /// Registry key of the bartender profession. Since 1.21.2 both loaders' trade-registration APIs
    /// (Fabric `TradeOfferHelper.registerVillagerOffers`, NeoForge `VillagerTradesEvent#getType`) are
    /// keyed by it rather than by the profession object, and `VillagerData#profession()` hands out a
    /// `RegistryEntry<VillagerProfession>` — so the key is what everything compares against.
    public static final ResourceKey<VillagerProfession> BARTENDER_PROFESSION_KEY =
            ResourceKey.create(Registries.VILLAGER_PROFESSION, PROFESSION_ID);

    public static final int POI_TICKET_COUNT = 1;
    public static final int POI_SEARCH_DISTANCE = 12;
    @Nullable public static VillagerProfession BAR_TENDER_PROFESSION;

    /// The bartender's workstation (brew-tap barrel) block states for the POI. Registration itself is
    /// loader-specific (Fabric: `PointOfInterestHelper`; NeoForge: a plain `Registry.register` of a
    /// `PointOfInterestType`) and lives in each platform's entrypoint; this only exposes the shared state set.
    public static Set<BlockState> poiBlockStates() {
        return ImmutableSet.copyOf(TavernBlocks.BARREL.block().getStateDefinition().getPossibleStates());
    }

    public static VillagerProfession createProfession(String name, ResourceKey<PoiType> workStation) {
        var id = Identifier.fromNamespaceAndPath(TavernsMod.ID, name);
        return new VillagerProfession(
                // 1.21.11: the record's first component is the displayed name as a `Text`, not the id
                // string vanilla used to build `entity.minecraft.villager.<id>` from. Pass the key the
                // existing translation files already carry: `entity.minecraft.villager.village_taverns.bartender`.
                Component.translatable("entity.minecraft.villager." + id.getNamespace() + "." + id.getPath()),
                (entry) -> {
                    return entry.is(workStation);
                },
                (entry) -> {
                    return entry.is(workStation);
                },
                ImmutableSet.of(),
                ImmutableSet.of(),
                SoundEvents.BOTTLE_FILL);
    }

    private static final int POTION_PRICE_T1 = 16;
    private static final int POTION_PRICE_T2 = 24;
    private static final int POTION_PRICE_T3 = 32;
    private static final int POTION_PRICE_T4 = 40;

    public static LinkedHashMap<Integer, List<VillagerTrades.ItemListing>> TRADES = new LinkedHashMap<>();

    /// Registers the bartender's always-work activity attribute. Loader-neutral vanilla registry
    /// insert; called from each platform's entrypoint (Fabric directly; NeoForge in the
    /// ENVIRONMENT_ATTRIBUTE `RegisterEvent` phase). Registration is not what makes the attribute
    /// work — it is only needed so the id round-trips through codecs/commands like any other.
    public static void registerSchedule() {
        Registry.register(BuiltInRegistries.ENVIRONMENT_ATTRIBUTE,
                Identifier.fromNamespaceAndPath(TavernsMod.ID, "gameplay/" + ALWAYS_WORK), ALWAYS_WORK_ACTIVITY);
    }

    /// Registers the bartender profession and builds the trade table. Loader-neutral. Trade-offer
    /// wiring is loader-specific and lives in each platform's entrypoint (Fabric `TradeOfferHelper` /
    /// NeoForge `VillagerTradesEvent`), consuming the shared #TRADES map this populates via setupTrades().
    public static void registerProfession() {
        var profession = createProfession(
                BARTENDER,
                ResourceKey.create(BuiltInRegistries.POINT_OF_INTEREST_TYPE.key(), PROFESSION_ID));
        Registry.register(BuiltInRegistries.VILLAGER_PROFESSION, PROFESSION_ID, profession);
        BAR_TENDER_PROFESSION = profession;

        setupTrades();
    }

    public static String CRIT_MOD_ID = "critical_strike";
    public static Identifier CRIT_CHANCE_POTION_ID = Identifier.fromNamespaceAndPath(CRIT_MOD_ID, CRIT_MOD_ID + "_chance");
    public static Identifier CRIT_DAMAGE_POTION_ID = Identifier.fromNamespaceAndPath(CRIT_MOD_ID, CRIT_MOD_ID + "_damage");

    public static void setupTrades() {
        TRADES.clear();

        var trades_level_1 = new ArrayList<VillagerTrades.ItemListing>();
        trades_level_1.add(new TavernTrades.Sell(Items.COOKED_CHICKEN, 2, 1, 12, 10));
        trades_level_1.add(new TavernTrades.Sell(Items.COOKED_BEEF, 4, 1, 12, 10));
        trades_level_1.add(new TavernTrades.Sell(Items.BREAD, 4, 1, 12, 10));
        trades_level_1.add(new TavernTrades.Sell(Items.COOKED_RABBIT, 6, 1, 12, 10));
        TRADES.put(1, trades_level_1);

        var trades_level_2 = new ArrayList<VillagerTrades.ItemListing>();
        trades_level_2.add(potionOffer(Potions.STRENGTH, POTION_PRICE_T1, 1, 3, 20));
        trades_level_2.add(potionOffer(Potions.REGENERATION, POTION_PRICE_T1, 1, 3, 20));
        trades_level_2.add(potionOffer(Potions.SWIFTNESS, POTION_PRICE_T1, 1, 3, 20));
        trades_level_2.add(potionOffer(Potions.FIRE_RESISTANCE, POTION_PRICE_T1, 1, 3, 20));
        TRADES.put(2, trades_level_2);

        var trades_level_3 = new ArrayList<VillagerTrades.ItemListing>();
        addIfNotNull(trades_level_3, potionOffer("spell_power:spell_power.arcane", POTION_PRICE_T2, 1, 3, 30));
        addIfNotNull(trades_level_3, potionOffer("spell_power:spell_power.fire", POTION_PRICE_T2, 1, 3, 30));
        addIfNotNull(trades_level_3, potionOffer("spell_power:spell_power.frost", POTION_PRICE_T2, 1, 3, 30));
        addIfNotNull(trades_level_3, potionOffer("spell_power:spell_power.healing", POTION_PRICE_T2, 1, 3, 30));
        addIfNotNull(trades_level_3, potionOffer("ranged_weapon:ranged_weapon.damage", POTION_PRICE_T2, 1, 3, 30));

        if (trades_level_3.isEmpty()) {
            trades_level_3.add(potionOffer(Potions.HARMING, POTION_PRICE_T2, 1, 3, 30));
            trades_level_3.add(potionOffer(Potions.NIGHT_VISION, POTION_PRICE_T2, 1, 3, 30));
            trades_level_3.add(potionOffer(Potions.WEAKNESS, POTION_PRICE_T2, 1, 3, 30));
        }
        TRADES.put(3, trades_level_3);

        var trades_level_4 = new ArrayList<VillagerTrades.ItemListing>();
        addIfNotNull(trades_level_4, potionOffer("spell_power:spell_power.critical_chance", POTION_PRICE_T3, 1, 3, 30));
        addIfNotNull(trades_level_4, potionOffer("spell_power:spell_power.critical_damage", POTION_PRICE_T3, 1, 3, 30));
        if (Platform.util().isModLoaded(CRIT_MOD_ID)) {
            addIfNotNull(trades_level_4, potionOffer(CRIT_CHANCE_POTION_ID.toString(), POTION_PRICE_T3, 1, 3, 30));
            addIfNotNull(trades_level_4, potionOffer(CRIT_DAMAGE_POTION_ID.toString(), POTION_PRICE_T3, 1, 3, 30));
        }
        if (trades_level_4.isEmpty()) {
            trades_level_4.add(potionOffer(Potions.LONG_REGENERATION, POTION_PRICE_T3, 1, 3, 30));
            trades_level_4.add(potionOffer(Potions.LONG_LEAPING, POTION_PRICE_T3, 1, 3, 30));
            trades_level_4.add(potionOffer(Potions.WATER_BREATHING, POTION_PRICE_T3, 1, 3, 30));
        }
        TRADES.put(4, trades_level_4);

        var trades_level_5 = new ArrayList<VillagerTrades.ItemListing>();
        addIfNotNull(trades_level_5, potionOffer("spell_power:spell_power.haste", POTION_PRICE_T4, 1, 3, 30));
        addIfNotNull(trades_level_5, potionOffer("ranged_weapon:ranged_weapon.haste", POTION_PRICE_T4, 1, 3, 30));
        trades_level_5.add(new TavernTrades.Sell(Items.OMINOUS_BOTTLE, 60, 1, 1, 40));
        trades_level_5.add(potionOffer(Potions.LONG_FIRE_RESISTANCE, POTION_PRICE_T4, 1, 3, 40));
        TRADES.put(5, trades_level_5);
    }

    private static <T> void addIfNotNull(List<T> list, T item) {
        if (item != null) {
            list.add(item);
        }
    }

    @Nullable
    private static TavernTrades.Sell potionOffer(String potionId, int price, int count, int maxUses, int experience) {
        var stack = createPotionStack(potionId);
        if (stack != null) {
            return new TavernTrades.Sell(stack, price, count, maxUses, experience);
        }
        return null;
    }

    private static TavernTrades.Sell potionOffer(Holder<Potion> potion, int price, int count, int maxUses, int experience) {
        var stack = createPotionStack(potion);
        return new TavernTrades.Sell(stack, price, count, maxUses, experience);
    }

    @Nullable
    private static ItemStack createPotionStack(String potionId) {
        var id = Identifier.parse(potionId);
        var potion = BuiltInRegistries.POTION.get(id);
        return potion
                .map(potionReference -> PotionContents.createItemStack(Items.POTION, potionReference))
                .orElse(null);
    }

    private static ItemStack createPotionStack(Holder<Potion> potion) {
        return PotionContents.createItemStack(Items.POTION, potion);
    }
}
