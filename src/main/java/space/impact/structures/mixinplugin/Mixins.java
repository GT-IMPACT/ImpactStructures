package space.impact.structures.mixinplugin;

import com.gtnewhorizon.gtnhmixins.builders.IBaseTransformer.Phase;
import com.gtnewhorizon.gtnhmixins.builders.IMixins;
import com.gtnewhorizon.gtnhmixins.builders.MixinBuilder;

/** Same loader/registry arrangement as Impact-Core, with vanilla/Forge targets in EARLY. */
public enum Mixins implements IMixins {
    BLOCK_CHANGES(new MixinBuilder("Notify structure listeners of block changes")
            .setPhase(Phase.EARLY)
            .addCommonMixins("blockChangeNotifier.MixinForgeHooks", "blockChangeNotifier.MixinWorld")),
    CHANNEL_PICK(new MixinBuilder("Select projector channels from inventory")
            .setPhase(Phase.EARLY)
            .addClientMixins("MixinGuiContainer_PickChannel"));

    private final MixinBuilder builder;
    Mixins(MixinBuilder builder) { this.builder = builder; }
    @Override
    public MixinBuilder getBuilder() { return builder; }
}
