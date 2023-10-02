package com.nkuppan.expensemanager.presentation.selection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.core.ui.theme.ExpenseManagerTheme

//TODO Modify this with categories and icons in future
private val iconSelectionList = listOf(
    R.drawable.account_balance,
    R.drawable.account_balance_wallet,
    R.drawable.agriculture,
    R.drawable.apartment,
    R.drawable.car_rental,
    R.drawable.car_repair,
    R.drawable.credit_card,
    R.drawable.devices,
    R.drawable.dinner_dining,
    R.drawable.directions_bike,
    R.drawable.directions_boat,
    R.drawable.directions_bus,
    R.drawable.directions_car,
    R.drawable.diversity,
    R.drawable.electric_rickshaw,
    R.drawable.electric_scooter,
    R.drawable.emoji_food_beverage,
    R.drawable.fitness_center,
    R.drawable.flight,
    R.drawable.fluid_med,
    R.drawable.hiking,
    R.drawable.home_health,
    R.drawable.interactive_space,
    R.drawable.kayaking,
    R.drawable.laptop_chromebook,
    R.drawable.liquor,
    R.drawable.local_shipping,
    R.drawable.lunch_dining,
    R.drawable.medication,
    R.drawable.medication_liquid,
    R.drawable.payments,
    R.drawable.pool,
    R.drawable.qr_code,
    R.drawable.redeem,
    R.drawable.savings,
    R.drawable.shopping_cart,
    R.drawable.snowmobile,
    R.drawable.sports_soccer,
    R.drawable.sports_tennis,
    R.drawable.store,
    R.drawable.train,
    R.drawable.travel,
    R.drawable.wallet
)

@Composable
fun IconSelectionScreen(onIconPicked: ((Int) -> Unit)? = null) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 96.dp)
    ) {
        item(span = {
            GridItemSpan(4)
        }) {
            SelectionTitle(stringResource(id = R.string.choose_icon))
        }
        items(iconSelectionList) { icon ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onIconPicked?.invoke(icon)
                    }
                    .height(72.dp)
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    modifier = Modifier.align(Alignment.Center),
                    contentDescription = null
                )
            }
        }
    }
}

@Preview
@Composable
private fun ColorSelectionPreview() {
    ExpenseManagerTheme {
        IconSelectionScreen()
    }
}
