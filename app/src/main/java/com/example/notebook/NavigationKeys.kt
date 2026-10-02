package com.example.notebook

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object NoteListRoute : NavKey

@Serializable
data class NoteDetailRoute(
    val noteId: Long = -1L,
    val isChecklist: Boolean = false
) : NavKey
