package space.impact.api.kotlinx.execution

/** Finish only a scope whose start succeeded, preserving an enclosing instrumentation scope on rejected nesting. */
internal fun <R> runLifecycleScope(
    start: () -> Unit,
    finish: () -> Unit,
    block: () -> R,
): R {
    start()
    try {
        return block()
    } finally {
        finish()
    }
}
