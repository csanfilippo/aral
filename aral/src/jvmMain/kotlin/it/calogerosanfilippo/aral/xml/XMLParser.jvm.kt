package it.calogerosanfilippo.aral.xml

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.SAXParseException
import org.xml.sax.helpers.DefaultHandler
import java.io.StringReader
import javax.xml.parsers.SAXParserFactory

internal actual fun internalGetParser(): XMLParser {
    return JVMXMLParser()
}

private class XMLHandler(private val producerScope: ProducerScope<XMLParserEvent>) :
    DefaultHandler() {
    override fun error(e: SAXParseException?) {
        producerScope.launch {
            producerScope.send(XMLParserEvent.Error(e ?: Exception()))
        }
    }

    override fun startElement(
        uri: String,
        localName: String,
        qName: String,
        attributes: Attributes
    ) {

        val attributesMap = (0 until attributes.length).map {
            val value = attributes.getValue(it)
            val key = attributes.getLocalName(it)

            return@map Pair<String, String>(key, value)
        }.toMap()

        producerScope.launch {
            producerScope.send(XMLParserEvent.ElementStartFound(qName, attributesMap))
        }
    }

    override fun endElement(uri: String, localName: String, qName: String) {
        producerScope.launch {
            producerScope.send(XMLParserEvent.ElementEndFound(qName))
        }
    }

    override fun characters(ch: CharArray, start: Int, length: Int) {
        val foundString = String(ch, start, length)

        producerScope.launch {
            producerScope.send(XMLParserEvent.CharactersFound(foundString))
        }
    }

    override fun endDocument() {
        producerScope.launch {
            producerScope.send(XMLParserEvent.DocumentEnd)
        }
    }

    override fun startDocument() {
        producerScope.launch {
            producerScope.send(XMLParserEvent.DocumentStart)
        }
    }
}


internal class JVMXMLParser: XMLParser() {
    override fun parse(string: String): Flow<XMLParserEvent> {

        if (string.isBlank()) {
            return flowOf(XMLParserEvent.Error(EmptyDocumentException()))
        }

        return channelFlow {
            val factory = SAXParserFactory.newInstance()
            val parser = factory.newSAXParser()

            val inputSource = InputSource()
            inputSource.encoding = "UTF-8"
            inputSource.characterStream = StringReader(string)

            try {
                withContext(Dispatchers.IO) {
                    parser.parse(inputSource, XMLHandler(this@channelFlow))
                }
            } catch (ex: Exception) {
                launch {
                    send(XMLParserEvent.Error(ex))
                }
            }
        }
    }
}