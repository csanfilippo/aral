package it.calogerosanfilippo.aral.xml

import kotlinx.coroutines.flow.Flow

public class EmptyDocumentException : Exception()

public sealed class XMLParserEvent {
    public data object DocumentStart : XMLParserEvent()
    public data object DocumentEnd : XMLParserEvent()
    public data class ElementStartFound(val name: String, val attributes: Map<String, String>) :
        XMLParserEvent()

    public data class ElementEndFound(val name: String) : XMLParserEvent()
    public data class CharactersFound(val characters: String) : XMLParserEvent()
    public data class Error(val exception: Exception) : XMLParserEvent()
}

public abstract class XMLParser {
    public abstract fun parse(string: String): Flow<XMLParserEvent>
}

internal expect fun internalGetParser(): XMLParser

public object XMLParserFactory {
    public fun getParser(): XMLParser = internalGetParser()
}