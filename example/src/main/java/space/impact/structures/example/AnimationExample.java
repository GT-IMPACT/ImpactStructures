package space.impact.structures.example;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = AnimationExample.MOD_ID, name = "Impact Structures Animation Example", version = "1.0",
        dependencies = "required-after:impactapi;")
public final class AnimationExample {

    public static final String MOD_ID = "impactstructures_example";
    public static ElevatorControllerBlock controller;

    @SidedProxy(clientSide = "space.impact.structures.example.client.ExampleClientProxy",
            serverSide = "space.impact.structures.example.ExampleProxy")
    public static ExampleProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        controller = new ElevatorControllerBlock();
        GameRegistry.registerBlock(controller, "elevator_controller");
        GameRegistry.registerTileEntity(ElevatorController.class, MOD_ID + ":elevator");
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new ElevatorCommand());
    }
}
