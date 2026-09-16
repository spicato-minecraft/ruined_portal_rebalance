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
	void locateAcceptsDirectPairedPortalIds() {
		assertTrue(PairedPortalLocator.isPairedPortalId(
				net.minecraft.resources.Identifier.fromNamespaceAndPath("ruined_portal_rebalance", "paired_portal")));
		assertTrue(PairedPortalLocator.isPairedPortalId(
				net.minecraft.resources.Identifier.fromNamespaceAndPath("ruined_portal_rebalance", "paired_portal_ocean")));
		assertTrue(!PairedPortalLocator.isPairedPortalId(
				net.minecraft.resources.Identifier.fromNamespaceAndPath("minecraft", "ruined_portal")));
	}

	@Test
	void vanillaLocateRadiusCanMissNearestNetherStart() {
		long seed = 42L;
		int locateNetherRadius = Math.floorDiv(100, NetherComplexGrid.NETHER_CHUNKS_PER_OVERWORLD_REGION) + 1;
		boolean missed = false;
		for (int x = 0; x < NetherComplexGrid.SPACING; x++) {
			for (int z = 0; z < NetherComplexGrid.SPACING; z++) {
				if (PairedPortalLocator.nearestNetherStartChunk(seed, x, z, locateNetherRadius).isEmpty()) {
					missed = true;
					break;
				}
			}
		}
		assertTrue(missed, "100-chunk overworld /locate maps to nether radius " + locateNetherRadius
				+ ", which is smaller than half of spacing " + NetherComplexGrid.SPACING);
	}

	@Test
	void fullRegionRadiusAlwaysFindsNearestNetherStart() {
		long seed = 42L;
		for (int x = 0; x < NetherComplexGrid.SPACING; x++) {
			for (int z = 0; z < NetherComplexGrid.SPACING; z++) {
				assertTrue(PairedPortalLocator.nearestNetherStartChunk(seed, x, z, NetherComplexGrid.SPACING).isPresent());
			}
		}
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
