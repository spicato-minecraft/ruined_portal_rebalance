package ruined_portal_rebalance;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.Optional;

/**
 * Seed-only nearest paired ruin for {@code /locate structure #ruined_portal_rebalance:paired_portals}.
 * Overworld: 8:1 of {@link NetherComplexGrid#netherLanding}.
 * Nether: that same landing (just outside the fortress/bastion wall).
 */
public final class PairedPortalLocator {
	public static final TagKey<Structure> PAIRED_PORTALS = TagKey.create(
			Registries.STRUCTURE,
			Identifier.fromNamespaceAndPath(RuinedPortalRebalance.MOD_ID, "paired_portals")
	);

	private PairedPortalLocator() {
	}

	public static boolean requestsPairedPortal(HolderSet<Structure> structures) {
		if (structures instanceof HolderSet.Named<Structure> named) {
			return PAIRED_PORTALS.equals(named.key());
		}
		for (Holder<Structure> holder : structures) {
			if (holder.is(PAIRED_PORTALS)) {
				return true;
			}
			if (holder.unwrapKey().map(key -> isPairedPortalId(key.identifier())).orElse(false)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isPairedPortalId(Identifier id) {
		return id != null
				&& RuinedPortalRebalance.MOD_ID.equals(id.getNamespace())
				&& id.getPath().startsWith("paired_portal");
	}

	public static Optional<Holder<Structure>> firstHolder(HolderSet<Structure> structures) {
		for (Holder<Structure> holder : structures) {
			return Optional.of(holder);
		}
		return Optional.empty();
	}

	public static Optional<Pair<BlockPos, Holder<Structure>>> find(
			ServerLevel level,
			HolderSet<Structure> structures,
			BlockPos origin,
			int searchRadiusChunks
	) {
		if (level.dimension() != Level.OVERWORLD && level.dimension() != Level.NETHER) {
			return Optional.empty();
		}
		Optional<Holder<Structure>> marker = firstHolder(structures);
		if (marker.isEmpty()) {
			return Optional.empty();
		}
		ChunkPos query = ChunkPos.containing(origin);
		// Vanilla /locate uses a 100-chunk spiral. Paired ruins follow nether_complex
		// spacing (27 nether chunks ≈ 216 overworld chunks at 8:1), so that cap often
		// misses the nearest pairing. Always search at least one full nether region
		// and return that seed landing even if it is farther than 100 chunks.
		int netherRadius = Math.max(
				effectiveNetherSearchRadius(level, searchRadiusChunks),
				NetherComplexGrid.SPACING
		);
		Optional<ChunkPos> netherStart = nearestNetherStartChunk(
				level.getSeed(),
				netherQueryChunk(level, query).x(),
				netherQueryChunk(level, query).z(),
				netherRadius
		);
		if (netherStart.isEmpty()) {
			return Optional.empty();
		}
		BlockPos pos = locatePos(level, netherStart.get(), origin.getY());
		return Optional.of(Pair.of(pos, marker.get()));
	}

	public static Optional<ChunkPos> nearestNetherStartChunk(
			long worldSeed,
			int queryNetherChunkX,
			int queryNetherChunkZ,
			int maxNetherChunkRadius
	) {
		int regionRadius = Math.max(1, Math.floorDiv(maxNetherChunkRadius, NetherComplexGrid.SPACING) + 2);
		int originRegionX = Math.floorDiv(queryNetherChunkX, NetherComplexGrid.SPACING);
		int originRegionZ = Math.floorDiv(queryNetherChunkZ, NetherComplexGrid.SPACING);
		ChunkPos best = null;
		long bestDist = Long.MAX_VALUE;
		for (int drx = -regionRadius; drx <= regionRadius; drx++) {
			for (int drz = -regionRadius; drz <= regionRadius; drz++) {
				int sampleX = (originRegionX + drx) * NetherComplexGrid.SPACING;
				int sampleZ = (originRegionZ + drz) * NetherComplexGrid.SPACING;
				ChunkPos start = NetherComplexGrid.startChunk(worldSeed, sampleX, sampleZ);
				long dist = chunkDistanceSq(queryNetherChunkX, queryNetherChunkZ, start.x(), start.z());
				if (chebyshev(queryNetherChunkX, queryNetherChunkZ, start.x(), start.z()) > maxNetherChunkRadius) {
					continue;
				}
				if (dist < bestDist) {
					bestDist = dist;
					best = start;
				}
			}
		}
		return Optional.ofNullable(best);
	}

	private static ChunkPos netherQueryChunk(ServerLevel level, ChunkPos query) {
		if (level.dimension() == Level.OVERWORLD) {
			return new ChunkPos(
					Math.floorDiv(query.x(), NetherComplexGrid.NETHER_CHUNKS_PER_OVERWORLD_REGION),
					Math.floorDiv(query.z(), NetherComplexGrid.NETHER_CHUNKS_PER_OVERWORLD_REGION)
			);
		}
		return query;
	}

	private static int effectiveNetherSearchRadius(ServerLevel level, int searchRadiusChunks) {
		if (level.dimension() == Level.OVERWORLD) {
			return Math.max(1, Math.floorDiv(searchRadiusChunks, NetherComplexGrid.NETHER_CHUNKS_PER_OVERWORLD_REGION) + 1);
		}
		return searchRadiusChunks;
	}

	private static BlockPos locatePos(ServerLevel level, ChunkPos netherStart, int y) {
		BlockPos nether = NetherComplexGrid.netherLanding(level.getSeed(), netherStart);
		if (level.dimension() == Level.OVERWORLD) {
			return new BlockPos(
					nether.getX() * NetherComplexGrid.NETHER_CHUNKS_PER_OVERWORLD_REGION,
					y,
					nether.getZ() * NetherComplexGrid.NETHER_CHUNKS_PER_OVERWORLD_REGION
			);
		}
		return new BlockPos(nether.getX(), y, nether.getZ());
	}

	private static long chunkDistance(ChunkPos a, ChunkPos b) {
		return chebyshev(a.x(), a.z(), b.x(), b.z());
	}

	private static long chebyshev(int ax, int az, int bx, int bz) {
		return Math.max(Math.abs((long) ax - bx), Math.abs((long) az - bz));
	}

	private static long chunkDistanceSq(int ax, int az, int bx, int bz) {
		long dx = (long) ax - bx;
		long dz = (long) az - bz;
		return dx * dx + dz * dz;
	}
}
