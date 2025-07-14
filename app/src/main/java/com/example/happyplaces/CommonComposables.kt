package com.example.happyplaces

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier // If you need to pass a modifier

@Composable
fun ConfirmationDialog(
    showDialog: Boolean,
    title: String,
    message: String,
    confirmButtonText: String = "Confirm",
    dismissButtonText: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss, // Action when dismissed by clicking outside or back button
            title = { Text(text = title) },
            text = { Text(text = message) },
            confirmButton = {
                TextButton(onClick = {
                    onConfirm()
                    onDismiss() // Ensure dialog is dismissed after confirm action
                }) {
                    Text(confirmButtonText)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(dismissButtonText)
                }
            },
            modifier = modifier
        )
    }
}
