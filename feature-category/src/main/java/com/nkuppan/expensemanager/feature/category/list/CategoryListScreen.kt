package com.nkuppan.expensemanager.feature.category.list

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nkuppan.expensemanager.feature.category.R

@Composable
fun CategoryListScreen() {

}

@Composable
fun CategoryItem(
    name: String,
    description: String,
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        Image(
            modifier = Modifier.padding(16.dp),
            painter = painterResource(id = icon),
            contentDescription = name
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterVertically)
        ) {
            Text(text = name)
            Text(text = description)
        }
    }
}

@Preview
@Composable
fun CategoryItemPreview() {
    MaterialTheme {
        CategoryItem(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp),
            name = "Utilities",
            description = "Spending which is relared to tools, maintanence, etc,.",
            icon = R.drawable.ic_add
        )
    }
}