package it.calogerosanfilippo.aral.xml

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext

/**
 * Thrown by [XMLParser.parse] when the input string is blank.
 */
public class EmptyDocumentException : Exception()

/**
 * Represents an event that occurs during XML parsing.
 */
public sealed class XMLParserEvent {
    /**
     * Indicates the start of the document.
     */
    public data object DocumentStart : XMLParserEvent()

    /**
     * Indicates the end of the document.
     */
    public data object DocumentEnd : XMLParserEvent()

    /**
     * Indicates that an element has started.
     *
     * @param name The qualified name of the element (e.g. `dc:title`). Equal to [localName] when there is no prefix.
     * @param namespaceURI The namespace URI, or `null` if the element has no namespace.
     * @param localName The local name of the element without any namespace prefix (e.g. `title`).
     * @param attributes A map of the element's attributes keyed by local name. Note: if two attributes
     * from different namespaces share the same local name, only one will be present in the map.
     * Full per-attribute namespace info is a known limitation deferred to a future version.
     */
    public data class ElementStartFound(
        val name: String,
        val namespaceURI: String?,
        val localName: String,
        val attributes: Map<String, String>,
    ) : XMLParserEvent()

    /**
     * Indicates that an element has ended.
     *
     * @param name The qualified name of the element (e.g. `dc:title`). Equal to [localName] when there is no prefix.
     * @param namespaceURI The namespace URI, or `null` if the element has no namespace.
     * @param localName The local name of the element without any namespace prefix (e.g. `title`).
     */
    public data class ElementEndFound(
        val name: String,
        val namespaceURI: String?,
        val localName: String,
    ) : XMLParserEvent()

    /**
     * Indicates that character data has been found.
     *
     * Adjacent character callbacks from the underlying parser are coalesced, so a single
     * contiguous run of text always produces exactly one [CharactersFound] event.
     *
     * @param characters The character data.
     */
    public data class CharactersFound(val characters: String) : XMLParserEvent()

    /**
     * Indicates that an error has occurred during parsing.
     *
     * @param exception The exception that occurred.
     */
    public data class Error(val exception: Exception) : XMLParserEvent()
}

/**
 * Parses XML strings into a [Flow] of [XMLParserEvent]s.
 *
 * Use [XMLParserFactory.getParser] to obtain an instance.
 */
public class XMLParser internal constructor(private val xmlReader: XMLReader){
    /**
     * Parses [string] and returns a cold [Flow] of [XMLParserEvent]s. Parsing starts only when
     * the flow is collected and runs on [kotlinx.coroutines.Dispatchers.IO].
     *
     * If [string] is blank, the flow emits [XMLParserEvent.DocumentStart] followed by
     * [XMLParserEvent.Error] wrapping an [EmptyDocumentException], then completes.
     *
     * @param string The XML string to parse.
     * @return A [Flow] of [XMLParserEvent]s.
     */
    public fun parse(string: String): Flow<XMLParserEvent> {

        val trimmedString = string.trim()

        if (trimmedString.isBlank()) {
            return flowOf(
                XMLParserEvent.DocumentStart,
                XMLParserEvent.Error(EmptyDocumentException()),
            )
        }

        return channelFlow {
            val callback: XMLReaderCallback = object : XMLReaderCallback {

                private val charactersBuffer = StringBuilder()

                private fun flushCharacters() {
                    if (charactersBuffer.isNotEmpty()) {
                        trySend(XMLParserEvent.CharactersFound(charactersBuffer.toString()))
                        charactersBuffer.clear()
                    }
                }

                override fun onDocumentStart() {
                    trySend(XMLParserEvent.DocumentStart)
                }

                override fun onDocumentEnd() {
                    flushCharacters()
                    trySend(XMLParserEvent.DocumentEnd)
                }

                override fun onElementStart(
                    name: String,
                    namespaceURI: String?,
                    localName: String,
                    attributes: Map<String, String>,
                ) {
                    flushCharacters()
                    trySend(XMLParserEvent.ElementStartFound(name, namespaceURI, localName, attributes))
                }

                override fun onElementEnd(name: String, namespaceURI: String?, localName: String) {
                    flushCharacters()
                    trySend(XMLParserEvent.ElementEndFound(name, namespaceURI, localName))
                }

                override fun onCharacters(characters: String) {
                    charactersBuffer.append(characters)
                }

                override fun onError(exception: Exception) {
                    trySend(XMLParserEvent.Error(exception))
                }
            }

            withContext(Dispatchers.IO) {
                xmlReader.read(trimmedString, callback)
            }
        }
    }
}

/**
 * Factory for obtaining [XMLParser] instances.
 */
public object XMLParserFactory {
    /**
     * Returns a new [XMLParser] backed by the platform's native XML parser.
     */
    public fun getParser(): XMLParser = XMLParser(platformXmlReader())
}
