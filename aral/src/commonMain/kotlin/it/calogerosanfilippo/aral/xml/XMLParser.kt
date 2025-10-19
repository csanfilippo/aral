/**
 * This file defines the core components for a multiplatform XML parsing library.
 *
 * It includes:
 * - [XMLParserEvent]: A sealed class representing all possible events during parsing (e.g., element start, characters found).
 * - [XMLParser]: The main entry point for parsing, which consumes an XML string and produces a Flow of [XMLParserEvent]s.
 * - [XMLParserFactory]: A factory to get a platform-specific instance of the [XMLParser].
 * - Internal interfaces ([XMLReader], [XMLReaderCallback]) to abstract the platform-specific SAX-style parser implementations.
 */
package it.calogerosanfilippo.aral.xml

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.withContext

/**
 * Thrown when the XML document is empty.
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
     * @param name The name of the element.
     * @param attributes A map of the element's attributes.
     */
    public data class ElementStartFound(val name: String, val attributes: Map<String, String>) :
        XMLParserEvent()

    /**
     * Indicates that an element has ended.
     *
     * @param name The name of the element.
     */
    public data class ElementEndFound(val name: String) : XMLParserEvent()

    /**
     * Indicates that character data has been found.
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
 * An XML parser.
 */
public class XMLParser internal constructor(private val xmlReader: XMLReader){
    /**
     * Parses the given XML string and returns a [Flow] of [XMLParserEvent]s.
     *
     * @param string The XML string to parse.
     * @return A [Flow] of [XMLParserEvent]s.
     * @throws EmptyDocumentException if the given string is empty.
     */
    public fun parse(string: String): Flow<XMLParserEvent> {
        return channelFlow {
            val callback: XMLReaderCallback = object : XMLReaderCallback {

                private var charactersBuffer: String? = null

                private fun flushCharacters() {
                    charactersBuffer?.let {
                        trySend(XMLParserEvent.CharactersFound(it))
                        charactersBuffer = null
                    }
                }

                override fun onDocumentStart() {
                    trySend(XMLParserEvent.DocumentStart)
                }

                override fun onDocumentEnd() {
                    trySend(XMLParserEvent.DocumentEnd)
                    channel.close()
                }

                override fun onElementStart(
                    name: String,
                    attributes: Map<String, String>
                ) {
                    flushCharacters()
                    trySend(XMLParserEvent.ElementStartFound(name, attributes))
                }

                override fun onElementEnd(name: String) {
                    flushCharacters()
                    trySend(XMLParserEvent.ElementEndFound(name))
                }

                override fun onCharacters(characters: String) {
                    charactersBuffer = charactersBuffer?.plus(characters) ?: characters
                }

                override fun onError(exception: Exception) {
                    trySend(XMLParserEvent.Error(exception))
                    channel.close()
                }
            }

            val cleanedString = string.trim()

            if (cleanedString.isBlank()) {
                send(XMLParserEvent.Error(EmptyDocumentException()))
            } else {
                // This is a blocking call, run it in a proper context
                withContext(Dispatchers.IO) {
                    xmlReader.read(cleanedString, callback)
                }
            }
        }
    }
}

/**
 * A factory for creating [XMLParser] instances.
 */
public object XMLParserFactory {
    /**
     * Gets an instance of [XMLParser].
     */
    public fun getParser(): XMLParser = XMLParser(XMLReaderFactory.createReader())
}
