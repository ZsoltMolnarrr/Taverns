package net.village_taverns.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.enums.NoteBlockInstrument;
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
                .instrument(NoteBlockInstrument.BASS)
                .strength(2.5F)
                .sounds(BlockSoundGroup.WOOD)
                .nonOpaque()
    ));

    public static void register() {
        for (var entry : all) {
            Registry.register(Registries.BLOCK, Identifier.of(TavernsMod.ID, entry.name), entry.block);
            Registry.register(Registries.ITEM, Identifier.of(TavernsMod.ID, entry.name), entry.item());
        }
        // Creative-tab placement (vanilla Functional tab) is wired per-platform from each loader's
        // entrypoint (Fabric ItemGroupEvents / NeoForge BuildCreativeModeTabContentsEvent), iterating `all`.
    }
}
