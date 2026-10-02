# 📓 Notebook - Modern Android Note & To-Do App

**Notebook** is a modern, fast, and feature-rich Android notebook and task-tracking app built with **Jetpack Compose**, **Material 3 (Material You)**, and native **SQLite** local persistence.

---

## ✨ Features

- **🎨 Material 3 & Custom Pastel Themes**:
  - 8 aesthetic pastel color themes (Default, Lemon, Mint, Sky, Lavender, Peach, Rose, Almond) for both Light and Dark mode.
- **📝 Text & Checklist / Task Modes**:
  - Seamlessly switch between freeform markdown-friendly notes and interactive checklists.
  - Check off items directly from the note card or the editor.
  - Visual completion progress bar (`X of Y done`).
- **📌 Pinning & Favorites**:
  - Pin important notes to keep them organized at the very top.
  - Star notes as favorites for quick access.
- **🔍 Real-Time Search & Category Filtering**:
  - Instant search across titles, contents, and checklist items.
  - Filter by categories: **All**, **Personal**, **Work**, **Ideas**, **Study**, **To-Do**, **⭐ Favorites**, **📦 Archive**, and **🗑️ Trash**.
- **🗂️ Sorting & View Options**:
  - Sort by Recently Modified, Recently Created, Title (A-Z), and Title (Z-A).
  - Toggle between **2-Column Grid** and **1-Column List** views.
- **⚡ Undo & Redo History**:
  - Full undo/redo history support during editing.
- **💾 Real-time Auto-Save**:
  - Never lose your notes: edits and checklist changes are auto-saved in the background.
- **📊 Live Note Statistics**:
  - Word count, character count, and estimated reading time.
  - Detailed metadata sheet with created and modified timestamps.
- **📤 Sharing & Duplication**:
  - Instant export and sharing via the Android Share Sheet.
  - One-tap note duplication.
- **🗑️ Soft Delete & Trash Management**:
  - Move notes to trash with instant **Undo Snackbar**.
  - Restore notes or permanently delete them with bulk "Empty Trash" option.

---

## 🏗️ Architecture & Tech Stack

- **UI**: 100% Declarative Jetpack Compose with Material 3 components (`Scaffold`, `TopAppBar`, `Card`, `FilterChip`, `ExtendedFloatingActionButton`, `LinearProgressIndicator`, etc.).
- **Navigation**: Modern **AndroidX Navigation 3** (`NavDisplay`, `entryProvider`, `rememberNavBackStack`, type-safe `@Serializable NavKey`).
- **State Management**: Android Architecture Components `ViewModel` with Kotlin Coroutines `StateFlow` and `combine` for reactive state flows.
- **Persistence**: Zero-overhead native Android SQLite database helper (`SQLiteOpenHelper`) with indexes, auto-migrations, and pre-seeded sample notes.
- **Language**: Kotlin 2.3 with Java 21 toolchain and Android SDK 36.

---

## 📁 Project Structure

```
Notebook/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/notebook/
│   │   │   │   ├── MainActivity.kt               # Main entry point with edge-to-edge support
│   │   │   │   ├── Navigation.kt                 # Navigation 3 backstack and entry provider
│   │   │   │   ├── NavigationKeys.kt             # Type-safe navigation routes
│   │   │   │   ├── data/
│   │   │   │   │   ├── NoteDatabaseHelper.kt     # SQLite database, tables, indexes & sample seed data
│   │   │   │   │   └── NoteRepository.kt         # Coroutine repository emitting reactive StateFlows
│   │   │   │   ├── model/
│   │   │   │   │   └── Note.kt                   # Note & ChecklistItem models with pure Kotlin serialization
│   │   │   │   ├── theme/
│   │   │   │   │   ├── Color.kt & Theme.kt       # Material 3 dynamic color scheme
│   │   │   │   │   └── NoteColors.kt             # 8 pastel card color palettes for light/dark mode
│   │   │   │   └── ui/
│   │   │   │       ├── components/
│   │   │   │       │   └── NoteCard.kt           # Note card with checklist progress & action menu
│   │   │   │       ├── editor/
│   │   │   │       │   ├── NoteEditorScreen.kt   # Note editor with undo/redo, checklists, color picker
│   │   │   │       │   └── NoteEditorViewModel.kt# Editor state & auto-save management
│   │   │   │       └── notes/
│   │   │   │           ├── NotesListScreen.kt    # Dashboard with search, category tabs, grid/list view
│   │   │   │           └── NotesViewModel.kt     # Reactive filtering, sorting, and trash handling
│   │   │   └── res/
│   │   │       └── drawable/                     # Custom XML vector icons (pin, sort, palette, etc.)
│   │   └── test/
│   │       └── java/com/example/notebook/model/  # Unit tests for Note, Checklist, and metrics
│   └── build.gradle.kts                          # App build configuration & dependencies
└── gradlew.bat                                   # Gradle wrapper
```

---

## 🚀 Building & Running

### Build Debug APK:
```bash
.\gradlew.bat assembleDebug
```
Output APK location:
```
Notebook\app\build\outputs\apk\debug\app-debug.apk
```

### Run Unit Tests:
```bash
.\gradlew.bat testDebugUnitTest
```

### Install onto Connected Device / Emulator:
```bash
adb install -r Notebook\app\build\outputs\apk\debug\app-debug.apk
```
Or with Android CLI:
```bash
android run --apks="Notebook\app\build\outputs\apk\debug\app-debug.apk"
```
