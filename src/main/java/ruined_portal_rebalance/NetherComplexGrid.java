package ruined_portal_rebalance;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;

/**
 * Seed-only replica of vanilla {@code nether_complexes} random_spread (linear),
 * plus a hashed exterior landing used by both dimensions so vanilla portal
 * linking (16-block nether POI search) finds the paired ruin.
 */
public final class NetherComplexGrid {
	public static final int SPACING = 27;
	public static final int SEPARATION = 4;
	public static final int SALT = 30084232;
	public static final int NETHER_CHUNKS_PER_OVERWORLD_REGION = 8;
	public static final int LANDING_SALT = 58492011;
	/**
	 * Blocks from the nether-complex start-chunk middle along a hashed cardinal.
	 * Kept near a typical fortress/bastion exterior so the 16-block portal POI
	 * window can hug the wall instead of sitting in open netherrack.
	 */
	public static final int LANDING_OFFSET = 24;

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
		return start.x() == chunkX && start.z() == chunkZ;
	}

	public static int landingDirectionIndex(long worldSeed, ChunkPos netherStartChunk) {
		WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
		random.setLargeFeatureWithSalt(worldSeed, netherStartChunk.x(), netherStartChunk.z(), LANDING_SALT);
		return random.nextInt(4);
	}

	public static int dirX(int directionIndex) {
		return switch (directionIndex) {
			case 0 -> 1;
			case 1 -> -1;
			default -> 0;
		};
	}

	public static int dirZ(int directionIndex) {
		return switch (directionIndex) {
			case 2 -> 1;
			case 3 -> -1;
			default -> 0;
		};
	}

	/**
	 * Nether XZ where the incomplete portal and the Overworld 8:1 destination agree.
	 */
	public static BlockPos netherLanding(long worldSeed, ChunkPos netherStartChunk) {
		int dir = landingDirectionIndex(worldSeed, netherStartChunk);
		return new BlockPos(
				netherStartChunk.getMiddleBlockX() + dirX(dir) * LANDING_OFFSET,
				0,
				netherStartChunk.getMiddleBlockZ() + dirZ(dir) * LANDING_OFFSET
		);
	}

	public static ChunkPos overworldAnchorChunk(long worldSeed, ChunkPos netherStartChunk) {
		BlockPos nether = netherLanding(worldSeed, netherStartChunk);
		return ChunkPos.containing(new BlockPos(
				nether.getX() * NETHER_CHUNKS_PER_OVERWORLD_REGION,
				0,
				nether.getZ() * NETHER_CHUNKS_PER_OVERWORLD_REGION
		));
	}

	public static boolean isOverworldAnchorChunk(long worldSeed, int overworldChunkX, int overworldChunkZ) {
		int sampleX = Math.floorDiv(overworldChunkX, NETHER_CHUNKS_PER_OVERWORLD_REGION);
		int sampleZ = Math.floorDiv(overworldChunkZ, NETHER_CHUNKS_PER_OVERWORLD_REGION);
		ChunkPos want = new ChunkPos(overworldChunkX, overworldChunkZ);
		for (int rx = -1; rx <= 1; rx++) {
			for (int rz = -1; rz <= 1; rz++) {
				ChunkPos start = startChunk(worldSeed, sampleX + rx * SPACING, sampleZ + rz * SPACING);
				if (overworldAnchorChunk(worldSeed, start).equals(want)) {
					return true;
				}
			}
		}
		return false;
	}
}
