package net.village_taverns.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.enums.Instrument;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.village_taverns.TavernsMod;

import java.util.ArrayList;

public class TavernBlocks {
    public record Entry(String name, Block block, BlockItem item) {
        public Entry(String name, Block block) {
            this(name, block, new BlockItem(block, new Item.Settings()));
        }

        /// The id the block and its `BlockItem` are both registered under.
        public Identifier id() {
            return new Identifier(TavernsMod.ID, name);
        }
    }

    public static final ArrayList<Entry> all = new ArrayList<>();

    private static Entry entry(String name, Block block) {
        var settings = new Item.Settings();
        var item = new BlockItem(block, settings);
        var entry = new Entry(name, block, item);
        all.add(entry);
        return entry;
    }

    public static final Entry BARREL = entry(BrewTapBlock.NAME, new BrewTapBlock(
            AbstractBlock.Settings.create()
                .mapColor(MapColor.OAK_TAN)
                .instrument(Instrument.BASS)
                .strength(2.5F)
                .sounds(BlockSoundGroup.WOOD)
                .nonOpaque()
    ));

    /// Blocks only. Forge opens exactly one registry per `RegisterEvent` window, so the `BlockItem`s are
    /// registered separately from #registerItems().
    public static void register() {
        for (var entry : all) {
            Registry.register(Registries.BLOCK, entry.id(), entry.block());
        }
    }

    public static void registerItems() {
        for (var entry : all) {
            Registry.register(Registries.ITEM, entry.id(), entry.item());
        }
        // Creative-tab placement (vanilla Functional tab) is wired per-platform from each loader's
        // entrypoint (Fabric ItemGroupEvents / Forge BuildCreativeModeTabContentsEvent), iterating `all`.
    }
}
