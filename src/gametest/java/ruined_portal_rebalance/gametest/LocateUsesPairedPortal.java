package ruined_portal_rebalance.gametest;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.Structure;
import ruined_portal_rebalance.PairedPortalLocator;

import java.util.Optional;

/**
 * {@code /locate} for the paired-portal tag must use the seed landing.
 * Vanilla locate does not return that block, so removing {@code ChunkGeneratorMixin} fails this test.
 */
public class LocateUsesPairedPortal {

	private static final int SEARCH_RADIUS_CHUNKS = 100;

	@GameTest(maxTicks = 20)
	public void nearestPairedPortalMatchesLocator(GameTestHelper context) {
		ServerLevel level = context.getLevel();
		HolderSet.Named<Structure> paired = level.registryAccess()
				.lookupOrThrow(Registries.STRUCTURE)
				.getOrThrow(PairedPortalLocator.PAIRED_PORTALS);
		BlockPos origin = new BlockPos(0, 64, 0);
		ChunkGenerator generator = level.getChunkSource().getGenerator();
		Pair<BlockPos, Holder<Structure>> found = generator.findNearestMapStructure(
				level,
				paired,
				origin,
				SEARCH_RADIUS_CHUNKS,
				false
		);
		if (found == null) {
			context.fail("findNearestMapStructure returned nothing for #ruined_portal_rebalance:paired_portals");
			return;
		}
		Optional<Pair<BlockPos, Holder<Structure>>> expected = PairedPortalLocator.find(
				level,
				paired,
				origin,
				SEARCH_RADIUS_CHUNKS
		);
		if (expected.isEmpty()) {
			context.fail("PairedPortalLocator found no landing for this seed");
			return;
		}
		context.assertValueEqual(found.getFirst(), expected.get().getFirst(), "paired portal position");
		Identifier id = found.getSecond().unwrapKey().map(key -> key.identifier()).orElse(null);
		context.assertTrue(PairedPortalLocator.isPairedPortalId(id), "located structure was " + id);
		context.succeed();
	}
}
