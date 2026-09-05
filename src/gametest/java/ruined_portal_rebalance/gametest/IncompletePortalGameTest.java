package ruined_portal_rebalance.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import ruined_portal_rebalance.IncompletePortalPlacer;

public class IncompletePortalGameTest {

	@GameTest(maxTicks = 100)
	public void incompleteFrameHasGapsAndChestNoPortalBlock(GameTestHelper context) {
		BlockPos floor = new BlockPos(1, 1, 2);
		context.setBlock(floor, Blocks.NETHERRACK);
		IncompletePortalPlacer.place(context.getLevel(), context.absolutePos(floor), RandomSource.create(1L));

		BlockPos origin = floor.above();
		context.assertBlockPresent(Blocks.AIR, origin.offset(0, 4, 0));
		context.assertBlockPresent(Blocks.AIR, origin.offset(1, 2, 0));
		boolean hasObsidian = false;
		for (int dx = 0; dx < 4; dx++) {
			for (int dy = 0; dy < 5; dy++) {
				var pos = origin.offset(dx, dy, 0);
				context.assertBlockNotPresent(Blocks.NETHER_PORTAL, pos);
				context.assertBlockNotPresent(Blocks.FIRE, pos);
				if (context.getBlockState(pos).is(Blocks.OBSIDIAN) || context.getBlockState(pos).is(Blocks.CRYING_OBSIDIAN)) {
					hasObsidian = true;
				}
			}
		}
		if (!hasObsidian) {
			context.fail("Expected incomplete obsidian frame");
			return;
		}
		ChestBlockEntity chest = context.getBlockEntity(origin.offset(4, 0, 1), ChestBlockEntity.class);
		if (chest == null) {
			context.fail("Expected ruined-portal chest");
			return;
		}
		context.succeed();
	}
}
