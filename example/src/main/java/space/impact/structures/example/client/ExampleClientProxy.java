package space.impact.structures.example.client;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.UnaryOperator;

import net.minecraft.init.Blocks;
import space.impact.structures.alignment.enumerable.ExtendedFacing;
import space.impact.structures.animation.Animations;
import space.impact.structures.animation.Vector3;
import space.impact.structures.example.DysonBlueprint;
import space.impact.structures.example.DysonController;
import space.impact.structures.example.DysonGeometry;
import space.impact.structures.example.ElevatorBlueprint;
import space.impact.structures.example.ElevatorController;
import space.impact.structures.example.ElevatorMotion;
import space.impact.structures.example.ExampleProxy;
import space.impact.structures.render.BlockModel;
import space.impact.structures.render.BlockPosition;
import space.impact.structures.render.RenderPart;
import space.impact.structures.render.StructureRenderBinding;
import space.impact.structures.render.client.RenderHandle;
import space.impact.structures.render.client.StructureRenderer;

public final class ExampleClientProxy extends ExampleProxy {

    private final Map<ElevatorController, RenderHandle> handles = new WeakHashMap<>();
    private final Map<DysonController, RenderHandle> dysonHandles = new WeakHashMap<>();
    private BlockModel collar;

    @Override
    public void update(ElevatorController controller) {
        RenderHandle handle = handles.get(controller);
        if (!controller.isRunning()) {
            remove(controller);
            return;
        }
        if (handle != null && !handle.isClosed()) {
            handle.setStartTick(controller.getStartTick());
            return;
        }
        // Neighbouring chunks may arrive after the controller packet. Retry without hiding an incomplete model.
        BlockPosition origin = controller.getStructureOrigin();
        if (controller.getWorldObj().getTotalWorldTime() % 10 != 0
                || !controller.getWorldObj().checkChunksExist(origin.x - 9, origin.y,
                        origin.z - 9, origin.x + 9, origin.y + 64, origin.z + 9)
                || !ElevatorBlueprint.check(controller)) {
            return;
        }
        StructureRenderBinding carriage = StructureRenderBinding.capture(
                ElevatorBlueprint.STRUCTURE, "main", ElevatorBlueprint.CARRIAGE,
                controller.getWorldObj(), origin, ExtendedFacing.DEFAULT, ElevatorBlueprint.OFFSET,
                UnaryOperator.identity());
        RenderPart movingCarriage = new RenderPart(carriage,
                (seconds, transform) -> transform.translate(0, ElevatorMotion.height(seconds), 0), true);
        // This model has no structural channel and does not correspond to any placed blocks.
        RenderPart freeCollar = new RenderPart(collar(),
                Animations.spin(18, Vector3.UP, new Vector3(0.5, 24.5, 0.5)));
        handles.put(controller, StructureRenderer.INSTANCE.show(controller.getWorldObj(), origin,
                controller.getStartTick(), movingCarriage, freeCollar));
    }

    private BlockModel collar() {
        if (collar == null) {
            BlockModel.Builder builder = BlockModel.builder();
            for (int x = -6; x <= 6; x++) {
                for (int z = -6; z <= 6; z++) {
                    int radius = x * x + z * z;
                    if (radius >= 30 && radius <= 42) {
                        builder.block(x, 24, z, (x + z) % 3 == 0 ? Blocks.glowstone : Blocks.gold_block, 0);
                    }
                }
            }
            collar = builder.build();
        }
        return collar;
    }

    @Override
    public void update(DysonController controller) {
        RenderHandle handle = dysonHandles.get(controller);
        if (!controller.isRunning()) {
            remove(controller);
            return;
        }
        if (handle != null && !handle.isClosed()) {
            handle.setStartTick(controller.getStartTick());
            return;
        }
        if (controller.getWorldObj().getTotalWorldTime() % 20 != 0 || !DysonBlueprint.check(controller)) {
            return;
        }
        BlockPosition origin = controller.getStructureOrigin();
        RenderPart[] parts = new RenderPart[DysonGeometry.LAYERS];
        for (int layer = 0; layer < parts.length; layer++) {
            StructureRenderBinding binding = StructureRenderBinding.capture(
                    DysonBlueprint.STRUCTURE,
                    "main",
                    DysonBlueprint.channel(layer),
                    controller.getWorldObj(),
                    origin,
                    ExtendedFacing.DEFAULT,
                    DysonBlueprint.OFFSET,
                    UnaryOperator.identity()
            );
            parts[layer] = new RenderPart(binding, DysonGeometry.animation(layer), true);
        }
        dysonHandles.put(controller, StructureRenderer.INSTANCE.show(
                controller.getWorldObj(),
                origin,
                controller.getStartTick(),
                parts
        ));
    }

    @Override
    public void remove(DysonController controller) {
        RenderHandle handle = dysonHandles.remove(controller);
        if (handle != null) {
            handle.close();
        }
    }

    @Override
    public void remove(ElevatorController controller) {
        RenderHandle handle = handles.remove(controller);
        if (handle != null) {
            handle.close();
        }
    }
}
