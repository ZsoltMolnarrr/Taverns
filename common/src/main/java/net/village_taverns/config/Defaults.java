package net.village_taverns.config;

import net.rpg_foundation.structure_pool.api.StructurePoolConfig;
import net.village_taverns.TavernsMod;

import java.util.ArrayList;
import java.util.List;

public class Defaults {
    public static final StructurePoolConfig villages;

    static {
        villages = new StructurePoolConfig();
        var weight = 10;
        var limit = 1;
        villages.entries = new ArrayList<>(List.of(
                new StructurePoolConfig.Entry("minecraft:village/desert/houses", TavernsMod.ID + ":village/desert/tavern", weight, limit),
                new StructurePoolConfig.Entry("minecraft:village/savanna/houses", TavernsMod.ID + ":village/savanna/tavern", weight, limit),
                new StructurePoolConfig.Entry("minecraft:village/plains/houses", TavernsMod.ID + ":village/plains/tavern", weight, limit),
                new StructurePoolConfig.Entry("minecraft:village/taiga/houses", TavernsMod.ID + ":village/taiga/tavern", weight, limit),
                new StructurePoolConfig.Entry("minecraft:village/snowy/houses", TavernsMod.ID + ":village/snowy/tavern", weight, limit)
        ));
    }
}
