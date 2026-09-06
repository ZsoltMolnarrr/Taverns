package net.village_taverns.forge.client;

import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.village_taverns.client.TavernsModClient;

/// Only ever touched behind `FMLEnvironment.dist == Dist.CLIENT` from `ForgeMod`, so no
/// `@EventBusSubscriber(Dist.CLIENT)` is needed (Forge 47 has no NeoForge-style dist-gated subscriber).
///
/// The barrel's cutout render layer needs no registration on Forge: `models/block/barrel.json` carries
/// the `render_type` model extension. Only Fabric needs `BlockRenderLayerMap`.
public class ForgeClientMod {
    private ForgeClientMod() { }

    public static void register(IEventBus modBus) {
        modBus.addListener(EventPriority.NORMAL, false, FMLClientSetupEvent.class,
                event -> TavernsModClient.init());
    }
}
