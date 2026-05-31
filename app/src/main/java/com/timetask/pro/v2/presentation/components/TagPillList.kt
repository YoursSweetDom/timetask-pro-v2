package com.timetask.pro.v2.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timetask.pro.v2.data.local.db.entity.TagEntity

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagPillList(
    tags: List<TagEntity>,
    modifier: Modifier = Modifier,
    maxVisible: Int = 3
) {
    if (tags.isEmpty()) return

    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val visibleTags = tags.take(maxVisible)
        val hiddenCount = tags.size - maxVisible

        visibleTags.forEach { tagEntity ->
            TagPill(tagEntity)
        }

        if (hiddenCount > 0) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "+$hiddenCount",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun TagPill(
    tag: TagEntity,
    modifier: Modifier = Modifier
) {
    val tagColor = tag.color?.let { Color(android.graphics.Color.parseColor(it)) } ?: MaterialTheme.colorScheme.primaryContainer
    val onTagColor = if (tag.color != null) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(tagColor.copy(alpha = 0.8f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (tag.emoji != null) {
            Text(text = tag.emoji, fontSize = 10.sp, modifier = Modifier.padding(end = 2.dp))
        }
        Text(
            text = tag.name,
            style = MaterialTheme.typography.labelSmall,
            color = onTagColor,
            fontSize = 10.sp
        )
    }
}
