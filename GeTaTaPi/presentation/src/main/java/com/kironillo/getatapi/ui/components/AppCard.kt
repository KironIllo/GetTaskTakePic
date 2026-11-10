package com.kironillo.getatapi.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.kironillo.getatapi.ui.theme.GeTaTaPiDimens
import com.kironillo.getatapi.ui.theme.GeTaTaPiTheme

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(GeTaTaPiDimens.md),
            content = content,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AppCardPreview() {
    GeTaTaPiTheme {
        AppCard {
            SectionTitle(text = "Задание")
        }
    }
}
