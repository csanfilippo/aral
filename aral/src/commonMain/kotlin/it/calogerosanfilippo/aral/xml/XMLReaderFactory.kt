package it.calogerosanfilippo.aral.xml

/**
 * An internal factory for creating platform-specific instances of [XMLReader].
 * It wraps the [platformXmlReader] function to provide the concrete implementation.
 */
internal object XMLReaderFactory {
    /**
     * Creates and returns a platform-specific [XMLReader].
     */
    fun createReader(): XMLReader = platformXmlReader()
}

/**
 * Creates a platform-specific [XMLReader].
 * This is an `expect` function that must be implemented in each platform-specific source set
 * to provide a concrete [XMLReader] instance.
 */
internal expect fun platformXmlReader(): XMLReader
