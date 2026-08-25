package net.village_taverns.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.render.BlockRenderLayer;
import net.village_taverns.block.TavernBlocks;
import net.village_taverns.client.TavernsModClient;

public final class FabricClientMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        TavernsModClient.init();
        // 1.21.11: `BlockRenderLayerMap` moved to `api.client.rendering.v1` and keys off the
        // `BlockRenderLayer` enum instead of a `RenderLayer` instance.
        BlockRenderLayerMap.putBlock(TavernBlocks.BARREL.block(), BlockRenderLayer.CUTOUT);
    }
}
