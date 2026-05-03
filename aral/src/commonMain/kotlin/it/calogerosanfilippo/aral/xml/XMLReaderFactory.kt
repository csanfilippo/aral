package it.calogerosanfilippo.aral.xml

/**
 * Creates a platform-specific [XMLReader].
 * This is an `expect` function that must be implemented in each platform-specific source set
 * to provide a concrete [XMLReader] instance.
 */
internal expect fun platformXmlReader(): XMLReader
