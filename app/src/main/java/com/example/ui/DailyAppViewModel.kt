package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.AppSettings
import com.example.data.local.ExpenseEntity
import com.example.data.local.GroceryItemEntity
import com.example.data.local.HealthVaultEntity
import com.example.data.local.MealPlanEntity
import com.example.data.local.MedicationEntity
import com.example.data.local.PantryItemEntity
import com.example.data.local.RecipeEntity
import com.example.data.local.RecipeIngredient
import com.example.data.local.SeedRecipes
import com.example.data.local.UserPreferencesRepository
import com.example.data.repository.DailyAppRepository
import com.example.util.DateUtils
import com.example.util.LocalFileStorage
import com.example.util.MedicationAlarmScheduler
import com.example.util.ReceiptOcrHelper
import com.example.util.ReceiptOcrResult
import com.example.util.VoiceAlertHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DailyAppViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val db = AppDatabase.getInstance(appContext)
    val repository = DailyAppRepository(db)
    private val prefs = UserPreferencesRepository(appContext)
    private val voiceHelper = VoiceAlertHelper(appContext)

    val expenses: StateFlow<List<ExpenseEntity>> = repository.expenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val groceryItems: StateFlow<List<GroceryItemEntity>> = repository.groceryItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pantryItems: StateFlow<List<PantryItemEntity>> = repository.pantryItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recipes: StateFlow<List<RecipeEntity>> = repository.recipes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mealPlans: StateFlow<List<MealPlanEntity>> = repository.mealPlans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val medications: StateFlow<List<MedicationEntity>> = repository.medications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val healthVault: StateFlow<List<HealthVaultEntity>> = repository.healthVault
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<AppSettings> = prefs.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    private val _snackbarEvents = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val snackbarEvents: SharedFlow<String> = _snackbarEvents.asSharedFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.ensureSeedDataLoaded()
        }
        MedicationAlarmScheduler.ensureNotificationChannel(appContext)
    }

    fun markUnlocked() {
        _isUnlocked.value = true
    }

    fun emitMessage(msg: String) {
        _snackbarEvents.tryEmit(msg)
    }

    // --- Settings ---
    fun toggleLanguage() {
        viewModelScope.launch {
            val next = if (settings.value.languageCode == "si") "en" else "si"
            prefs.setLanguage(next)
        }
    }

    fun setLanguage(code: String) {
        viewModelScope.launch { prefs.setLanguage(code) }
    }

    fun setMonthlyBudget(amount: Double) {
        viewModelScope.launch { prefs.setMonthlyBudget(amount) }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch { prefs.setThemeMode(mode) }
    }

    fun setBiometricLock(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setBiometricLock(enabled)
            if (enabled) _isUnlocked.value = true
        }
    }

    fun setVoiceAlertsEnabled(enabled: Boolean) {
        viewModelScope.launch { prefs.setVoiceAlertsEnabled(enabled) }
    }

    // --- Expenses & Offline Receipt OCR ---
    fun addExpense(
        title: String,
        amount: Double,
        category: String,
        date: String = DateUtils.todayIso(),
        receiptPath: String? = null,
        notes: String = ""
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addExpense(
                ExpenseEntity(
                    title = title.trim(),
                    amount = amount,
                    category = category,
                    date = date,
                    receiptImgPath = receiptPath,
                    notes = notes.trim()
                )
            )
        }
    }

    fun deleteExpense(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteExpense(id)
        }
    }

    suspend fun processReceiptImageOffline(uri: Uri): Pair<String?, ReceiptOcrResult> {
        return withContext(Dispatchers.IO) {
            val savedPath = LocalFileStorage.copyUriToInternalStorage(
                context = appContext,
                sourceUri = uri,
                subDir = "receipts",
                prefix = "receipt"
            )
            val ocrResult = ReceiptOcrHelper.scanReceipt(appContext, uri)
            savedPath to ocrResult
        }
    }

    // --- Grocery & Interconnection #2 (Grocery -> Expenses + Pantry) ---
    fun addGroceryItem(name: String, quantity: String, unit: String, estimatedPrice: Double) {
        if (name.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            repository.addGroceryItem(
                GroceryItemEntity(
                    name = name.trim(),
                    quantity = quantity.ifBlank { "1" },
                    unit = unit.ifBlank { "pcs" },
                    estimatedPrice = estimatedPrice
                )
            )
        }
    }

    fun toggleGroceryBought(item: GroceryItemEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateGroceryItem(item.copy(isBought = !item.isBought))
        }
    }

    fun deleteGroceryItem(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteGroceryItem(id)
        }
    }

    fun logBoughtGroceriesToExpense(
        title: String,
        totalAmount: Double,
        alsoAddToPantry: Boolean,
        clearAfterLogging: Boolean,
        successMessage: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val count = repository.logBoughtGroceriesToExpense(
                title = title,
                totalAmount = totalAmount,
                date = DateUtils.todayIso(),
                alsoAddToPantry = alsoAddToPantry,
                clearAfterLogging = clearAfterLogging
            )
            if (count > 0) {
                emitMessage(successMessage)
            }
        }
    }

    // --- Pantry ---
    fun addPantryItem(name: String, quantity: String, unit: String, expiryDate: String) {
        if (name.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            repository.addPantryItem(
                PantryItemEntity(
                    itemName = name.trim(),
                    quantity = quantity.ifBlank { "1" },
                    unit = unit.ifBlank { "pkt" },
                    expiryDate = expiryDate.trim()
                )
            )
        }
    }

    fun deletePantryItem(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletePantryItem(id)
        }
    }

    // --- Recipes, Meal Plans & Interconnection #1 (Meal Plan -> Grocery List) ---
    fun toggleRecipeFavorite(recipe: RecipeEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleRecipeFavorite(recipe)
        }
    }

    fun addCustomRecipe(
        name: String,
        nameSi: String,
        category: String,
        prepTime: Int,
        servings: Int,
        ingredientsCsv: String,
        instructions: String
    ) {
        if (name.isBlank()) return
        val ingredients = ingredientsCsv.split(",", "\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { RecipeIngredient(name = it, quantity = "1", unit = "portion") }
        viewModelScope.launch(Dispatchers.IO) {
            repository.addRecipe(
                RecipeEntity(
                    name = name.trim(),
                    nameSi = nameSi.trim(),
                    ingredientsJson = SeedRecipes.serializeIngredients(ingredients),
                    instructions = instructions.trim().ifBlank { "Prepare ingredients and cook to taste." },
                    instructionsSi = instructions.trim(),
                    prepTime = prepTime.coerceAtLeast(5),
                    category = category,
                    servings = servings.coerceAtLeast(1)
                )
            )
        }
    }

    fun assignMealPlan(date: String, mealType: String, recipeId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.assignMealPlan(date, mealType, recipeId)
        }
    }

    fun removeMealPlan(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeMealPlan(id)
        }
    }

    fun syncRecipesToGrocery(
        recipesToSync: List<RecipeEntity>,
        syncedSuffix: String,
        allInPantryMsg: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val count = repository.syncMissingIngredientsToGrocery(recipesToSync)
            if (count > 0) {
                emitMessage("$count $syncedSuffix")
            } else {
                emitMessage(allInPantryMsg)
            }
        }
    }

    // --- Medications & Voice Alerts ---
    fun addMedication(
        medName: String,
        dosage: String,
        times: List<String>,
        memberName: String,
        voiceAlert: Boolean
    ) {
        if (medName.isBlank()) return
        val cleanTimes = times.ifEmpty { listOf("08:00") }
        viewModelScope.launch(Dispatchers.IO) {
            val id = repository.addMedication(
                MedicationEntity(
                    medName = medName.trim(),
                    dosage = dosage.trim().ifBlank { "1 dose" },
                    scheduleTimesJson = repository.serializeStringList(cleanTimes),
                    voiceAlert = voiceAlert,
                    memberName = memberName
                )
            )
            MedicationAlarmScheduler.scheduleReminders(
                context = appContext,
                medId = id,
                medName = medName.trim(),
                dosage = dosage.trim(),
                memberName = memberName,
                times = cleanTimes
            )
        }
    }

    fun toggleMedicationDoseTaken(medication: MedicationEntity, dateIso: String, timeSlot: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleMedicationDoseTaken(medication, "$dateIso|$timeSlot")
        }
    }

    fun deleteMedication(medication: MedicationEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            MedicationAlarmScheduler.cancelReminders(appContext, medication.id)
            repository.deleteMedication(medication.id)
        }
    }

    fun speakMedicationAlert(medication: MedicationEntity) {
        val isSi = settings.value.languageCode == "si"
        voiceHelper.speakMedicationReminder(
            medName = medication.medName,
            dosage = medication.dosage,
            memberName = medication.memberName,
            isSinhala = isSi
        )
    }

    // --- Health Vault & Interconnection #3 (Medical Bill -> Health Expense) ---
    suspend fun saveHealthAttachmentLocally(uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            LocalFileStorage.copyUriToInternalStorage(
                context = appContext,
                sourceUri = uri,
                subDir = "health_vault",
                prefix = "report"
            )
        }
    }

    fun addHealthRecord(
        title: String,
        doctorName: String,
        memberName: String,
        tags: String,
        billAmount: Double,
        autoLogExpense: Boolean,
        filePath: String?,
        date: String = DateUtils.todayIso()
    ) {
        if (title.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            repository.addHealthRecordWithOptionalExpense(
                record = HealthVaultEntity(
                    title = title.trim(),
                    doctorName = doctorName.trim(),
                    date = date,
                    filePath = filePath,
                    tags = tags.trim(),
                    memberName = memberName,
                    billAmount = billAmount
                ),
                autoLogExpense = autoLogExpense
            )
        }
    }

    fun deleteHealthRecord(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteHealthRecord(id)
        }
    }

    // --- Offline Backup & Restore ---
    fun exportBackupToUri(uri: Uri, successMsg: String, failMsg: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val ok = runCatching {
                val json = repository.exportBackupJson()
                appContext.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(json.toByteArray(Charsets.UTF_8))
                }
            }.isSuccess
            emitMessage(if (ok) successMsg else failMsg)
        }
    }

    fun importBackupFromUri(uri: Uri, successMsg: String, failMsg: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val ok = runCatching {
                val json = appContext.contentResolver.openInputStream(uri)?.bufferedReader()?.use {
                    it.readText()
                } ?: return@runCatching false
                repository.importBackupJson(json)
            }.getOrDefault(false)
            emitMessage(if (ok) successMsg else failMsg)
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceHelper.shutdown()
    }
}
