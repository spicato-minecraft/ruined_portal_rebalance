package ruined_portal_rebalance;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FrontDoorFinderTest {

	@Test
	void outerBandIsNearBoxEdge() {
		BoundingBox box = new BoundingBox(0, 40, 0, 40, 80, 40);
		assertTrue(FrontDoorFinder.isOuterBand(new BlockPos(1, 50, 20), box));
		assertTrue(FrontDoorFinder.isOuterBand(new BlockPos(20, 50, 2), box));
		assertFalse(FrontDoorFinder.isOuterBand(new BlockPos(20, 50, 20), box));
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
