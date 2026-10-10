package ruined_portal_rebalance.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

/**
 * Loads the classes named by {@code @Mixin} so a missing target fails here.
 * Binary names match {@code StructureStartMixin} and {@code ChunkGeneratorMixin}.
 */
public class ServerMixinApplySweep {

	@GameTest(maxTicks = 20)
	public void loadsMixinTargets(GameTestHelper context) {
		// @Mixin(StructureStart.class) on StructureStartMixin
		load(context, "net.minecraft.world.level.levelgen.structure.StructureStart");
		// @Mixin(ChunkGenerator.class) on ChunkGeneratorMixin
		load(context, "net.minecraft.world.level.chunk.ChunkGenerator");
		context.succeed();
	}

	private static void load(GameTestHelper context, String binaryName) {
		try {
			Class.forName(binaryName);
		} catch (ClassNotFoundException e) {
			context.fail("Mixin target failed to load: " + binaryName);
		}
	}
}
