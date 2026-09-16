package ruined_portal_rebalance;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Sits the Nether ruin against the fortress/bastion wall — just outside the
 * footprint, still inside vanilla's 16-block nether portal POI search of the
 * seed landing (Overworld 8:1 destination).
 */
public final class ExteriorPortalSiting {
	/** Preferred Chebyshev gap from the structure box to the frame origin. */
	public static final int MIN_OUTSIDE = 4;
	/** Hard cap; the POI window is the real limit. */
	public static final int MAX_OUTSIDE = 16;
	/** Matches {@code PortalForcer.findClosestPortalPosition} when dest is the Nether. */
	public static final int NETHER_PORTAL_SEARCH = 16;
	/** Incomplete frame is 4 wide in +X with a chest at +4, +1 Z. */
	private static final int FRAME_SIZE_X = 4;
	private static final int FRAME_SIZE_Z = 1;

	private ExteriorPortalSiting() {
	}

	public static boolean containsXZ(BoundingBox box, int x, int z) {
		return x >= box.minX() && x <= box.maxX() && z >= box.minZ() && z <= box.maxZ();
	}

	/** Chebyshev distance from XZ to the box (0 if inside). */
	public static int distanceOutside(BoundingBox box, int x, int z) {
		if (containsXZ(box, x, z)) {
			return 0;
		}
		int dx = 0;
		if (x < box.minX()) {
			dx = box.minX() - x;
		} else if (x > box.maxX()) {
			dx = x - box.maxX();
		}
		int dz = 0;
		if (z < box.minZ()) {
			dz = box.minZ() - z;
		} else if (z > box.maxZ()) {
			dz = z - box.maxZ();
		}
		return Math.max(dx, dz);
	}

	public static boolean frameOverlapsStructure(BoundingBox box, int originX, int originZ) {
		for (int dx = 0; dx <= FRAME_SIZE_X; dx++) {
			for (int dz = 0; dz <= FRAME_SIZE_Z; dz++) {
				if (containsXZ(box, originX + dx, originZ + dz)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Picks XZ inside the 16-block POI window that sits just outside the
	 * complex, hugging the wall the hashed direction was aiming at.
	 */
	public static BlockPos snapXZ(BoundingBox box, BlockPos seed, int dirX, int dirZ) {
		int sx = seed.getX();
		int sz = seed.getZ();
		BlockPos best = null;
		int bestScore = Integer.MAX_VALUE;
		for (int dx = -NETHER_PORTAL_SEARCH; dx <= NETHER_PORTAL_SEARCH; dx++) {
			for (int dz = -NETHER_PORTAL_SEARCH; dz <= NETHER_PORTAL_SEARCH; dz++) {
				if (chebyshev(0, 0, dx, dz) > NETHER_PORTAL_SEARCH) {
					continue;
				}
				int x = sx + dx;
				int z = sz + dz;
				int outside = distanceOutside(box, x, z);
				if (outside == 0 || frameOverlapsStructure(box, x, z)) {
					continue;
				}
				int score = scoreCandidate(outside, dx, dz, dirX, dirZ);
				if (score < bestScore) {
					bestScore = score;
					best = new BlockPos(x, 0, z);
				}
			}
		}
		if (best != null) {
			return best;
		}
		return fallbackAlongDirection(box, seed, dirX, dirZ);
	}

	static int scoreCandidate(int outside, int dx, int dz, int dirX, int dirZ) {
		int gap = Math.abs(outside - MIN_OUTSIDE);
		if (outside > MAX_OUTSIDE) {
			gap += (outside - MAX_OUTSIDE) * 8;
		}
		int along = 0;
		if (dirX != 0 || dirZ != 0) {
			along = -(dx * dirX + dz * dirZ);
		}
		return gap * 1000 + along * 10 + Math.abs(dx) + Math.abs(dz);
	}

	private static BlockPos fallbackAlongDirection(BoundingBox box, BlockPos seed, int dirX, int dirZ) {
		int x = seed.getX();
		int z = seed.getZ();
		if (dirX == 0 && dirZ == 0) {
			return new BlockPos(x, 0, z);
		}
		int walked = 0;
		while (containsXZ(box, x, z) && walked < NETHER_PORTAL_SEARCH) {
			x += dirX;
			z += dirZ;
			walked++;
		}
		return new BlockPos(x, 0, z);
	}

	public static BlockPos findFloor(WorldGenLevel level, BoundingBox box, BlockPos seed, int dirX, int dirZ) {
		BlockPos xz = snapXZ(box, seed, dirX, dirZ);
		int structureTop = Math.min(level.getMaxY() - 4, box.maxY());
		int structureBot = Math.max(level.getMinY() + 4, box.minY());
		if (structureTop < structureBot) {
			int swap = structureTop;
			structureTop = structureBot;
			structureBot = swap;
		}
		for (int y = structureTop; y >= structureBot; y--) {
			BlockPos floor = new BlockPos(xz.getX(), y, xz.getZ());
			if (isWalkableStand(level, floor)) {
				return floor.immutable();
			}
		}
		int yMax = Math.min(level.getMaxY() - 4, Math.max(box.maxY() + 8, 96));
		int yMin = Math.max(level.getMinY() + 4, Math.min(box.minY() - 4, 32));
		for (int y = yMax; y >= yMin; y--) {
			if (y >= structureBot && y <= structureTop) {
				continue;
			}
			BlockPos floor = new BlockPos(xz.getX(), y, xz.getZ());
			if (isWalkableStand(level, floor)) {
				return floor.immutable();
			}
		}
		int y = Math.max(yMin, Math.min(structureBot + 2, structureTop));
		return new BlockPos(xz.getX(), y, xz.getZ());
	}

	public static boolean isWalkableStand(WorldGenLevel level, BlockPos floor) {
		BlockState ground = level.getBlockState(floor);
		BlockState feet = level.getBlockState(floor.above());
		BlockState head = level.getBlockState(floor.above(2));
		if (ground.isAir() || ground.liquid() || ground.is(Blocks.LAVA) || ground.is(Blocks.FIRE)) {
			return false;
		}
		if (!ground.isSolid()) {
			return false;
		}
		return feet.isAir() && head.isAir();
	}

	static int chebyshev(int ax, int az, int bx, int bz) {
		return Math.max(Math.abs(ax - bx), Math.abs(az - bz));
	}
}
