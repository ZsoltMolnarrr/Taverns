package net.village_taverns.block;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
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
    private static Entry entry(String name, Function<BlockBehaviour.Properties, Block> blockFactory, String hint) {
        var id = Identifier.fromNamespaceAndPath(TavernsMod.ID, name);
        var block = blockFactory.apply(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, id)));
        var itemSettings = new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, id))
                .useBlockDescriptionPrefix();
        var entry = new Entry(name, block, new TavernBlockItem(block, itemSettings, hint));
        all.add(entry);
        return entry;
    }

    public static final Entry BARREL = entry(BrewTapBlock.NAME, settings ->
            new BrewTapBlock(settings
                .mapColor(MapColor.WOOD)
                .instrument(NoteBlockInstrument.BASS)
                .strength(2.5F)
                .sound(SoundType.WOOD)
                .noOcclusion()
    ), "block." + TavernsMod.ID + "." + BrewTapBlock.NAME + ".hint");

    public static void register() {
        for (var entry : all) {
            Registry.register(BuiltInRegistries.BLOCK, Identifier.fromNamespaceAndPath(TavernsMod.ID, entry.name), entry.block);
            Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(TavernsMod.ID, entry.name), entry.item());
        }
        // Creative-tab placement (vanilla Functional tab) is wired per-platform from each loader's
        // entrypoint (Fabric ItemGroupEvents / NeoForge BuildCreativeModeTabContentsEvent), iterating `all`.
    }
}
