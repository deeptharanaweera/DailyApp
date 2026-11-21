package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.local.DailyAppBackupPayload
import com.example.data.local.ExpenseEntity
import com.example.data.local.GroceryItemEntity
import com.example.data.local.HealthVaultEntity
import com.example.data.local.MealPlanEntity
import com.example.data.local.MedicationEntity
import com.example.data.local.PantryItemEntity
import com.example.data.local.RecipeEntity
import com.example.data.local.SeedRecipes
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.Flow

class DailyAppRepository(private val db: AppDatabase) {

    private val moshi = Moshi.Builder().build()
    private val backupAdapter = moshi.adapter(DailyAppBackupPayload::class.java)
    private val stringListAdapter = moshi.adapter<List<String>>(
        Types.newParameterizedType(List::class.java, String::class.java)
    )

    val expenses: Flow<List<ExpenseEntity>> = db.expenseDao().getAllExpenses()
    val groceryItems: Flow<List<GroceryItemEntity>> = db.groceryDao().getAllGroceryItems()
    val pantryItems: Flow<List<PantryItemEntity>> = db.pantryDao().getAllPantryItems()
    val recipes: Flow<List<RecipeEntity>> = db.recipeDao().getAllRecipes()
    val mealPlans: Flow<List<MealPlanEntity>> = db.mealPlanDao().getAllMealPlans()
    val medications: Flow<List<MedicationEntity>> = db.medicationDao().getAllMedications()
    val healthVault: Flow<List<HealthVaultEntity>> = db.healthVaultDao().getAllHealthRecords()

    suspend fun ensureSeedDataLoaded() {
        if (db.recipeDao().getRecipeCount() == 0) {
            db.recipeDao().insertAll(SeedRecipes.getPreloadedRecipes())
        }
    }

    // --- Expenses ---
    suspend fun addExpense(expense: ExpenseEntity): Long = db.expenseDao().insertExpense(expense)
    suspend fun deleteExpense(id: Long) = db.expenseDao().deleteById(id)

    // --- Grocery ---
    suspend fun addGroceryItem(item: GroceryItemEntity): Long = db.groceryDao().insertItem(item)
    suspend fun updateGroceryItem(item: GroceryItemEntity) = db.groceryDao().updateItem(item)
    suspend fun deleteGroceryItem(id: Long) = db.groceryDao().deleteById(id)
    suspend fun clearBoughtGroceries() = db.groceryDao().clearBoughtItems()

    /**
     * SMART INTERCONNECTION #2: Grocery List -> Expense Tracker (+ optional Pantry restock)
     * Converts bought grocery items into a single Expense entry in 1 click.
     */
    suspend fun logBoughtGroceriesToExpense(
        title: String,
        totalAmount: Double,
        date: String,
        alsoAddToPantry: Boolean,
        clearAfterLogging: Boolean
    ): Int {
        val bought = db.groceryDao().getAllGrocerySnapshot().filter { it.isBought }
        if (bought.isEmpty()) return 0

        val itemNamesSummary = bought.joinToString(", ") { it.name }
        db.withTransaction {
            db.expenseDao().insertExpense(
                ExpenseEntity(
                    title = title.ifBlank { "Grocery Shopping (${bought.size} items)" },
                    amount = totalAmount,
                    category = "Groceries",
                    date = date,
                    notes = itemNamesSummary
                )
            )
            if (alsoAddToPantry) {
                val existingPantry = db.pantryDao().getAllPantrySnapshot()
                    .map { it.itemName.trim().lowercase() }
                    .toSet()
                bought.forEach { g ->
                    if (g.name.trim().lowercase() !in existingPantry) {
                        db.pantryDao().insertItem(
                            PantryItemEntity(
                                itemName = g.name,
                                quantity = g.quantity,
                                unit = g.unit
                            )
                        )
                    }
                }
            }
            if (clearAfterLogging) {
                db.groceryDao().clearBoughtItems()
            } else {
                bought.forEach { g ->
                    db.groceryDao().updateItem(g.copy(loggedToExpense = true))
                }
            }
        }
        return bought.size
    }

    // --- Pantry ---
    suspend fun addPantryItem(item: PantryItemEntity): Long = db.pantryDao().insertItem(item)
    suspend fun deletePantryItem(id: Long) = db.pantryDao().deleteById(id)

    // --- Recipes & Meal Plans ---
    suspend fun addRecipe(recipe: RecipeEntity): Long = db.recipeDao().insertRecipe(recipe)
    suspend fun toggleRecipeFavorite(recipe: RecipeEntity) =
        db.recipeDao().updateRecipe(recipe.copy(isFavorite = !recipe.isFavorite))

    suspend fun assignMealPlan(date: String, mealType: String, recipeId: Long) {
        db.withTransaction {
            db.mealPlanDao().deleteByDateAndMealType(date, mealType)
            db.mealPlanDao().insertMealPlan(
                MealPlanEntity(date = date, mealType = mealType, recipeId = recipeId)
            )
        }
    }

    suspend fun removeMealPlan(id: Long) = db.mealPlanDao().deleteById(id)

    /**
     * SMART INTERCONNECTION #1: Meal Planner -> Grocery List (checks Pantry Inventory first)
     * Finds ingredients from the given recipe(s) that are NOT currently in pantry_inventory
     * and NOT already pending in grocery_items, then auto-adds them to grocery_items.
     */
    suspend fun syncMissingIngredientsToGrocery(recipesToSync: List<RecipeEntity>): Int {
        val pantryNames = db.pantryDao().getAllPantrySnapshot()
            .map { it.itemName.trim().lowercase() }
        val existingGroceries = db.groceryDao().getAllGrocerySnapshot()
            .filter { !it.isBought }
            .map { it.name.trim().lowercase() }
            .toMutableSet()

        var addedCount = 0
        for (recipe in recipesToSync) {
            val ingredients = SeedRecipes.parseIngredients(recipe.ingredientsJson)
            for (ing in ingredients) {
                val normalized = ing.name.trim().lowercase()
                val normalizedSi = ing.nameSi.trim().lowercase()
                val inPantry = pantryNames.any { p ->
                    p.contains(normalized) || normalized.contains(p) ||
                        (normalizedSi.isNotEmpty() && (p.contains(normalizedSi) || normalizedSi.contains(p)))
                }
                val alreadyInGrocery = existingGroceries.any { g ->
                    g.contains(normalized) || normalized.contains(g)
                }
                if (!inPantry && !alreadyInGrocery) {
                    db.groceryDao().insertItem(
                        GroceryItemEntity(
                            name = ing.name,
                            quantity = ing.quantity.ifBlank { "1" },
                            unit = ing.unit.ifBlank { "pcs" },
                            category = "Meal Plan"
                        )
                    )
                    existingGroceries.add(normalized)
                    addedCount++
                }
            }
        }
        return addedCount
    }

    // --- Medications ---
    suspend fun addMedication(medication: MedicationEntity): Long =
        db.medicationDao().insertMedication(medication)

    suspend fun updateMedication(medication: MedicationEntity) =
        db.medicationDao().updateMedication(medication)

    suspend fun deleteMedication(id: Long) = db.medicationDao().deleteById(id)

    fun parseStringList(json: String): List<String> =
        runCatching { stringListAdapter.fromJson(json) ?: emptyList() }.getOrDefault(emptyList())

    fun serializeStringList(list: List<String>): String = stringListAdapter.toJson(list)

    suspend fun toggleMedicationDoseTaken(medication: MedicationEntity, doseKey: String) {
        val current = parseStringList(medication.takenDatesJson).toMutableList()
        if (doseKey in current) {
            current.remove(doseKey)
        } else {
            current.add(doseKey)
        }
        db.medicationDao().updateMedication(
            medication.copy(takenDatesJson = serializeStringList(current.takeLast(120)))
        )
    }

    // --- Health Vault ---
    /**
     * SMART INTERCONNECTION #3: Health Vault / Med Purchase -> Expense Tracker
     * When saving a medical report/prescription with a bill amount > 0, automatically logs a "Health" expense.
     */
    suspend fun addHealthRecordWithOptionalExpense(
        record: HealthVaultEntity,
        autoLogExpense: Boolean
    ): Long {
        var insertedId = 0L
        db.withTransaction {
            insertedId = db.healthVaultDao().insertRecord(record)
            if (autoLogExpense && record.billAmount > 0.0) {
                db.expenseDao().insertExpense(
                    ExpenseEntity(
                        title = "${record.title} (${record.memberName})",
                        amount = record.billAmount,
                        category = "Health",
                        date = record.date,
                        receiptImgPath = record.filePath,
                        notes = if (record.doctorName.isNotBlank()) "Dr. ${record.doctorName}" else "Medical record"
                    )
                )
            }
        }
        return insertedId
    }

    suspend fun deleteHealthRecord(id: Long) = db.healthVaultDao().deleteById(id)

    // --- Offline Backup & Restore (JSON) ---
    suspend fun exportBackupJson(): String {
        val payload = DailyAppBackupPayload(
            expenses = db.expenseDao().getAllExpensesSnapshot(),
            groceryItems = db.groceryDao().getAllGrocerySnapshot(),
            pantryItems = db.pantryDao().getAllPantrySnapshot(),
            recipes = db.recipeDao().getAllRecipesSnapshot(),
            mealPlans = db.mealPlanDao().getAllMealPlansSnapshot(),
            medications = db.medicationDao().getAllMedicationsSnapshot(),
            healthVault = db.healthVaultDao().getAllHealthRecordsSnapshot()
        )
        return backupAdapter.toJson(payload)
    }

    suspend fun importBackupJson(json: String): Boolean {
        val payload = runCatching { backupAdapter.fromJson(json) }.getOrNull() ?: return false
        db.withTransaction {
            db.expenseDao().clearAll()
            db.groceryDao().clearAll()
            db.pantryDao().clearAll()
            db.mealPlanDao().clearAll()
            db.medicationDao().clearAll()
            db.healthVaultDao().clearAll()

            if (payload.recipes.isNotEmpty()) {
                db.recipeDao().clearAll()
                db.recipeDao().insertAll(payload.recipes)
            }
            if (payload.expenses.isNotEmpty()) db.expenseDao().insertAll(payload.expenses)
            if (payload.groceryItems.isNotEmpty()) db.groceryDao().insertAll(payload.groceryItems)
            if (payload.pantryItems.isNotEmpty()) db.pantryDao().insertAll(payload.pantryItems)
            if (payload.mealPlans.isNotEmpty()) db.mealPlanDao().insertAll(payload.mealPlans)
            if (payload.medications.isNotEmpty()) db.medicationDao().insertAll(payload.medications)
            if (payload.healthVault.isNotEmpty()) db.healthVaultDao().insertAll(payload.healthVault)
        }
        return true
    }
}
