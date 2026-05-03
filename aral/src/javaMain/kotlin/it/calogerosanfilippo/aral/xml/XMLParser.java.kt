package it.calogerosanfilippo.aral.xml

import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.SAXParseException
import org.xml.sax.ext.LexicalHandler
import org.xml.sax.helpers.DefaultHandler
import java.io.StringReader
import javax.xml.parsers.SAXParserFactory

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
        val attributesMap = (0 until attributes.length)
            .filter { i -> attributes.getQName(i).let { qn -> qn != "xmlns" && !qn.startsWith("xmlns:") } }
            .associate { i -> attributes.getLocalName(i) to attributes.getValue(i) }
        callback.onElementStart(qName.ifEmpty { localName }, uri.ifEmpty { null }, localName, attributesMap)
    }

    override fun endElement(uri: String, localName: String, qName: String) {
        callback.onElementEnd(qName.ifEmpty { localName }, uri.ifEmpty { null }, localName)
    }

    override fun characters(ch: CharArray, start: Int, length: Int) {
        callback.onCharacters(String(ch, start, length))
    }

    override fun startDocument() { callback.onDocumentStart() }
    override fun endDocument() { callback.onDocumentEnd() }

    override fun comment(ch: CharArray?, start: Int, length: Int) {}
    override fun startCDATA() {}
    override fun endCDATA() {}
    override fun startDTD(name: String?, publicId: String?, systemId: String?) {}
    override fun endDTD() {}
    override fun startEntity(name: String?) {}
    override fun endEntity(name: String?) {}
}

internal class JavaXMLReader : XMLReader {

    override fun read(xmlString: String, callback: XMLReaderCallback) {
        val parser = factory.newSAXParser()
        val inputSource = InputSource().apply {
            encoding = "UTF-8"
            characterStream = StringReader(xmlString)
        }
        try {
            val xmlHandler = XMLHandler(callback)
            parser.setProperty("http://xml.org/sax/properties/lexical-handler", xmlHandler)
            parser.parse(inputSource, xmlHandler)
        } catch (ex: Exception) {
            callback.onError(ex)
        }
    }

    private companion object {
        val factory: SAXParserFactory = SAXParserFactory.newInstance().also {
            it.isNamespaceAware = true
            it.setFeature("http://xml.org/sax/features/namespace-prefixes", true)
        }
    }
}
