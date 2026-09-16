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
		long seed = server.getSeed();
		ChunkPos startChunk = start.getChunkPos();
		int dir = NetherComplexGrid.landingDirectionIndex(seed, startChunk);
		BlockPos seedLanding = NetherComplexGrid.netherLanding(seed, startChunk);
		BlockPos floor = ExteriorPortalSiting.findFloor(
				level,
				box,
				seedLanding,
				NetherComplexGrid.dirX(dir),
				NetherComplexGrid.dirZ(dir)
		);
		IncompletePortalPlacer.place(level, floor, random);
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
