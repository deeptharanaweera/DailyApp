package com.example.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String,
    val date: String, // yyyy-MM-dd
    @ColumnInfo(name = "receipt_img_path") val receiptImgPath: String? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
@Entity(tableName = "grocery_items")
data class GroceryItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val quantity: String = "1",
    val unit: String = "pcs",
    @ColumnInfo(name = "is_bought") val isBought: Boolean = false,
    @ColumnInfo(name = "estimated_price") val estimatedPrice: Double = 0.0,
    val category: String = "Groceries",
    @ColumnInfo(name = "logged_to_expense") val loggedToExpense: Boolean = false
)

@JsonClass(generateAdapter = true)
@Entity(tableName = "pantry_inventory")
data class PantryItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "item_name") val itemName: String,
    val quantity: String = "1",
    val unit: String = "pkt",
    @ColumnInfo(name = "expiry_date") val expiryDate: String = "" // yyyy-MM-dd or empty
)

@JsonClass(generateAdapter = true)
data class RecipeIngredient(
    val name: String,
    val nameSi: String = "",
    val quantity: String = "1",
    val unit: String = ""
)

@JsonClass(generateAdapter = true)
@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "name_si") val nameSi: String = "",
    @ColumnInfo(name = "ingredients_json") val ingredientsJson: String,
    val instructions: String,
    @ColumnInfo(name = "instructions_si") val instructionsSi: String = "",
    @ColumnInfo(name = "prep_time") val prepTime: Int = 25, // minutes
    val category: String = "Sri Lankan",
    val servings: Int = 4,
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean = false
)

@JsonClass(generateAdapter = true)
@Entity(tableName = "meal_plans")
data class MealPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // yyyy-MM-dd
    @ColumnInfo(name = "meal_type") val mealType: String, // Breakfast, Lunch, Dinner
    @ColumnInfo(name = "recipe_id") val recipeId: Long
)

@JsonClass(generateAdapter = true)
@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "med_name") val medName: String,
    val dosage: String,
    @ColumnInfo(name = "schedule_times_json") val scheduleTimesJson: String, // JSON list of "HH:mm"
    @ColumnInfo(name = "voice_alert") val voiceAlert: Boolean = true,
    @ColumnInfo(name = "member_name") val memberName: String = "Self",
    @ColumnInfo(name = "taken_dates_json") val takenDatesJson: String = "[]", // JSON list of "yyyy-MM-dd|HH:mm"
    @ColumnInfo(name = "is_active") val isActive: Boolean = true
)

@JsonClass(generateAdapter = true)
@Entity(tableName = "health_vault")
data class HealthVaultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    @ColumnInfo(name = "doctor_name") val doctorName: String = "",
    val date: String, // yyyy-MM-dd
    @ColumnInfo(name = "file_path") val filePath: String? = null,
    val tags: String = "",
    @ColumnInfo(name = "member_name") val memberName: String = "Self",
    @ColumnInfo(name = "bill_amount") val billAmount: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class DailyAppBackupPayload(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val expenses: List<ExpenseEntity> = emptyList(),
    val groceryItems: List<GroceryItemEntity> = emptyList(),
    val pantryItems: List<PantryItemEntity> = emptyList(),
    val recipes: List<RecipeEntity> = emptyList(),
    val mealPlans: List<MealPlanEntity> = emptyList(),
    val medications: List<MedicationEntity> = emptyList(),
    val healthVault: List<HealthVaultEntity> = emptyList()
)
