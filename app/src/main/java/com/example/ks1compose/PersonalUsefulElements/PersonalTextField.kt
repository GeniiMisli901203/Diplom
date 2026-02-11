package com.example.ks1compose.PersonalUsefulElements

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ks1compose.ui.theme.TextFieldBGColor

@Composable
fun PersonalTextField(
    maxLines: Int = 1,
    singleLine: Boolean = true,
    text: String,
    label: String,
    padding: Int,
    onValueChange: (String) -> Unit
) {
    TextField(
        value = text,
        onValueChange = { onValueChange(it) },
        label = { Text(label) },
        shape = RoundedCornerShape(25.dp),
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = TextFieldBGColor,
            focusedContainerColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedTextColor = TextFieldBGColor,
            unfocusedLabelColor = Color.White,
            focusedLabelColor = TextFieldBGColor,
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = padding.dp)
            .border(3.dp, TextFieldBGColor, RoundedCornerShape(25.dp)),
        singleLine = singleLine,
        maxLines = maxLines
    )
}
