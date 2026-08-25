package net.village_taverns.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.village_taverns.TavernsMod;

import java.util.ArrayList;
import java.util.function.Function;

public class TavernBlocks {
    public record Entry(String name, Block block, BlockItem item) { }

    public static final ArrayList<Entry> all = new ArrayList<>();

    /// 1.21.2+ requires every `AbstractBlock.Settings` / `Item.Settings` to carry its own
    /// `registryKey` (`Block id not set` / `Item id not set` on the first construction otherwise),
    /// so blocks are built from a factory that receives settings already keyed by their id.
    /// `useBlockPrefixedTranslationKey()` keeps the item on the `block.<ns>.<path>` lang key.
    private static Entry entry(String name, Function<AbstractBlock.Settings, Block> blockFactory, String hint) {
        var id = Identifier.of(TavernsMod.ID, name);
        var block = blockFactory.apply(AbstractBlock.Settings.create()
                .registryKey(RegistryKey.of(RegistryKeys.BLOCK, id)));
        var itemSettings = new Item.Settings()
                .registryKey(RegistryKey.of(RegistryKeys.ITEM, id))
                .useBlockPrefixedTranslationKey();
        var entry = new Entry(name, block, new TavernBlockItem(block, itemSettings, hint));
        all.add(entry);
        return entry;
    }

    public static final Entry BARREL = entry(BrewTapBlock.NAME, settings ->
            new BrewTapBlock(settings
                .mapColor(MapColor.OAK_TAN)
                .instrument(NoteBlockInstrument.BASS)
                .strength(2.5F)
                .sounds(BlockSoundGroup.WOOD)
                .nonOpaque()
    ), "block." + TavernsMod.ID + "." + BrewTapBlock.NAME + ".hint");

    public static void register() {
        for (var entry : all) {
            Registry.register(Registries.BLOCK, Identifier.of(TavernsMod.ID, entry.name), entry.block);
            Registry.register(Registries.ITEM, Identifier.of(TavernsMod.ID, entry.name), entry.item());
        }
        // Creative-tab placement (vanilla Functional tab) is wired per-platform from each loader's
        // entrypoint (Fabric ItemGroupEvents / NeoForge BuildCreativeModeTabContentsEvent), iterating `all`.
    }
}
