package ruined_portal_rebalance.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.BuiltinStructureSets;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

/**
 * Reads the structure sets the running world actually loaded.
 * A missing datapack leaves vanilla frequency and weights, and this test fails.
 */
public class DatapackRemovesScatter {

	@GameTest(maxTicks = 20)
	public void readsLoadedStructureSets(GameTestHelper context) {
		Registry<StructureSet> sets = context.getLevel().registryAccess().lookupOrThrow(Registries.STRUCTURE_SET);
		StructureSet ruined = sets.getValueOrThrow(BuiltinStructureSets.RUINED_PORTALS);
		float frequency = frequency(ruined.placement());
		context.assertTrue(frequency == 0.0F, "ruined_portals frequency was " + frequency);

		StructureSet complexes = sets.getValueOrThrow(BuiltinStructureSets.NETHER_COMPLEXES);
		int bastionWeight = -1;
		int fortressWeight = -1;
		for (StructureSet.StructureSelectionEntry entry : complexes.structures()) {
			ResourceKey<Structure> key = entry.structure().unwrapKey().orElse(null);
			if (BuiltinStructures.BASTION_REMNANT.equals(key)) {
				bastionWeight = entry.weight();
			} else if (BuiltinStructures.FORTRESS.equals(key)) {
				fortressWeight = entry.weight();
			}
		}
		context.assertValueEqual(bastionWeight, 4, "bastion_remnant weight");
		context.assertValueEqual(fortressWeight, 1, "fortress weight");
		context.succeed();
	}

	private static float frequency(StructurePlacement placement) {
		Tag encoded = StructurePlacement.CODEC.encodeStart(NbtOps.INSTANCE, placement).getOrThrow();
		if (!(encoded instanceof CompoundTag tag)) {
			throw new IllegalStateException("structure placement did not encode as a compound");
		}
		return tag.getFloatOr("frequency", -1.0F);
	}
}
