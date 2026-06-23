package com.sapicare.app.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.layout.fillMaxWidth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }
    val state = rememberDatePickerState()

    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))
                        onValueChange(sdf.format(Date(millis)))
                    }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Batal") } }
        ) { DatePicker(state = state) }
    }

    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        leadingIcon = { Icon(Icons.Default.CalendarMonth, null) },
        trailingIcon = {
            IconButton(onClick = { showPicker = true }) {
                Icon(Icons.Default.CalendarMonth, null, tint = Color(0xFF2E7D32))
            }
        },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            disabledTextColor = Color.Black,

            focusedLabelColor = Color(0xFF2E7D32),
            unfocusedLabelColor = Color.Gray,
            disabledLabelColor = Color.Gray,

            focusedBorderColor = Color(0xFF2E7D32),
            unfocusedBorderColor = Color.Gray,
            disabledBorderColor = Color.Gray,

            focusedLeadingIconColor = Color.Gray,
            unfocusedLeadingIconColor = Color.Gray,

            focusedTrailingIconColor = Color(0xFF2E7D32),
            unfocusedTrailingIconColor = Color.Gray
        )
    )
}
