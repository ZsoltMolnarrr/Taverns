package net.village_taverns.village;

import org.jetbrains.annotations.Nullable;

/// Loader seam for vanilla-village structure-pool injection.
///
/// On 1.20.1 StructurePoolAPI is a **Fabric-only** artifact (`maven.modrinth:structure-pool-api:1.0+1.20.1`),
/// so neither the API classes nor the `villages.json` config may be referenced from `common` — the Forge
/// module would fail to load them. The Fabric entrypoint installs an [Injector]; on Forge the injector
/// stays null and the tavern reaches villages only through the data-driven paths shipped in
/// `resources/data/village_taverns` (the Lithostitched worldgen modifiers).
public final class VillageStructures {
    private VillageStructures() { }

    @FunctionalInterface
    public interface Injector {
        void inject();
    }

    @Nullable
    public static Injector injector = null;

    public static void injectIfAvailable() {
        var injector = VillageStructures.injector;
        if (injector != null) {
            injector.inject();
        }
    }
}
