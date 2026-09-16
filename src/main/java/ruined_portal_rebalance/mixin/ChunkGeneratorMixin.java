package ruined_portal_rebalance.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ruined_portal_rebalance.PairedPortalLocator;

import java.util.Optional;

@Mixin(ChunkGenerator.class)
public class ChunkGeneratorMixin {

	@Inject(method = "findNearestMapStructure", at = @At("HEAD"), cancellable = true)
	private void ruinedPortalRebalance$locatePairedPortal(
			ServerLevel level,
			HolderSet<Structure> structures,
			BlockPos origin,
			int searchRadius,
			boolean skipKnownStructures,
			CallbackInfoReturnable<Pair<BlockPos, Holder<Structure>>> cir
	) {
		if (!PairedPortalLocator.requestsPairedPortalTag(structures)) {
			return;
		}
		Optional<Pair<BlockPos, Holder<Structure>>> paired =
				PairedPortalLocator.find(level, structures, origin, searchRadius);
		cir.setReturnValue(paired.orElse(null));
	}
}
