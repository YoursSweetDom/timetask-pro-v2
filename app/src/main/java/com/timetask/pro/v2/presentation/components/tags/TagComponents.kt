package com.timetask.pro.v2.presentation.components.tags

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Tag
import com.timetask.pro.v2.data.local.db.entity.TagEntity
import com.timetask.pro.v2.ui.theme.Spacing

/**
 * A pill-shaped component representing a single selected tag.
 * Designed after TickTick's semi-transparent tag style.
 */
@Composable
fun TagPill(
    tag: TagEntity,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    val contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Tag,
            contentDescription = null,
            tint = contentColor.copy(alpha = 0.7f),
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = tag.name,
            color = contentColor,
            style = MaterialTheme.typography.labelMedium,
            fontSize = 13.sp
        )
    }
}

/**
 * A row displaying a list of selected tags, wrapping to the next line if necessary,
 * with a "+" button at the end to add more tags.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagSelectionRow(
    selectedTags: List<TagEntity>,
    onAddTagClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        selectedTags.forEach { tag ->
            TagPill(tag = tag, onClick = onAddTagClick)
        }
        
        // Add Button
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .clickable { onAddTagClick() }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Tag",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = if (selectedTags.isEmpty()) "Добавить метку" else "",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
                fontSize = 13.sp
            )
        }
    }
}
