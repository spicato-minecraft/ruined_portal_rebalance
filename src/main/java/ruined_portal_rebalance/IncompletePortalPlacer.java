package ruined_portal_rebalance;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

/**
 * Incomplete 4×5 obsidian frame plus ruined-portal chest. Never places portal blocks or fire.
 */
public final class IncompletePortalPlacer {
	private IncompletePortalPlacer() {
	}

	public static boolean alreadyPresent(WorldGenLevel level, BlockPos origin) {
		BlockPos chest = origin.offset(4, 0, 1);
		BlockEntity be = level.getBlockEntity(chest);
		return be instanceof ChestBlockEntity;
	}

	public static void place(WorldGenLevel level, BlockPos floor, RandomSource random) {
		BlockPos origin = floor.above();
		if (alreadyPresent(level, origin)) {
			return;
		}
		for (int dx = 0; dx < 4; dx++) {
			for (int dy = 0; dy < 5; dy++) {
				BlockPos pos = origin.offset(dx, dy, 0);
				boolean frame = isFrameCell(dx, dy);
				boolean skip = isMissingCell(dx, dy);
				if (!frame) {
					if (dx >= 1 && dx <= 2 && dy >= 1 && dy <= 3) {
						level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
					}
					continue;
				}
				if (skip) {
					level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
					continue;
				}
				level.setBlock(pos, frameBlock(random), 2);
			}
		}
		BlockPos chestPos = origin.offset(4, 0, 1);
		level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
		BlockEntity be = level.getBlockEntity(chestPos);
		if (be instanceof ChestBlockEntity chest) {
			chest.setLootTable(BuiltInLootTables.RUINED_PORTAL, random.nextLong());
		}
	}

	static boolean isFrameCell(int dx, int dy) {
		boolean vertical = (dx == 0 || dx == 3) && dy >= 0 && dy <= 4;
		boolean horizontal = (dy == 0 || dy == 4) && dx >= 0 && dx <= 3;
		return vertical || horizontal;
	}

	static boolean isMissingCell(int dx, int dy) {
		return (dx == 0 && dy == 4) || (dx == 3 && dy == 4) || (dx == 0 && dy == 2);
	}

	private static BlockState frameBlock(RandomSource random) {
		return random.nextFloat() < 0.2f
				? Blocks.CRYING_OBSIDIAN.defaultBlockState()
				: Blocks.OBSIDIAN.defaultBlockState();
	}
}
