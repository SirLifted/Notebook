package com.example.notebook.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.notebook.model.ChecklistItem
import com.example.notebook.model.Note

class NoteDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "notebook.db"
        const val DATABASE_VERSION = 1

        const val TABLE_NOTES = "notes"
        const val COL_ID = "_id"
        const val COL_TITLE = "title"
        const val COL_CONTENT = "content"
        const val COL_CATEGORY = "category"
        const val COL_COLOR_INDEX = "color_index"
        const val COL_IS_PINNED = "is_pinned"
        const val COL_IS_FAVORITE = "is_favorite"
        const val COL_IS_ARCHIVED = "is_archived"
        const val COL_IS_TRASH = "is_trash"
        const val COL_IS_CHECKLIST = "is_checklist"
        const val COL_CHECKLIST_DATA = "checklist_data"
        const val COL_CREATED_AT = "created_at"
        const val COL_MODIFIED_AT = "modified_at"

        @Volatile
        private var INSTANCE: NoteDatabaseHelper? = null

        fun getInstance(context: Context): NoteDatabaseHelper {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NoteDatabaseHelper(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NOTES (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TITLE TEXT NOT NULL,
                $COL_CONTENT TEXT NOT NULL,
                $COL_CATEGORY TEXT NOT NULL,
                $COL_COLOR_INDEX INTEGER DEFAULT 0,
                $COL_IS_PINNED INTEGER DEFAULT 0,
                $COL_IS_FAVORITE INTEGER DEFAULT 0,
                $COL_IS_ARCHIVED INTEGER DEFAULT 0,
                $COL_IS_TRASH INTEGER DEFAULT 0,
                $COL_IS_CHECKLIST INTEGER DEFAULT 0,
                $COL_CHECKLIST_DATA TEXT,
                $COL_CREATED_AT INTEGER NOT NULL,
                $COL_MODIFIED_AT INTEGER NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTableQuery)

        // Create indexes for fast filtering and sorting
        db.execSQL("CREATE INDEX idx_notes_trash ON $TABLE_NOTES ($COL_IS_TRASH)")
        db.execSQL("CREATE INDEX idx_notes_pinned ON $TABLE_NOTES ($COL_IS_PINNED)")
        db.execSQL("CREATE INDEX idx_notes_category ON $TABLE_NOTES ($COL_CATEGORY)")
        db.execSQL("CREATE INDEX idx_notes_modified ON $TABLE_NOTES ($COL_MODIFIED_AT DESC)")

        // Insert initial sample notes
        insertSampleNotes(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Future migrations
    }

    private fun insertSampleNotes(db: SQLiteDatabase) {
        val now = System.currentTimeMillis()

        // 1. Welcome Note
        val welcomeNote = ContentValues().apply {
            put(COL_TITLE, "Welcome to Notebook! 📝✨")
            put(
                COL_CONTENT,
                """
                Your new personal notebook and task companion!

                Here are a few quick tips to get started:
                • Tap '+ Note' to write down your thoughts or '+ Task' for checklists.
                • Pin critical notes to keep them right at the top.
                • Personalize with 8 soothing pastel card colors.
                • Organize your notes by Category (Personal, Work, Ideas, Study, To-Do).
                • Real-time search by title and content.
                • Tap the share button to instantly export or send notes.
                • Never worry about saving: changes are auto-saved in real time!
                """.trimIndent()
            )
            put(COL_CATEGORY, Note.CATEGORY_IDEAS)
            put(COL_COLOR_INDEX, 1) // Lemon
            put(COL_IS_PINNED, 1)
            put(COL_IS_FAVORITE, 1)
            put(COL_IS_ARCHIVED, 0)
            put(COL_IS_TRASH, 0)
            put(COL_IS_CHECKLIST, 0)
            put(COL_CHECKLIST_DATA, "")
            put(COL_CREATED_AT, now - 100000)
            put(COL_MODIFIED_AT, now - 50000)
        }
        db.insert(TABLE_NOTES, null, welcomeNote)

        // 2. Weekly Groceries Checklist
        val groceryItems = listOf(
            ChecklistItem(text = "Organic avocados (3 pcs)", isChecked = true),
            ChecklistItem(text = "Unsweetened almond milk", isChecked = true),
            ChecklistItem(text = "Greek yogurt & wildflower honey", isChecked = false),
            ChecklistItem(text = "Fresh blueberries & raspberries", isChecked = false),
            ChecklistItem(text = "Artisan sourdough bread", isChecked = false),
            ChecklistItem(text = "Cold pressed extra virgin olive oil", isChecked = false)
        )
        val groceryNote = ContentValues().apply {
            put(COL_TITLE, "Weekly Groceries & Market Run 🛒")
            put(COL_CONTENT, "")
            put(COL_CATEGORY, Note.CATEGORY_TODO)
            put(COL_COLOR_INDEX, 2) // Mint
            put(COL_IS_PINNED, 1)
            put(COL_IS_FAVORITE, 0)
            put(COL_IS_ARCHIVED, 0)
            put(COL_IS_TRASH, 0)
            put(COL_IS_CHECKLIST, 1)
            put(COL_CHECKLIST_DATA, Note.serializeChecklist(groceryItems))
            put(COL_CREATED_AT, now - 80000)
            put(COL_MODIFIED_AT, now - 40000)
        }
        db.insert(TABLE_NOTES, null, groceryNote)

        // 3. Work Sprint Roadmap
        val workNote = ContentValues().apply {
            put(COL_TITLE, "Q4 Product Strategy & Roadmap 🚀")
            put(
                COL_CONTENT,
                """
                Key Milestones:
                1. Sprint Planning & Architecture Review - Complete.
                2. Design System overhaul & accessibility audit.
                3. Beta release to pilot users by end of month.
                4. Performance profiling: ensure 60fps animations.

                Next Sync: Thursday 10:00 AM with team.
                """.trimIndent()
            )
            put(COL_CATEGORY, Note.CATEGORY_WORK)
            put(COL_COLOR_INDEX, 3) // Sky
            put(COL_IS_PINNED, 0)
            put(COL_IS_FAVORITE, 1)
            put(COL_IS_ARCHIVED, 0)
            put(COL_IS_TRASH, 0)
            put(COL_IS_CHECKLIST, 0)
            put(COL_CHECKLIST_DATA, "")
            put(COL_CREATED_AT, now - 60000)
            put(COL_MODIFIED_AT, now - 30000)
        }
        db.insert(TABLE_NOTES, null, workNote)

        // 4. Study / Reading List
        val studyNote = ContentValues().apply {
            put(COL_TITLE, "Essential Reading & Book Notes 📚")
            put(
                COL_CONTENT,
                """
                Recommended Reads:
                • Atomic Habits - James Clear
                  "You do not rise to the level of your goals. You fall to the level of your systems."
                • Deep Work - Cal Newport
                  Focus blocks and uninterrupted concentration create compounding value.
                • Design of Everyday Things - Don Norman
                  Affordance, signifiers, and intuitive feedback.
                """.trimIndent()
            )
            put(COL_CATEGORY, Note.CATEGORY_STUDY)
            put(COL_COLOR_INDEX, 4) // Lavender
            put(COL_IS_PINNED, 0)
            put(COL_IS_FAVORITE, 0)
            put(COL_IS_ARCHIVED, 0)
            put(COL_IS_TRASH, 0)
            put(COL_IS_CHECKLIST, 0)
            put(COL_CHECKLIST_DATA, "")
            put(COL_CREATED_AT, now - 40000)
            put(COL_MODIFIED_AT, now - 20000)
        }
        db.insert(TABLE_NOTES, null, studyNote)

        // 5. Weekend Roadtrip Checklist
        val tripItems = listOf(
            ChecklistItem(text = "Check tire pressures and oil level", isChecked = true),
            ChecklistItem(text = "Download offline navigation maps", isChecked = true),
            ChecklistItem(text = "Pack warm layers & hiking boots", isChecked = false),
            ChecklistItem(text = "Camera, spare batteries & portable charger", isChecked = false),
            ChecklistItem(text = "First aid travel pouch & sunscreen", isChecked = false)
        )
        val roadtripNote = ContentValues().apply {
            put(COL_TITLE, "Weekend Roadtrip Checklist 🌲🚗")
            put(COL_CONTENT, "")
            put(COL_CATEGORY, Note.CATEGORY_PERSONAL)
            put(COL_COLOR_INDEX, 5) // Peach
            put(COL_IS_PINNED, 0)
            put(COL_IS_FAVORITE, 0)
            put(COL_IS_ARCHIVED, 0)
            put(COL_IS_TRASH, 0)
            put(COL_IS_CHECKLIST, 1)
            put(COL_CHECKLIST_DATA, Note.serializeChecklist(tripItems))
            put(COL_CREATED_AT, now - 20000)
            put(COL_MODIFIED_AT, now - 10000)
        }
        db.insert(TABLE_NOTES, null, roadtripNote)
    }

    fun getAllNotes(): List<Note> {
        val list = mutableListOf<Note>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NOTES,
            null,
            null,
            null,
            null,
            null,
            "$COL_IS_PINNED DESC, $COL_MODIFIED_AT DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(cursorToNote(it))
            }
        }
        return list
    }

    fun getNoteById(id: Long): Note? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NOTES,
            null,
            "$COL_ID = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )
        cursor.use {
            if (it.moveToFirst()) {
                return cursorToNote(it)
            }
        }
        return null
    }

    fun insertNote(note: Note): Long {
        val db = writableDatabase
        val values = noteToContentValues(note)
        return db.insert(TABLE_NOTES, null, values)
    }

    fun updateNote(note: Note): Int {
        val db = writableDatabase
        val values = noteToContentValues(note.copy(modifiedAt = System.currentTimeMillis()))
        return db.update(TABLE_NOTES, values, "$COL_ID = ?", arrayOf(note.id.toString()))
    }

    fun togglePin(id: Long): Boolean {
        val note = getNoteById(id) ?: return false
        val newPinned = !note.isPinned
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_IS_PINNED, if (newPinned) 1 else 0)
            put(COL_MODIFIED_AT, System.currentTimeMillis())
        }
        db.update(TABLE_NOTES, values, "$COL_ID = ?", arrayOf(id.toString()))
        return newPinned
    }

    fun toggleFavorite(id: Long): Boolean {
        val note = getNoteById(id) ?: return false
        val newFav = !note.isFavorite
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_IS_FAVORITE, if (newFav) 1 else 0)
            put(COL_MODIFIED_AT, System.currentTimeMillis())
        }
        db.update(TABLE_NOTES, values, "$COL_ID = ?", arrayOf(id.toString()))
        return newFav
    }

    fun moveToTrash(id: Long) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_IS_TRASH, 1)
            put(COL_IS_PINNED, 0)
            put(COL_MODIFIED_AT, System.currentTimeMillis())
        }
        db.update(TABLE_NOTES, values, "$COL_ID = ?", arrayOf(id.toString()))
    }

    fun restoreFromTrash(id: Long) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_IS_TRASH, 0)
            put(COL_MODIFIED_AT, System.currentTimeMillis())
        }
        db.update(TABLE_NOTES, values, "$COL_ID = ?", arrayOf(id.toString()))
    }

    fun toggleArchive(id: Long): Boolean {
        val note = getNoteById(id) ?: return false
        val newArchived = !note.isArchived
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_IS_ARCHIVED, if (newArchived) 1 else 0)
            if (newArchived) put(COL_IS_PINNED, 0)
            put(COL_MODIFIED_AT, System.currentTimeMillis())
        }
        db.update(TABLE_NOTES, values, "$COL_ID = ?", arrayOf(id.toString()))
        return newArchived
    }

    fun deletePermanently(id: Long): Int {
        val db = writableDatabase
        return db.delete(TABLE_NOTES, "$COL_ID = ?", arrayOf(id.toString()))
    }

    fun emptyTrash(): Int {
        val db = writableDatabase
        return db.delete(TABLE_NOTES, "$COL_IS_TRASH = 1", null)
    }

    fun duplicateNote(id: Long): Long {
        val note = getNoteById(id) ?: return -1L
        val copy = note.copy(
            id = 0L,
            title = if (note.title.isNotBlank()) "${note.title} (Copy)" else "Copy",
            isPinned = false,
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis()
        )
        return insertNote(copy)
    }

    private fun noteToContentValues(note: Note): ContentValues {
        return ContentValues().apply {
            put(COL_TITLE, note.title)
            put(COL_CONTENT, note.content)
            put(COL_CATEGORY, note.category)
            put(COL_COLOR_INDEX, note.colorIndex)
            put(COL_IS_PINNED, if (note.isPinned) 1 else 0)
            put(COL_IS_FAVORITE, if (note.isFavorite) 1 else 0)
            put(COL_IS_ARCHIVED, if (note.isArchived) 1 else 0)
            put(COL_IS_TRASH, if (note.isTrash) 1 else 0)
            put(COL_IS_CHECKLIST, if (note.isChecklist) 1 else 0)
            put(COL_CHECKLIST_DATA, Note.serializeChecklist(note.checklistItems))
            put(COL_CREATED_AT, note.createdAt)
            put(COL_MODIFIED_AT, note.modifiedAt)
        }
    }

    private fun cursorToNote(cursor: Cursor): Note {
        val id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID))
        val title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE)) ?: ""
        val content = cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTENT)) ?: ""
        val category = cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY)) ?: Note.CATEGORY_PERSONAL
        val colorIndex = cursor.getInt(cursor.getColumnIndexOrThrow(COL_COLOR_INDEX))
        val isPinned = cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_PINNED)) == 1
        val isFavorite = cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_FAVORITE)) == 1
        val isArchived = cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_ARCHIVED)) == 1
        val isTrash = cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_TRASH)) == 1
        val isChecklist = cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_CHECKLIST)) == 1
        val checklistData = cursor.getString(cursor.getColumnIndexOrThrow(COL_CHECKLIST_DATA)) ?: ""
        val createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COL_CREATED_AT))
        val modifiedAt = cursor.getLong(cursor.getColumnIndexOrThrow(COL_MODIFIED_AT))

        return Note(
            id = id,
            title = title,
            content = content,
            category = category,
            colorIndex = colorIndex,
            isPinned = isPinned,
            isFavorite = isFavorite,
            isArchived = isArchived,
            isTrash = isTrash,
            isChecklist = isChecklist,
            checklistItems = Note.deserializeChecklist(checklistData),
            createdAt = createdAt,
            modifiedAt = modifiedAt
        )
    }
}
