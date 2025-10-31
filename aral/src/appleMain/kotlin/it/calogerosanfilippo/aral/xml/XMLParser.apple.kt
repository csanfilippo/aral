/**
 * This file provides the Apple-specific (iOS, macOS, etc.) implementation of the [XMLReader].
 * It uses the `NSXMLParser` from the Foundation framework, which is Apple's standard API for SAX-style
 * XML parsing. It defines a delegate to handle parsing events and converts them to the common
 * [XMLReaderCallback] events.
 */
@file:OptIn(kotlinx.cinterop.BetaInteropApi::class)

package it.calogerosanfilippo.aral.xml

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
 * A custom exception to wrap parsing errors from [NSXMLParser].
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

/**
 * Converts a raw `Map<Any?, *>` from `NSXMLParser` attributes into a `List<Pair<String, String>>`.
 * It safely handles type checking to prevent runtime errors.
 */
private fun Map<Any?, *>.toListOfStringPairs(): List<Pair<String, String>> =
    mapNotNull { (key, value) ->
        if (key is String && value is String) {
            key to value
        } else {
            null
        }
    }

/**
 * A private delegate class that implements [NSXMLParserDelegateProtocol].
 * It receives events directly from the [NSXMLParser] and translates them into the common [XMLReaderCallback] events.
 *
 * @param callback The common callback to which parser events are forwarded.
 */
private class ParserDelegate(private val callback: XMLReaderCallback) :
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
        callback.onError(nSXMLParsingException)
    }

    override fun parserDidEndDocument(parser: NSXMLParser) {
        callback.onDocumentEnd()
    }

    override fun parserDidStartDocument(parser: NSXMLParser) {
        callback.onDocumentStart()
    }

    override fun parser(parser: NSXMLParser, foundCharacters: String) {
        callback.onCharacters(foundCharacters)
    }

    override fun parser(
        parser: NSXMLParser,
        didEndElement: String,
        namespaceURI: String?,
        qualifiedName: String?
    ) {
        callback.onElementEnd(didEndElement)
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

        callback.onElementStart(didStartElement, parserEventAttributes)
    }
}

/**
 * The Apple-specific implementation of the [XMLReader] interface.
 * It configures and runs an `NSXMLParser`.
 */
internal class IOSXMLReader : XMLReader {

    override fun read(xmlString: String, callback: XMLReaderCallback) {
        val stringAsData = NSString.create(string = xmlString).dataUsingEncoding(NSUTF8StringEncoding) ?: NSData()
        val parser = NSXMLParser(stringAsData)
        val delegate = ParserDelegate(callback)
        parser.delegate = delegate
        parser.parse()
    }
}
