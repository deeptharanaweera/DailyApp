package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AddCard
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DocumentScanner
import androidx.compose.material.icons.rounded.LocalGroceryStore
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material.icons.rounded.OfflinePin
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.RestaurantMenu
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.AppSettings
import com.example.data.local.ExpenseEntity
import com.example.data.local.GroceryItemEntity
import com.example.data.local.MealPlanEntity
import com.example.data.local.MedicationEntity
import com.example.data.local.RecipeEntity
import com.example.ui.components.EmptyStateCard
import com.example.ui.theme.CeylonEmerald
import com.example.ui.theme.CeylonEmeraldDark
import com.example.ui.theme.CeylonEmeraldLight
import com.example.ui.theme.ClinicalTeal
import com.example.ui.theme.GoldenTurmeric
import com.example.ui.theme.TerracottaSpice
import com.example.util.DateUtils
import com.example.util.DomainLabels
import com.example.util.LocalAppStrings

@Composable
fun DashboardScreen(
    expenses: List<ExpenseEntity>,
    groceryItems: List<GroceryItemEntity>,
    mealPlans: List<MealPlanEntity>,
    recipes: List<RecipeEntity>,
    medications: List<MedicationEntity>,
    settings: AppSettings,
    parseTimes: (String) -> List<String>,
    onToggleMedTaken: (MedicationEntity, String, String) -> Unit,
    onSpeakMed: (MedicationEntity) -> Unit,
    onNavigateToExpenses: (openScanner: Boolean, openAdd: Boolean) -> Unit,
    onNavigateToGrocery: () -> Unit,
    onNavigateToMeals: () -> Unit,
    onNavigateToHealth: (openAddMed: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val s = LocalAppStrings.current
    val todayIso = remember { DateUtils.todayIso() }
    val monthPrefix = remember { DateUtils.currentMonthPrefix() }
    val greeting = remember(s) {
        when (DateUtils.currentHour()) {
            in 4..11 -> s.goodMorning
            in 12..16 -> s.goodAfternoon
            else -> s.goodEvening
        }
    }

    val monthlyTotal = remember(expenses, monthPrefix) {
        expenses.filter { it.date.startsWith(monthPrefix) }.sumOf { it.amount }
    }
    val budgetRatio = if (settings.monthlyBudget > 0) {
        (monthlyTotal / settings.monthlyBudget).toFloat().coerceIn(0f, 1f)
    } else 0f
    val isOverBudget = monthlyTotal > settings.monthlyBudget && settings.monthlyBudget > 0
    val pendingGroceries = remember(groceryItems) { groceryItems.filter { !it.isBought } }
    val recipeMap = remember(recipes) { recipes.associateBy { it.id } }
    val todayMeals = remember(mealPlans, todayIso) {
        mealPlans.filter { it.date == todayIso }
    }
    val activeMeds = remember(medications) { medications.filter { it.isActive } }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Warm Botanical Overview Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(CeylonEmeraldDark, CeylonEmerald, CeylonEmeraldLight)
                        )
                    )
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = greeting,
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${s.todayOverview} • ${DateUtils.formatPrettyDate(todayIso)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.82f)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.White.copy(alpha = 0.16f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.OfflinePin,
                                    contentDescription = s.offlineBadge,
                                    tint = Color(0xFFA7F3D0),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = s.offlineBadge,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Monthly Spend Gauge inside Hero
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { onNavigateToExpenses(false, false) }
                            .testTag("dashboard_budget_card"),
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White.copy(alpha = 0.12f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text(
                                        text = s.monthlySpending,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                    Text(
                                        text = DateUtils.formatCurrency(monthlyTotal, settings.currencySymbol),
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    val diff = kotlin.math.abs(settings.monthlyBudget - monthlyTotal)
                                    Text(
                                        text = "${s.budgetOf} ${DateUtils.formatCurrency(settings.monthlyBudget, settings.currencySymbol)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.78f)
                                    )
                                    Text(
                                        text = "${DateUtils.formatCurrency(diff, settings.currencySymbol)} ${if (isOverBudget) s.overBudget else s.remaining}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isOverBudget) Color(0xFFFFB4B4) else Color(0xFFFDE68A)
                                    )
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { budgetRatio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(50)),
                                color = if (isOverBudget) Color(0xFFFF8A9F) else Color(0xFFF7C96B),
                                trackColor = Color.White.copy(alpha = 0.22f),
                                strokeCap = StrokeCap.Round
                            )
                        }
                    }
                }
            }
        }

        // 2. Quick Actions Grid (Large Touch Targets for One-Handed Ease)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = s.quickActions,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionTile(
                        icon = Icons.Rounded.DocumentScanner,
                        label = s.scanReceipt,
                        accent = GoldenTurmeric,
                        onClick = { onNavigateToExpenses(true, false) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_scan_receipt")
                    )
                    QuickActionTile(
                        icon = Icons.Rounded.AddCard,
                        label = s.addExpense,
                        accent = CeylonEmerald,
                        onClick = { onNavigateToExpenses(false, true) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_add_expense")
                    )
                    QuickActionTile(
                        icon = Icons.Rounded.RestaurantMenu,
                        label = s.planMeal,
                        accent = TerracottaSpice,
                        onClick = onNavigateToMeals,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_plan_meal")
                    )
                    QuickActionTile(
                        icon = Icons.Rounded.Medication,
                        label = s.addMed,
                        accent = ClinicalTeal,
                        onClick = { onNavigateToHealth(true) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_add_med")
                    )
                }
            }
        }

        // 3. Today's Medications Section with Voice Alert
        item {
            SectionHeader(
                title = s.todaysMeds,
                actionLabel = s.viewAll,
                onAction = { onNavigateToHealth(false) }
            )
            Spacer(Modifier.height(8.dp))
            if (activeMeds.isEmpty()) {
                EmptyStateCard(
                    icon = Icons.Rounded.Medication,
                    title = s.noMedsToday,
                    subtitle = s.noMedsHint,
                    accentColor = ClinicalTeal
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    activeMeds.take(4).forEach { med ->
                        val times = parseTimes(med.scheduleTimesJson)
                        val takenKeys = parseTimes(med.takenDatesJson).toSet()
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
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(ClinicalTeal.copy(alpha = 0.14f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Rounded.Medication,
                                            contentDescription = med.medName,
                                            tint = ClinicalTeal
                                        )
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = med.medName,
                                            style = MaterialTheme.typography.titleMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${med.dosage} • ${DomainLabels.memberLabel(med.memberName, s.isSinhala)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(
                                        onClick = { onSpeakMed(med) },
                                        modifier = Modifier.testTag("dashboard_speak_med_${med.id}")
                                    ) {
                                        Icon(
                                            Icons.Rounded.VolumeUp,
                                            contentDescription = s.speakNow,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    times.forEach { slot ->
                                        val isTaken = "$todayIso|$slot" in takenKeys
                                        Surface(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(14.dp))
                                                .clickable { onToggleMedTaken(med, todayIso, slot) },
                                            shape = RoundedCornerShape(14.dp),
                                            color = if (isTaken) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceContainer
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isTaken) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                                    contentDescription = if (isTaken) s.taken else s.markTaken,
                                                    tint = if (isTaken) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = slot,
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (isTaken) MaterialTheme.colorScheme.onPrimaryContainer
                                                    else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Today's Meal Plan Card
        item {
            SectionHeader(
                title = s.todaysMeals,
                actionLabel = s.viewAll,
                onAction = onNavigateToMeals
            )
            Spacer(Modifier.height(8.dp))
            if (todayMeals.isEmpty()) {
                EmptyStateCard(
                    icon = Icons.Rounded.RestaurantMenu,
                    title = s.noMealsToday,
                    subtitle = s.noMealsHint,
                    accentColor = TerracottaSpice
                )
            } else {
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
                        DomainLabels.mealTypes.forEach { mealType ->
                            val plan = todayMeals.firstOrNull { it.mealType == mealType }
                            val recipe = plan?.let { recipeMap[it.recipeId] }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                    .clickable(onClick = onNavigateToMeals)
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = TerracottaSpice.copy(alpha = 0.14f)
                                ) {
                                    Text(
                                        text = DomainLabels.mealTypeLabel(mealType, s.isSinhala),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TerracottaSpice,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = if (recipe != null) {
                                        if (s.isSinhala && recipe.nameSi.isNotBlank()) recipe.nameSi else recipe.name
                                    } else s.tapToAssignMeal,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (recipe != null) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (recipe != null) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Grocery Basket Compact Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(onClick = onNavigateToGrocery)
                    .testTag("dashboard_grocery_card"),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.65f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(GoldenTurmeric.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocalGroceryStore,
                            contentDescription = s.groceryPendingTitle,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = s.groceryPendingTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = if (pendingGroceries.isEmpty()) s.allCaughtUp
                            else "${pendingGroceries.size} ${s.itemsToBuy}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.82f)
                        )
                    }
                    FilledTonalButton(onClick = onNavigateToGrocery) {
                        Text(s.viewAll)
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun QuickActionTile(
    icon: ImageVector,
    label: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = accent,
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        TextButton(onClick = onAction) {
            Text(actionLabel)
        }
    }
}
