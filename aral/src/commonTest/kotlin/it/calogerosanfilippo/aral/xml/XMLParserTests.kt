package it.calogerosanfilippo.aral.xml

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class XMLParserTests  {

    @Test
    fun `when the string to parse is empty the parser emits an error`() = runTest {

        val parser = XMLParserFactory.getParser()

        parser.parse("").test {

            assertErrorEvent(awaitItem())

            awaitComplete()
        }
    }

    @Test
    fun `when parsing a simple tag the parser emits start and end events`() = runTest {

        val parser = XMLParserFactory.getParser()

        parser.parse("<tag></tag>").test {
            assertEquals(XMLParserEvent.DocumentStart, awaitItem())

            assertElementStart(awaitItem(), "tag")

            assertElementEnd(awaitItem(), "tag")

            assertEquals(XMLParserEvent.DocumentEnd, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `when parsing a simple tag with attributes the parser emits the element start with all the attributes`() = runTest {

        val parser = XMLParserFactory.getParser()

        parser.parse("<tag attr1=\"val1\" attr2=\"val2\"></tag>").test {
            assertEquals(XMLParserEvent.DocumentStart, awaitItem())

            assertElementStart(awaitItem(), "tag", mapOf("attr1" to "val1", "attr2" to "val2"))

            assertElementEnd(awaitItem(), "tag")

            assertEquals(XMLParserEvent.DocumentEnd, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `when parsing a not valid XML the parser emits an error and completes`() = runTest {

        val parser = XMLParserFactory.getParser()

        parser.parse("<tag attr1=\"val1\" attr2=\"val2\"></tag").test {
            assertEquals(XMLParserEvent.DocumentStart, awaitItem())

            assertElementStart(awaitItem(), "tag", mapOf("attr1" to "val1", "attr2" to "val2"))

            assertIs<XMLParserEvent.Error>(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `when parsing a tag with characters the parser emits the character event`() = runTest {

        val parser = XMLParserFactory.getParser()

        parser.parse("<tag>characters</tag>").test {
            assertEquals(XMLParserEvent.DocumentStart, awaitItem())

            assertElementStart(awaitItem(), "tag")

            assertCharacterEvent(awaitItem(), "characters")

            assertElementEnd(awaitItem(), "tag")

            assertEquals(XMLParserEvent.DocumentEnd, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `when parsing a tag with interleaved characters and elements the parser emits the correct events`() = runTest {

        val parser = XMLParserFactory.getParser()

        parser.parse("<root>Some text here<child>Child text</child>Some more text</root>").test {
            assertEquals(XMLParserEvent.DocumentStart, awaitItem())

            assertElementStart(awaitItem(), "root")

            assertCharacterEvent(awaitItem(), "Some text here")

            assertElementStart(awaitItem(), "child")

            assertCharacterEvent(awaitItem(), "Child text")

            assertElementEnd(awaitItem(), "child")

            assertCharacterEvent(awaitItem(), "Some more text")

            assertElementEnd(awaitItem(), "root")

            assertEquals(XMLParserEvent.DocumentEnd, awaitItem())

            awaitComplete()
        }
    }
}

private fun assertElementStart(event: XMLParserEvent, name: String, attributes: Map<String, String> = emptyMap()) {
    assertIs<XMLParserEvent.ElementStartFound>(event)
    assertEquals(name, event.name)
    assertEquals(attributes, event.attributes)
}

private fun assertElementEnd(event: XMLParserEvent, name: String) {
    assertIs<XMLParserEvent.ElementEndFound>(event)
    assertEquals(name, event.name)
}


private fun assertCharacterEvent(event: XMLParserEvent, characters: String) {
    assertIs<XMLParserEvent.CharactersFound>(event)
    assertEquals(characters, event.characters)
}

private fun assertErrorEvent(event: XMLParserEvent) {
    assertIs<XMLParserEvent.Error>(event)
}