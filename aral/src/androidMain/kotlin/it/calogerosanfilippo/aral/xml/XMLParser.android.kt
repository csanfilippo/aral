/**
 * This file provides the Android-specific implementation of the [XMLReader].
 * It uses the standard Android `org.xml.sax` package, which provides a SAX-style (Simple API for XML)
 * push parser. This is the underlying mechanism for parsing XML on the Android platform.
 */
package it.calogerosanfilippo.aral.xml

import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.SAXParseException
import org.xml.sax.helpers.DefaultHandler
import java.io.StringReader
import javax.xml.parsers.SAXParserFactory

/**
 * A private `DefaultHandler` that acts as the SAX callback handler.
 * It listens for events from the SAX parser and translates them into the common [XMLReaderCallback] events
 * used by the multiplatform [XMLParser].
 *
 * @param callback The common callback to which parser events are forwarded.
 */
private class XMLHandler(private val callback: XMLReaderCallback) :
    DefaultHandler() {

    override fun error(e: SAXParseException?) {
        callback.onError(e ?: Exception())
    }

    override fun startElement(
        uri: String,
        localName: String,
        elementName: String,
        attributes: Attributes
    ) {

        val attributesMap = (0 until attributes.length).map {
            val value = attributes.getValue(it)
            val key = attributes.getLocalName(it)

            return@map Pair<String, String>(key, value)
        }.toMap()

        callback.onElementStart(elementName, attributesMap)
    }

    override fun endElement(uri: String, localName: String, elementName: String) {
        callback.onElementEnd(elementName)
    }

    override fun characters(ch: CharArray, start: Int, length: Int) {
        callback.onCharacters(String(ch, start, length))
    }

    override fun endDocument() {
        callback.onDocumentEnd()
    }

    override fun startDocument() {
        callback.onDocumentStart()
    }
}

/**
 * The Android-specific implementation of the [XMLReader] interface.
 * It configures and runs a standard SAX parser.
 */
internal class AndroidXMLReader: XMLReader {
    override fun read(xmlString: String, callback: XMLReaderCallback) {
        val factory = SAXParserFactory.newInstance()
        val parser = factory.newSAXParser()

        val inputSource = InputSource()
        inputSource.encoding = "UTF-8"
        inputSource.characterStream = StringReader(xmlString)

        try {
            parser.parse(inputSource, XMLHandler(callback))
        } catch (ex: Exception) {
            callback.onError(ex)
        }
    }
}
