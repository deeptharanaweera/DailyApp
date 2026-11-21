package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY date DESC, createdAt DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses")
    suspend fun getAllExpensesSnapshot(): List<ExpenseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(expenses: List<ExpenseEntity>)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM expenses")
    suspend fun clearAll()
}

@Dao
interface GroceryDao {
    @Query("SELECT * FROM grocery_items ORDER BY is_bought ASC, id DESC")
    fun getAllGroceryItems(): Flow<List<GroceryItemEntity>>

    @Query("SELECT * FROM grocery_items")
    suspend fun getAllGrocerySnapshot(): List<GroceryItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: GroceryItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<GroceryItemEntity>)

    @Update
    suspend fun updateItem(item: GroceryItemEntity)

    @Query("DELETE FROM grocery_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM grocery_items WHERE is_bought = 1")
    suspend fun clearBoughtItems()

    @Query("DELETE FROM grocery_items")
    suspend fun clearAll()
}

@Dao
interface PantryDao {
    @Query("SELECT * FROM pantry_inventory ORDER BY item_name ASC")
    fun getAllPantryItems(): Flow<List<PantryItemEntity>>

    @Query("SELECT * FROM pantry_inventory")
    suspend fun getAllPantrySnapshot(): List<PantryItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: PantryItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<PantryItemEntity>)

    @Update
    suspend fun updateItem(item: PantryItemEntity)

    @Query("DELETE FROM pantry_inventory WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM pantry_inventory")
    suspend fun clearAll()
}

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes ORDER BY is_favorite DESC, name ASC")
    fun getAllRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes")
    suspend fun getAllRecipesSnapshot(): List<RecipeEntity>

    @Query("SELECT COUNT(*) FROM recipes")
    suspend fun getRecipeCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recipes: List<RecipeEntity>)

    @Update
    suspend fun updateRecipe(recipe: RecipeEntity)

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM recipes")
    suspend fun clearAll()
}

@Dao
interface MealPlanDao {
    @Query("SELECT * FROM meal_plans ORDER BY date ASC")
    fun getAllMealPlans(): Flow<List<MealPlanEntity>>

    @Query("SELECT * FROM meal_plans")
    suspend fun getAllMealPlansSnapshot(): List<MealPlanEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealPlan(plan: MealPlanEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(plans: List<MealPlanEntity>)

    @Query("DELETE FROM meal_plans WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM meal_plans WHERE date = :date AND meal_type = :mealType")
    suspend fun deleteByDateAndMealType(date: String, mealType: String)

    @Query("DELETE FROM meal_plans")
    suspend fun clearAll()
}

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications ORDER BY is_active DESC, id DESC")
    fun getAllMedications(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications")
    suspend fun getAllMedicationsSnapshot(): List<MedicationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: MedicationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(medications: List<MedicationEntity>)

    @Update
    suspend fun updateMedication(medication: MedicationEntity)

    @Query("DELETE FROM medications WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM medications")
    suspend fun clearAll()
}

@Dao
interface HealthVaultDao {
    @Query("SELECT * FROM health_vault ORDER BY date DESC, id DESC")
    fun getAllHealthRecords(): Flow<List<HealthVaultEntity>>

    @Query("SELECT * FROM health_vault")
    suspend fun getAllHealthRecordsSnapshot(): List<HealthVaultEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: HealthVaultEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<HealthVaultEntity>)

    @Query("DELETE FROM health_vault WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM health_vault")
    suspend fun clearAll()
}
