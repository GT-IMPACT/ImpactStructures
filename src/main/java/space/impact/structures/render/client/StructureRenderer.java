package space.impact.structures.render.client;

import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import space.impact.structures.Main;
import space.impact.structures.animation.RenderTransform;
import space.impact.structures.client.AngelicaCompat;
import space.impact.structures.render.BlockAppearance;
import space.impact.structures.render.BlockPosition;
import space.impact.structures.render.RenderPart;

/** Independent renderer; does not use hint particles, projector settings or channel configuration values. */
public final class StructureRenderer {

    public static final StructureRenderer INSTANCE = new StructureRenderer();
    private final List<RenderHandle> scenes = new ArrayList<>();
    private final RenderTransform transform = new RenderTransform();
    private final DoubleBuffer matrix = BufferUtils.createDoubleBuffer(16);
    private boolean installed;
    private boolean angelica;

    private StructureRenderer() {
    }

    public void install() {
        if (installed) {
            return;
        }
        installed = true;
        angelica = Loader.isModLoaded("angelica");
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);
        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(manager -> {
            for (RenderHandle scene : scenes) {
                scene.invalidateGeometry();
            }
        });
    }

    /** All calls, including handle mutations, must run on the client main thread. */
    public RenderHandle show(World world, BlockPosition origin, double startTick, RenderPart... parts) {
        if (!world.isRemote || world != Minecraft.getMinecraft().theWorld) {
            throw new IllegalArgumentException("Scenes belong to the current client world");
        }
        RenderHandle handle = new RenderHandle(world, Objects.requireNonNull(origin, "origin"), startTick, parts);
        scenes.add(handle);
        return handle;
    }

    @SubscribeEvent
    public void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        World current = Minecraft.getMinecraft().theWorld;
        for (RenderHandle scene : scenes) {
            if (!scene.isClosed() && (scene.world != current || !scene.sourcesValid())) {
                scene.close();
            }
        }
        Iterator<RenderHandle> iterator = scenes.iterator();
        while (iterator.hasNext()) {
            RenderHandle scene = iterator.next();
            if (scene.isClosed()) {
                dispose(scene);
                iterator.remove();
            }
        }
    }

    @SubscribeEvent
    public void unload(WorldEvent.Unload event) {
        if (event.world.isRemote) {
            for (RenderHandle scene : scenes) {
                if (scene.world == event.world) {
                    scene.close();
                }
            }
            HiddenBlocks.replace(null, Collections.emptyMap());
        }
    }

    void refreshHiddenBlocks() {
        World world = Minecraft.getMinecraft().theWorld;
        Map<BlockPosition, BlockAppearance> sources = new HashMap<>();
        for (RenderHandle scene : scenes) {
            if (scene.ready && !scene.isClosed() && scene.world == world) {
                for (RenderPart part : scene.parts) {
                    sources.putAll(part.hiddenSources);
                }
            }
        }
        HiddenBlocks.replace(world, sources);
    }

    @SubscribeEvent
    public void render(RenderWorldLastEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        Entity camera = minecraft.renderViewEntity;
        if (camera == null || scenes.isEmpty()) {
            return;
        }
        double cameraX = camera.lastTickPosX + (camera.posX - camera.lastTickPosX) * event.partialTicks;
        double cameraY = camera.lastTickPosY + (camera.posY - camera.lastTickPosY) * event.partialTicks;
        double cameraZ = camera.lastTickPosZ + (camera.posZ - camera.lastTickPosZ) * event.partialTicks;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();
        try {
            GL11.glTranslated(-cameraX, -cameraY, -cameraZ);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_CULL_FACE);
            minecraft.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
            minecraft.entityRenderer.enableLightmap(event.partialTicks);
            for (RenderHandle scene : scenes) {
                if (scene.isClosed() || scene.world != minecraft.theWorld) {
                    continue;
                }
                try {
                    draw(scene, event.partialTicks);
                } catch (RuntimeException exception) {
                    scene.close();
                    Main.LOGGER.error("Structure render failed; restored original blocks", exception);
                }
            }
        } finally {
            ForgeHooksClient.setRenderPass(-1);
            minecraft.entityRenderer.disableLightmap(event.partialTicks);
            GL11.glPopMatrix();
            GL11.glPopAttrib();
            GL11.glColor4f(1, 1, 1, 1);
        }
    }

    private void draw(RenderHandle scene, float partialTicks) {
        long tick = scene.world.getTotalWorldTime();
        // Poll the sampled light, but retain display lists while their inputs are unchanged.
        if (scene.geometryDirty || tick - scene.lightCheckTick >= 20 || tick < scene.lightCheckTick) {
            compile(scene);
            scene.geometryDirty = false;
            scene.lightCheckTick = tick;
            if (!scene.ready) {
                scene.ready = true;
                refreshHiddenBlocks();
            }
        }
        GL11.glPushMatrix();
        try {
            GL11.glTranslated(scene.origin.x, scene.origin.y, scene.origin.z);
            double seconds = scene.seconds(partialTicks);
            if (angelica) {
                for (BlockAppearance block : scene.appearances) {
                    for (int side = 0; side < 6; side++) {
                        AngelicaCompat.markTextureUsed(block.block.getIcon(side, block.metadata));
                    }
                }
            }
            for (int pass = 0; pass < 2; pass++) {
                if (pass == 0) {
                    GL11.glDisable(GL11.GL_BLEND);
                    GL11.glDepthMask(true);
                } else {
                    GL11.glEnable(GL11.GL_BLEND);
                    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                    GL11.glDepthMask(false);
                }
                for (int index = 0; index < scene.parts.size(); index++) {
                    RenderPart part = scene.parts.get(index);
                    GL11.glPushMatrix();
                    try {
                        transform.identity();
                        part.animation.apply(seconds, transform);
                        transform.writeTo(matrix);
                        GL11.glMultMatrix(matrix);
                        GL11.glCallList(scene.displayLists[index] + pass);
                    } finally {
                        GL11.glPopMatrix();
                    }
                }
            }
        } finally {
            GL11.glPopMatrix();
        }
    }

    private void compile(RenderHandle scene) {
        for (int index = 0; index < scene.parts.size(); index++) {
            RenderPart part = scene.parts.get(index);
            ModelBlockAccess previous = scene.blockAccess[index];
            if (!scene.geometryDirty && previous != null && !previous.hasLightingChanged()) {
                continue;
            }
            if (scene.displayLists[index] == 0) {
                scene.displayLists[index] = GLAllocation.generateDisplayLists(2);
            }
            ModelBlockAccess access = new ModelBlockAccess(part.model, scene.world, scene.origin);
            RenderBlocks renderer = new RenderBlocks(access);
            for (int pass = 0; pass < 2; pass++) {
                GL11.glNewList(scene.displayLists[index] + pass, GL11.GL_COMPILE);
                Tessellator tessellator = Tessellator.instance;
                tessellator.startDrawingQuads();
                try {
                    ForgeHooksClient.setRenderPass(pass);
                    for (Map.Entry<BlockPosition, BlockAppearance> entry : part.model.getBlocks().entrySet()) {
                        BlockPosition position = entry.getKey();
                        BlockAppearance block = entry.getValue();
                        if (block.block.canRenderInPass(pass)) {
                            renderer.renderBlockByRenderType(block.block, position.x, position.y, position.z);
                        }
                    }
                } finally {
                    tessellator.draw();
                    GL11.glEndList();
                }
            }
            scene.blockAccess[index] = access;
        }
    }

    private static void dispose(RenderHandle scene) {
        for (int index = 0; index < scene.displayLists.length; index++) {
            if (scene.displayLists[index] != 0) {
                GL11.glDeleteLists(scene.displayLists[index], 2);
                scene.displayLists[index] = 0;
            }
        }
    }
}
