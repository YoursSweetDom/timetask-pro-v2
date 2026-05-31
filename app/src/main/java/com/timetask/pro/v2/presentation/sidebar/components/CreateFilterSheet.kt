package com.timetask.pro.v2.presentation.sidebar.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.timetask.pro.v2.domain.model.FilterField
import com.timetask.pro.v2.domain.model.FilterLogic
import com.timetask.pro.v2.domain.model.FilterMode
import com.timetask.pro.v2.domain.model.FilterOperator
import com.timetask.pro.v2.domain.model.FilterRule
import com.timetask.pro.v2.ui.theme.Spacing
import com.timetask.pro.v2.ui.theme.TitaniumPrimary
import com.timetask.pro.v2.presentation.components.EmojiPickerBottomSheet
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/**
 * BottomSheet — полный конструктор фильтров (TickTick-стиль).
 * Normal + Advanced: Match All/Any, правила по полю/оператору/значениям.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateFilterSheet(
    onDismiss: () -> Unit,
    onAdd: (name: String, icon: String?, emoji: String?, logicJson: String) -> Unit,
    initialName: String = "",
    initialIcon: String = "",
    initialEmoji: String = "",
    initialLogicJson: String = ""
) {
    val isEditMode = initialName.isNotEmpty()
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    val initialLogic = remember(initialLogicJson) { deserializeFilterLogic(initialLogicJson) }
    var name by remember { mutableStateOf(initialName) }
    var icon by remember { mutableStateOf(initialIcon) }
    var emoji by remember { mutableStateOf<String?>(initialEmoji.takeIf { it.isNotBlank() }) }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var mode by remember { mutableStateOf(initialLogic.mode) }
    val rules = remember { mutableStateListOf(*initialLogic.rules.toTypedArray()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = if (isEditMode) "Редактировать фильтр" else "Новый фильтр",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(Modifier.height(Spacing.md))

            // Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TitaniumPrimary,
                    cursorColor = TitaniumPrimary,
                    focusedLabelColor = TitaniumPrimary,
                ),
            )

            Spacer(Modifier.height(Spacing.sm))

            // Icon / Emoji
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showEmojiPicker = true }
                    .padding(vertical = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Эмодзи: ", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = emoji ?: icon.ifBlank { "Выберите..." },
                    fontSize = if (emoji != null || icon.isNotBlank()) 24.sp else 16.sp,
                    color = if (emoji != null || icon.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(Spacing.lg))

            // Mode: Match All / Match Any
            Text("Логика", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(Spacing.xs))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                FilterChip(
                    selected = mode == FilterMode.MATCH_ALL,
                    onClick = { mode = FilterMode.MATCH_ALL },
                    label = { Text("Все условия (AND)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TitaniumPrimary.copy(alpha = 0.2f),
                        selectedLabelColor = TitaniumPrimary,
                    ),
                )
                FilterChip(
                    selected = mode == FilterMode.MATCH_ANY,
                    onClick = { mode = FilterMode.MATCH_ANY },
                    label = { Text("Любое (OR)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TitaniumPrimary.copy(alpha = 0.2f),
                        selectedLabelColor = TitaniumPrimary,
                    ),
                )
            }

            Spacer(Modifier.height(Spacing.lg))

            // Rules
            Text("Условия", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(Spacing.sm))

            rules.forEachIndexed { index, rule ->
                FilterRuleRow(
                    rule = rule,
                    onRuleChanged = { updated -> rules[index] = updated },
                    onDelete = { rules.removeAt(index) }
                )
                Spacer(Modifier.height(Spacing.sm))
            }

            TextButton(
                onClick = {
                    rules.add(FilterRule(FilterField.TAG, FilterOperator.IS))
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(Spacing.xs))
                Text("Добавить условие")
            }

            Spacer(Modifier.height(Spacing.lg))

            // Create button
            Button(
                onClick = {
                    scope.launch {
                        sheetState.hide()
                        val logic = FilterLogic(mode = mode, rules = rules.toList())
                        val json = serializeFilterLogic(logic)
                        onAdd(name.trim(), icon.trim().ifBlank { null }, emoji, json)
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = TitaniumPrimary),
            ) {
                Text(if (isEditMode) "Сохранить" else "Создать фильтр")
            }
            Spacer(Modifier.height(Spacing.md))
        }
    }
}

@Composable
private fun FilterRuleRow(
    rule: FilterRule,
    onRuleChanged: (FilterRule) -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        // Field selector
        FieldDropdown(
            selectedField = rule.field,
            onFieldSelected = { onRuleChanged(rule.copy(field = it)) },
            modifier = Modifier.weight(1f)
        )

        // Operator selector
        OperatorDropdown(
            selectedOperator = rule.operator,
            onOperatorSelected = { onRuleChanged(rule.copy(operator = it)) },
            modifier = Modifier.weight(1f)
        )

        // Delete button
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = MaterialTheme.colorScheme.error)
        }
    }

    // Value input
    val valueText = rule.values.joinToString(", ")
    OutlinedTextField(
        value = valueText,
        onValueChange = { text ->
            val values = text.split(",").map { it.trim() }.filter { it.isNotBlank() }
            onRuleChanged(rule.copy(values = values))
        },
        label = { Text("Значения (через запятую)") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = TitaniumPrimary,
            cursorColor = TitaniumPrimary,
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FieldDropdown(
    selectedField: FilterField,
    onFieldSelected: (FilterField) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedField.toLabel(),
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(),
            textStyle = MaterialTheme.typography.bodySmall,
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            FilterField.entries.forEach { field ->
                DropdownMenuItem(
                    text = { Text(field.toLabel()) },
                    onClick = {
                        onFieldSelected(field)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OperatorDropdown(
    selectedOperator: FilterOperator,
    onOperatorSelected: (FilterOperator) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedOperator.toLabel(),
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(),
            textStyle = MaterialTheme.typography.bodySmall,
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            FilterOperator.entries.forEach { op ->
                DropdownMenuItem(
                    text = { Text(op.toLabel()) },
                    onClick = {
                        onOperatorSelected(op)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun FilterField.toLabel(): String = when (this) {
    FilterField.FOLDER -> "Папка"
    FilterField.TAG -> "Метка"
    FilterField.CATEGORY -> "Категория"
    FilterField.PRIORITY -> "Приоритет"
    FilterField.DUE_DATE -> "Дата"
    FilterField.KEYWORD -> "Ключевое слово"
}

private fun FilterOperator.toLabel(): String = when (this) {
    FilterOperator.IS -> "Равно"
    FilterOperator.IS_NOT -> "Не равно"
    FilterOperator.CONTAINS -> "Содержит"
    FilterOperator.NOT_CONTAINS -> "Не содержит"
}

/**
 * Простая JSON-сериализация FilterLogic (без kotlinx.serialization зависимости).
 * Формат: {"mode":"MATCH_ALL","rules":[{"field":"TAG","operator":"IS","values":["work"]}]}
 */
private fun serializeFilterLogic(logic: FilterLogic): String {
    val rulesJson = logic.rules.joinToString(",") { rule ->
        val valuesJson = rule.values.joinToString(",") { "\"$it\"" }
        """{"field":"${rule.field.name}","operator":"${rule.operator.name}","values":[$valuesJson]}"""
    }
    return """{"mode":"${logic.mode.name}","rules":[$rulesJson]}"""
}

private fun deserializeFilterLogic(jsonString: String): FilterLogic {
    if (jsonString.isBlank() || jsonString == "{}") return FilterLogic()
    return try {
        val json = org.json.JSONObject(jsonString)
        val modeStr = json.optString("mode", FilterMode.MATCH_ALL.name)
        val mode = try { FilterMode.valueOf(modeStr) } catch (e: Exception) { FilterMode.MATCH_ALL }
        
        val rulesList = mutableListOf<FilterRule>()
        val rulesArray = json.optJSONArray("rules")
        if (rulesArray != null) {
            for (i in 0 until rulesArray.length()) {
                val ruleJson = rulesArray.optJSONObject(i) ?: continue
                val fieldStr = ruleJson.optString("field", FilterField.TAG.name)
                val operatorStr = ruleJson.optString("operator", FilterOperator.IS.name)
                
                val field = try { FilterField.valueOf(fieldStr) } catch (e: Exception) { FilterField.TAG }
                val operator = try { FilterOperator.valueOf(operatorStr) } catch (e: Exception) { FilterOperator.IS }
                
                val valuesList = mutableListOf<String>()
                val valuesArray = ruleJson.optJSONArray("values")
                if (valuesArray != null) {
                    for (j in 0 until valuesArray.length()) {
                        valuesList.add(valuesArray.optString(j, ""))
                    }
                }
                
                rulesList.add(FilterRule(field, operator, valuesList))
            }
        }
        FilterLogic(mode, rulesList)
    } catch (e: Exception) {
        FilterLogic()
    }
}
