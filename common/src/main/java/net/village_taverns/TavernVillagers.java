package net.village_taverns;

import com.google.common.collect.ImmutableSet;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Schedule;
import net.minecraft.entity.ai.brain.ScheduleBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.poi.PointOfInterestType;
import net.village_taverns.block.TavernBlocks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class TavernVillagers {
    public static final String BARTENDER = "bartender";
    public static final String ALWAYS_WORK = "always_work";
    public static final Schedule ALWAYS_WORK_SCHEDULE = new ScheduleBuilder(new Schedule())
            .withActivity(50, Activity.WORK).withActivity(23950, Activity.REST).build();
    public static final Identifier PROFESSION_ID = new Identifier(TavernsMod.ID, BARTENDER);
    public static final int POI_TICKET_COUNT = 1;
    public static final int POI_SEARCH_DISTANCE = 12;
    @Nullable public static VillagerProfession BAR_TENDER_PROFESSION;

    /// The bartender's workstation (brew-tap barrel) block states for the POI. Registration itself is
    /// loader-specific (Fabric: `PointOfInterestHelper`; Forge: a plain `Registry.register` of a
    /// `PointOfInterestType`) and lives in each platform's entrypoint; this only exposes the shared state set.
    public static Set<BlockState> poiBlockStates() {
        return ImmutableSet.copyOf(TavernBlocks.BARREL.block().getStateManager().getStates());
    }

    public static VillagerProfession createProfession(String name, RegistryKey<PointOfInterestType> workStation) {
        var id = new Identifier(TavernsMod.ID, name);
        return new VillagerProfession(
                id.toString(),
                (entry) -> {
                    return entry.matchesKey(workStation);
                },
                (entry) -> {
                    return entry.matchesKey(workStation);
                },
                ImmutableSet.of(),
                ImmutableSet.of(),
                SoundEvents.ITEM_BOTTLE_FILL);
    }

    private static final int POTION_PRICE_T1 = 16;
    private static final int POTION_PRICE_T2 = 24;
    private static final int POTION_PRICE_T3 = 32;
    private static final int POTION_PRICE_T4 = 40;

    /// Vanilla's own price multiplier for `SellItemFactory` — reproduced by the `sell(...)` helper below.
    private static final float PRICE_MULTIPLIER = 0.05F;

    public static LinkedHashMap<Integer, List<TradeOffers.Factory>> TRADES = new LinkedHashMap<>();

    /// Registers the bartender's always-work schedule. Loader-neutral vanilla registry insert; called
    /// from each platform's entrypoint (Fabric directly; Forge in the SCHEDULE `RegisterEvent` window).
    public static void registerSchedule() {
        Registry.register(Registries.SCHEDULE, new Identifier(TavernsMod.ID, ALWAYS_WORK), ALWAYS_WORK_SCHEDULE);
    }

    /// Registers the bartender profession and builds the trade table. Loader-neutral. Trade-offer
    /// wiring is loader-specific and lives in each platform's entrypoint (Fabric `TradeOfferHelper` /
    /// Forge `VillagerTradesEvent`), consuming the shared #TRADES map this populates via setupTrades().
    public static void registerProfession() {
        var profession = createProfession(
                BARTENDER,
                RegistryKey.of(RegistryKeys.POINT_OF_INTEREST_TYPE, PROFESSION_ID));
        Registry.register(Registries.VILLAGER_PROFESSION, PROFESSION_ID, profession);
        BAR_TENDER_PROFESSION = profession;

        setupTrades();
    }

    public static String CRIT_MOD_ID = "critical_strike";
    public static Identifier CRIT_CHANCE_POTION_ID = new Identifier(CRIT_MOD_ID, CRIT_MOD_ID + "_chance");
    public static Identifier CRIT_DAMAGE_POTION_ID = new Identifier(CRIT_MOD_ID, CRIT_MOD_ID + "_damage");

    public static void setupTrades() {
        TRADES.clear();

        var trades_level_1 = new ArrayList<TradeOffers.Factory>();
        trades_level_1.add(sell(new ItemStack(Items.COOKED_CHICKEN), 2, 1, 12, 10));
        trades_level_1.add(sell(new ItemStack(Items.COOKED_BEEF), 4, 1, 12, 10));
        trades_level_1.add(sell(new ItemStack(Items.BREAD), 4, 1, 12, 10));
        trades_level_1.add(sell(new ItemStack(Items.COOKED_RABBIT), 6, 1, 12, 10));
        TRADES.put(1, trades_level_1);

        var trades_level_2 = new ArrayList<TradeOffers.Factory>();
        trades_level_2.add(potionOffer(Potions.STRENGTH, POTION_PRICE_T1, 1, 3, 20));
        trades_level_2.add(potionOffer(Potions.REGENERATION, POTION_PRICE_T1, 1, 3, 20));
        trades_level_2.add(potionOffer(Potions.SWIFTNESS, POTION_PRICE_T1, 1, 3, 20));
        trades_level_2.add(potionOffer(Potions.FIRE_RESISTANCE, POTION_PRICE_T1, 1, 3, 20));
        TRADES.put(2, trades_level_2);

        var trades_level_3 = new ArrayList<TradeOffers.Factory>();
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

        var trades_level_4 = new ArrayList<TradeOffers.Factory>();
        addIfNotNull(trades_level_4, potionOffer("spell_power:spell_power.critical_chance", POTION_PRICE_T3, 1, 3, 30));
        addIfNotNull(trades_level_4, potionOffer("spell_power:spell_power.critical_damage", POTION_PRICE_T3, 1, 3, 30));
        if (Platform.util().isModLoaded("critical_strike")) {
            addIfNotNull(trades_level_4, potionOffer(CRIT_CHANCE_POTION_ID.toString(), POTION_PRICE_T3, 1, 3, 30));
            addIfNotNull(trades_level_4, potionOffer(CRIT_DAMAGE_POTION_ID.toString(), POTION_PRICE_T3, 1, 3, 30));
        }
        if (trades_level_4.isEmpty()) {
            trades_level_4.add(potionOffer(Potions.LONG_REGENERATION, POTION_PRICE_T3, 1, 3, 30));
            trades_level_4.add(potionOffer(Potions.LONG_LEAPING, POTION_PRICE_T3, 1, 3, 30));
            trades_level_4.add(potionOffer(Potions.WATER_BREATHING, POTION_PRICE_T3, 1, 3, 30));
        }
        TRADES.put(4, trades_level_4);

        var trades_level_5 = new ArrayList<TradeOffers.Factory>();
        addIfNotNull(trades_level_5, potionOffer("spell_power:spell_power.haste", POTION_PRICE_T4, 1, 3, 30));
        addIfNotNull(trades_level_5, potionOffer("ranged_weapon:ranged_weapon.haste", POTION_PRICE_T4, 1, 3, 30));
        // 1.20.1 sacrifice: 1.21.1's tier-5 Ominous Bottle offer has no counterpart here — the item
        // was added in 1.20.5. The offer is dropped rather than substituted, so the 1.20.1 bartender's
        // top tier has one fewer trade than on 1.21.1. Deliberate; do not fill it with a stand-in.
        trades_level_5.add(potionOffer(Potions.LONG_FIRE_RESISTANCE, POTION_PRICE_T4, 1, 3, 40));
        TRADES.put(5, trades_level_5);
    }

    private static <T> void addIfNotNull(List<T> list, T item) {
        if (item != null) {
            list.add(item);
        }
    }

    /// Hand-rolled stand-in for `TradeOffers.SellItemFactory`, for two reasons:
    /// 1. 1.20.1's `SellItemFactory.create` builds the sold stack as `new ItemStack(sell.getItem(), count)`,
    ///    which **drops the NBT** — every potion offer would degrade into a plain water bottle. Potions
    ///    carry their identity in NBT on 1.20.1 (no data components), so the stack must be copied.
    /// 2. `TradeOffers.SellItemFactory` is not reliably accessible from a lean `common` classpath
    ///    (see jewelry-port-notes.md §4); the raw `TradeOffer` constructor always is.
    ///
    /// Argument order and the 0.05F price multiplier reproduce vanilla exactly.
    private static TradeOffers.Factory sell(ItemStack stack, int price, int count, int maxUses, int experience) {
        return (entity, random) -> new TradeOffer(
                new ItemStack(Items.EMERALD, price), stack.copyWithCount(count),
                maxUses, experience, PRICE_MULTIPLIER);
    }

    @Nullable
    private static TradeOffers.Factory potionOffer(String potionId, int price, int count, int maxUses, int experience) {
        var stack = createPotionStack(potionId);
        if (stack != null) {
            return sell(stack, price, count, maxUses, experience);
        }
        return null;
    }

    private static TradeOffers.Factory potionOffer(Potion potion, int price, int count, int maxUses, int experience) {
        return sell(createPotionStack(potion), price, count, maxUses, experience);
    }

    @Nullable
    private static ItemStack createPotionStack(String potionId) {
        var id = Identifier.tryParse(potionId);
        if (id == null) {
            return null;
        }
        return Registries.POTION.getOrEmpty(id)
                .map(TavernVillagers::createPotionStack)
                .orElse(null);
    }

    private static ItemStack createPotionStack(Potion potion) {
        return PotionUtil.setPotion(new ItemStack(Items.POTION), potion);
    }
}
