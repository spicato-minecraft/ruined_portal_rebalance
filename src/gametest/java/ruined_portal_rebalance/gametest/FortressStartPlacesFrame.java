package ruined_portal_rebalance.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import ruined_portal_rebalance.ExteriorPortalSiting;
import ruined_portal_rebalance.NetherComplexGrid;

import java.util.Comparator;
import java.util.List;

/**
 * Places a real fortress start through {@link StructureStart#placeInChunk}.
 * The incomplete frame is added by {@code StructureStartMixin}; calling the placer directly would hide a missing mixin.
 */
public class FortressStartPlacesFrame {

	@GameTest(maxTicks = 200)
	public void placesIncompleteFrameOutsideFootprint(GameTestHelper context) {
		ServerLevel nether = context.getLevel().getServer().getLevel(Level.NETHER);
		if (nether == null) {
			context.fail("Nether dimension is not loaded");
			return;
		}
		StructureStart start = smallestPlaceableFortress(nether);
		if (!start.isValid()) {
			context.fail("No fortress structure start could be generated");
			return;
		}
		BoundingBox footprint = start.getBoundingBox();
		BlockPos floor = exteriorFloor(nether, start);
		ChunkGenerator generator = nether.getChunkSource().getGenerator();
		ChunkPos chunkPos = start.getChunkPos();
		preload(nether, chunkPos, floor);
		BoundingBox chunkBox = new BoundingBox(
				chunkPos.getMinBlockX(),
				nether.getMinY() + 1,
				chunkPos.getMinBlockZ(),
				chunkPos.getMinBlockX() + 15,
				nether.getMaxY(),
				chunkPos.getMinBlockZ() + 15
		);
		start.placeInChunk(
				nether,
				nether.structureManager(),
				generator,
				RandomSource.create(nether.getSeed()),
				chunkBox,
				chunkPos
		);
		assertIncompleteFrame(context, nether, footprint, floor);
		context.succeed();
	}

	private static StructureStart smallestPlaceableFortress(ServerLevel nether) {
		Holder<Structure> fortress = nether.registryAccess()
				.lookupOrThrow(Registries.STRUCTURE)
				.getOrThrow(BuiltinStructures.FORTRESS);
		ChunkGenerator generator = nether.getChunkSource().getGenerator();
		StructureStart smallest = StructureStart.INVALID_START;
		int smallestSpan = Integer.MAX_VALUE;
		for (ChunkPos chunkPos : nearbyChunks()) {
			StructureStart generated = fortress.value().generate(
					fortress,
					Level.NETHER,
					nether.registryAccess(),
					generator,
					generator.getBiomeSource(),
					nether.getChunkSource().randomState(),
					nether.getStructureManager(),
					nether.getSeed(),
					chunkPos,
					0,
					nether,
					biome -> true
			);
			if (!generated.isValid() || generated.getPieces().isEmpty()) {
				continue;
			}
			if (sitesOutside(nether, generated)) {
				return generated;
			}
			StructureStart startPiece = smallestPieceThatSites(nether, generated);
			if (startPiece.isValid()) {
				int span = horizontalSpan(startPiece.getBoundingBox());
				if (span < smallestSpan) {
					smallestSpan = span;
					smallest = startPiece;
				}
			}
		}
		return smallest;
	}

	private static StructureStart smallestPieceThatSites(ServerLevel nether, StructureStart generated) {
		List<StructurePiece> pieces = generated.getPieces().stream()
				.sorted(Comparator.comparingInt(candidate -> horizontalSpan(candidate.getBoundingBox())))
				.toList();
		for (StructurePiece piece : pieces) {
			StructureStart single = new StructureStart(
					generated.getStructure(),
					generated.getChunkPos(),
					generated.getReferences(),
					new PiecesContainer(List.of(piece))
			);
			if (sitesOutside(nether, single)) {
				return single;
			}
		}
		return StructureStart.INVALID_START;
	}

	private static boolean sitesOutside(ServerLevel nether, StructureStart start) {
		BlockPos floor = exteriorFloor(nether, start);
		BoundingBox box = start.getBoundingBox();
		int outside = ExteriorPortalSiting.distanceOutside(box, floor.getX(), floor.getZ());
		return outside > 0
				&& outside <= ExteriorPortalSiting.MAX_OUTSIDE
				&& !ExteriorPortalSiting.frameOverlapsStructure(box, floor.getX(), floor.getZ());
	}

	private static BlockPos exteriorFloor(ServerLevel nether, StructureStart start) {
		int direction = NetherComplexGrid.landingDirectionIndex(nether.getSeed(), start.getChunkPos());
		BlockPos landing = NetherComplexGrid.netherLanding(nether.getSeed(), start.getChunkPos());
		BlockPos snapped = ExteriorPortalSiting.snapXZ(
				start.getBoundingBox(),
				landing,
				NetherComplexGrid.dirX(direction),
				NetherComplexGrid.dirZ(direction)
		);
		return new BlockPos(snapped.getX(), 0, snapped.getZ());
	}

	private static void preload(ServerLevel nether, ChunkPos startChunk, BlockPos floor) {
		nether.getChunk(startChunk.x(), startChunk.z());
		ChunkPos floorChunk = ChunkPos.containing(new BlockPos(floor.getX(), 64, floor.getZ()));
		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				nether.getChunk(floorChunk.x() + dx, floorChunk.z() + dz);
			}
		}
	}

	private static void assertIncompleteFrame(GameTestHelper context, ServerLevel nether, BoundingBox footprint, BlockPos floor) {
		BlockPos origin = null;
		for (int y = nether.getMinY(); y < nether.getMaxY() - 5; y++) {
			BlockPos chestPos = new BlockPos(floor.getX() + 4, y, floor.getZ() + 1);
			if (!(nether.getBlockEntity(chestPos) instanceof ChestBlockEntity)) {
				continue;
			}
			BlockPos candidate = chestPos.offset(-4, 0, -1);
			if (isIncompleteFrame(nether, candidate)) {
				origin = candidate;
				break;
			}
		}
		if (origin == null) {
			context.fail("placeInChunk did not leave an incomplete frame with a chest");
			return;
		}
		int outside = ExteriorPortalSiting.distanceOutside(footprint, origin.getX(), origin.getZ());
		context.assertTrue(outside > 0 && outside <= ExteriorPortalSiting.MAX_OUTSIDE,
				"frame was " + outside + " blocks outside the footprint at " + origin);
		context.assertTrue(!ExteriorPortalSiting.frameOverlapsStructure(footprint, origin.getX(), origin.getZ()),
				"frame overlapped the fortress footprint");
	}

	/**
	 * Same gaps as {@code IncompletePortalPlacer}: corners of the top bar and one side post are missing.
	 * Interior is empty. No portal block and no fire.
	 */
	private static boolean isIncompleteFrame(ServerLevel level, BlockPos origin) {
		boolean hasObsidian = false;
		for (int dx = 0; dx < 4; dx++) {
			for (int dy = 0; dy < 5; dy++) {
				BlockState state = level.getBlockState(origin.offset(dx, dy, 0));
				if (state.is(Blocks.NETHER_PORTAL) || state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE)) {
					return false;
				}
				boolean frame = isFrameCell(dx, dy);
				boolean missing = isMissingCell(dx, dy);
				if (missing) {
					if (!state.isAir()) {
						return false;
					}
					continue;
				}
				if (frame) {
					if (!state.is(Blocks.OBSIDIAN) && !state.is(Blocks.CRYING_OBSIDIAN)) {
						return false;
					}
					hasObsidian = true;
					continue;
				}
				if (dx >= 1 && dx <= 2 && dy >= 1 && dy <= 3 && !state.isAir()) {
					return false;
				}
			}
		}
		return hasObsidian;
	}

	private static boolean isFrameCell(int dx, int dy) {
		boolean vertical = (dx == 0 || dx == 3) && dy >= 0 && dy <= 4;
		boolean horizontal = (dy == 0 || dy == 4) && dx >= 0 && dx <= 3;
		return vertical || horizontal;
	}

	private static boolean isMissingCell(int dx, int dy) {
		return (dx == 0 && dy == 4) || (dx == 3 && dy == 4) || (dx == 0 && dy == 2);
	}

	private static int horizontalSpan(BoundingBox box) {
		return box.getXSpan() * box.getZSpan();
	}

	private static List<ChunkPos> nearbyChunks() {
		List<ChunkPos> chunks = new java.util.ArrayList<>();
		chunks.add(new ChunkPos(0, 0));
		for (int radius = 1; radius <= 3; radius++) {
			for (int dx = -radius; dx <= radius; dx++) {
				chunks.add(new ChunkPos(dx, radius));
				chunks.add(new ChunkPos(dx, -radius));
			}
			for (int dz = -radius + 1; dz <= radius - 1; dz++) {
				chunks.add(new ChunkPos(radius, dz));
				chunks.add(new ChunkPos(-radius, dz));
			}
		}
		return chunks;
	}
}
