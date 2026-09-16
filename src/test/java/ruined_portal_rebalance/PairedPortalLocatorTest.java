package ruined_portal_rebalance;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PairedPortalLocatorTest {

	@BeforeAll
	static void bootstrapMinecraft() {
		MinecraftTestBootstrap.init();
	}

	@Test
	void nearestStartIsTheRegionStartWhenQueryIsOnIt() {
		long seed = 42L;
		ChunkPos start = NetherComplexGrid.startChunk(seed, 0, 0);
		ChunkPos found = PairedPortalLocator.nearestNetherStartChunk(seed, start.x(), start.z(), 100).orElseThrow();
		assertEquals(start, found);
	}

	@Test
	void nearestStartStaysOnThisComplexOneChunkAway() {
		long seed = 42L;
		ChunkPos start = NetherComplexGrid.startChunk(seed, 0, 0);
		ChunkPos found = PairedPortalLocator.nearestNetherStartChunk(seed, start.x() + 1, start.z(), 100).orElseThrow();
		assertEquals(start, found);
	}

	@Test
	void nearestStartResolvesADifferentRegionSample() {
		long seed = 42L;
		ChunkPos origin = NetherComplexGrid.startChunk(seed, 0, 0);
		ChunkPos other = NetherComplexGrid.startChunk(seed, NetherComplexGrid.SPACING * 3, NetherComplexGrid.SPACING * 2);
		ChunkPos found = PairedPortalLocator.nearestNetherStartChunk(seed, other.x(), other.z(), 100).orElseThrow();
		assertEquals(other, found);
		assertTrue(!origin.equals(other));
	}

	@Test
	void pairedPortalsTagIsModNamespace() {
		assertEquals("ruined_portal_rebalance", PairedPortalLocator.PAIRED_PORTALS.location().getNamespace());
		assertEquals("paired_portals", PairedPortalLocator.PAIRED_PORTALS.location().getPath());
	}

	@Test
	void overworldAnchorIsEightTimesNetherLanding() {
		long seed = 999L;
		ChunkPos nether = NetherComplexGrid.startChunk(seed, 0, 0);
		BlockPos landing = NetherComplexGrid.netherLanding(seed, nether);
		ChunkPos anchor = NetherComplexGrid.overworldAnchorChunk(seed, nether);
		assertEquals(ChunkPos.containing(new BlockPos(
				landing.getX() * NetherComplexGrid.NETHER_CHUNKS_PER_OVERWORLD_REGION,
				0,
				landing.getZ() * NetherComplexGrid.NETHER_CHUNKS_PER_OVERWORLD_REGION
		)), anchor);
	}
}
