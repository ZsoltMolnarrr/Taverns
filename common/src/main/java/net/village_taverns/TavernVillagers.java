package net.village_taverns;

import com.google.common.collect.ImmutableSet;
import net.minecraft.block.BlockState;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Schedule;
import net.minecraft.entity.ai.brain.ScheduleBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
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
    public static final Identifier PROFESSION_ID = Identifier.of(TavernsMod.ID, BARTENDER);
    public static final int POI_TICKET_COUNT = 1;
    public static final int POI_SEARCH_DISTANCE = 12;
    @Nullable public static VillagerProfession BAR_TENDER_PROFESSION;

    /// The bartender's workstation (brew-tap barrel) block states for the POI. Registration itself is
    /// loader-specific (Fabric: `PointOfInterestHelper`; NeoForge: a plain `Registry.register` of a
    /// `PointOfInterestType`) and lives in each platform's entrypoint; this only exposes the shared state set.
    public static Set<BlockState> poiBlockStates() {
        return ImmutableSet.copyOf(TavernBlocks.BARREL.block().getStateManager().getStates());
    }

    public static VillagerProfession createProfession(String name, RegistryKey<PointOfInterestType> workStation) {
        var id = Identifier.of(TavernsMod.ID, name);
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

    public static LinkedHashMap<Integer, List<TradeOffers.Factory>> TRADES = new LinkedHashMap<>();

    /// Registers the bartender's always-work schedule. Loader-neutral vanilla registry insert; called
    /// from each platform's entrypoint (Fabric directly; NeoForge in the SCHEDULE `RegisterEvent` phase).
    public static void registerSchedule() {
        Registry.register(Registries.SCHEDULE, Identifier.of(TavernsMod.ID, ALWAYS_WORK), ALWAYS_WORK_SCHEDULE);
    }

    /// Registers the bartender profession and builds the trade table. Loader-neutral. Trade-offer
    /// wiring is loader-specific and lives in each platform's entrypoint (Fabric `TradeOfferHelper` /
    /// NeoForge `VillagerTradesEvent`), consuming the shared #TRADES map this populates via setupTrades().
    public static void registerProfession() {
        var profession = createProfession(
                BARTENDER,
                RegistryKey.of(Registries.POINT_OF_INTEREST_TYPE.getKey(), PROFESSION_ID));
        Registry.register(Registries.VILLAGER_PROFESSION, PROFESSION_ID, profession);
        BAR_TENDER_PROFESSION = profession;

        setupTrades();
    }

    public static String CRIT_MOD_ID = "critical_strike";
    public static Identifier CRIT_CHANCE_POTION_ID = Identifier.of(CRIT_MOD_ID, CRIT_MOD_ID + "_chance");
    public static Identifier CRIT_DAMAGE_POTION_ID = Identifier.of(CRIT_MOD_ID, CRIT_MOD_ID + "_damage");

    public static void setupTrades() {
        var trades_level_1 = new ArrayList<TradeOffers.Factory>();
        trades_level_1.add(new TradeOffers.SellItemFactory(Items.COOKED_CHICKEN, 2, 1, 12, 10));
        trades_level_1.add(new TradeOffers.SellItemFactory(Items.COOKED_BEEF, 4, 1, 12, 10));
        trades_level_1.add(new TradeOffers.SellItemFactory(Items.BREAD, 4, 1, 12, 10));
        trades_level_1.add(new TradeOffers.SellItemFactory(Items.COOKED_RABBIT, 6, 1, 12, 10));
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
        trades_level_5.add(new TradeOffers.SellItemFactory(Items.OMINOUS_BOTTLE, 60, 1, 1, 40));
        trades_level_5.add(potionOffer(Potions.LONG_FIRE_RESISTANCE, POTION_PRICE_T4, 1, 3, 40));
        TRADES.put(5, trades_level_5);
    }

    private static <T> void addIfNotNull(List<T> list, T item) {
        if (item != null) {
            list.add(item);
        }
    }

    private static TradeOffers.SellItemFactory potionOffer(String potionId, int price, int count, int maxUses, int experience) {
        var stack = createPotionStack(potionId);
        if (stack != null) {
            return new TradeOffers.SellItemFactory(stack, price, count, maxUses, experience);
        }
        return null;
    }

    private static TradeOffers.SellItemFactory potionOffer(RegistryEntry<Potion> potion, int price, int count, int maxUses, int experience) {
        var stack = createPotionStack(potion);
        return new TradeOffers.SellItemFactory(stack, price, count, maxUses, experience);
    }

    @Nullable
    private static ItemStack createPotionStack(String potionId) {
        var id = Identifier.of(potionId);
        var potion = Registries.POTION.getEntry(id);
        return potion
                .map(potionReference -> PotionContentsComponent.createStack(Items.POTION, potionReference))
                .orElse(null);
    }

    private static ItemStack createPotionStack(RegistryEntry<Potion> potion) {
        return PotionContentsComponent.createStack(Items.POTION, potion);
    }
}
