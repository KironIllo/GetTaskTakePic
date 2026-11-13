package com.kironillo.presentation.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.kironillo.presentation.ui.theme.GeTaTaPiDimens
import com.kironillo.presentation.ui.theme.GeTaTaPiTheme

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        contentPadding = PaddingValues(GeTaTaPiDimens.md),
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Preview(showBackground = true)
@Composable
private fun SecondaryButtonPreview() {
    GeTaTaPiTheme {
        SecondaryButton(text = "Подробнее", onClick = {})
    }
}
