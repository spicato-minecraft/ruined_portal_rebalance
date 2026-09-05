package ruined_portal_rebalance;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.structures.NetherFortressStructure;

import java.util.Optional;

public final class PortalPairing {
	private static final Identifier BASTION = Identifier.fromNamespaceAndPath("minecraft", "bastion_remnant");
	private static final Identifier FORTRESS = Identifier.fromNamespaceAndPath("minecraft", "fortress");

	private PortalPairing() {
	}

	public static void afterNetherStructurePlaced(
			WorldGenLevel level,
			StructureStart start,
			ChunkPos placingChunk,
			RandomSource random
	) {
		if (!(level instanceof ServerLevel server) || server.dimension() != Level.NETHER) {
			return;
		}
		if (!placingChunk.equals(start.getChunkPos())) {
			return;
		}
		if (!isNetherComplex(server, start.getStructure())) {
			return;
		}
		BoundingBox box = start.getBoundingBox();
		BlockPos prefer = placingChunk.getMiddleBlockPosition(box.minY() + 4);
		BlockPos door = FrontDoorFinder.find(level, box, prefer);
		if (door == null) {
			RuinedPortalRebalance.LOGGER.debug("No front-door stand in {} at {}", start.getStructure(), placingChunk);
			return;
		}
		IncompletePortalPlacer.place(level, door, random);
	}

	public static void tryPlaceOverworldAnchor(ServerLevel level, ChunkPos chunk, ChunkGenerator generator) {
		if (level.dimension() != Level.OVERWORLD) {
			return;
		}
		long seed = level.getSeed();
		if (!NetherComplexGrid.isOverworldAnchorChunk(seed, chunk.x, chunk.z)) {
			return;
		}
		int x = chunk.getMiddleBlockX();
		int z = chunk.getMiddleBlockZ();
		int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
		if (y < level.getMinY() + 4) {
			y = generator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, level, level.getChunkSource().randomState());
		}
		BlockPos floor = new BlockPos(x, y - 1, z);
		IncompletePortalPlacer.place(level, floor, RandomSource.create(seed ^ chunk.toLong()));
	}

	static boolean isNetherComplex(ServerLevel level, Structure structure) {
		Optional<ResourceKey<Structure>> key = level.registryAccess()
				.lookupOrThrow(Registries.STRUCTURE)
				.getResourceKey(structure);
		if (key.isPresent()) {
			Identifier id = key.get().identifier();
			return id.equals(BASTION) || id.equals(FORTRESS);
		}
		return structure instanceof NetherFortressStructure;
	}
}
