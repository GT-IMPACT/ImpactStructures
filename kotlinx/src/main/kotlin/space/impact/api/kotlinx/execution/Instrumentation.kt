package space.impact.api.kotlinx.execution

import space.impact.api.ImpactAPI

/** Instrument the current thread for the duration of [block], including exceptional exits. */
public fun <R> withInstrumentation(
    identifier: Any,
    block: () -> R,
): R {
    return runLifecycleScope(
        start = {
            ImpactAPI.enableInstrument(identifier)
        },
        finish = {
            ImpactAPI.disableInstrument()
        },
        block = block,
    )
}
