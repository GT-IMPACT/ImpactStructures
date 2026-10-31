package space.impact.structures.kotlinx.execution

import space.impact.structures.ImpactStructures

/** Instrument the current thread for the duration of [block], including exceptional exits. */
public fun <R> withInstrumentation(
    identifier: Any,
    block: () -> R,
): R {
    return runLifecycleScope(
        start = {
            ImpactStructures.enableInstrument(identifier)
        },
        finish = {
            ImpactStructures.disableInstrument()
        },
        block = block,
    )
}
