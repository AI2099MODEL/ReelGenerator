package com.example.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.theme.RoseQuartzContainerHighest
import com.example.ui.theme.RoseQuartzPrimary
import com.example.ui.theme.RoseQuartzTextPrimary

@Composable
fun LedgerSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = RoseQuartzPrimary) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = RoseQuartzPrimary,
            unfocusedBorderColor = RoseQuartzContainerHighest,
            focusedTextColor = RoseQuartzTextPrimary,
            unfocusedTextColor = RoseQuartzTextPrimary
        )
    )
}
