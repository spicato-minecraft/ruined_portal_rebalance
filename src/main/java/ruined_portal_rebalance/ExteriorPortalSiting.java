package ruined_portal_rebalance;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Sits the Nether ruin outside the fortress/bastion footprint, within
 * {@link #MAX_OUTSIDE} blocks, without straying more than vanilla's 16-block
 * nether portal POI search from the seed landing (Overworld 8:1 destination).
 */
public final class ExteriorPortalSiting {
	public static final int MAX_OUTSIDE = 50;
	public static final int MIN_OUTSIDE = 8;
	/** Matches {@code PortalForcer.findClosestPortalPosition} when dest is the Nether. */
	public static final int NETHER_PORTAL_SEARCH = 16;

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

	public static BlockPos snapXZ(BoundingBox box, BlockPos seed, int dirX, int dirZ) {
		int x = seed.getX();
		int z = seed.getZ();
		if (dirX == 0 && dirZ == 0) {
			return new BlockPos(x, 0, z);
		}
		if (!containsXZ(box, x, z)) {
			return new BlockPos(x, 0, z);
		}
		int walked = 0;
		while (containsXZ(box, x, z) && walked < NETHER_PORTAL_SEARCH) {
			x += dirX;
			z += dirZ;
			walked++;
		}
		if (containsXZ(box, x, z)) {
			return new BlockPos(seed.getX(), 0, seed.getZ());
		}
		int extra = 0;
		while (extra < MIN_OUTSIDE) {
			int nx = x + dirX;
			int nz = z + dirZ;
			if (chebyshev(seed.getX(), seed.getZ(), nx, nz) > NETHER_PORTAL_SEARCH) {
				break;
			}
			if (distanceOutside(box, nx, nz) > MAX_OUTSIDE) {
				break;
			}
			x = nx;
			z = nz;
			extra++;
		}
		return new BlockPos(x, 0, z);
	}

	public static BlockPos findFloor(WorldGenLevel level, BoundingBox box, BlockPos seed, int dirX, int dirZ) {
		BlockPos xz = snapXZ(box, seed, dirX, dirZ);
		int yMax = Math.min(level.getMaxY() - 4, Math.max(box.maxY() + 12, 96));
		int yMin = Math.max(level.getMinY() + 4, Math.min(box.minY() - 4, 32));
		for (int y = yMax; y >= yMin; y--) {
			BlockPos floor = new BlockPos(xz.getX(), y, xz.getZ());
			if (isWalkableStand(level, floor)) {
				return floor.immutable();
			}
		}
		int y = Math.max(yMin, Math.min(64, box.minY() + 2));
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
