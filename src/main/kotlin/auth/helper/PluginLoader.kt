package auth.helper

import java.net.URLClassLoader
import java.net.URL

public class PluginLoader<T> {
    fun loadClass(jarUrl: String, classPath: String): T {
        val loader = URLClassLoader.newInstance(
            arrayOf(URL(jarUrl)),
            javaClass.classLoader
        )
        val newClass = Class.forName(classPath, true, loader) as Class<T>

        val constructor = newClass.getConstructor()
        return constructor.newInstance()
    }
}
