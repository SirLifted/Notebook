package com.example.notebook.ui.editor

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notebook.R
import com.example.notebook.model.Note
import com.example.notebook.theme.NoteColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    noteId: Long,
    isChecklistInitial: Boolean = false,
    onBack: () -> Unit,
    viewModel: NoteEditorViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(noteId) {
        viewModel.loadNote(noteId, isChecklistInitial)
    }

    // Auto-save on back press
    BackHandler {
        viewModel.autoSave()
        onBack()
    }

    var showMenu by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val bgColor = NoteColors.getColor(state.colorIndex, isDark)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.autoSave()
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor),
                actions = {
                    // Undo
                    IconButton(
                        onClick = { viewModel.undo() },
                        enabled = state.canUndo
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_undo),
                            contentDescription = "Undo",
                            modifier = Modifier.size(20.dp),
                            tint = if (state.canUndo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }

                    // Redo
                    IconButton(
                        onClick = { viewModel.redo() },
                        enabled = state.canRedo
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_redo),
                            contentDescription = "Redo",
                            modifier = Modifier.size(20.dp),
                            tint = if (state.canRedo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }

                    // Pin
                    IconButton(onClick = { viewModel.togglePin() }) {
                        Icon(
                            painter = painterResource(if (state.isPinned) R.drawable.ic_pin_filled else R.drawable.ic_pin),
                            contentDescription = if (state.isPinned) "Unpin" else "Pin",
                            modifier = Modifier.size(20.dp),
                            tint = if (state.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    // Favorite
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Favorite",
                            modifier = Modifier.size(20.dp),
                            tint = if (state.isFavorite) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                        )
                    }

                    // Share
                    IconButton(onClick = {
                        shareNote(context, state.title, viewModel.getShareableText())
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(20.dp))
                    }

                    // More Menu
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More", modifier = Modifier.size(20.dp))
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (state.isChecklist) "Switch to text mode" else "Switch to checklist mode") },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(if (state.isChecklist) R.drawable.ic_list_view else R.drawable.ic_checklist),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    viewModel.toggleChecklistMode()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Note details") },
                                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                onClick = {
                                    showMenu = false
                                    showInfoDialog = true
                                }
                            )
                            if (state.noteId > 0) {
                                DropdownMenuItem(
                                    text = { Text("Duplicate") },
                                    leadingIcon = { Icon(painterResource(R.drawable.ic_copy), contentDescription = null, modifier = Modifier.size(18.dp)) },
                                    onClick = {
                                        showMenu = false
                                        viewModel.duplicateNote {
                                            onBack()
                                        }
                                    }
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("Move to Trash", color = MaterialTheme.colorScheme.error) },
                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) },
                                    onClick = {
                                        showMenu = false
                                        showDeleteConfirm = true
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            // Live Stats Bar
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${state.wordCount} words  •  ${state.characterCount} chars  •  ~${state.readingTimeMinutes}m read",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Saved",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Color Palette Selector Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_palette),
                    contentDescription = "Color Palette",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                NoteColors.PALETTE.forEachIndexed { index, themeColor ->
                    val isSelected = state.colorIndex == index
                    val swatchColor = if (isDark) themeColor.darkBg else themeColor.lightBg
                    val swatchBorder = if (isDark) themeColor.darkBorder else themeColor.lightBorder

                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(swatchColor)
                            .border(
                                border = BorderStroke(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else swatchBorder
                                ),
                                shape = CircleShape
                            )
                            .clickable { viewModel.setColorIndex(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Category Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Note.DEFAULT_CATEGORIES.forEach { category ->
                    val selected = state.category == category
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.setCategory(category) },
                        label = { Text(category, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }

                // Checklist / Text Mode Toggle Chip
                AssistChip(
                    onClick = { viewModel.toggleChecklistMode() },
                    label = { Text(if (state.isChecklist) "Checklist" else "Text Mode", style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(if (state.isChecklist) R.drawable.ic_checklist else R.drawable.ic_list_view),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title Field
            TextField(
                value = state.title,
                onValueChange = { viewModel.onTitleChange(it) },
                placeholder = {
                    Text(
                        "Title",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    )
                },
                textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Body Content: Checklist vs Plain Text
            if (state.isChecklist) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    state.checklistItems.forEachIndexed { index, item ->
                        ChecklistRow(
                            item = item,
                            onTextChange = { newText -> viewModel.updateChecklistItem(item.id, newText) },
                            onCheckChange = { viewModel.toggleChecklistItemChecked(item.id) },
                            onRemove = { viewModel.removeChecklistItem(item.id) },
                            onAddNew = { viewModel.addChecklistItem(afterId = item.id) }
                        )
                    }

                    // Add new item button
                    TextButton(
                        onClick = { viewModel.addChecklistItem() },
                        modifier = Modifier.padding(start = 4.dp, top = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add item", style = MaterialTheme.typography.labelLarge)
                    }
                }
            } else {
                TextField(
                    value = state.content,
                    onValueChange = { viewModel.onContentChange(it) },
                    placeholder = {
                        Text(
                            "Start typing your note...",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(lineHeight = 26.sp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 350.dp)
                )
            }
        }
    }

    // Note Details Info Dialog
    if (showInfoDialog) {
        val dateFormat = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("Note Details") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailRow(label = "Category", value = state.category)
                    DetailRow(label = "Type", value = if (state.isChecklist) "Checklist" else "Text Note")
                    DetailRow(label = "Word count", value = "${state.wordCount}")
                    DetailRow(label = "Character count", value = "${state.characterCount}")
                    DetailRow(label = "Estimated read", value = "~${state.readingTimeMinutes} min")
                    DetailRow(label = "Created", value = dateFormat.format(Date(state.createdAt)))
                    DetailRow(label = "Last modified", value = dateFormat.format(Date(state.modifiedAt)))
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Move note to Trash?") },
            text = { Text("You can restore this note from Trash anytime.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.moveToTrash()
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ChecklistRow(
    item: com.example.notebook.model.ChecklistItem,
    onTextChange: (String) -> Unit,
    onCheckChange: () -> Unit,
    onRemove: () -> Unit,
    onAddNew: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.isChecked,
            onCheckedChange = { onCheckChange() },
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        )

        TextField(
            value = item.text,
            onValueChange = onTextChange,
            placeholder = { Text("List item", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)) },
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                color = if (item.isChecked) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f) else MaterialTheme.colorScheme.onSurface
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { onAddNew() }
            ),
            modifier = Modifier.weight(1f)
        )

        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove item",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

fun shareNote(context: Context, title: String, content: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TITLE, title)
        putExtra(Intent.EXTRA_SUBJECT, title)
        putExtra(Intent.EXTRA_TEXT, content)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share note via")
    context.startActivity(shareIntent)
}
