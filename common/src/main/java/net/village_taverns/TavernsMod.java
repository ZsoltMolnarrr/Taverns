package net.village_taverns;

import net.fabric_extras.structure_pool.api.StructurePoolAPI;
import net.fabric_extras.structure_pool.api.StructurePoolConfig;
import net.tiny_config.ConfigManager;
import net.village_taverns.block.TavernBlocks;
import net.village_taverns.config.Defaults;

public class TavernsMod {

    public static final String ID = "village_taverns";

    public static ConfigManager<StructurePoolConfig> villageConfig = new ConfigManager<>
            ("villages", Defaults.villages)
            .builder()
            .setDirectory(ID)
            .sanitize(true)
            .build();

    public static void init() {
        villageConfig.refresh();
        if (!Platform.util().isModLoaded("lithostitched")) {
            // Only inject the village if the Lithostitched is not present
            StructurePoolAPI.injectAll(villageConfig.value);
        }
        villageConfig.save();
    }

    public static void registerBlocks() {
        TavernBlocks.register();
    }
}
