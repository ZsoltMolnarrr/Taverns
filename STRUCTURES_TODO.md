# TODO — the 5 tavern structure NBTs need a manual re-export on 1.20.1

**Status: BLOCKING for world generation on this branch. Not fixable from code.**

Taverns has no legacy `1.20.1` branch (the mod postdates 1.20.1), so there is no older copy of these
files to restore. Every shipped structure template was authored on 1.21.x and is therefore **too new
for 1.20.1**, which reads structure templates at DataVersion **3465**:

| File (under `common/src/main/resources/`) | DataVersion | 1.20.1 max | Verdict |
|---|---|---|---|
| `data/village_taverns/structures/village/desert/tavern.nbt` | 3955 | 3465 | needs manual re-export |
| `data/village_taverns/structures/village/plains/tavern.nbt` | 3955 | 3465 | needs manual re-export |
| `data/village_taverns/structures/village/savanna/tavern.nbt` | 3955 | 3465 | needs manual re-export |
| `data/village_taverns/structures/village/snowy/tavern.nbt` | 3955 | 3465 | needs manual re-export |
| `data/village_taverns/structures/village/taiga/tavern.nbt` | 3955 | 3465 | needs manual re-export |

The files are **left in place, unmodified** — they were deliberately not downgraded and not deleted.

## What this does and does not break

- **Boot is unaffected.** A too-new DataVersion is not read at startup; structure templates are loaded
  lazily, at placement time.
- **Placement will fail** when a village tries to generate a tavern (on either loader, and through
  either the Fabric StructurePoolAPI path or the Lithostitched worldgen modifiers). Expect
  `Failed to load structure … / Unsupported DataVersion`-shaped errors during world gen.

## How to fix (owner action)

Re-export each of the five taverns from a **1.20.1** world with structure blocks (or run each file
through a 1.20.1-targeting NBT downgrade), keeping the exact same paths and names, then drop the new
files over the existing ones. Nothing else in the port needs to change — the template-pool references
in `data/village_taverns/lithostitched/worldgen_modifier/village/*.json` and in
`net.village_taverns.fabric.village.FabricVillageStructures` already point at these ids and carry no
1.21-only schema.

Delete this file once the five templates are back at 3465.
