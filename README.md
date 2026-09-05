# Ruined Portal Rebalance

A Fabric mod for Minecraft **1.21.11**. Vanilla ruined-portal scatter is removed. Each nether fortress and bastion remnant gets an **incomplete** ruined portal as an **alternate front door**. The Overworld has a matching ruin at the 8:1 projection of that nether structure, so you can find complexes from the Overworld. Lighting the Overworld portal uses **vanilla linking** and should drop you inside the structure — not next to the loot. You still have to clear the rest of the complex.

New chunks only. Existing worlds keep vanilla scatter in already generated chunks.

## Compatibility

| Component     | Version           |
|---------------|-------------------|
| Minecraft     | 1.21.11           |
| Fabric Loader | 0.19.3+           |
| Fabric API    | 0.141.6+1.21.11   |
| Java          | 21+               |

## Behavior

- Vanilla `ruined_portals` structure set frequency is 0 (no independent scatter).
- `nether_complexes` weights start at bastion **4** / fortress **1** (vanilla was 3:2). Grid spacing is unchanged.
- Overworld locator: seed-only nether-complex start × 8.
- Nether inject: after fortress/bastion generate, one incomplete frame on the outer walkable band, away from chests/spawners/gold.

## Branch policy

Each supported Minecraft version has its own branch (e.g. `1.21.11`). Version branches are the source of truth — there is no `main`.

```bash
git clone https://github.com/spicato-minecraft/ruined_portal_rebalance.git
cd ruined_portal_rebalance
git checkout 1.21.11
```

## Build & test

```bash
./gradlew test
./gradlew runGameTest
./gradlew build
```

The remapped JAR is `build/libs/ruined_portal_rebalance-<version>.jar`.

## Test strategy

- **Unit:** nether-complex grid math and 8:1 Overworld anchors; front-door outer-band / incomplete-frame rules.
- **GameTest:** incomplete frame has gaps, a chest, and no portal/fire blocks.
- **Manual UAT:** Mod Catalog Required tests (new world, locate, repair, light, front-door arrival, loot still requires traversal).

## Releases and Modrinth

Merging does **not** publish. A maintainer creates a GitHub Release with tag `{minecraft_version}-{mod_version}` and changelog `releases/{tag}.md`. See `minecraft-mods/docs/README.md`.

First release target: **`1.21.11-1.0.0`** after UAT. Modrinth project + `MODRINTH_ID` are a follow-up task.

## Architecture

- Datapack overrides under `src/main/resources/data/minecraft/worldgen/structure_set/`.
- `NetherComplexGrid` matches vanilla random_spread (spacing 27, salt `30084232`).
- `StructureStartMixin` injects the Nether front door after `placeInChunk`.
- `ChunkStatusTasksMixin` places the Overworld anchor after `generateFeatures`.
