package net.village_taverns.village;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

/// Trade-offer factories for the bartender.
///
/// Vanilla's `TradeOffers.SellItemFactory` became package-private in 1.21.11 (Fabric API widens it
/// transitively, NeoForge does not), so common code can no longer reference it on both loaders — it
/// would compile and then throw on NeoForge. This is a behaviour-identical re-implementation over
/// the public [TradeOffer] / [TradedItem] API. See LESSONS §4.26.
public final class TavernTrades {
    private TavernTrades() { }

    /// Villager sells `count` x `stack` for `price` emeralds.
    /// Mirrors vanilla `TradeOffers.SellItemFactory(stack, price, count, maxUses, experience)`
    /// (which itself defaults `multiplier` to 0.05F and calls `stack.setCount(count)`).
    public record Sell(ItemStack stack, int price, int count, int maxUses, int experience, float multiplier)
            implements VillagerTrades.ItemListing {
        public Sell(ItemStack stack, int price, int count, int maxUses, int experience) {
            this(stack, price, count, maxUses, experience, 0.05F);
        }

        public Sell(Item item, int price, int count, int maxUses, int experience) {
            this(new ItemStack(item), price, count, maxUses, experience);
        }

        @Override
        public MerchantOffer getOffer(ServerLevel world, Entity entity, RandomSource random) {
            var sold = stack.copy();
            sold.setCount(count);
            return new MerchantOffer(new ItemCost(Items.EMERALD, price), sold, maxUses, experience, multiplier);
        }
    }
}
