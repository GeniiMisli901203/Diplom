package com.example.ks1compose.PersonalUsefulElements

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.example.ks1compose.ui.theme.ButtonBGColor

@Composable
fun PersonalButton(
    text:String,
    onClick: () -> Unit
) {
    Button(
        onClick = {onClick()},
        modifier = Modifier.fillMaxWidth(0.7f),
        colors = ButtonDefaults.buttonColors(
            containerColor = ButtonBGColor,
            contentColor = Color.White
        )
    ) {
        Text (text = text,
            fontSize = 15.sp)
    }
}