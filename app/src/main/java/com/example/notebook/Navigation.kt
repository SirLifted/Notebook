package com.example.notebook

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.notebook.ui.editor.NoteEditorScreen
import com.example.notebook.ui.notes.NotesListScreen

@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(NoteListRoute)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<NoteListRoute> {
                NotesListScreen(
                    onOpenNote = { noteId ->
                        backStack.add(NoteDetailRoute(noteId = noteId))
                    },
                    onCreateNote = { isChecklist ->
                        backStack.add(NoteDetailRoute(noteId = -1L, isChecklist = isChecklist))
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding()
                )
            }
            entry<NoteDetailRoute> { key ->
                NoteEditorScreen(
                    noteId = key.noteId,
                    isChecklistInitial = key.isChecklist,
                    onBack = {
                        backStack.removeLastOrNull()
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding()
                )
            }
        }
    )
}
