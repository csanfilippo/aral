package it.calogerosanfilippo.aral.xml

import kotlinx.coroutines.flow.Flow

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
public abstract class XMLParser {
    /**
     * Parses the given XML string and returns a [Flow] of [XMLParserEvent]s.
     *
     * @param string The XML string to parse.
     * @return A [Flow] of [XMLParserEvent]s.
     * @throws EmptyDocumentException if the given string is empty.
     */
    public abstract fun parse(string: String): Flow<XMLParserEvent>
}

/**
 * Gets an instance of [XMLParser].
 */
internal expect fun internalGetParser(): XMLParser

/**
 * A factory for creating [XMLParser] instances.
 */
public object XMLParserFactory {
    /**
     * Gets an instance of [XMLParser].
     */
    public fun getParser(): XMLParser = internalGetParser()
}
