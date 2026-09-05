package ruined_portal_rebalance;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;

/**
 * Seed-only replica of vanilla {@code nether_complexes} random_spread (linear).
 * Spacing 27, separation 4, salt 30084232.
 */
public final class NetherComplexGrid {
	public static final int SPACING = 27;
	public static final int SEPARATION = 4;
	public static final int SALT = 30084232;
	public static final int NETHER_CHUNKS_PER_OVERWORLD_REGION = 8;

	private NetherComplexGrid() {
	}

	public static ChunkPos startChunk(long worldSeed, int queryChunkX, int queryChunkZ) {
		int regionX = Math.floorDiv(queryChunkX, SPACING);
		int regionZ = Math.floorDiv(queryChunkZ, SPACING);
		WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
		random.setLargeFeatureWithSalt(worldSeed, regionX, regionZ, SALT);
		int range = SPACING - SEPARATION;
		int offsetX = random.nextInt(range);
		int offsetZ = random.nextInt(range);
		return new ChunkPos(regionX * SPACING + offsetX, regionZ * SPACING + offsetZ);
	}

	public static boolean isStructureChunk(long worldSeed, int chunkX, int chunkZ) {
		ChunkPos start = startChunk(worldSeed, chunkX, chunkZ);
		return start.x == chunkX && start.z == chunkZ;
	}

	/**
	 * Overworld chunk that holds the 8:1 projection of a nether structure-start chunk origin.
	 */
	public static ChunkPos overworldAnchorChunk(ChunkPos netherStartChunk) {
		return new ChunkPos(
				netherStartChunk.x * NETHER_CHUNKS_PER_OVERWORLD_REGION,
				netherStartChunk.z * NETHER_CHUNKS_PER_OVERWORLD_REGION
		);
	}

	public static boolean isOverworldAnchorChunk(long worldSeed, int overworldChunkX, int overworldChunkZ) {
		if (Math.floorMod(overworldChunkX, NETHER_CHUNKS_PER_OVERWORLD_REGION) != 0
				|| Math.floorMod(overworldChunkZ, NETHER_CHUNKS_PER_OVERWORLD_REGION) != 0) {
			return false;
		}
		int netherX = Math.floorDiv(overworldChunkX, NETHER_CHUNKS_PER_OVERWORLD_REGION);
		int netherZ = Math.floorDiv(overworldChunkZ, NETHER_CHUNKS_PER_OVERWORLD_REGION);
		return isStructureChunk(worldSeed, netherX, netherZ);
	}

	public static int overworldBlockX(ChunkPos netherStartChunk) {
		return netherStartChunk.getMiddleBlockX() * NETHER_CHUNKS_PER_OVERWORLD_REGION;
	}

	public static int overworldBlockZ(ChunkPos netherStartChunk) {
		return netherStartChunk.getMiddleBlockZ() * NETHER_CHUNKS_PER_OVERWORLD_REGION;
	}
}
