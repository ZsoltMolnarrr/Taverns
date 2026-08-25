package net.village_taverns.village;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.TradedItem;

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
            implements TradeOffers.Factory {
        public Sell(ItemStack stack, int price, int count, int maxUses, int experience) {
            this(stack, price, count, maxUses, experience, 0.05F);
        }

        public Sell(Item item, int price, int count, int maxUses, int experience) {
            this(new ItemStack(item), price, count, maxUses, experience);
        }

        @Override
        public TradeOffer create(ServerWorld world, Entity entity, Random random) {
            var sold = stack.copy();
            sold.setCount(count);
            return new TradeOffer(new TradedItem(Items.EMERALD, price), sold, maxUses, experience, multiplier);
        }
    }
}
