[1.3.0]
----------
### Changed
*   Ported to Minecraft 26.1.2 (NeoForge 26.1.2.124, Java 25). Gameplay is unchanged.
*   Requires JEI 29 and Patchouli 26.1 for their integrations.
*   Saved data from 1.21.1 does not carry over for assimilations and the Mutation Matrix contents.

[1.2.0]
----------
### Added
*   **Assimilations:** deed-earned traits stored on the player. Earning them and their effects need a carried Maw of the assimilation's stage or higher.
    *   **Gilded Pheromones** (Latent): spend time in the Nether. Piglins and hoglins ignore you and barter better.
    *   **Nictitating Membrane** (Advanced): spend time in the End. Endermen ignore your gaze.
    *   **Aquanaut Coalescence** (Advanced): kill an Elder Guardian. Grants water breathing and speed.
    *   **Oxytocinergic Esca** (Noble): win a Bad Omen V raid. Stacks up to 10 to discount trades, and villagers gift you items.
    *   **Eldritch Antenna** (Excelsior): earn the other four and own a Maw with all 14 mutations. Mobs no longer start attacks (bosses, Elder Guardians and raiders excepted). Toggle it in the config screen.
*   Assimilation advancements, ending in "Fully Assimilated", plus Codex entries and custom icons.
*   Electric Mending now drains energy per melee hit. With Undying Flesh a hit is free but arcs lightning to up to 3 nearby mobs.
*   Config screen Values page (Voracity kills, Esca stacks), and a tooltip showing stage, energy, and mutation and assimilation counts. Shift lists mutations by stage.
*   Stage-up now plays a sound, particles and a title, and credits every player who hurt the boss, and the Codex now explains it.
*   Warnings for low durability, a weakened Electric Mending hit, and harmful effects absorbed by Symbiotic Aid.
*   Sneaking breaks a single block with Tectonic Bite, and the area outline hides while sneaking.
*   MIT license.

### Changed
*   _BREAKING CHANGE!_ Commands are regrouped under one `/ravenousmaw` root (permission level 2). The old flat names are gone:
    *   `mutation add <m>|add all|remove <m>|destabilize <m>|possible`
    *   `assimilation add <a>|add all|remove <a>`
    *   `energy charge|surcharge`
    *   `stage up`
    *   `debug tick <n>|damage`
*   Mental Supremacy is now an Advanced mutation and Insatiable Voracity a Noble one.
*   Stage objectives are ordered Wither, Ender Dragon, Warden, and the Codex stage entries match.
*   Electric Mending battery capacity scales with stage.
*   Insatiable Voracity damage is capped per stage.
*   Command mutation suggestions filter by the typed prefix.

### Fixed
*   **Crashes:** dedicated servers failing at startup (client-only classes were loaded), a null pointer when reloading Iris bullets, and missing or unknown mutation parameters.
*   Attack damage bonus no longer fails against non-living targets, dragon parts or a missing weapon.
*   Stage-up now keeps mutation state and enchantments, and no longer breaks on missing stored enchantments.
*   Reckless Devourer's loot clearing was inverted, it skipped player victims wrongly, and its multiplier now applies to the whole hit.
*   Symbiotic Immunity no longer removes beneficial effects, works from either hand, and cleanses every tick while held.
*   Symbiotic Aid keeps every Maw on death.
*   Arcane Hypertrophy now applies its Sharpness bonus.
*   Iris Out needs its toggle on before the bow draw starts, and the bullet tracing flag syncs to the client.
*   Mental Supremacy consumes the click when taming, so pets stay sitting.
*   Tectonic Bite keeps its area break above the durability threshold. Ore drops duplicate safely and skip Silk Touch.
*   Combustive explosions are real, and Fire Aspect hits explode after a short fuse.
*   Melee hits consume durability, and attacks below the threshold are blocked.
*   The Maw tick timer counted more than once per player per tick.
*   The Mutation Matrix enforces the stage gate using the Maw's real stage, and its result slot is take-only.
*   Ability craft, update and decraft lifecycle completed.
*   Config payloads are validated, and the menu binds to the main-hand Maw.
*   Commands validate mutation keys.
