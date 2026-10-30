package space.impact.api.kotlinx.channels

import java.util.Locale
import space.impact.api.structure.IChannelDefinition

public fun channelDefinition(
    name: String,
    values: IntRange = 1..Int.MAX_VALUE,
): IChannelDefinition {
    require(value = name.isNotBlank() && name == name.lowercase(Locale.ROOT)) {
        "Channel names must be nonblank and lowercase"
    }
    require(value = !values.isEmpty() && values.first >= 1) {
        "Channel values must form a nonempty positive range"
    }
    return IChannelDefinition.bounded(
        name,
        values.first,
        values.last,
    )
}
