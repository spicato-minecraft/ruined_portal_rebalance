package ruined_portal_rebalance;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

import java.util.Optional;

/**
 * Structure-set placement: Overworld chunks that are the 8:1 projection of a nether-complex start.
 * Datapack structure sets list surface-only {@code ruined_portal_rebalance:paired_portal*} structures; this type supplies where.
 */
public final class OverworldNetherComplexAnchorPlacement extends StructurePlacement {
	public static final MapCodec<OverworldNetherComplexAnchorPlacement> CODEC = RecordCodecBuilder.mapCodec(
			instance -> placementCodec(instance).apply(instance, OverworldNetherComplexAnchorPlacement::new)
	);

	public static StructurePlacementType<OverworldNetherComplexAnchorPlacement> TYPE;

	private OverworldNetherComplexAnchorPlacement(
			Vec3i locateOffset,
			FrequencyReductionMethod frequencyReductionMethod,
			float frequency,
			int salt,
			Optional<ExclusionZone> exclusionZone
	) {
		super(locateOffset, frequencyReductionMethod, frequency, salt, exclusionZone);
	}

	public static void register() {
		if (TYPE != null) {
			return;
		}
		TYPE = Registry.register(
				BuiltInRegistries.STRUCTURE_PLACEMENT,
				Identifier.fromNamespaceAndPath(RuinedPortalRebalance.MOD_ID, "overworld_nether_complex_anchor"),
				() -> CODEC
		);
	}

	@Override
	protected boolean isPlacementChunk(ChunkGeneratorStructureState state, int chunkX, int chunkZ) {
		return NetherComplexGrid.isOverworldAnchorChunk(state.getLevelSeed(), chunkX, chunkZ);
	}

	@Override
	public StructurePlacementType<?> type() {
		return TYPE;
	}
}
