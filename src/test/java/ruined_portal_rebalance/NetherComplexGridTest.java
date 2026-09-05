package ruined_portal_rebalance;

import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NetherComplexGridTest {

	@BeforeAll
	static void bootstrapMinecraft() {
		MinecraftTestBootstrap.init();
	}

	@Test
	void modMainClassLoads() {
		assertNotNull(RuinedPortalRebalance.MOD_ID);
		assertEquals("ruined_portal_rebalance", RuinedPortalRebalance.MOD_ID);
	}

	@Test
	void startChunkIsIdempotentOnTheStart() {
		long seed = 42L;
		ChunkPos start = NetherComplexGrid.startChunk(seed, 0, 0);
		assertEquals(start, NetherComplexGrid.startChunk(seed, start.x, start.z));
		assertTrue(NetherComplexGrid.isStructureChunk(seed, start.x, start.z));
	}

	@Test
	void onlyStartChunkIsStructureChunk() {
		long seed = 12345L;
		ChunkPos start = NetherComplexGrid.startChunk(seed, 10, 10);
		assertTrue(NetherComplexGrid.isStructureChunk(seed, start.x, start.z));
		assertFalse(NetherComplexGrid.isStructureChunk(seed, start.x + 1, start.z));
	}

	@Test
	void overworldAnchorIsEightTimesNetherStart() {
		ChunkPos nether = new ChunkPos(3, -5);
		ChunkPos overworld = NetherComplexGrid.overworldAnchorChunk(nether);
		assertEquals(24, overworld.x);
		assertEquals(-40, overworld.z);
	}

	@Test
	void overworldAnchorMatchesStructureStarts() {
		long seed = 999L;
		ChunkPos netherStart = NetherComplexGrid.startChunk(seed, 0, 0);
		ChunkPos ow = NetherComplexGrid.overworldAnchorChunk(netherStart);
		assertTrue(NetherComplexGrid.isOverworldAnchorChunk(seed, ow.x, ow.z));
		assertFalse(NetherComplexGrid.isOverworldAnchorChunk(seed, ow.x + 1, ow.z));
	}
}
