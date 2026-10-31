package space.impact.structures;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import cpw.mods.fml.common.event.FMLInterModComms;

public class DevelopHelper {

    private static final boolean DEBUG = Boolean.getBoolean("impactapi.develop_helper");

    public static void onPreInit() {
        if (!DEBUG) return;
        Main.LOGGER.info("Running develop helper");
        ImpactStructures.registerChannelDescription("sandstone", ImpactStructures.MOD_ID, "variants of Sandstone");
        ImpactStructures.registerChannelItem("sandstone", ImpactStructures.MOD_ID, 1, new ItemStack(Blocks.sandstone));
        ImpactStructures
                .registerChannelItem("sandstone", ImpactStructures.MOD_ID, 2, new ItemStack(Blocks.sandstone, 1, 1));
        ImpactStructures
                .registerChannelItem("sandstone", ImpactStructures.MOD_ID, 3, new ItemStack(Blocks.sandstone, 1, 2));
        ImpactStructures.registerChannelDescription("sandbricks", ImpactStructures.MOD_ID, "variants of Sandbricks");
        ImpactStructures.registerChannelDescription("apple", ImpactStructures.MOD_ID, "apple");
        ImpactStructures.registerChannelDescription("bright", ImpactStructures.MOD_ID, "bright");
        ImpactStructures.registerChannelDescription("apple_blight", ImpactStructures.MOD_ID, "apple_blight");
        ImpactStructures.registerChannelDescription("app_eagle", ImpactStructures.MOD_ID, "app_eagle");
    }

    public void x() {
        NBTTagCompound command = new NBTTagCompound();
        NBTTagList items = new NBTTagList();
        int[] metas = { 0, 1, 2 };
        for (int meta : metas) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setTag("Item", new ItemStack(Blocks.sandstone, 1, meta).writeToNBT(new NBTTagCompound()));
            NBTTagList channels = new NBTTagList();
            NBTTagCompound channel = new NBTTagCompound();
            channel.setString("Channel", "sandstone");
            channel.setInteger("Value", meta + 1);
            channels.appendTag(channel);
            tag.setTag("Channels", channels);
            items.appendTag(tag);
        }
        command.setTag("Items", items);
        FMLInterModComms.sendMessage("impactapi", "register_channel_item", command);
    }
}
