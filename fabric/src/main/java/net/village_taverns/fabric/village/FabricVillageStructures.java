package net.village_taverns.fabric.village;

import net.fabric_extras.structure_pool.api.StructurePoolAPI;
import net.fabric_extras.structure_pool.api.StructurePoolConfig;
import net.tiny_config.ConfigManager;
import net.village_taverns.Platform;
import net.village_taverns.TavernsMod;
import net.village_taverns.village.VillageStructures;

import java.util.ArrayList;
import java.util.List;

/// Fabric-only implementation of [VillageStructures]: StructurePoolAPI has no Forge artifact on
/// 1.20.1, so both the `config/village_taverns/villages.json` config and the injection call live here.
public final class FabricVillageStructures {
    private FabricVillageStructures() { }

    public static final ConfigManager<StructurePoolConfig> villageConfig = new ConfigManager<StructurePoolConfig>
            ("villages", defaults())
            .builder()
            .setDirectory(TavernsMod.ID)
            .sanitize(true)
            .build();

    /// Installs the injector and loads (or writes) the config file. Called from the Fabric entrypoint
    /// before `TavernsMod.init()`.
    public static void install() {
        villageConfig.refresh();
        VillageStructures.injector = () -> {
            if (!Platform.util().isModLoaded("lithostitched")) {
                // Only inject the tavern if Lithostitched is not present
                StructurePoolAPI.injectAll(villageConfig.value);
            }
            villageConfig.save();
        };
    }

    private static StructurePoolConfig defaults() {
        var config = new StructurePoolConfig();
        var weight = 10;
        var limit = 1;
        config.entries = new ArrayList<>(List.of(
                new StructurePoolConfig.Entry("minecraft:village/desert/houses", TavernsMod.ID + ":village/desert/tavern", weight, limit),
                new StructurePoolConfig.Entry("minecraft:village/savanna/houses", TavernsMod.ID + ":village/savanna/tavern", weight, limit),
                new StructurePoolConfig.Entry("minecraft:village/plains/houses", TavernsMod.ID + ":village/plains/tavern", weight, limit),
                new StructurePoolConfig.Entry("minecraft:village/taiga/houses", TavernsMod.ID + ":village/taiga/tavern", weight, limit),
                new StructurePoolConfig.Entry("minecraft:village/snowy/houses", TavernsMod.ID + ":village/snowy/tavern", weight, limit)
        ));
        return config;
    }
}
