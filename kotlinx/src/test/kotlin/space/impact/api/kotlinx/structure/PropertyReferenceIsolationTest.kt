package space.impact.api.kotlinx.structure

import java.net.URL
import java.net.URLClassLoader
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class PropertyReferenceIsolationTest {

    @Test
    fun actualTierPropertyGetAndSetWorkWithoutKotlinReflectImplementation() {
        val urls = linkedSetOf<URL>()
        var current: ClassLoader? = javaClass.classLoader
        while (current != null) {
            if (current is URLClassLoader) {
                urls.addAll(current.getURLs().toList())
            }
            current = current.parent
        }
        val isolated = object : URLClassLoader(
            urls.toTypedArray(),
            null,
        ) {
            override fun loadClass(
                name: String,
                resolve: Boolean,
            ): Class<*> {
                if (name.startsWith("kotlin.reflect.jvm.") || name.startsWith("kotlin.reflect.full.")) {
                    throw ClassNotFoundException(name)
                }
                return super.loadClass(
                    name,
                    resolve,
                )
            }
        }
        isolated.use { loader ->
            val probe = loader.loadClass("space.impact.api.kotlinx.structure.PropertyReferenceProbe")
            val instance = probe.getField("INSTANCE").get(null)
            assertEquals(
                true,
                probe.getMethod("run").invoke(instance),
            )
        }
    }
}
