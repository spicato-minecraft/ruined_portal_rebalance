package ruined_portal_rebalance.mixin;

import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StaticCache2D;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatusTasks;
import net.minecraft.world.level.chunk.status.ChunkStep;
import net.minecraft.world.level.chunk.status.WorldGenContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ruined_portal_rebalance.PortalPairing;
import ruined_portal_rebalance.RuinedPortalRebalance;

import java.util.concurrent.CompletableFuture;

@Mixin(ChunkStatusTasks.class)
public class ChunkStatusTasksMixin {

	@Inject(method = "generateFeatures", at = @At("RETURN"))
	private static void ruinedPortalRebalance$afterFeatures(
			WorldGenContext context,
			ChunkStep step,
			StaticCache2D<GenerationChunkHolder> cache,
			ChunkAccess chunk,
			CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir
	) {
		try {
			ServerLevel level = context.level();
			PortalPairing.tryPlaceOverworldAnchor(level, chunk.getPos(), context.generator());
		} catch (RuntimeException e) {
			RuinedPortalRebalance.LOGGER.error("Overworld paired portal failed in chunk {}", chunk.getPos(), e);
		}
	}
}
