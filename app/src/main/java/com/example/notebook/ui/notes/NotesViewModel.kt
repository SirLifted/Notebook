package com.example.notebook.ui.notes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.notebook.data.NoteRepository
import com.example.notebook.model.Note
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class NoteSortOrder(val displayName: String) {
    MODIFIED_DESC("Recently Modified"),
    CREATED_DESC("Recently Created"),
    TITLE_ASC("Title (A to Z)"),
    TITLE_DESC("Title (Z to A)")
}

enum class NotesViewMode {
    GRID,
    LIST
}

data class NotesUiState(
    val allNotes: List<Note> = emptyList(),
    val displayedPinnedNotes: List<Note> = emptyList(),
    val displayedOtherNotes: List<Note> = emptyList(),
    val selectedCategory: String = Note.CATEGORY_ALL,
    val searchQuery: String = "",
    val sortOrder: NoteSortOrder = NoteSortOrder.MODIFIED_DESC,
    val viewMode: NotesViewMode = NotesViewMode.GRID,
    val categoryCounts: Map<String, Int> = emptyMap(),
    val trashCount: Int = 0,
    val archiveCount: Int = 0,
    val favoritesCount: Int = 0,
    val lastDeletedNote: Note? = null
)

data class FilterParams(
    val selectedCategory: String,
    val searchQuery: String,
    val sortOrder: NoteSortOrder,
    val viewMode: NotesViewMode
)

class NotesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = NoteRepository.getInstance(application)

    private val _selectedCategory = MutableStateFlow(Note.CATEGORY_ALL)
    private val _searchQuery = MutableStateFlow("")
    private val _sortOrder = MutableStateFlow(NoteSortOrder.MODIFIED_DESC)
    private val _viewMode = MutableStateFlow(NotesViewMode.GRID)
    private val _lastDeletedNote = MutableStateFlow<Note?>(null)

    private val _filterParams = combine(
        _selectedCategory,
        _searchQuery,
        _sortOrder,
        _viewMode
    ) { category, query, sort, mode ->
        FilterParams(category, query, sort, mode)
    }

    val uiState: StateFlow<NotesUiState> = combine(
        repository.notes,
        _filterParams,
        _lastDeletedNote
    ) { allNotes, filter, lastDeleted ->
        val activeNotes = allNotes.filter { !it.isTrash && !it.isArchived }
        val trashNotes = allNotes.filter { it.isTrash }
        val archiveNotes = allNotes.filter { !it.isTrash && it.isArchived }
        val favoriteNotes = activeNotes.filter { it.isFavorite }

        // Category counts for active notes
        val counts = mutableMapOf<String, Int>()
        counts[Note.CATEGORY_ALL] = activeNotes.size
        Note.DEFAULT_CATEGORIES.forEach { cat ->
            counts[cat] = activeNotes.count { it.category == cat }
        }

        // Filter based on selected category / view tab
        val filteredNotes = when (filter.selectedCategory) {
            Note.CATEGORY_ALL -> activeNotes
            "Favorites" -> favoriteNotes
            "Archive" -> archiveNotes
            "Trash" -> trashNotes
            else -> activeNotes.filter { it.category == filter.selectedCategory }
        }

        // Search query filter
        val searchFiltered = if (filter.searchQuery.isBlank()) {
            filteredNotes
        } else {
            val q = filter.searchQuery.trim().lowercase()
            filteredNotes.filter { note ->
                note.title.lowercase().contains(q) ||
                note.content.lowercase().contains(q) ||
                note.checklistItems.any { it.text.lowercase().contains(q) }
            }
        }

        // Sort notes
        val sortedNotes = when (filter.sortOrder) {
            NoteSortOrder.MODIFIED_DESC -> searchFiltered.sortedByDescending { it.modifiedAt }
            NoteSortOrder.CREATED_DESC -> searchFiltered.sortedByDescending { it.createdAt }
            NoteSortOrder.TITLE_ASC -> searchFiltered.sortedBy { it.title.lowercase() }
            NoteSortOrder.TITLE_DESC -> searchFiltered.sortedByDescending { it.title.lowercase() }
        }

        // When viewing regular categories, separate pinned and others
        val isStandardView = filter.selectedCategory != "Trash" && filter.selectedCategory != "Archive"
        val pinned = if (isStandardView && filter.searchQuery.isBlank()) {
            sortedNotes.filter { it.isPinned }
        } else {
            emptyList()
        }
        val others = if (isStandardView && filter.searchQuery.isBlank()) {
            sortedNotes.filter { !it.isPinned }
        } else {
            sortedNotes
        }

        NotesUiState(
            allNotes = allNotes,
            displayedPinnedNotes = pinned,
            displayedOtherNotes = others,
            selectedCategory = filter.selectedCategory,
            searchQuery = filter.searchQuery,
            sortOrder = filter.sortOrder,
            viewMode = filter.viewMode,
            categoryCounts = counts,
            trashCount = trashNotes.size,
            archiveCount = archiveNotes.size,
            favoritesCount = favoriteNotes.size,
            lastDeletedNote = lastDeleted
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        NotesUiState()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSortOrder(order: NoteSortOrder) {
        _sortOrder.value = order
    }

    fun toggleViewMode() {
        _viewMode.value = if (_viewMode.value == NotesViewMode.GRID) NotesViewMode.LIST else NotesViewMode.GRID
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            repository.togglePin(note.id)
        }
    }

    fun toggleFavorite(note: Note) {
        viewModelScope.launch {
            repository.toggleFavorite(note.id)
        }
    }

    fun moveToTrash(note: Note) {
        _lastDeletedNote.value = note
        viewModelScope.launch {
            repository.moveToTrash(note.id)
        }
    }

    fun undoDelete() {
        val note = _lastDeletedNote.value ?: return
        viewModelScope.launch {
            repository.restoreFromTrash(note.id)
            _lastDeletedNote.value = null
        }
    }

    fun restoreFromTrash(note: Note) {
        viewModelScope.launch {
            repository.restoreFromTrash(note.id)
        }
    }

    fun toggleArchive(note: Note) {
        viewModelScope.launch {
            repository.toggleArchive(note.id)
        }
    }

    fun deletePermanently(note: Note) {
        viewModelScope.launch {
            repository.deletePermanently(note.id)
            if (_lastDeletedNote.value?.id == note.id) {
                _lastDeletedNote.value = null
            }
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            repository.emptyTrash()
            _lastDeletedNote.value = null
        }
    }

    fun duplicateNote(note: Note) {
        viewModelScope.launch {
            repository.duplicateNote(note.id)
        }
    }

    fun toggleChecklistItem(note: Note, itemId: String) {
        val updatedItems = note.checklistItems.map {
            if (it.id == itemId) it.copy(isChecked = !it.isChecked) else it
        }
        viewModelScope.launch {
            repository.saveNote(note.copy(checklistItems = updatedItems))
        }
    }
}
