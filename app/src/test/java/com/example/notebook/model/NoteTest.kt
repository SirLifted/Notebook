package com.example.notebook.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteTest {

    @Test
    fun testWordCountAndCharacterCount_forPlainText() {
        val note = Note(
            title = "Meeting Notes",
            content = "This is a five word sentence."
        )
        assertEquals(6, note.wordCount)
        assertEquals(29, note.characterCount)
        assertEquals(1, note.readingTimeMinutes)
    }

    @Test
    fun testWordCount_withEmptyAndWhitespaceContent() {
        val noteEmpty = Note(content = "")
        val noteSpaces = Note(content = "   \n\t  ")
        assertEquals(0, noteEmpty.wordCount)
        assertEquals(0, noteSpaces.wordCount)
    }

    @Test
    fun testChecklistProgress_calculatesAccurately() {
        val items = listOf(
            ChecklistItem(text = "Item 1", isChecked = true),
            ChecklistItem(text = "Item 2", isChecked = false),
            ChecklistItem(text = "Item 3", isChecked = true)
        )
        val note = Note(
            isChecklist = true,
            checklistItems = items
        )
        val (completed, total) = note.checklistProgress
        assertEquals(2, completed)
        assertEquals(3, total)
    }

    @Test
    fun testChecklistSerializationRoundtrip() {
        val items = listOf(
            ChecklistItem(id = "1", text = "Buy milk", isChecked = false),
            ChecklistItem(id = "2", text = "Read book", isChecked = true)
        )
        val serialized = Note.serializeChecklist(items)
        val deserialized = Note.deserializeChecklist(serialized)

        assertEquals(2, deserialized.size)
        assertEquals("Buy milk", deserialized[0].text)
        assertFalse(deserialized[0].isChecked)
        assertEquals("Read book", deserialized[1].text)
        assertTrue(deserialized[1].isChecked)
    }

    @Test
    fun testEmptyChecklistSerialization() {
        val deserialized = Note.deserializeChecklist("")
        assertTrue(deserialized.isEmpty())
    }
}
