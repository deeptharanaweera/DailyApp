package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Kitchen
import androidx.compose.material.icons.rounded.LocalGroceryStore
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
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
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.MealPlanEntity
import com.example.data.local.PantryItemEntity
import com.example.data.local.RecipeEntity
import com.example.data.local.SeedRecipes
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.SegmentedPillBar
import com.example.ui.theme.CeylonEmerald
import com.example.ui.theme.GoldenTurmeric
import com.example.ui.theme.TerracottaSpice
import com.example.util.DateUtils
import com.example.util.DomainLabels
import com.example.util.LocalAppStrings

private val quickKitchenFilterChips = listOf(
    "Red Lentils" to "පරිප්පු",
    "Potato" to "අල",
    "Eggs" to "බිත්තර",
    "Coconut" to "පොල්",
    "Chicken" to "කුකුළු මස්",
    "Rice" to "සහල්",
    "Fish" to "මාළු",
    "Onion" to "ලූනු",
    "Gotukola" to "ගොටුකොළ",
    "Pumpkin" to "වට්ටක්කා"
)

private val sriLankanStaples = listOf(
    "Red Lentils (Dhal)" to "500 g",
    "Coconut Milk" to "2 cups",
    "Grated Coconut" to "2 cups",
    "Red Rice" to "2 kg",
    "Potato" to "500 g",
    "Eggs" to "6 pcs",
    "Red Onion" to "250 g",
    "Garlic" to "100 g",
    "Curry Leaves" to "1 bunch",
    "Turmeric Powder" to "1 pkt",
    "Chili Flakes" to "1 pkt",
    "Green Chili" to "100 g"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MealsScreen(
    recipes: List<RecipeEntity>,
    mealPlans: List<MealPlanEntity>,
    pantryItems: List<PantryItemEntity>,
    onToggleFavorite: (RecipeEntity) -> Unit,
    onAssignMealPlan: (date: String, mealType: String, recipeId: Long) -> Unit,
    onRemoveMealPlan: (Long) -> Unit,
    onSyncMissingToGrocery: (List<RecipeEntity>, String, String) -> Unit,
    onAddPantryItem: (name: String, qty: String, unit: String, expiry: String) -> Unit,
    onDeletePantryItem: (Long) -> Unit,
    onAddCustomRecipe: (String, String, String, Int, Int, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val s = LocalAppStrings.current
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val weekDays = remember { DateUtils.getNext7Days() }
    var selectedDayIso by remember { mutableStateOf(weekDays.first().isoDate) }

    var pickingMealSlot by remember { mutableStateOf<Pair<String, String>?>(null) } // date to mealType
    var viewingRecipe by remember { mutableStateOf<RecipeEntity?>(null) }
    var showAddRecipeSheet by remember { mutableStateOf(false) }

    val recipeMap = remember(recipes) { recipes.associateBy { it.id } }
    val pantryLowerNames = remember(pantryItems) {
        pantryItems.map { it.itemName.trim().lowercase() }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(10.dp))
        SegmentedPillBar(
            options = listOf(s.weeklyPlanTab, s.recipesTab, "${s.pantryTab} (${pantryItems.size})"),
            selectedIndex = selectedSubTab,
            onSelect = { selectedSubTab = it },
            testTagPrefix = "meals_subtab"
        )
        Spacer(Modifier.height(12.dp))

        when (selectedSubTab) {
            0 -> {
                // WEEKLY MEAL PLANNER TAB
                val plansForDay = remember(mealPlans, selectedDayIso) {
                    mealPlans.filter { it.date == selectedDayIso }
                }
                val plannedRecipesForWeek = remember(mealPlans, recipeMap, weekDays) {
                    val weekSet = weekDays.map { it.isoDate }.toSet()
                    mealPlans.filter { it.date in weekSet }.mapNotNull { recipeMap[it.recipeId] }.distinctBy { it.id }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 7-Day Horizontal Strip
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(weekDays) { day ->
                                val isSelected = day.isoDate == selectedDayIso
                                val dayHasPlan = mealPlans.any { it.date == day.isoDate }
                                Surface(
                                    modifier = Modifier
                                        .width(62.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .clickable { selectedDayIso = day.isoDate },
                                    shape = RoundedCornerShape(18.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.secondary
                                    else MaterialTheme.colorScheme.surface,
                                    tonalElevation = if (isSelected) 0.dp else 2.dp
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = day.dayShort,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (isSelected) MaterialTheme.colorScheme.onSecondary
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = day.dayNumber,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) MaterialTheme.colorScheme.onSecondary
                                            else MaterialTheme.colorScheme.onSurface
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when {
                                                        dayHasPlan && isSelected -> MaterialTheme.colorScheme.onSecondary
                                                        dayHasPlan -> TerracottaSpice
                                                        else -> androidx.compose.ui.graphics.Color.Transparent
                                                    }
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // SMART INTERCONNECTION #1: Auto-Sync Meal Plan Missing Ingredients -> Grocery List
                    if (plannedRecipesForWeek.isNotEmpty()) {
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
                                        Icons.Rounded.LocalGroceryStore,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(26.dp)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = s.syncMissingToGrocery,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = "${plannedRecipesForWeek.size} ${if (s.isSinhala) "සැලසුම් කළ වට්ටෝරු" else "planned meals this week"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            onSyncMissingToGrocery(
                                                plannedRecipesForWeek,
                                                s.syncedToGroceryMsg,
                                                s.allInPantryMsg
                                            )
                                        },
                                        modifier = Modifier.testTag("sync_meal_plan_groceries_button"),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Text(if (s.isSinhala) "එක් කරන්න" else "Sync")
                                    }
                                }
                            }
                        }
                    }

                    // Meal Slots (Breakfast, Lunch, Dinner)
                    items(DomainLabels.mealTypes) { mealType ->
                        val plan = plansForDay.firstOrNull { it.mealType == mealType }
                        val recipe = plan?.let { recipeMap[it.recipeId] }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .clickable {
                                    if (recipe != null) viewingRecipe = recipe
                                    else pickingMealSlot = selectedDayIso to mealType
                                }
                                .testTag("meal_slot_$mealType"),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp
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
                                        .background(TerracottaSpice.copy(alpha = 0.14f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Rounded.Restaurant,
                                        contentDescription = mealType,
                                        tint = TerracottaSpice
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = DomainLabels.mealTypeLabel(mealType, s.isSinhala),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TerracottaSpice,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (recipe != null) {
                                            if (s.isSinhala && recipe.nameSi.isNotBlank()) recipe.nameSi else recipe.name
                                        } else s.tapToAssignMeal,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (recipe != null) MaterialTheme.colorScheme.onSurface
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (recipe != null) {
                                        Text(
                                            text = "${recipe.prepTime} ${s.prepMins} • ${recipe.servings} ${s.servingsLabel}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                if (plan != null) {
                                    IconButton(onClick = { pickingMealSlot = selectedDayIso to mealType }) {
                                        Icon(Icons.Rounded.Add, contentDescription = "Change meal")
                                    }
                                    IconButton(onClick = { onRemoveMealPlan(plan.id) }) {
                                        Icon(Icons.Rounded.Close, contentDescription = "Remove meal")
                                    }
                                } else {
                                    FilledTonalButton(onClick = { pickingMealSlot = selectedDayIso to mealType }) {
                                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // RECIPES & "WHAT'S IN MY KITCHEN?" MATCHER TAB
                var searchQuery by remember { mutableStateOf("") }
                var selectedIngredients by remember { mutableStateOf(setOf<String>()) }
                var matchMyPantryOnly by remember { mutableStateOf(false) }

                val scoredRecipes = remember(recipes, searchQuery, selectedIngredients, matchMyPantryOnly, pantryLowerNames) {
                    recipes.map { recipe ->
                        val ingredients = SeedRecipes.parseIngredients(recipe.ingredientsJson)
                        val availableSet = if (selectedIngredients.isNotEmpty()) {
                            selectedIngredients.map { it.lowercase() }
                        } else {
                            pantryLowerNames
                        }
                        val matchedCount = ingredients.count { ing ->
                            val lowEn = ing.name.lowercase()
                            val lowSi = ing.nameSi.lowercase()
                            availableSet.any { avail ->
                                lowEn.contains(avail) || avail.contains(lowEn) ||
                                    (lowSi.isNotEmpty() && (lowSi.contains(avail) || avail.contains(lowSi)))
                            }
                        }
                        Triple(recipe, matchedCount, ingredients.size)
                    }.filter { (recipe, matchedCount, _) ->
                        val q = searchQuery.trim().lowercase()
                        val matchesQuery = q.isEmpty() ||
                            recipe.name.lowercase().contains(q) ||
                            recipe.nameSi.lowercase().contains(q) ||
                            recipe.ingredientsJson.lowercase().contains(q)
                        val matchesFilter = when {
                            selectedIngredients.isNotEmpty() -> matchedCount > 0
                            matchMyPantryOnly -> matchedCount > 0
                            else -> true
                        }
                        matchesQuery && matchesFilter
                    }.sortedWith(
                        compareByDescending<Triple<RecipeEntity, Int, Int>> {
                            if (selectedIngredients.isNotEmpty() || matchMyPantryOnly) it.second else 0
                        }.thenByDescending { it.first.isFavorite }
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text(s.searchRecipesHint) },
                                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("recipe_search_input"),
                                shape = RoundedCornerShape(16.dp)
                            )
                            FilledTonalButton(
                                onClick = { showAddRecipeSheet = true },
                                modifier = Modifier
                                    .height(54.dp)
                                    .testTag("add_custom_recipe_button"),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = s.addCustomRecipe)
                            }
                        }
                    }

                    // "What's in my kitchen?" Interactive Ingredient Filter
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = s.whatsInKitchen,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    FilterChip(
                                        selected = matchMyPantryOnly,
                                        onClick = { matchMyPantryOnly = !matchMyPantryOnly },
                                        label = { Text(s.matchPantryFilter) },
                                        leadingIcon = {
                                            Icon(Icons.Rounded.Kitchen, contentDescription = null, modifier = Modifier.size(16.dp))
                                        }
                                    )
                                }
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(quickKitchenFilterChips) { (en, si) ->
                                        val isChosen = en in selectedIngredients
                                        FilterChip(
                                            selected = isChosen,
                                            onClick = {
                                                selectedIngredients = if (isChosen) selectedIngredients - en
                                                else selectedIngredients + en
                                            },
                                            label = { Text(if (s.isSinhala) si else en) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    items(scoredRecipes, key = { it.first.id }) { (recipe, matched, total) ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .clickable { viewingRecipe = recipe },
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = if (s.isSinhala && recipe.nameSi.isNotBlank()) recipe.nameSi else recipe.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (s.isSinhala && recipe.nameSi.isNotBlank()) {
                                        Text(
                                            text = recipe.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Rounded.Schedule,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(Modifier.width(3.dp))
                                            Text(
                                                text = "${recipe.prepTime} ${s.prepMins}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (matched > 0) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = CeylonEmerald.copy(alpha = 0.14f)
                                            ) {
                                                Text(
                                                    text = "$matched/$total ${if (s.isSinhala) "ඇත" else "matched"}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = CeylonEmerald,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                IconButton(onClick = { onToggleFavorite(recipe) }) {
                                    Icon(
                                        imageVector = if (recipe.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                        contentDescription = "Favorite",
                                        tint = if (recipe.isFavorite) TerracottaSpice else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            else -> {
                // PANTRY INVENTORY TAB
                var pantryName by remember { mutableStateOf("") }
                var pantryQty by remember { mutableStateOf("1") }
                var pantryUnit by remember { mutableStateOf("pkt") }
                var pantryExpiry by remember { mutableStateOf("") }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
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
                                    value = pantryName,
                                    onValueChange = { pantryName = it },
                                    label = { Text(s.addPantryItem) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("pantry_name_input"),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = pantryQty,
                                        onValueChange = { pantryQty = it },
                                        label = { Text(s.qtyLabel) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    OutlinedTextField(
                                        value = pantryUnit,
                                        onValueChange = { pantryUnit = it },
                                        label = { Text(s.unitLabel) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                }
                                OutlinedTextField(
                                    value = pantryExpiry,
                                    onValueChange = { pantryExpiry = it },
                                    label = { Text(s.expiryLabel) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                Button(
                                    onClick = {
                                        if (pantryName.isNotBlank()) {
                                            onAddPantryItem(pantryName, pantryQty, pantryUnit, pantryExpiry)
                                            pantryName = ""
                                            pantryQty = "1"
                                            pantryExpiry = ""
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("add_pantry_button"),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(Icons.Rounded.Add, contentDescription = null)
                                    Spacer(Modifier.width(6.dp))
                                    Text(s.save)
                                }
                            }
                        }
                    }

                    // Quick-Add Sri Lankan Staples
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = s.quickAddStaples,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                sriLankanStaples.forEach { (staple, qtyUnit) ->
                                    val alreadyHave = pantryLowerNames.any { it == staple.lowercase() }
                                    AssistChip(
                                        onClick = {
                                            if (!alreadyHave) {
                                                val parts = qtyUnit.split(" ")
                                                onAddPantryItem(staple, parts.first(), parts.getOrElse(1) { "pkt" }, "")
                                            }
                                        },
                                        label = { Text(staple) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = if (alreadyHave) Icons.Rounded.CheckCircle else Icons.Rounded.Add,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = if (alreadyHave) CeylonEmerald else MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (pantryItems.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Rounded.Kitchen,
                                title = s.pantryEmpty,
                                subtitle = s.pantryEmptyHint,
                                accentColor = GoldenTurmeric
                            )
                        }
                    } else {
                        items(pantryItems, key = { it.id }) { item ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 1.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.itemName, style = MaterialTheme.typography.titleSmall)
                                        Text(
                                            text = "${item.quantity} ${item.unit}" +
                                                if (item.expiryDate.isNotBlank()) " • Exp: ${item.expiryDate}" else "",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(onClick = { onDeletePantryItem(item.id) }) {
                                        Icon(
                                            Icons.Rounded.DeleteOutline,
                                            contentDescription = "Remove from pantry",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
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

    // Recipe Picker Bottom Sheet for Meal Plan Slot
    pickingMealSlot?.let { (date, mealType) ->
        var query by remember { mutableStateOf("") }
        val filtered = remember(recipes, query) {
            if (query.isBlank()) recipes else recipes.filter {
                it.name.contains(query, true) || it.nameSi.contains(query, true)
            }
        }
        ModalBottomSheet(
            onDismissRequest = { pickingMealSlot = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "${DomainLabels.mealTypeLabel(mealType, s.isSinhala)} • ${DateUtils.formatPrettyDate(date)}",
                    style = MaterialTheme.typography.titleLarge
                )
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text(s.searchRecipesHint) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )
                LazyColumn(
                    modifier = Modifier.height(340.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered, key = { it.id }) { recipe ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    onAssignMealPlan(date, mealType, recipe.id)
                                    pickingMealSlot = null
                                },
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (s.isSinhala && recipe.nameSi.isNotBlank()) recipe.nameSi else recipe.name,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = "${recipe.prepTime} ${s.prepMins} • ${recipe.category}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(Icons.Rounded.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }

    // Recipe Detail Bottom Sheet
    viewingRecipe?.let { recipe ->
        val ingredients = remember(recipe) { SeedRecipes.parseIngredients(recipe.ingredientsJson) }
        ModalBottomSheet(
            onDismissRequest = { viewingRecipe = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (s.isSinhala && recipe.nameSi.isNotBlank()) recipe.nameSi else recipe.name,
                    style = MaterialTheme.typography.headlineSmall
                )
                if (recipe.nameSi.isNotBlank()) {
                    Text(
                        text = if (s.isSinhala) recipe.name else recipe.nameSi,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${recipe.prepTime} ${s.prepMins} • ${recipe.servings} ${s.servingsLabel} • ${recipe.category}",
                    style = MaterialTheme.typography.labelLarge,
                    color = TerracottaSpice
                )

                Text(s.ingredientsTitle, style = MaterialTheme.typography.titleMedium)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ingredients.forEach { ing ->
                        val inPantry = pantryLowerNames.any { p ->
                            p.contains(ing.name.lowercase()) || ing.name.lowercase().contains(p)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (inPantry) Icons.Rounded.CheckCircle else Icons.Rounded.LocalGroceryStore,
                                contentDescription = null,
                                tint = if (inPantry) CeylonEmerald else TerracottaSpice,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (s.isSinhala && ing.nameSi.isNotBlank()) "${ing.nameSi} (${ing.name})" else ing.name,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${ing.quantity} ${ing.unit}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Text(s.instructionsTitle, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = if (s.isSinhala && recipe.instructionsSi.isNotBlank()) recipe.instructionsSi else recipe.instructions,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onSyncMissingToGrocery(listOf(recipe), s.syncedToGroceryMsg, s.allInPantryMsg)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(if (s.isSinhala) "බඩු ලැයිස්තුවට" else "Missing ➔ Grocery", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Button(
                        onClick = {
                            onAssignMealPlan(selectedDayIso, "Lunch", recipe.id)
                            viewingRecipe = null
                            selectedSubTab = 0
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(s.addToPlan, maxLines = 1)
                    }
                }
            }
        }
    }

    // Add Custom Recipe Sheet
    if (showAddRecipeSheet) {
        var name by remember { mutableStateOf("") }
        var nameSi by remember { mutableStateOf("") }
        var prep by remember { mutableStateOf("25") }
        var ingredientsCsv by remember { mutableStateOf("") }
        var instructions by remember { mutableStateOf("") }

        ModalBottomSheet(
            onDismissRequest = { showAddRecipeSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(s.addCustomRecipe, style = MaterialTheme.typography.headlineSmall)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Recipe Name (English)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = nameSi,
                    onValueChange = { nameSi = it },
                    label = { Text("වට්ටෝරුවේ නම (Sinhala - optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = prep,
                    onValueChange = { prep = it },
                    label = { Text("Prep time (${s.prepMins})") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = ingredientsCsv,
                    onValueChange = { ingredientsCsv = it },
                    label = { Text("${s.ingredientsTitle} (comma separated)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text(s.instructionsTitle) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            onAddCustomRecipe(
                                name,
                                nameSi,
                                "Home Special",
                                prep.toIntOrNull() ?: 25,
                                4,
                                ingredientsCsv,
                                instructions
                            )
                            showAddRecipeSheet = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) { Text(s.save) }
            }
        }
    }
}
