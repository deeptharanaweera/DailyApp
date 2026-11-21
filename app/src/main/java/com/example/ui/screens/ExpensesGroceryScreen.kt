package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DocumentScanner
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.LocalGroceryStore
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.ShoppingCartCheckout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.local.AppSettings
import com.example.data.local.ExpenseEntity
import com.example.data.local.GroceryItemEntity
import com.example.ui.components.CategoryDonutChart
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.LocalImagePreviewDialog
import com.example.ui.components.SegmentedPillBar
import com.example.ui.components.categoryColor
import com.example.ui.components.categoryIcon
import com.example.util.DateUtils
import com.example.util.DomainLabels
import com.example.util.LocalAppStrings
import com.example.util.LocalFileStorage
import com.example.util.ReceiptOcrResult
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesGroceryScreen(
    expenses: List<ExpenseEntity>,
    groceryItems: List<GroceryItemEntity>,
    settings: AppSettings,
    initialSubTab: Int = 0,
    openScannerOnLaunch: Boolean = false,
    openAddExpenseOnLaunch: Boolean = false,
    onConsumeLaunchFlags: () -> Unit,
    onAddExpense: (title: String, amount: Double, category: String, receiptPath: String?, notes: String) -> Unit,
    onDeleteExpense: (Long) -> Unit,
    onProcessReceiptOffline: suspend (Uri) -> Pair<String?, ReceiptOcrResult>,
    onAddGrocery: (name: String, qty: String, unit: String, estPrice: Double) -> Unit,
    onToggleGroceryBought: (GroceryItemEntity) -> Unit,
    onDeleteGrocery: (Long) -> Unit,
    onLogBoughtGroceries: (title: String, total: Double, alsoAddToPantry: Boolean, clearAfter: Boolean, msg: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val s = LocalAppStrings.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedSubTab by remember { mutableIntStateOf(initialSubTab) }
    var selectedCategory by remember { mutableStateOf("All") }
    var showAddExpenseSheet by remember { mutableStateOf(false) }
    var autoTriggerScan by remember { mutableStateOf(false) }
    var previewImagePair by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showCheckoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(initialSubTab, openScannerOnLaunch, openAddExpenseOnLaunch) {
        if (initialSubTab != selectedSubTab) selectedSubTab = initialSubTab
        if (openScannerOnLaunch) {
            selectedSubTab = 0
            showAddExpenseSheet = true
            autoTriggerScan = true
            onConsumeLaunchFlags()
        } else if (openAddExpenseOnLaunch) {
            selectedSubTab = 0
            showAddExpenseSheet = true
            autoTriggerScan = false
            onConsumeLaunchFlags()
        }
    }

    val monthPrefix = remember { DateUtils.currentMonthPrefix() }
    val monthlyExpenses = remember(expenses, monthPrefix) {
        expenses.filter { it.date.startsWith(monthPrefix) }
    }
    val monthlyTotal = remember(monthlyExpenses) { monthlyExpenses.sumOf { it.amount } }
    val categoryTotals = remember(monthlyExpenses) {
        monthlyExpenses.groupBy { it.category }.mapValues { (_, list) -> list.sumOf { it.amount } }
    }
    val filteredExpenses = remember(expenses, selectedCategory) {
        if (selectedCategory == "All") expenses else expenses.filter { it.category == selectedCategory }
    }

    val pendingGroceries = remember(groceryItems) { groceryItems.filter { !it.isBought } }
    val boughtGroceries = remember(groceryItems) { groceryItems.filter { it.isBought } }
    val boughtEstimatedTotal = remember(boughtGroceries) {
        boughtGroceries.sumOf { it.estimatedPrice }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(10.dp))
        SegmentedPillBar(
            options = listOf(s.expensesTab, "${s.groceryTab} (${pendingGroceries.size})"),
            selectedIndex = selectedSubTab,
            onSelect = { selectedSubTab = it },
            testTagPrefix = "expenses_subtab"
        )
        Spacer(Modifier.height(12.dp))

        if (selectedSubTab == 0) {
            // EXPENSES VIEW
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Action Buttons Row (Scan Receipt OCR + Add Expense)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                autoTriggerScan = true
                                showAddExpenseSheet = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("scan_receipt_button"),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Rounded.DocumentScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(s.scanReceipt, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        FilledTonalButton(
                            onClick = {
                                autoTriggerScan = false
                                showAddExpenseSheet = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("add_expense_button"),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(s.addExpense, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }

                // Category Donut Chart Card
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = s.spendingByCategory,
                                style = MaterialTheme.typography.titleMedium
                            )
                            CategoryDonutChart(
                                categoryTotals = categoryTotals,
                                totalAmount = monthlyTotal,
                                currencySymbol = settings.currencySymbol,
                                isSinhala = s.isSinhala
                            )
                        }
                    }
                }

                // Category Filter Chips
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = selectedCategory == "All",
                                onClick = { selectedCategory = "All" },
                                label = { Text(s.allCategories) }
                            )
                        }
                        items(DomainLabels.expenseCategories) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(DomainLabels.categoryLabel(cat, s.isSinhala)) }
                            )
                        }
                    }
                }

                if (filteredExpenses.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Rounded.ReceiptLong,
                            title = s.noExpensesYet,
                            subtitle = s.noExpensesHint
                        )
                    }
                } else {
                    items(filteredExpenses, key = { it.id }) { expense ->
                        ExpenseRowCard(
                            expense = expense,
                            currencySymbol = settings.currencySymbol,
                            isSinhala = s.isSinhala,
                            onViewReceipt = { path -> previewImagePair = expense.title to path },
                            onDelete = { onDeleteExpense(expense.id) }
                        )
                    }
                }
            }
        } else {
            // GROCERY LIST VIEW
            var itemName by remember { mutableStateOf("") }
            var itemQty by remember { mutableStateOf("1") }
            var itemUnit by remember { mutableStateOf("kg") }
            var itemEstPrice by remember { mutableStateOf("") }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Quick Add Grocery Form
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = itemName,
                                onValueChange = { itemName = it },
                                placeholder = { Text(s.addGroceryHint) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("grocery_name_input"),
                                shape = RoundedCornerShape(14.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = itemQty,
                                    onValueChange = { itemQty = it },
                                    label = { Text(s.qtyLabel) },
                                    singleLine = true,
                                    modifier = Modifier.weight(0.8f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                OutlinedTextField(
                                    value = itemUnit,
                                    onValueChange = { itemUnit = it },
                                    label = { Text(s.unitLabel) },
                                    singleLine = true,
                                    modifier = Modifier.weight(0.9f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                OutlinedTextField(
                                    value = itemEstPrice,
                                    onValueChange = { itemEstPrice = it },
                                    label = { Text(s.estPriceLabel) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1.3f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                            }
                            Button(
                                onClick = {
                                    if (itemName.isNotBlank()) {
                                        onAddGrocery(
                                            itemName,
                                            itemQty,
                                            itemUnit,
                                            itemEstPrice.toDoubleOrNull() ?: 0.0
                                        )
                                        itemName = ""
                                        itemQty = "1"
                                        itemEstPrice = ""
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("add_grocery_button"),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text(s.save)
                            }
                        }
                    }
                }

                // SMART INTERCONNECTION #2 BANNER: Bought Groceries -> Daily Expenses
                if (boughtGroceries.isNotEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Rounded.ShoppingCartCheckout,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${boughtGroceries.size} ${s.boughtSection}",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = DateUtils.formatCurrency(boughtEstimatedTotal, settings.currencySymbol),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                                Button(
                                    onClick = { showCheckoutDialog = true },
                                    modifier = Modifier.testTag("checkout_groceries_button"),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text(s.logBoughtToExpenses, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }

                if (groceryItems.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Rounded.LocalGroceryStore,
                            title = s.noGroceriesYet,
                            subtitle = s.noGroceriesHint
                        )
                    }
                } else {
                    if (pendingGroceries.isNotEmpty()) {
                        item {
                            Text(
                                text = "${s.toBuySection} (${pendingGroceries.size})",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        items(pendingGroceries, key = { it.id }) { item ->
                            GroceryRowCard(
                                item = item,
                                currencySymbol = settings.currencySymbol,
                                onToggle = { onToggleGroceryBought(item) },
                                onDelete = { onDeleteGrocery(item.id) }
                            )
                        }
                    }
                    if (boughtGroceries.isNotEmpty()) {
                        item {
                            Text(
                                text = "${s.boughtSection} (${boughtGroceries.size})",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        items(boughtGroceries, key = { it.id }) { item ->
                            GroceryRowCard(
                                item = item,
                                currencySymbol = settings.currencySymbol,
                                onToggle = { onToggleGroceryBought(item) },
                                onDelete = { onDeleteGrocery(item.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Add / Scan Receipt Expense Modal Bottom Sheet
    if (showAddExpenseSheet) {
        AddExpenseWithOcrSheet(
            autoPromptScan = autoTriggerScan,
            onDismiss = {
                showAddExpenseSheet = false
                autoTriggerScan = false
            },
            onProcessReceiptOffline = onProcessReceiptOffline,
            onSave = { title, amount, category, receiptPath, notes ->
                onAddExpense(title, amount, category, receiptPath, notes)
                showAddExpenseSheet = false
                autoTriggerScan = false
            }
        )
    }

    // Checkout Bought Groceries -> Expense Confirmation Dialog
    if (showCheckoutDialog) {
        var billTitle by remember {
            mutableStateOf(
                if (s.isSinhala) "සුපර් මාකට් බඩු (${boughtGroceries.size})"
                else "Grocery Run (${boughtGroceries.size} items)"
            )
        }
        var totalInput by remember {
            mutableStateOf(if (boughtEstimatedTotal > 0) boughtEstimatedTotal.toInt().toString() else "")
        }
        var alsoPantry by remember { mutableStateOf(true) }
        var clearAfter by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showCheckoutDialog = false },
            title = { Text(s.logBoughtToExpenses) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = billTitle,
                        onValueChange = { billTitle = it },
                        label = { Text(s.titleLabel) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = totalInput,
                        onValueChange = { totalInput = it },
                        label = { Text(s.amountLabel) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(s.alsoAddToPantry, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        Switch(checked = alsoPantry, onCheckedChange = { alsoPantry = it })
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(s.clearBoughtAfterLog, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        Switch(checked = clearAfter, onCheckedChange = { clearAfter = it })
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val total = totalInput.toDoubleOrNull() ?: boughtEstimatedTotal
                        if (total > 0.0) {
                            onLogBoughtGroceries(
                                billTitle,
                                total,
                                alsoPantry,
                                clearAfter,
                                if (s.isSinhala) "වියදම් වෙත සාර්ථකව එක් කළා!" else "Logged grocery total to Expenses!"
                            )
                            showCheckoutDialog = false
                        }
                    }
                ) { Text(s.save) }
            },
            dismissButton = {
                TextButton(onClick = { showCheckoutDialog = false }) { Text(s.cancel) }
            }
        )
    }

    previewImagePair?.let { (title, path) ->
        LocalImagePreviewDialog(
            filePath = path,
            title = title,
            onDismiss = { previewImagePair = null }
        )
    }
}

@Composable
private fun ExpenseRowCard(
    expense: ExpenseEntity,
    currencySymbol: String,
    isSinhala: Boolean,
    onViewReceipt: (String) -> Unit,
    onDelete: () -> Unit
) {
    val accent = categoryColor(expense.category)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon(expense.category),
                    contentDescription = expense.category,
                    tint = accent,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${DomainLabels.categoryLabel(expense.category, isSinhala)} • ${DateUtils.formatPrettyDate(expense.date)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (expense.notes.isNotBlank()) {
                    Text(
                        text = expense.notes,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = DateUtils.formatCurrency(expense.amount, currencySymbol),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row {
                    if (!expense.receiptImgPath.isNullOrBlank()) {
                        IconButton(
                            onClick = { onViewReceipt(expense.receiptImgPath) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Image,
                                contentDescription = "View receipt",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Rounded.DeleteOutline,
                            contentDescription = "Delete expense",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GroceryRowCard(
    item: GroceryItemEntity,
    currencySymbol: String,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(16.dp),
        color = if (item.isBought) MaterialTheme.colorScheme.surfaceContainerLow
        else MaterialTheme.colorScheme.surface,
        tonalElevation = if (item.isBought) 0.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isBought,
                onCheckedChange = { onToggle() }
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall,
                    textDecoration = if (item.isBought) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (item.isBought) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${item.quantity} ${item.unit}" +
                        if (item.estimatedPrice > 0) " • ${DateUtils.formatCurrency(item.estimatedPrice, currencySymbol)}" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Rounded.DeleteOutline,
                    contentDescription = "Delete grocery item",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExpenseWithOcrSheet(
    autoPromptScan: Boolean,
    onDismiss: () -> Unit,
    onProcessReceiptOffline: suspend (Uri) -> Pair<String?, ReceiptOcrResult>,
    onSave: (title: String, amount: Double, category: String, receiptPath: String?, notes: String) -> Unit
) {
    val s = LocalAppStrings.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Groceries") }
    var notes by remember { mutableStateOf("") }
    var receiptPath by remember { mutableStateOf<String?>(null) }
    var isScanning by remember { mutableStateOf(false) }
    var ocrStatus by remember { mutableStateOf<String?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    fun handleReceiptUri(uri: Uri) {
        scope.launch {
            isScanning = true
            ocrStatus = s.scanningReceipt
            val (savedPath, result) = onProcessReceiptOffline(uri)
            receiptPath = savedPath
            if (result.detectedTotal != null) {
                amountText = if (result.detectedTotal % 1.0 == 0.0) {
                    result.detectedTotal.toInt().toString()
                } else {
                    String.format(java.util.Locale.US, "%.2f", result.detectedTotal)
                }
                ocrStatus = "${s.ocrDetectedAmount}: Rs. $amountText"
            } else {
                ocrStatus = s.ocrNoAmountFound
            }
            if (title.isBlank() && !result.detectedTitle.isNullOrBlank()) {
                title = result.detectedTitle
            }
            isScanning = false
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) handleReceiptUri(uri)
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingCameraUri
        if (success && uri != null) handleReceiptUri(uri)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val (uri, _) = LocalFileStorage.createCameraCaptureUri(context, "receipt")
            pendingCameraUri = uri
            takePictureLauncher.launch(uri)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (autoPromptScan) s.scanReceiptOcr else s.addExpense,
                style = MaterialTheme.typography.headlineSmall
            )

            // Offline OCR Receipt Capture Row
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Rounded.DocumentScanner,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = s.scanReceiptOcr,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val hasCam = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasCam) {
                                    val (uri, _) = LocalFileStorage.createCameraCaptureUri(context, "receipt")
                                    pendingCameraUri = uri
                                    takePictureLauncher.launch(uri)
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(s.takePhoto)
                        }
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(s.pickPhoto)
                        }
                    }
                    if (isScanning) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Text(s.scanningReceipt, style = MaterialTheme.typography.bodySmall)
                        }
                    } else if (ocrStatus != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(ocrStatus!!, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(s.titleLabel) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("expense_title_input"),
                shape = RoundedCornerShape(14.dp)
            )
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text(s.amountLabel) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("expense_amount_input"),
                shape = RoundedCornerShape(14.dp)
            )

            Text(s.categoryLabel, style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(DomainLabels.expenseCategories) { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = { Text(DomainLabels.categoryLabel(cat, s.isSinhala)) }
                    )
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(s.notesLabel) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: return@Button
                    if (amount > 0.0) {
                        onSave(
                            title.ifBlank { DomainLabels.categoryLabel(category, s.isSinhala) },
                            amount,
                            category,
                            receiptPath,
                            notes
                        )
                    }
                },
                enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0.0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_expense_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(s.save, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
