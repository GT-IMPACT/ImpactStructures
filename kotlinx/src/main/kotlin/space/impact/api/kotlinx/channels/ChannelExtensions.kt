package space.impact.api.kotlinx.channels

import net.minecraft.item.ItemStack
import space.impact.api.alignment.constructable.ChannelDataAccessor

public fun ItemStack.channelValue(name: String): Int {
    return ChannelDataAccessor.getChannelData(
        this,
        name,
    )
}

public fun ItemStack.channelValueOrNull(name: String): Int? {
    if (
        !ChannelDataAccessor.hasSubChannel(
            this,
            name,
        )
    ) {
        return null
    }
    return channelValue(name)
}

public fun ItemStack.setChannelValue(
    name: String,
    value: Int,
) {
    ChannelDataAccessor.setChannelData(
        this,
        name,
        value,
    )
}

public fun ItemStack.removeChannel(name: String) {
    ChannelDataAccessor.unsetChannelData(
        this,
        name,
    )
}

public fun ItemStack.clearChannels() {
    ChannelDataAccessor.wipeChannelData(this)
}

public val ItemStack.channels: Map<String, Int>
    get() {
        val snapshot: MutableMap<String, Int> = linkedMapOf()
        ChannelDataAccessor.iterateChannelData(this).forEach { channel ->
            snapshot[channel.key] = channel.value
        }
        return snapshot.toMap()
    }
