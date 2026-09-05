package ruined_portal_rebalance;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Picks a walkable outer-band cell: alternate front door, not a loot skip.
 */
public final class FrontDoorFinder {
	public static final int OUTER_BAND = 6;
	public static final int LOOT_AVOID_RADIUS = 8;

	private FrontDoorFinder() {
	}

	public static boolean isOuterBand(BlockPos pos, BoundingBox box) {
		if (!box.isInside(pos)) {
			return false;
		}
		int dx = Math.min(pos.getX() - box.minX(), box.maxX() - pos.getX());
		int dz = Math.min(pos.getZ() - box.minZ(), box.maxZ() - pos.getZ());
		return dx <= OUTER_BAND || dz <= OUTER_BAND;
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

	public static boolean nearLoot(WorldGenLevel level, BlockPos pos, BoundingBox box) {
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		int minX = Math.max(box.minX(), pos.getX() - LOOT_AVOID_RADIUS);
		int maxX = Math.min(box.maxX(), pos.getX() + LOOT_AVOID_RADIUS);
		int minY = Math.max(box.minY(), pos.getY() - LOOT_AVOID_RADIUS);
		int maxY = Math.min(box.maxY(), pos.getY() + LOOT_AVOID_RADIUS);
		int minZ = Math.max(box.minZ(), pos.getZ() - LOOT_AVOID_RADIUS);
		int maxZ = Math.min(box.maxZ(), pos.getZ() + LOOT_AVOID_RADIUS);
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				for (int z = minZ; z <= maxZ; z++) {
					cursor.set(x, y, z);
					BlockState state = level.getBlockState(cursor);
					if (state.is(Blocks.CHEST) || state.is(Blocks.SPAWNER) || state.is(Blocks.GOLD_BLOCK)) {
						return true;
					}
					BlockEntity be = level.getBlockEntity(cursor);
					if (be instanceof SpawnerBlockEntity) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static BlockPos find(WorldGenLevel level, BoundingBox box, BlockPos preferNear) {
		BlockPos best = null;
		double bestScore = Double.MAX_VALUE;
		int step = 2;
		for (int x = box.minX(); x <= box.maxX(); x += step) {
			for (int z = box.minZ(); z <= box.maxZ(); z += step) {
				if (!isOuterColumn(x, z, box)) {
					continue;
				}
				int yMin = box.minY() + 1;
				int yMax = box.maxY() - 3;
				for (int y = yMin; y <= yMax; y += 2) {
					BlockPos floor = new BlockPos(x, y, z);
					if (!isWalkableStand(level, floor)) {
						continue;
					}
					if (nearLoot(level, floor.above(), box)) {
						continue;
					}
					double dist = floor.distSqr(preferNear);
					if (dist < bestScore) {
						bestScore = dist;
						best = floor.immutable();
					}
				}
			}
		}
		return best;
	}

	static boolean isOuterColumn(int x, int z, BoundingBox box) {
		int dx = Math.min(x - box.minX(), box.maxX() - x);
		int dz = Math.min(z - box.minZ(), box.maxZ() - z);
		return dx <= OUTER_BAND || dz <= OUTER_BAND;
	}
}
