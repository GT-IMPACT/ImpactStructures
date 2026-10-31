package space.impact.api;

import static space.impact.api.ImpactStructures.CHANNEL_SHOW_ERROR;
import static space.impact.api.ImpactStructures.MOD_ID;
import static space.impact.impactapi.BuildConfigKt.MODNAME;
import static space.impact.impactapi.BuildConfigKt.VERSION;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import space.impact.api.block.BlockHint;
import space.impact.api.block.ConstructionPlaceholderBlock;
import space.impact.api.command.CommandConfigureChannels;
import space.impact.api.command.CommandRegistryDebug;
import space.impact.api.item.ItemBlockHint;
import space.impact.api.item.ItemConstructableTrigger;
import space.impact.api.item.ItemDebugConstructableTrigger;
import space.impact.api.item.ItemFrontRotationTool;
import space.impact.api.net.AlignmentMessage;
import space.impact.api.net.ErrorHintParticleMessage;
import space.impact.api.net.RegistryOrderSyncMessage;
import space.impact.api.net.SetChannelDataMessage;
import space.impact.api.net.UpdateHintParticleMessage;
import space.impact.api.net.HintMarkerMessage;
import space.impact.api.net.ServerTasks;
import space.impact.api.util.InventoryUtility;
import space.impact.api.util.XSTR;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.MinecraftForge;

/**
 * This class does not contain a stable API. Refrain from using this class.
 */
@Mod(
        modid = MOD_ID,
        name = MODNAME,
        version = VERSION,
        dependencies = "required-after:Forge@[10.13.4.1614,);required-after:unimixins;",
        guiFactory = "space.impact.api.GuiFactory")
public class Main {

    private static final String STRUCTURECOMPAT_MODID = "structurecompat";
    public static boolean DEBUG_MODE;
    public static boolean PANIC_MODE = Boolean.getBoolean("impactapi.panic");
    public static final Logger LOGGER = LogManager.getLogger("Impact Structures");

    @SidedProxy(
            serverSide = "space.impact.api.CommonProxy",
            clientSide = "space.impact.api.ClientProxy")
    static CommonProxy proxy;

    static final SimpleNetworkWrapper net = NetworkRegistry.INSTANCE.newSimpleChannel(MOD_ID);

    static {
        net.registerMessage(
                AlignmentMessage.ServerHandler.class,
                AlignmentMessage.AlignmentQuery.class,
                0,
                Side.SERVER);
        net.registerMessage(AlignmentMessage.ClientHandler.class, AlignmentMessage.AlignmentData.class, 1, Side.CLIENT);
        net.registerMessage(UpdateHintParticleMessage.Handler.class, UpdateHintParticleMessage.class, 2, Side.CLIENT);
        net.registerMessage(SetChannelDataMessage.Handler.class, SetChannelDataMessage.class, 3, Side.SERVER);
        net.registerMessage(ErrorHintParticleMessage.Handler.class, ErrorHintParticleMessage.class, 4, Side.CLIENT);
        net.registerMessage(RegistryOrderSyncMessage.Handler.class, RegistryOrderSyncMessage.class, 5, Side.SERVER);
        net.registerMessage(HintMarkerMessage.Handler.class, HintMarkerMessage.class, 6, Side.CLIENT);

        try {
            DEBUG_MODE = Boolean.parseBoolean(System.getProperty("impactapi.debug",
                    String.valueOf(Launch.blackboard.get("fml.deobfuscatedEnvironment"))));
        } catch (IllegalArgumentException | NullPointerException e) {
            // turn on debug by default in dev mode
            // this will be overridden if above property is present and set to false
            DEBUG_MODE = (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
        }
    }

    public static final XSTR RANDOM = new XSTR();

    @Mod.Instance
    static Main INSTANCE;
    @Mod.Instance(STRUCTURECOMPAT_MODID)
    static Object COMPAT;

    static Block blockHint;
    public static Block constructionPlaceholder;
    static Item itemBlockHint;
    static Item itemFrontRotationTool;
    static Item itemConstructableTrigger;
    static Item itemDebugConstructableTrigger;
    public static final CreativeTabs creativeTab = new CreativeTabs("impactapi") {

        @Override
        @SideOnly(Side.CLIENT)
        public Item getTabIconItem() {
            return ImpactStructures.getItemBlockHint();
        }
    };

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        ConfigurationHandler.INSTANCE.init(e.getSuggestedConfigurationFile());
        GameRegistry.registerBlock(blockHint = new BlockHint(), ItemBlockHint.class, "blockhint");
        GameRegistry.registerBlock(constructionPlaceholder = new ConstructionPlaceholderBlock(),
                "construction_placeholder");
        itemBlockHint = ItemBlock.getItemFromBlock(ImpactStructures.getBlockHint());
        GameRegistry.registerItem(
                itemFrontRotationTool = new ItemFrontRotationTool(),
                itemFrontRotationTool.getUnlocalizedName());
        GameRegistry.registerItem(
                itemConstructableTrigger = new ItemConstructableTrigger(),
                itemConstructableTrigger.getUnlocalizedName());
        GameRegistry.registerItem(
                itemDebugConstructableTrigger = new ItemDebugConstructableTrigger(),
                itemDebugConstructableTrigger.getUnlocalizedName());
        proxy.preInit(e);
        NetworkRegistry.INSTANCE.registerGuiHandler(instance(), new GuiHandler());

        SortedRegistry.initializeLifecycle();
        InventoryUtility.init();
        MinecraftForge.EVENT_BUS.register(new ItemTooltipRegistry());
        FMLCommonHandler.instance().bus().register(new ServerTasks());

        ChannelDescription.set(CHANNEL_SHOW_ERROR, MOD_ID, "channels.impactapi.show_errors");

        DevelopHelper.onPreInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        proxy.init(e);
    }

    @Mod.EventHandler
    public void loadComplete(FMLLoadCompleteEvent e) {
        proxy.loadComplete(e);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent e) {
        e.registerServerCommand(new CommandConfigureChannels());
        e.registerServerCommand(new CommandRegistryDebug());
    }

    @Mod.EventHandler
    public void handleIMC(FMLInterModComms.IMCEvent event) {
        for (FMLInterModComms.IMCMessage message : event.getMessages()) {
            switch (message.key) {
                case "register_channel":
                    processRegisterChannel(message);
                    break;
                case "register_channel_item":
                    processRegisterChannelItem(message);
                    break;
            }
        }
    }

    private void processRegisterChannel(FMLInterModComms.IMCMessage message) {
        NBTTagList tags = message.getNBTValue().getTagList("Channels", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tags.tagCount(); i++) {
            NBTTagCompound tag = tags.getCompoundTagAt(i);
            ImpactStructures.registerChannelDescription(
                    tag.getString("Channel"),
                    message.getSender(),
                    tag.getString("Description"));
        }
    }

    private void processRegisterChannelItem(FMLInterModComms.IMCMessage message) {
        NBTTagList items = message.getNBTValue().getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < items.tagCount(); i++) {
            NBTTagCompound itemTag = items.getCompoundTagAt(i);
            ItemStack item = ItemStack.loadItemStackFromNBT(itemTag.getCompoundTag("Item"));
            NBTTagList tags = itemTag.getTagList("Channels", Constants.NBT.TAG_COMPOUND);
            for (int j = 0; j < tags.tagCount(); j++) {
                NBTTagCompound tag = tags.getCompoundTagAt(j);
                ImpactStructures.registerChannelItem(
                        tag.getString("Channel"),
                        message.getSender(),
                        tag.getInteger("Value"),
                        item);
            }
        }
    }

    public static void addClientSideChatMessages(String... messages) {
        proxy.addClientSideChatMessages(messages);
    }

    public static EntityPlayer getCurrentPlayer() {
        return proxy.getCurrentPlayer();
    }

    public static boolean isCurrentPlayer(EntityPlayer player) {
        return proxy.isCurrentPlayer(player);
    }

    public static long getOverworldTime() {
        return proxy.getOverworldTime();
    }

    public static Main instance() {
        return INSTANCE;
    }

    public static void sendToPlayer(EntityPlayerMP player, IMessage message) {
        net.sendTo(message, player);
    }

    public CommonProxy proxy() {
        return proxy;
    }
}
