package it.calogerosanfilippo.aral.xml

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.fail

class XMLParserTests  {

    @Test
    fun testEmptyString() = runTest {

        val parser = XMLParserFactory.getParser()

        parser.parse("").test {
            when (val event = awaitItem()) {
                is XMLParserEvent.Error ->
                    assertIs<EmptyDocumentException>(event.exception)

                else -> {
                    fail("Unexpected event $event")
                }
            }

            awaitComplete()
        }
    }

    @Test
    fun testSimpleXML() = runTest {

        val parser = XMLParserFactory.getParser()

        parser.parse("<tag></tag>").test {
            assertEquals(XMLParserEvent.DocumentStart, awaitItem())

            when (val event = awaitItem()) {
                is XMLParserEvent.ElementStartFound -> assertEquals("tag", event.name)
                else -> {
                    fail("Unexpected event $event")
                }
            }

            when (val event = awaitItem()) {
                is XMLParserEvent.ElementEndFound -> assertEquals("tag", event.name)
                else -> {
                    fail("Unexpected event $event")
                }
            }

            assertEquals(XMLParserEvent.DocumentEnd, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun testSimpleXMLWithAttributes() = runTest {

        val parser = XMLParserFactory.getParser()

        parser.parse("<tag attr1=\"val1\" attr2=\"val2\"></tag>").test {
            assertEquals(XMLParserEvent.DocumentStart, awaitItem())

            when (val event = awaitItem()) {
                is XMLParserEvent.ElementStartFound -> {
                    assertEquals("tag", event.name)
                    assertEquals(mapOf("attr1" to "val1", "attr2" to "val2"), event.attributes)
                }

                else -> {
                    fail("Unexpected event $event")
                }
            }

            when (val event = awaitItem()) {
                is XMLParserEvent.ElementEndFound -> assertEquals("tag", event.name)
                else -> {
                    fail("Unexpected event $event")
                }
            }

            assertEquals(XMLParserEvent.DocumentEnd, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun testNotValidXML() = runTest {

        val parser = XMLParserFactory.getParser()

        parser.parse("<tag attr1=\"val1\" attr2=\"val2\"></tag").test {
            assertEquals(XMLParserEvent.DocumentStart, awaitItem())

            when (val event = awaitItem()) {
                is XMLParserEvent.ElementStartFound -> {
                    assertEquals("tag", event.name)
                    assertEquals(mapOf("attr1" to "val1", "attr2" to "val2"), event.attributes)
                }

                else -> {
                    fail("Unexpected event $event")
                }
            }

            assertIs<XMLParserEvent.Error>(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun testCharacters() = runTest {

        val parser = XMLParserFactory.getParser()

        parser.parse("<tag>characters</tag>").test {
            assertEquals(XMLParserEvent.DocumentStart, awaitItem())

            when (val event = awaitItem()) {
                is XMLParserEvent.ElementStartFound -> {
                    assertEquals("tag", event.name)
                }

                else -> {
                    fail("Unexpected event $event")
                }
            }

            when (val event = awaitItem()) {
                is XMLParserEvent.CharactersFound -> {
                    assertEquals("characters", event.characters)
                }

                else -> {
                    fail("Unexpected event $event")
                }
            }

            when (val event = awaitItem()) {
                is XMLParserEvent.ElementEndFound -> {
                    assertEquals("tag", event.name)
                }

                else -> {
                    fail("Unexpected event $event")
                }
            }

            assertEquals(XMLParserEvent.DocumentEnd, awaitItem())
            awaitComplete()
        }
    }

}