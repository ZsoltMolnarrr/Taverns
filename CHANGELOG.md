# 1.3.0.001

> ### ⚠️ Read this before updating
>
> This release is a **major technical overhaul and is not backwards compatible.**
>
> - **Requires the matching Spell Engine release.** This version will not run on Spell Engine
>   **0.9.x**, and mods built against 0.9.x will not work alongside it.
> - **Update the whole set together.** Spell Engine and every RPG Series mod must be on
>   matching versions. Mixing in an older add-on will break at startup or misbehave in play.
> - **Spell books must be re-obtained.** Spell books from an older world no longer carry valid
>   spell data. Re-craft them, or re-bind their spells at the Spell Binding Table.
>
> **Back up your world before updating.**

- Ported to Minecraft 1.20.1 (Fabric + Forge 47).

# 1.3.0

- Added brewing recipes for all Spell Power and Ranged Weapon potions, previously these were only obtainable from bartender trades
- Spell Power potions brew from Thick potion (Water Bottle + Glowstone Dust):
  - Arcane Power - Amethyst Shard
  - Fire Power - Blaze Rod
  - Frost Power - Snowball
  - Healing Power - Honeycomb
  - Lightning Power - Glow Ink Sac
  - Soul Power - Rotten Flesh
  - Spell Volatility - Glow Berries
  - Amplify Spell - Sugar
  - Spell Haste - Chorus Fruit
- Ranged Weapon potions brew from Mundane potion (Water Bottle + Redstone):
  - Ranged Damage - Sweet Berries
  - Draw Speed - Feather
- Fermented Spider Eye flips a school potion into its opposite: Fire <-> Frost, Healing <-> Soul, Arcane <-> Lightning
- All of the above work as Splash and Lingering potions, and as Tipped Arrows
- Brewing recipes are fully configurable in `config/village_taverns/brewing.json`, any base potion, ingredient and result combination can be added

# 1.2.0

- NeoForge version no longer needs Forgified Fabric API
- Fully translated content, now supporting 20 languages

# 1.1.5

- Add compatibility with Critical Strike mod

# 1.1.4

- Fix RWA effect compat

# 1.1.3

- Fix bartender profession being screwed

# 1.1.2

- Fix Tiny Config embedding

# 1.1.1

- Fix NeoForge mod descriptor

# 1.1.0

- Migrate to Architectury

# 1.0.7

- Improve snowy village structure

# 1.0.6

- Update translations

# 1.0.5

- Update barrel block model, thanks to D1scoball! <3

# 1.0.4

- Remove miscellaneous mixins

# 1.0.3

- Village spawn secret

# 1.0.2

- Fix crashing without Spell Power and RWA

# 1.0.1

- Compatibility with Lithostitched 1.4

# 1.0.0

- Initial release

#
