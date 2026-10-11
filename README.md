# Ruined Portal Rebalance

A Fabric mod for Minecraft **26.3**. Vanilla ruined-portal scatter is removed. Each nether fortress and bastion remnant gets an **incomplete** ruined portal **against the exterior wall** — a gate the complex built to reach the Overworld. The Overworld has a matching **surface** ruin at the 8:1 projection of that landing, so you can find complexes from the Overworld. Lighting the Overworld portal uses **vanilla linking** and drops you beside the structure — not in the treasure. You still have to enter and clear the complex.

New chunks only. Existing worlds keep vanilla scatter in already generated chunks.

## Compatibility

| Component     | Version           |
|---------------|-------------------|
| Minecraft     | 26.3              |
| Fabric Loader | 0.19.5+           |
| Fabric API    | 0.162.0+26.3      |
| Java          | 25+               |

## Behavior

- Vanilla `ruined_portals` structure set frequency is 0 (no independent scatter).
- `nether_complexes` weights start at bastion **4** / fortress **1** (vanilla was 3:2). Grid spacing is unchanged.
- Overworld: datapack structures `ruined_portal_rebalance:paired_portal*` reuse vanilla ruined-portal templates with **surface** setups only (no cave/underground). Placement is `overworld_nether_complex_anchor` (8:1 of the nether exterior landing). Oceans/swamps still use the seafloor setup.
- Nether inject: after fortress/bastion generate, one incomplete frame **just outside** the bounding box (about 4 blocks from the wall), still within 16 blocks of the seed landing so vanilla portal search links instead of carving a new portal in the center.
- Testing: `/locate structure #ruined_portal_rebalance:paired_portals` or `/locate structure ruined_portal_rebalance:paired_portal` uses that same landing (seed-only).

## Branch policy

Each supported Minecraft version has its own branch (e.g. `26.3`). Version branches are the source of truth — there is no `main`.

```bash
git clone https://github.com/spicato-minecraft/ruined_portal_rebalance.git
cd ruined_portal_rebalance
git checkout 26.3
```

## Build & test

```bash
./gradlew test
./gradlew runGameTest
./gradlew build
```

The JAR is `build/libs/ruined_portal_rebalance-<version>.jar`.

## Test strategy

- **Unit:** nether-complex grid math and 8:1 Overworld anchors; nearest paired-portal locate; exterior siting / incomplete-frame rules.
- **GameTest:** incomplete frame has gaps, a chest, and no portal/fire blocks.
- **Manual UAT:** Mod Catalog Required tests (new world, locate, repair, light, land outside the complex, loot still requires traversal).

## Releases and Modrinth

Merging does **not** publish. A maintainer creates a GitHub Release with tag `{minecraft_version}-{mod_version}` and changelog `releases/{tag}.md`. See `minecraft-mods/docs/README.md`.

First 26.1 release target: **`26.1-1.0.0`** after UAT. Modrinth project + `MODRINTH_ID` are a follow-up task.

## Architecture

- Datapack overrides under `src/main/resources/data/minecraft/worldgen/structure_set/` (silence scatter; nether-complex weights).
- Datapack structure set `data/ruined_portal_rebalance/worldgen/structure_set/paired_overworld_portals.json` references surface-only `paired_portal*` structures (vanilla `minecraft:ruined_portal` type).
- `OverworldNetherComplexAnchorPlacement` is the spawn rule those JSON files cannot express alone.
- `NetherComplexGrid` matches vanilla random_spread (spacing 27, salt `30084232`) and a hashed exterior landing.
- `StructureStartMixin` places the Nether ruin after `placeInChunk` via `ExteriorPortalSiting`.
