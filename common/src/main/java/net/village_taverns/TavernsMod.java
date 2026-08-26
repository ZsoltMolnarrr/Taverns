package net.village_taverns;

import net.rpg_foundation.structure_pool.api.StructurePoolAPI;
import net.rpg_foundation.structure_pool.api.StructurePoolConfig;
import net.tiny_config.ConfigManager;
import net.village_taverns.block.TavernBlocks;
import net.village_taverns.config.BrewingConfig;
import net.village_taverns.config.Defaults;

public class TavernsMod {

    public static final String ID = "village_taverns";

    public static ConfigManager<StructurePoolConfig> villageConfig = new ConfigManager<>
            ("villages", Defaults.villages)
            .builder()
            .setDirectory(ID)
            .sanitize(true)
            .build();

    /// Brewing recipes for the SpellPower / RangedWeaponAPI potions. Config-driven because brewing
    /// has no datapack path in 1.21.1 — see [BrewingConfig]. Versioned, so bumping
    /// [BrewingConfig#SCHEMA_VERSION] regenerates stale files instead of leaving players on an old
    /// recipe list.
    public static ConfigManager<BrewingConfig> brewingConfig = new ConfigManager<>
            ("brewing", Defaults.brewing)
            .builder()
            .setDirectory(ID)
            .sanitize(true)
            .schemaVersion(BrewingConfig.SCHEMA_VERSION)
            .build();

    public static void init() {
        villageConfig.refresh();
        // Refreshed here, not at brewing-registry build time: the registry is rebuilt per world load,
        // and re-reading the file on each of those would let a mid-session edit apply inconsistently.
        brewingConfig.refresh();
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
