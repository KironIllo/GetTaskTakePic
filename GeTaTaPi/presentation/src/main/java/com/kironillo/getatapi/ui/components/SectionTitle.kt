package com.kironillo.getatapi.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.kironillo.getatapi.ui.theme.GeTaTaPiTheme

@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Preview(showBackground = true)
@Composable
private fun SectionTitlePreview() {
    GeTaTaPiTheme {
        SectionTitle(text = "Текущее задание")
    }
}
