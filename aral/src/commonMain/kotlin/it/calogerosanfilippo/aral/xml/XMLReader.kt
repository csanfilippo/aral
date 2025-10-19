package it.calogerosanfilippo.aral.xml

/**
 * An internal callback interface used by platform-specific readers to push parsing events
 * back to the common [XMLParser].
 */
internal interface XMLReaderCallback {
    fun onDocumentStart()
    fun onDocumentEnd()
    fun onElementStart(name: String, attributes: Map<String, String>)
    fun onElementEnd(name: String)
    fun onCharacters(characters: String)
    fun onError(exception: Exception)
}

/**
 * An internal interface for a platform-specific XML reader.
 * Implementations of this interface will use a native SAX-style parser to read an XML string
 * and invoke the methods on the provided [XMLReaderCallback].
 */
internal interface XMLReader {
    fun read(xmlString: String, callback: XMLReaderCallback)
}
