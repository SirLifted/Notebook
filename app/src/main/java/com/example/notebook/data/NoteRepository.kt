package com.example.notebook.data

import android.content.Context
import com.example.notebook.model.Note
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NoteRepository(context: Context) {
    private val dbHelper = NoteDatabaseHelper.getInstance(context)
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    init {
        refreshNotes()
    }

    fun refreshNotes() {
        repositoryScope.launch {
            val list = dbHelper.getAllNotes()
            _notes.value = list
        }
    }

    suspend fun getNote(id: Long): Note? = withContext(Dispatchers.IO) {
        dbHelper.getNoteById(id)
    }

    suspend fun saveNote(note: Note): Long = withContext(Dispatchers.IO) {
        val id = if (note.id <= 0L) {
            dbHelper.insertNote(note)
        } else {
            dbHelper.updateNote(note)
            note.id
        }
        refreshNotes()
        id
    }

    suspend fun togglePin(id: Long): Boolean = withContext(Dispatchers.IO) {
        val result = dbHelper.togglePin(id)
        refreshNotes()
        result
    }

    suspend fun toggleFavorite(id: Long): Boolean = withContext(Dispatchers.IO) {
        val result = dbHelper.toggleFavorite(id)
        refreshNotes()
        result
    }

    suspend fun moveToTrash(id: Long) = withContext(Dispatchers.IO) {
        dbHelper.moveToTrash(id)
        refreshNotes()
    }

    suspend fun restoreFromTrash(id: Long) = withContext(Dispatchers.IO) {
        dbHelper.restoreFromTrash(id)
        refreshNotes()
    }

    suspend fun toggleArchive(id: Long): Boolean = withContext(Dispatchers.IO) {
        val result = dbHelper.toggleArchive(id)
        refreshNotes()
        result
    }

    suspend fun deletePermanently(id: Long) = withContext(Dispatchers.IO) {
        dbHelper.deletePermanently(id)
        refreshNotes()
    }

    suspend fun emptyTrash() = withContext(Dispatchers.IO) {
        dbHelper.emptyTrash()
        refreshNotes()
    }

    suspend fun duplicateNote(id: Long): Long = withContext(Dispatchers.IO) {
        val newId = dbHelper.duplicateNote(id)
        refreshNotes()
        newId
    }

    companion object {
        @Volatile
        private var INSTANCE: NoteRepository? = null

        fun getInstance(context: Context): NoteRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NoteRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
