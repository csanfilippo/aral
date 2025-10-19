package it.calogerosanfilippo.aral.xml

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.fail

class XMLParserTests  {

    @Test
    fun `when the string to parse is empty the parser emits an error`() = runTest {

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
    fun `when parsing a simple tag the parser emits start and end events`() = runTest {

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
    fun `when parsing a simple tag with attributes the parser emits the element start with all the attributes`() = runTest {

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
    fun `when parsing a not valid XML the parser emits an error and completes`() = runTest {

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
    fun `when parsing a tag with characters the parser emits the character event`() = runTest {

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

    @Test
    fun `when parsing a tag with interleaved characters and elements the parser emits the correct events`() = runTest {

        val parser = XMLParserFactory.getParser()

        parser.parse("<root>Some text here<child>Child text</child>Some more text</root>").test {
            assertEquals(XMLParserEvent.DocumentStart, awaitItem())

            when (val event = awaitItem()) {
                is XMLParserEvent.ElementStartFound -> {
                    assertEquals("root", event.name)
                }

                else -> {
                    fail("Unexpected event $event")
                }
            }

            when (val event = awaitItem()) {
                is XMLParserEvent.CharactersFound -> {
                    assertEquals("Some text here", event.characters)
                }

                else -> {
                    fail("Unexpected event $event")
                }
            }

            when (val event = awaitItem()) {
                is XMLParserEvent.ElementStartFound -> {
                    assertEquals("child", event.name)
                }

                else -> {
                    fail("Unexpected event $event")
                }
            }

            when (val event = awaitItem()) {
                is XMLParserEvent.CharactersFound -> {
                    assertEquals("Child text", event.characters)
                }

                else -> {
                    fail("Unexpected event $event")
                }
            }

            when (val event = awaitItem()) {
                is XMLParserEvent.ElementEndFound -> {
                    assertEquals("child", event.name)
                }

                else -> {
                    fail("Unexpected event $event")
                }
            }

            when (val event = awaitItem()) {
                is XMLParserEvent.CharactersFound -> {
                    assertEquals("Some more text", event.characters)
                }

                else -> {
                    fail("Unexpected event $event")
                }
            }

            when (val event = awaitItem()) {
                is XMLParserEvent.ElementEndFound -> {
                    assertEquals("root", event.name)
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