package ruined_portal_rebalance;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExteriorPortalSitingTest {

	@Test
	void snapMovesInsidePointOutsideAlongDirection() {
		BoundingBox box = new BoundingBox(0, 40, 0, 30, 80, 30);
		BlockPos seed = new BlockPos(15, 0, 15);
		BlockPos snapped = ExteriorPortalSiting.snapXZ(box, seed, 1, 0);
		assertFalse(ExteriorPortalSiting.containsXZ(box, snapped.getX(), snapped.getZ()));
		assertFalse(ExteriorPortalSiting.frameOverlapsStructure(box, snapped.getX(), snapped.getZ()));
		assertTrue(ExteriorPortalSiting.distanceOutside(box, snapped.getX(), snapped.getZ())
				<= ExteriorPortalSiting.MAX_OUTSIDE);
		assertTrue(ExteriorPortalSiting.chebyshev(seed.getX(), seed.getZ(), snapped.getX(), snapped.getZ())
				<= ExteriorPortalSiting.NETHER_PORTAL_SEARCH);
	}

	@Test
	void snapPullsAFarSeedInAgainstTheWall() {
		BoundingBox box = new BoundingBox(0, 40, 0, 20, 80, 20);
		BlockPos seed = new BlockPos(36, 0, 10);
		BlockPos snapped = ExteriorPortalSiting.snapXZ(box, seed, 1, 0);
		assertFalse(ExteriorPortalSiting.containsXZ(box, snapped.getX(), snapped.getZ()));
		assertEquals(ExteriorPortalSiting.MIN_OUTSIDE, ExteriorPortalSiting.distanceOutside(box, snapped.getX(), snapped.getZ()));
		assertTrue(ExteriorPortalSiting.chebyshev(seed.getX(), seed.getZ(), snapped.getX(), snapped.getZ())
				<= ExteriorPortalSiting.NETHER_PORTAL_SEARCH);
	}

	@Test
	void snapKeepsTheFrameClearOfTheStructure() {
		BoundingBox box = new BoundingBox(0, 40, 0, 20, 80, 20);
		BlockPos seed = new BlockPos(10, 0, 10);
		BlockPos snapped = ExteriorPortalSiting.snapXZ(box, seed, -1, 0);
		assertFalse(ExteriorPortalSiting.frameOverlapsStructure(box, snapped.getX(), snapped.getZ()));
	}

	@Test
	void incompleteFrameOmitsCornersAndOneSide() {
		assertTrue(IncompletePortalPlacer.isFrameCell(0, 0));
		assertTrue(IncompletePortalPlacer.isMissingCell(0, 4));
		assertTrue(IncompletePortalPlacer.isMissingCell(3, 4));
		assertTrue(IncompletePortalPlacer.isMissingCell(0, 2));
		assertFalse(IncompletePortalPlacer.isMissingCell(1, 0));
		assertFalse(IncompletePortalPlacer.isFrameCell(1, 2));
	}
}
