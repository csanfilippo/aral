/**
 * This file provides the JVM-specific implementation of the [XMLReader].
 * It uses the standard Java `org.xml.sax` package, which provides a SAX-style (Simple API for XML)
 * push parser. This is the underlying mechanism for parsing XML on the JVM platform.
 */
package it.calogerosanfilippo.aral.xml

import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.SAXParseException
import org.xml.sax.ext.LexicalHandler
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
    DefaultHandler(), LexicalHandler {

    override fun error(e: SAXParseException?) {
        callback.onError(e ?: Exception())
    }

    override fun startElement(
        uri: String,
        localName: String,
        qName: String,
        attributes: Attributes
    ) {

        val attributesMap = (0 until attributes.length).associate {
            attributes.getLocalName(it) to attributes.getValue(it)
        }

        callback.onElementStart(qName, attributesMap)
    }

    override fun endElement(uri: String, localName: String, qName: String) {
        callback.onElementEnd(qName)
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

    override fun comment(ch: CharArray?, start: Int, length: Int) { }

    override fun endCDATA() { }

    override fun endDTD() { }

    override fun endEntity(name: String?) { }

    override fun startCDATA() { }

    override fun startDTD(
        name: String?,
        publicId: String?,
        systemId: String?
    ) { }

    override fun startEntity(p0: String?) {}
}

/**
 * The JVM-specific implementation of the [XMLReader] interface.
 * It configures and runs a standard SAX parser.
 */
internal class JVMXMLReader: XMLReader {
    override fun read(xmlString: String, callback: XMLReaderCallback) {
        val factory = SAXParserFactory.newInstance()
        val parser = factory.newSAXParser()

        val inputSource = InputSource()
        inputSource.encoding = "UTF-8"
        inputSource.characterStream = StringReader(xmlString)

        try {
            val xmlHandler = XMLHandler(callback)
            parser.setProperty("http://xml.org/sax/properties/lexical-handler", xmlHandler)
            parser.parse(inputSource, xmlHandler)
        } catch (ex: Exception) {
            callback.onError(ex)
        }
    }
}
