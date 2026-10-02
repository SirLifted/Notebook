package com.example.notebook.ui.editor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.notebook.data.NoteRepository
import com.example.notebook.model.ChecklistItem
import com.example.notebook.model.Note
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Stack
import java.util.UUID

data class EditorSnapshot(
    val title: String,
    val content: String,
    val checklistItems: List<ChecklistItem>
)

data class NoteEditorUiState(
    val noteId: Long = 0L,
    val title: String = "",
    val content: String = "",
    val category: String = Note.CATEGORY_PERSONAL,
    val colorIndex: Int = 0,
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val isTrash: Boolean = false,
    val isChecklist: Boolean = false,
    val checklistItems: List<ChecklistItem> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
    val isLoaded: Boolean = false,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val wordCount: Int = 0,
    val characterCount: Int = 0,
    val readingTimeMinutes: Int = 1
)

class NoteEditorViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = NoteRepository.getInstance(application)

    private val _uiState = MutableStateFlow(NoteEditorUiState())
    val uiState: StateFlow<NoteEditorUiState> = _uiState.asStateFlow()

    private val undoStack = Stack<EditorSnapshot>()
    private val redoStack = Stack<EditorSnapshot>()

    fun loadNote(noteId: Long, initialIsChecklist: Boolean = false) {
        if (_uiState.value.isLoaded) return

        viewModelScope.launch {
            if (noteId > 0) {
                val note = repository.getNote(noteId)
                if (note != null) {
                    _uiState.value = NoteEditorUiState(
                        noteId = note.id,
                        title = note.title,
                        content = note.content,
                        category = note.category,
                        colorIndex = note.colorIndex,
                        isPinned = note.isPinned,
                        isFavorite = note.isFavorite,
                        isArchived = note.isArchived,
                        isTrash = note.isTrash,
                        isChecklist = note.isChecklist,
                        checklistItems = note.checklistItems,
                        createdAt = note.createdAt,
                        modifiedAt = note.modifiedAt,
                        isLoaded = true,
                        wordCount = note.wordCount,
                        characterCount = note.characterCount,
                        readingTimeMinutes = note.readingTimeMinutes
                    )
                    return@launch
                }
            }

            // New note initialization
            val defaultItems = if (initialIsChecklist) {
                listOf(ChecklistItem(text = ""))
            } else {
                emptyList()
            }
            _uiState.value = NoteEditorUiState(
                isChecklist = initialIsChecklist,
                checklistItems = defaultItems,
                category = if (initialIsChecklist) Note.CATEGORY_TODO else Note.CATEGORY_PERSONAL,
                isLoaded = true
            )
        }
    }

    private fun pushUndoSnapshot() {
        val current = _uiState.value
        undoStack.push(EditorSnapshot(current.title, current.content, current.checklistItems))
        redoStack.clear()
        updateUndoRedoStatus()
    }

    private fun updateUndoRedoStatus() {
        _uiState.value = _uiState.value.copy(
            canUndo = undoStack.isNotEmpty(),
            canRedo = redoStack.isNotEmpty()
        )
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val current = _uiState.value
        redoStack.push(EditorSnapshot(current.title, current.content, current.checklistItems))
        val previous = undoStack.pop()
        updateContentState(previous.title, previous.content, previous.checklistItems)
        updateUndoRedoStatus()
        autoSave()
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val current = _uiState.value
        undoStack.push(EditorSnapshot(current.title, current.content, current.checklistItems))
        val next = redoStack.pop()
        updateContentState(next.title, next.content, next.checklistItems)
        updateUndoRedoStatus()
        autoSave()
    }

    private fun updateContentState(title: String, content: String, items: List<ChecklistItem>) {
        val words = if (_uiState.value.isChecklist) {
            val combined = items.joinToString(" ") { it.text }.trim()
            if (combined.isEmpty()) 0 else combined.split("\\s+".toRegex()).size
        } else {
            if (content.isBlank()) 0 else content.trim().split("\\s+".toRegex()).size
        }
        val chars = if (_uiState.value.isChecklist) items.sumOf { it.text.length } else content.length
        val readingTime = if (words <= 200) 1 else (words / 200) + 1

        _uiState.value = _uiState.value.copy(
            title = title,
            content = content,
            checklistItems = items,
            wordCount = words,
            characterCount = chars,
            readingTimeMinutes = readingTime,
            modifiedAt = System.currentTimeMillis()
        )
    }

    fun onTitleChange(newTitle: String) {
        if (_uiState.value.title != newTitle) {
            pushUndoSnapshot()
            updateContentState(newTitle, _uiState.value.content, _uiState.value.checklistItems)
            autoSave()
        }
    }

    fun onContentChange(newContent: String) {
        if (_uiState.value.content != newContent) {
            pushUndoSnapshot()
            updateContentState(_uiState.value.title, newContent, _uiState.value.checklistItems)
            autoSave()
        }
    }

    fun setCategory(newCategory: String) {
        _uiState.value = _uiState.value.copy(category = newCategory)
        autoSave()
    }

    fun setColorIndex(newColorIndex: Int) {
        _uiState.value = _uiState.value.copy(colorIndex = newColorIndex)
        autoSave()
    }

    fun togglePin() {
        val newPinned = !_uiState.value.isPinned
        _uiState.value = _uiState.value.copy(isPinned = newPinned)
        autoSave()
    }

    fun toggleFavorite() {
        val newFav = !_uiState.value.isFavorite
        _uiState.value = _uiState.value.copy(isFavorite = newFav)
        autoSave()
    }

    fun toggleChecklistMode() {
        pushUndoSnapshot()
        val current = _uiState.value
        val newChecklistMode = !current.isChecklist

        if (newChecklistMode) {
            // Convert plain text lines into checklist items
            val lines = current.content.lines().filter { it.isNotBlank() }
            val items = if (lines.isNotEmpty()) {
                lines.map { line ->
                    val cleanText = line.removePrefix("- [ ] ").removePrefix("- [x] ").removePrefix("• ").removePrefix("- ")
                    ChecklistItem(text = cleanText)
                }
            } else {
                listOf(ChecklistItem(text = ""))
            }
            _uiState.value = current.copy(
                isChecklist = true,
                checklistItems = items
            )
        } else {
            // Convert checklist items into formatted bullet list
            val text = current.checklistItems.joinToString("\n") { item ->
                if (item.isChecked) "• [Completed] ${item.text}" else "• ${item.text}"
            }
            _uiState.value = current.copy(
                isChecklist = false,
                content = text
            )
        }
        updateUndoRedoStatus()
        autoSave()
    }

    fun addChecklistItem(afterId: String? = null) {
        pushUndoSnapshot()
        val currentItems = _uiState.value.checklistItems.toMutableList()
        val newItem = ChecklistItem(id = UUID.randomUUID().toString(), text = "")
        if (afterId != null) {
            val index = currentItems.indexOfFirst { it.id == afterId }
            if (index >= 0) {
                currentItems.add(index + 1, newItem)
            } else {
                currentItems.add(newItem)
            }
        } else {
            currentItems.add(newItem)
        }
        updateContentState(_uiState.value.title, _uiState.value.content, currentItems)
        updateUndoRedoStatus()
        autoSave()
    }

    fun updateChecklistItem(id: String, text: String) {
        val currentItems = _uiState.value.checklistItems.map {
            if (it.id == id) it.copy(text = text) else it
        }
        updateContentState(_uiState.value.title, _uiState.value.content, currentItems)
        autoSave()
    }

    fun toggleChecklistItemChecked(id: String) {
        pushUndoSnapshot()
        val currentItems = _uiState.value.checklistItems.map {
            if (it.id == id) it.copy(isChecked = !it.isChecked) else it
        }
        updateContentState(_uiState.value.title, _uiState.value.content, currentItems)
        updateUndoRedoStatus()
        autoSave()
    }

    fun removeChecklistItem(id: String) {
        pushUndoSnapshot()
        val currentItems = _uiState.value.checklistItems.filter { it.id != id }
        val finalItems = if (currentItems.isEmpty()) listOf(ChecklistItem(text = "")) else currentItems
        updateContentState(_uiState.value.title, _uiState.value.content, finalItems)
        updateUndoRedoStatus()
        autoSave()
    }

    fun autoSave() {
        val s = _uiState.value
        // Only save if there is a title or content or non-empty checklist items
        val hasContent = s.title.isNotBlank() || s.content.isNotBlank() || s.checklistItems.any { it.text.isNotBlank() }
        if (!hasContent && s.noteId <= 0L) {
            return
        }

        viewModelScope.launch {
            val note = Note(
                id = s.noteId,
                title = s.title,
                content = s.content,
                category = s.category,
                colorIndex = s.colorIndex,
                isPinned = s.isPinned,
                isFavorite = s.isFavorite,
                isArchived = s.isArchived,
                isTrash = s.isTrash,
                isChecklist = s.isChecklist,
                checklistItems = s.checklistItems,
                createdAt = s.createdAt,
                modifiedAt = System.currentTimeMillis()
            )
            val savedId = repository.saveNote(note)
            if (s.noteId <= 0L) {
                _uiState.value = _uiState.value.copy(noteId = savedId)
            }
        }
    }

    fun getShareableText(): String {
        val s = _uiState.value
        val sb = StringBuilder()
        if (s.title.isNotBlank()) {
            sb.appendLine(s.title)
            sb.appendLine("-------------------")
        }
        if (s.isChecklist) {
            s.checklistItems.forEach { item ->
                val box = if (item.isChecked) "[✓]" else "[ ]"
                sb.appendLine("$box ${item.text}")
            }
        } else {
            sb.append(s.content)
        }
        return sb.toString().trim()
    }

    fun moveToTrash() {
        val id = _uiState.value.noteId
        if (id > 0) {
            viewModelScope.launch {
                repository.moveToTrash(id)
            }
        }
    }

    fun duplicateNote(onDone: (Long) -> Unit) {
        val id = _uiState.value.noteId
        if (id > 0) {
            viewModelScope.launch {
                val newId = repository.duplicateNote(id)
                onDone(newId)
            }
        }
    }
}
