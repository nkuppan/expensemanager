package com.nkuppan.expensemanager.presentation.category.create

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.core.ui.extensions.getDrawable
import com.nkuppan.expensemanager.core.ui.theme.NavigationButton
import com.nkuppan.expensemanager.domain.model.CategoryType
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import kotlinx.coroutines.launch


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CategoryCreateScreen(
    navController: NavController,
    categoryId: String?
) {

    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    val scaffoldState = rememberBottomSheetScaffoldState(
        rememberStandardBottomSheetState(
            skipHiddenState = false,
            initialValue = SheetValue.Hidden
        )
    )

    val viewModel: CategoryCreateViewModel = hiltViewModel()
    viewModel.readCategoryInfo(categoryId)

    val categoryCreated by viewModel.categoryCreated.collectAsState(false)

    val categoryName by viewModel.categoryName.collectAsState()
    val colorValue by viewModel.colorValue.collectAsState()
    val iconValue by viewModel.icon.collectAsState()
    val selectedCategoryType by viewModel.categoryType.collectAsState()

    if (categoryCreated) {
        LaunchedEffect(key1 = "completed", block = {
            scaffoldState.snackbarHostState.showSnackbar(
                message = context.getString(R.string.category_create_success)
            )
            navController.popBackStack()
        })
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            IconSelectionBottomSheet {
                scope.launch {
                    viewModel.setIcon(context.resources.getResourceName(it))
                    scaffoldState.bottomSheetState.hide()
                }
            }
        }, topBar = {
            TopAppBar(navigationIcon = {
                NavigationButton(
                    navController,
                    navigationIcon = R.drawable.ic_close
                )
            }, title = {
                Row {
                    Text(
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically),
                        text = stringResource(R.string.category)
                    )
                    if (categoryId?.isNotBlank() == true) {
                        IconButton(onClick = viewModel::delete) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_delete),
                                contentDescription = ""
                            )
                        }
                    }
                }
            })
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            CategoryCreateScreen(
                modifier = Modifier.padding(innerPadding),
                selectedColor = colorValue,
                selectedIcon = iconValue,
                categoryName = categoryName,
                selectedCategoryType = selectedCategoryType,
                onColorPicked = viewModel::setColorValue,
                onCategoryTypeChange = viewModel::setCategoryType,
                onNameChange = {
                    viewModel.categoryName.value = it
                },
                openIconPicker = {
                    scope.launch {
                        if (scaffoldState.bottomSheetState.isVisible) {
                            scaffoldState.bottomSheetState.hide()
                        } else {
                            scaffoldState.bottomSheetState.expand()
                        }
                    }
                }
            )

            FloatingActionButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                onClick = viewModel::onSaveClick
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_done),
                    contentDescription = ""
                )
            }
        }
    }
}

@Composable
private fun IconSelectionBottomSheet(
    onIconPicked: ((Int) -> Unit)? = null
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 96.dp)
    ) {
        items(ICONS) { icon ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
            ) {
                IconButton(
                    modifier = Modifier.align(Alignment.Center),
                    onClick = {
                        onIconPicked?.invoke(icon)
                    }
                ) {
                    Icon(
                        painterResource(id = icon),
                        contentDescription = null
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryCreateScreen(
    modifier: Modifier = Modifier,
    selectedCategoryType: CategoryType = CategoryType.EXPENSE,
    categoryName: String = "",
    selectedColor: String = "#000000",
    selectedIcon: String = "ic_calendar",
    openIconPicker: (() -> Unit)? = null,
    onColorPicked: ((Int) -> Unit)? = null,
    onCategoryTypeChange: ((CategoryType) -> Unit)? = null,
    onNameChange: ((String) -> Unit)? = null
) {

    val context = LocalContext.current

    val focusManager = LocalFocusManager.current

    Column(modifier = modifier) {

        CategoryTypeSelectionView(selectedCategoryType, onCategoryTypeChange)

        OutlinedTextField(modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = 8.dp)
            .fillMaxWidth(),
            value = categoryName,
            label = {
                Text(text = stringResource(id = R.string.category_name))
            },
            onValueChange = {
                onNameChange?.invoke(it)
            })

        Row(
            modifier = Modifier
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth(),
        ) {
            FilledTonalButton(
                modifier = Modifier.wrapContentSize(), onClick = {
                    openColorPicker(context) {
                        onColorPicked?.invoke(it)
                    }
                    focusManager.clearFocus(force = true)
                }, colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(android.graphics.Color.parseColor(selectedColor)),
                    contentColor = colorResource(id = R.color.white)
                ), shape = ButtonDefaults.shape
            ) {
                Text(text = stringResource(id = R.string.select_color))
            }
            FilledTonalIconButton(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .wrapContentSize(),
                onClick = {
                    //Open icon selector
                    openIconPicker?.invoke()
                    focusManager.clearFocus(force = true)
                }) {
                Icon(
                    painter = painterResource(
                        context.getDrawable(selectedIcon)
                    ), contentDescription = ""
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun CategoryTypeSelectionView(
    categoryType: CategoryType,
    onCategoryTypeChange: ((CategoryType) -> Unit)? = null
) {

    Row(
        modifier = Modifier
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            .fillMaxWidth(),
    ) {
        FilterChip(
            selected = categoryType.isIncome(),
            modifier = Modifier.align(Alignment.CenterVertically),
            onClick = {
                onCategoryTypeChange?.invoke(CategoryType.INCOME)
            },
            label = {
                Text(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    text = stringResource(id = R.string.income)
                )
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_downward),
                    contentDescription = ""
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedLabelColor = Color.White,
                selectedContainerColor = colorResource(id = R.color.green_500),
                selectedLeadingIconColor = Color.White
            )
        )
        FilterChip(
            selected = categoryType.isExpense(),
            modifier = Modifier
                .padding(start = 16.dp)
                .align(Alignment.CenterVertically),
            onClick = {
                onCategoryTypeChange?.invoke(CategoryType.EXPENSE)
            },
            label = {
                Text(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    text = stringResource(id = R.string.expense)
                )
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_upward),
                    contentDescription = ""
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedLabelColor = Color.White,
                selectedContainerColor = colorResource(id = R.color.red_500),
                selectedLeadingIconColor = Color.White
            )
        )
    }
}

private fun CategoryType.isIncome(): Boolean {
    return this == CategoryType.INCOME
}

private fun CategoryType.isExpense(): Boolean {
    return this == CategoryType.EXPENSE
}

fun openColorPicker(
    context: Context,
    selectedColor: String = "#000000",
    callback: (Int) -> Unit
) {
    ColorPickerDialog.Builder(context).setTitle(context.getString(R.string.title_color_picker))
        .setPreferenceName("MyColorPickerDialog")
        .setPositiveButton(context.getString(R.string.select),
            ColorEnvelopeListener { envelope, _ ->
                callback.invoke(envelope.color)
            }).setNegativeButton(context.getString(R.string.cancel)) { dialogInterface, _ ->
            dialogInterface.dismiss()
        }.attachAlphaSlideBar(true)
        .attachBrightnessSlideBar(true)
        .setBottomSpace(12)
        .show()
}

@Preview
@Composable
fun CategoryCreateStatePreview() {
    MaterialTheme {
        CategoryCreateScreen(
            modifier = Modifier.fillMaxSize()
        )
    }
}

val ICONS = listOf(
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
    R.drawable.wallet,
)