package net.village_taverns.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.village_taverns.client.TavernsModClient;

public final class FabricClientMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        TavernsModClient.init();
        // 26.1: `BlockRenderLayerMap` is gone — the chunk section layer is derived from the block model
        // (the barrel's `models/block/barrel.json` renders cutout without any registration).
    }
}
