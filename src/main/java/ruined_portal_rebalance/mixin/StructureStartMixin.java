package ruined_portal_rebalance.mixin;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ruined_portal_rebalance.PortalPairing;
import ruined_portal_rebalance.RuinedPortalRebalance;

@Mixin(StructureStart.class)
public class StructureStartMixin {

	@Inject(method = "placeInChunk", at = @At("RETURN"))
	private void ruinedPortalRebalance$afterPlace(
			WorldGenLevel level,
			StructureManager structureManager,
			ChunkGenerator generator,
			RandomSource random,
			BoundingBox chunkBox,
			ChunkPos chunkPos,
			CallbackInfo ci
	) {
		try {
			PortalPairing.afterNetherStructurePlaced(level, (StructureStart) (Object) this, chunkPos, random);
		} catch (RuntimeException e) {
			RuinedPortalRebalance.LOGGER.error("Nether exterior portal failed in chunk {}", chunkPos, e);
		}
	}
}
