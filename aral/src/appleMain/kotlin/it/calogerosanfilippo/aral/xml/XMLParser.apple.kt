package it.calogerosanfilippo.aral.xml

import kotlinx.cinterop.BetaInteropApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSXMLParser
import platform.Foundation.NSXMLParserDelegateProtocol
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.darwin.NSObject

/**
 * Exception thrown when an error occurs during XML parsing on Apple platforms.
 *
 * @property nsError The underlying [NSError] from the parser.
 * @property failureReason A string containing the localized description of the reason for the error.
 * @property recoverySuggestion A string containing the localized description of how to recover from the error.
 * @property message The detailed error message.
 */
internal data class NSXMLParsingException(
    val nsError: NSError,
    val failureReason: String?,
    val recoverySuggestion: String?,
    override val message: String?,
) : Exception()


private fun Map<Any?, *>.toListOfStringPairs(): List<Pair<String, String>> =
    map {
        return@map if (it.key is String && it.value is String) {
            it.key as String to it.value as String
        } else {
            null
        }
    }.filterNotNull()

/**
 * A delegate for [NSXMLParser] that converts parser events into [XMLParserEvent]s and sends them to a [ProducerScope].
 *
 * @param producerScope The scope to which the parser events are sent.
 */
private class ParserDelegate(private val producerScope: ProducerScope<XMLParserEvent>) :
    NSObject(),
    NSXMLParserDelegateProtocol {

    override fun parser(parser: NSXMLParser, parseErrorOccurred: NSError) {

        val nSXMLParsingException = NSXMLParsingException(
            nsError = parseErrorOccurred,
            message = parseErrorOccurred.localizedDescription,
            failureReason = parseErrorOccurred.localizedFailureReason,
            recoverySuggestion = parseErrorOccurred.localizedRecoverySuggestion,
        )

        parser.abortParsing()

        producerScope.launch {
            producerScope.send(XMLParserEvent.Error(nSXMLParsingException))
        }
    }

    override fun parserDidEndDocument(parser: NSXMLParser) {
        producerScope.launch {
            producerScope.send(XMLParserEvent.DocumentEnd)
        }
    }

    override fun parserDidStartDocument(parser: NSXMLParser) {
        producerScope.launch {
            producerScope.send(XMLParserEvent.DocumentStart)
        }
    }

    override fun parser(parser: NSXMLParser, foundCharacters: String) {
        producerScope.launch {
            producerScope.send(XMLParserEvent.CharactersFound(foundCharacters))
        }
    }

    override fun parser(
        parser: NSXMLParser,
        didEndElement: String,
        namespaceURI: String?,
        qualifiedName: String?
    ) {
        producerScope.launch {
            producerScope.send(XMLParserEvent.ElementEndFound(didEndElement))
        }
    }

    override fun parser(
        parser: NSXMLParser,
        didStartElement: String,
        namespaceURI: String?,
        qualifiedName: String?,
        attributes: Map<Any?, *>
    ) {
        val parserEventAttributes = attributes
            .toListOfStringPairs()
            .toMap()

        producerScope.launch {
            producerScope.send(
                XMLParserEvent.ElementStartFound(
                    didStartElement,
                    parserEventAttributes
                )
            )
        }
    }
}

/**
 * An implementation of [XMLParser] for Apple platforms that uses [NSXMLParser].
 */
internal class IOSXMLParser : XMLParser() {
    @OptIn(BetaInteropApi::class)
    override fun parse(string: String): Flow<XMLParserEvent> {
        val cleanXml = string.trim()

        if (cleanXml.isBlank()) {
            return flowOf(XMLParserEvent.Error(EmptyDocumentException()))
        }

        val stringAsData = NSString.create(string = cleanXml).dataUsingEncoding(NSUTF8StringEncoding) ?: NSData()

        return channelFlow {
            withContext(Dispatchers.IO) {
                val parser = NSXMLParser(stringAsData)
                val delegate = ParserDelegate(this@channelFlow)
                parser.delegate = delegate
                parser.parse()
            }
        }
    }
}

/**
 * Returns an instance of [IOSXMLParser] for use on Apple platforms.
 */
internal actual fun internalGetParser(): XMLParser {
    return IOSXMLParser()
}
