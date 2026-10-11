package ruined_portal_rebalance;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
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
		assertEquals(start, NetherComplexGrid.startChunk(seed, start.x(), start.z()));
		assertTrue(NetherComplexGrid.isStructureChunk(seed, start.x(), start.z()));
	}

	@Test
	void onlyStartChunkIsStructureChunk() {
		long seed = 12345L;
		ChunkPos start = NetherComplexGrid.startChunk(seed, 10, 10);
		assertTrue(NetherComplexGrid.isStructureChunk(seed, start.x(), start.z()));
		assertFalse(NetherComplexGrid.isStructureChunk(seed, start.x() + 1, start.z()));
	}

	@Test
	void overworldAnchorIsEightTimesNetherLanding() {
		long seed = 42L;
		ChunkPos nether = new ChunkPos(3, -5);
		BlockPos landing = NetherComplexGrid.netherLanding(seed, nether);
		ChunkPos overworld = NetherComplexGrid.overworldAnchorChunk(seed, nether);
		assertEquals(ChunkPos.containing(new BlockPos(
				landing.getX() * NetherComplexGrid.NETHER_CHUNKS_PER_OVERWORLD_REGION,
				0,
				landing.getZ() * NetherComplexGrid.NETHER_CHUNKS_PER_OVERWORLD_REGION
		)), overworld);
	}

	@Test
	void overworldAnchorMatchesStructureStarts() {
		long seed = 999L;
		ChunkPos netherStart = NetherComplexGrid.startChunk(seed, 0, 0);
		ChunkPos ow = NetherComplexGrid.overworldAnchorChunk(seed, netherStart);
		assertTrue(NetherComplexGrid.isOverworldAnchorChunk(seed, ow.x(), ow.z()));
		assertFalse(NetherComplexGrid.isOverworldAnchorChunk(seed, ow.x() + 1, ow.z()));
	}

	@Test
	void overworldPlacementTypeRegisters() {
		OverworldNetherComplexAnchorPlacement.register();
		assertSame(
				OverworldNetherComplexAnchorPlacement.CODEC,
				BuiltInRegistries.STRUCTURE_PLACEMENT.getValue(
						Identifier.fromNamespaceAndPath(RuinedPortalRebalance.MOD_ID, "overworld_nether_complex_anchor"))
		);
	}
}
