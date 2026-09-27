package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NumberKeypad(
    modifier: Modifier = Modifier,
    onDigitClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    onSubmitClick: () -> Unit,
    showNegativeOption: Boolean = false,
    onNegativeClick: () -> Unit = {}
) {
    val buttonModifier = Modifier
        .height(64.dp)
        .padding(4.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Row 1: 1, 2, 3
        Row(modifier = Modifier.fillMaxWidth()) {
            KeypadDigitButton(digit = "1", modifier = buttonModifier.weight(1f), onClick = onDigitClick)
            KeypadDigitButton(digit = "2", modifier = buttonModifier.weight(1f), onClick = onDigitClick)
            KeypadDigitButton(digit = "3", modifier = buttonModifier.weight(1f), onClick = onDigitClick)
        }

        // Row 2: 4, 5, 6
        Row(modifier = Modifier.fillMaxWidth()) {
            KeypadDigitButton(digit = "4", modifier = buttonModifier.weight(1f), onClick = onDigitClick)
            KeypadDigitButton(digit = "5", modifier = buttonModifier.weight(1f), onClick = onDigitClick)
            KeypadDigitButton(digit = "6", modifier = buttonModifier.weight(1f), onClick = onDigitClick)
        }

        // Row 3: 7, 8, 9
        Row(modifier = Modifier.fillMaxWidth()) {
            KeypadDigitButton(digit = "7", modifier = buttonModifier.weight(1f), onClick = onDigitClick)
            KeypadDigitButton(digit = "8", modifier = buttonModifier.weight(1f), onClick = onDigitClick)
            KeypadDigitButton(digit = "9", modifier = buttonModifier.weight(1f), onClick = onDigitClick)
        }

        // Row 4: Backspace, 0, Submit
        Row(modifier = Modifier.fillMaxWidth()) {
            FilledTonalButton(
                onClick = onBackspaceClick,
                modifier = buttonModifier
                    .weight(1f)
                    .testTag("keypad_backspace"),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Backspace",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            KeypadDigitButton(digit = "0", modifier = buttonModifier.weight(1f), onClick = onDigitClick)

            Button(
                onClick = onSubmitClick,
                modifier = buttonModifier
                    .weight(1f)
                    .testTag("keypad_submit"),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Submit Answer"
                )
            }
        }
    }
}

@Composable
private fun KeypadDigitButton(
    digit: String,
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit
) {
    FilledTonalButton(
        onClick = { onClick(digit) },
        modifier = modifier.testTag("keypad_digit_$digit"),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Text(
            text = digit,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
