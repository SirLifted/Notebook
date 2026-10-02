package com.example.notebook.model

import java.util.UUID

data class ChecklistItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val isChecked: Boolean = false
)

data class Note(
    val id: Long = 0L,
    val title: String = "",
    val content: String = "",
    val category: String = CATEGORY_PERSONAL,
    val colorIndex: Int = 0,
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val isTrash: Boolean = false,
    val isChecklist: Boolean = false,
    val checklistItems: List<ChecklistItem> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis()
) {
    val wordCount: Int
        get() {
            val textToCount = if (isChecklist) {
                checklistItems.joinToString(" ") { it.text }
            } else {
                content
            }
            if (textToCount.isBlank()) return 0
            return textToCount.trim().split("\\s+".toRegex()).size
        }

    val characterCount: Int
        get() {
            return if (isChecklist) {
                checklistItems.sumOf { it.text.length }
            } else {
                content.length
            }
        }

    val readingTimeMinutes: Int
        get() {
            val words = wordCount
            return if (words <= 200) 1 else (words / 200) + 1
        }

    val checklistProgress: Pair<Int, Int>
        get() {
            val completed = checklistItems.count { it.isChecked }
            return Pair(completed, checklistItems.size)
        }

    companion object {
        const val CATEGORY_ALL = "All"
        const val CATEGORY_PERSONAL = "Personal"
        const val CATEGORY_WORK = "Work"
        const val CATEGORY_IDEAS = "Ideas"
        const val CATEGORY_STUDY = "Study"
        const val CATEGORY_TODO = "To-Do"

        val DEFAULT_CATEGORIES = listOf(
            CATEGORY_PERSONAL,
            CATEGORY_WORK,
            CATEGORY_IDEAS,
            CATEGORY_STUDY,
            CATEGORY_TODO
        )

        fun serializeChecklist(items: List<ChecklistItem>): String {
            if (items.isEmpty()) return ""
            return items.joinToString("\n<ITEM>\n") { item ->
                val escapedText = item.text.replace("\\", "\\\\").replace("\n", "\\n")
                val escapedId = item.id.replace("\\", "\\\\")
                "${if (item.isChecked) "1" else "0"}|$escapedId|$escapedText"
            }
        }

        fun deserializeChecklist(data: String): List<ChecklistItem> {
            if (data.isBlank()) return emptyList()
            val list = mutableListOf<ChecklistItem>()
            val items = data.split("\n<ITEM>\n")
            for (entry in items) {
                val parts = entry.split("|", limit = 3)
                if (parts.size == 3) {
                    val isChecked = parts[0] == "1" || parts[0].equals("true", ignoreCase = true)
                    val id = parts[1].replace("\\\\", "\\")
                    val text = parts[2].replace("\\n", "\n").replace("\\\\", "\\")
                    list.add(ChecklistItem(id = id, text = text, isChecked = isChecked))
                }
            }
            return list
        }
    }
}
