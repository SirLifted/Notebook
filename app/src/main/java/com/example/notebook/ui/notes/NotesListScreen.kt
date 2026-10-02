package com.example.notebook.ui.notes

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notebook.R
import com.example.notebook.model.Note
import com.example.notebook.ui.components.NoteCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    onOpenNote: (Long) -> Unit,
    onCreateNote: (Boolean) -> Unit,
    viewModel: NotesViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showSortMenu by remember { mutableStateOf(false) }
    var showEmptyTrashConfirm by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    // Observe note deletion to show Undo Snackbar
    LaunchedEffect(state.lastDeletedNote) {
        state.lastDeletedNote?.let { deleted ->
            val result = snackbarHostState.showSnackbar(
                message = "\"${deleted.title.ifBlank { "Note" }}\" moved to trash",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoDelete()
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 4.dp)
            ) {
                // App Branding & Actions Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showAboutDialog = true }
                    ) {
                        Image(
                            painter = painterResource(R.drawable.app_logo),
                            contentDescription = "Journal App Logo",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Journal",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-0.5).sp
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "${state.allNotes.count { !it.isTrash && !it.isArchived }}",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            Text(
                                text = "Notebook & Tasks",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Toggle View Mode (Grid vs List)
                        IconButton(onClick = { viewModel.toggleViewMode() }) {
                            Icon(
                                painter = painterResource(
                                    if (state.viewMode == NotesViewMode.GRID) R.drawable.ic_list_view else R.drawable.ic_grid_view
                                ),
                                contentDescription = "Toggle View Mode",
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Sort Menu Button
                        Box {
                            IconButton(onClick = { showSortMenu = true }) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_sort),
                                    contentDescription = "Sort Notes",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                NoteSortOrder.entries.forEach { order ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                order.displayName,
                                                fontWeight = if (state.sortOrder == order) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        trailingIcon = {
                                            if (state.sortOrder == order) {
                                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                            }
                                        },
                                        onClick = {
                                            viewModel.setSortOrder(order)
                                            showSortMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search notes and tasks...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    },
                    trailingIcon = {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search", modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    shape = RoundedCornerShape(28.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Category Filter Pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val categories = listOf(
                        Note.CATEGORY_ALL to (state.categoryCounts[Note.CATEGORY_ALL] ?: 0),
                        Note.CATEGORY_PERSONAL to (state.categoryCounts[Note.CATEGORY_PERSONAL] ?: 0),
                        Note.CATEGORY_WORK to (state.categoryCounts[Note.CATEGORY_WORK] ?: 0),
                        Note.CATEGORY_IDEAS to (state.categoryCounts[Note.CATEGORY_IDEAS] ?: 0),
                        Note.CATEGORY_STUDY to (state.categoryCounts[Note.CATEGORY_STUDY] ?: 0),
                        Note.CATEGORY_TODO to (state.categoryCounts[Note.CATEGORY_TODO] ?: 0),
                        "Favorites" to state.favoritesCount,
                        "Archive" to state.archiveCount,
                        "Trash" to state.trashCount
                    )

                    categories.forEach { (cat, count) ->
                        val selected = state.selectedCategory == cat
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.setSelectedCategory(cat) },
                            label = {
                                Text(
                                    text = if (count > 0 && cat != Note.CATEGORY_ALL) "$cat ($count)" else cat,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (state.selectedCategory != "Trash") {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Quick Task / Checklist FAB
                    SmallFloatingActionButton(
                        onClick = { onCreateNote(true) },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Icon(painterResource(R.drawable.ic_checklist), contentDescription = "Add Checklist", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Task", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    // Main Create Note FAB
                    ExtendedFloatingActionButton(
                        onClick = { onCreateNote(false) },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        icon = { Icon(Icons.Default.Add, contentDescription = "New Note") },
                        text = { Text("Note", fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Trash Banner
            if (state.selectedCategory == "Trash") {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Notes in trash (${state.trashCount})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        if (state.trashCount > 0) {
                            TextButton(onClick = { showEmptyTrashConfirm = true }) {
                                Text("Empty Trash", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            val totalNotesCount = state.displayedPinnedNotes.size + state.displayedOtherNotes.size

            if (totalNotesCount == 0) {
                // Empty State
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = when (state.selectedCategory) {
                                "Trash" -> Icons.Default.Delete
                                "Favorites" -> Icons.Default.Star
                                else -> Icons.Default.Edit
                            },
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = when {
                                state.searchQuery.isNotBlank() -> "No notes match \"${state.searchQuery}\""
                                state.selectedCategory == "Trash" -> "Trash is empty"
                                state.selectedCategory == "Favorites" -> "No favorite notes yet"
                                state.selectedCategory == "Archive" -> "No archived notes"
                                else -> "No notes in ${state.selectedCategory}"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (state.selectedCategory != "Trash" && state.searchQuery.isBlank()) {
                            Text(
                                text = "Tap '+ Note' or '+ Task' to create your first note",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            } else {
                // Display Notes (Grid or List)
                if (state.viewMode == NotesViewMode.GRID) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 88.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Pinned Section
                        if (state.displayedPinnedNotes.isNotEmpty()) {
                            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                                SectionHeader(title = "PINNED", icon = R.drawable.ic_pin_filled)
                            }
                            items(state.displayedPinnedNotes, key = { it.id }) { note ->
                                NoteCard(
                                    note = note,
                                    onClick = { onOpenNote(note.id) },
                                    onTogglePin = { viewModel.togglePin(note) },
                                    onToggleFavorite = { viewModel.toggleFavorite(note) },
                                    onToggleChecklistItem = { itemId -> viewModel.toggleChecklistItem(note, itemId) },
                                    onShare = { shareNoteCard(context, note) },
                                    onDuplicate = { viewModel.duplicateNote(note) },
                                    onArchive = { viewModel.toggleArchive(note) },
                                    onDelete = { viewModel.moveToTrash(note) },
                                    onRestore = { viewModel.restoreFromTrash(note) },
                                    onPermanentDelete = { viewModel.deletePermanently(note) }
                                )
                            }
                        }

                        // Other Notes Section
                        if (state.displayedOtherNotes.isNotEmpty()) {
                            if (state.displayedPinnedNotes.isNotEmpty()) {
                                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                                    SectionHeader(title = "OTHERS", icon = null)
                                }
                            }
                            items(state.displayedOtherNotes, key = { it.id }) { note ->
                                NoteCard(
                                    note = note,
                                    onClick = { onOpenNote(note.id) },
                                    onTogglePin = { viewModel.togglePin(note) },
                                    onToggleFavorite = { viewModel.toggleFavorite(note) },
                                    onToggleChecklistItem = { itemId -> viewModel.toggleChecklistItem(note, itemId) },
                                    onShare = { shareNoteCard(context, note) },
                                    onDuplicate = { viewModel.duplicateNote(note) },
                                    onArchive = { viewModel.toggleArchive(note) },
                                    onDelete = { viewModel.moveToTrash(note) },
                                    onRestore = { viewModel.restoreFromTrash(note) },
                                    onPermanentDelete = { viewModel.deletePermanently(note) }
                                )
                            }
                        }
                    }
                } else {
                    // List View
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Pinned Section
                        if (state.displayedPinnedNotes.isNotEmpty()) {
                            item {
                                SectionHeader(title = "PINNED", icon = R.drawable.ic_pin_filled)
                            }
                            items(state.displayedPinnedNotes, key = { it.id }) { note ->
                                NoteCard(
                                    note = note,
                                    onClick = { onOpenNote(note.id) },
                                    onTogglePin = { viewModel.togglePin(note) },
                                    onToggleFavorite = { viewModel.toggleFavorite(note) },
                                    onToggleChecklistItem = { itemId -> viewModel.toggleChecklistItem(note, itemId) },
                                    onShare = { shareNoteCard(context, note) },
                                    onDuplicate = { viewModel.duplicateNote(note) },
                                    onArchive = { viewModel.toggleArchive(note) },
                                    onDelete = { viewModel.moveToTrash(note) },
                                    onRestore = { viewModel.restoreFromTrash(note) },
                                    onPermanentDelete = { viewModel.deletePermanently(note) }
                                )
                            }
                        }

                        // Other Notes Section
                        if (state.displayedOtherNotes.isNotEmpty()) {
                            if (state.displayedPinnedNotes.isNotEmpty()) {
                                item {
                                    SectionHeader(title = "OTHERS", icon = null)
                                }
                            }
                            items(state.displayedOtherNotes, key = { it.id }) { note ->
                                NoteCard(
                                    note = note,
                                    onClick = { onOpenNote(note.id) },
                                    onTogglePin = { viewModel.togglePin(note) },
                                    onToggleFavorite = { viewModel.toggleFavorite(note) },
                                    onToggleChecklistItem = { itemId -> viewModel.toggleChecklistItem(note, itemId) },
                                    onShare = { shareNoteCard(context, note) },
                                    onDuplicate = { viewModel.duplicateNote(note) },
                                    onArchive = { viewModel.toggleArchive(note) },
                                    onDelete = { viewModel.moveToTrash(note) },
                                    onRestore = { viewModel.restoreFromTrash(note) },
                                    onPermanentDelete = { viewModel.deletePermanently(note) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Empty Trash Confirmation Dialog
    if (showEmptyTrashConfirm) {
        AlertDialog(
            onDismissRequest = { showEmptyTrashConfirm = false },
            title = { Text("Empty Trash?") },
            text = { Text("All notes currently in the trash will be permanently deleted.") },
            confirmButton = {
                Button(
                    onClick = {
                        showEmptyTrashConfirm = false
                        viewModel.emptyTrash()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyTrashConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // About Journal Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            icon = {
                Image(
                    painter = painterResource(R.drawable.app_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(18.dp))
                )
            },
            title = {
                Text("Journal", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Smart Notebook & Tasks",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Version 1.0",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "A modern note taking and task tracking companion with custom pastel color themes, interactive checklists, real-time auto-save, and instant search.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun SectionHeader(title: String, icon: Int?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

fun shareNoteCard(context: Context, note: Note) {
    val sb = StringBuilder()
    if (note.title.isNotBlank()) {
        sb.appendLine(note.title)
        sb.appendLine("-------------------")
    }
    if (note.isChecklist) {
        note.checklistItems.forEach { item ->
            val box = if (item.isChecked) "[✓]" else "[ ]"
            sb.appendLine("$box ${item.text}")
        }
    } else {
        sb.append(note.content)
    }
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TITLE, note.title)
        putExtra(Intent.EXTRA_SUBJECT, note.title)
        putExtra(Intent.EXTRA_TEXT, sb.toString().trim())
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "Share note via"))
}
