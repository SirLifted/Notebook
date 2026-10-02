package com.example.notebook.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notebook.R
import com.example.notebook.model.Note
import com.example.notebook.theme.NoteColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleChecklistItem: (String) -> Unit,
    onShare: () -> Unit,
    onDuplicate: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    onRestore: (() -> Unit)? = null,
    onPermanentDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = NoteColors.getColor(note.colorIndex, isDark)
    val borderColor = NoteColors.getBorderColor(note.colorIndex, isDark)
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp, pressedElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Category Pill & Quick Action Icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Category Chip
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isDark) 0.15f else 0.08f)
                ) {
                    Text(
                        text = note.category,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!note.isTrash) {
                        // Pin Button
                        IconButton(
                            onClick = onTogglePin,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                painter = painterResource(if (note.isPinned) R.drawable.ic_pin_filled else R.drawable.ic_pin),
                                contentDescription = if (note.isPinned) "Unpin" else "Pin",
                                modifier = Modifier.size(16.dp),
                                tint = if (note.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }

                        // Favorite Star Button
                        IconButton(
                            onClick = onToggleFavorite,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (note.isFavorite) Icons.Default.Star else Icons.Default.Star,
                                contentDescription = "Favorite",
                                modifier = Modifier.size(16.dp),
                                tint = if (note.isFavorite) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                            )
                        }

                        // More Actions Dropdown
                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Options",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Share") },
                                    leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                    onClick = {
                                        showMenu = false
                                        onShare()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Duplicate") },
                                    leadingIcon = { Icon(painterResource(R.drawable.ic_copy), contentDescription = null, modifier = Modifier.size(18.dp)) },
                                    onClick = {
                                        showMenu = false
                                        onDuplicate()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (note.isArchived) "Unarchive" else "Archive") },
                                    leadingIcon = { Icon(painterResource(R.drawable.ic_archive), contentDescription = null, modifier = Modifier.size(18.dp)) },
                                    onClick = {
                                        showMenu = false
                                        onArchive()
                                    }
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("Move to Trash", color = MaterialTheme.colorScheme.error) },
                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) },
                                    onClick = {
                                        showMenu = false
                                        onDelete()
                                    }
                                )
                            }
                        }
                    } else {
                        // In Trash: Restore & Delete permanently actions
                        IconButton(
                            onClick = { onRestore?.invoke() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_restore),
                                contentDescription = "Restore",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = { onPermanentDelete?.invoke() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete permanently",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Note Title
            if (note.title.isNotBlank()) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Note Content or Checklist
            if (note.isChecklist) {
                val previewItems = note.checklistItems.take(4)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    previewItems.forEach { item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (!note.isTrash) onToggleChecklistItem(item.id)
                                }
                        ) {
                            Checkbox(
                                checked = item.isChecked,
                                onCheckedChange = { if (!note.isTrash) onToggleChecklistItem(item.id) },
                                modifier = Modifier.size(20.dp),
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary,
                                    uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = item.text.ifBlank { "Task" },
                                style = MaterialTheme.typography.bodyMedium,
                                textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                                color = if (item.isChecked) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    if (note.checklistItems.size > 4) {
                        Text(
                            text = "+${note.checklistItems.size - 4} more items",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 26.dp, top = 2.dp)
                        )
                    }

                    // Progress indicator
                    val (done, total) = note.checklistProgress
                    if (total > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            LinearProgressIndicator(
                                progress = { done.toFloat() / total.toFloat() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$done/$total",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            } else if (note.content.isNotBlank()) {
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer: Date & Word count
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatNoteDate(note.modifiedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )

                if (note.wordCount > 0) {
                    Text(
                        text = "${note.wordCount} words",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

fun formatNoteDate(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> {
            val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
            "Today, " + sdf.format(Date(timestamp))
        }
        diff < 172800_000 -> {
            val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
            "Yesterday, " + sdf.format(Date(timestamp))
        }
        else -> {
            val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}
