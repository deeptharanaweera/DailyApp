package com.example

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.RestaurantMenu
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.DailyAppViewModel
import com.example.ui.components.LanguageTogglePill
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExpensesGroceryScreen
import com.example.ui.screens.HealthScreen
import com.example.ui.screens.MealsScreen
import com.example.ui.screens.SettingsBottomSheet
import com.example.ui.theme.DailyAppTheme
import com.example.util.EnglishStrings
import com.example.util.LocalAppStrings
import com.example.util.SinhalaStrings

private data class NavTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
)

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val vm: DailyAppViewModel = viewModel()
            val settings by vm.settings.collectAsStateWithLifecycle()
            val isUnlocked by vm.isUnlocked.collectAsStateWithLifecycle()
            val appStrings = if (settings.languageCode == "si") SinhalaStrings else EnglishStrings

            CompositionLocalProvider(LocalAppStrings provides appStrings) {
                DailyAppTheme(themeMode = settings.themeMode) {
                    if (settings.biometricLockEnabled && !isUnlocked) {
                        BiometricLockGate(
                            onRequestAuth = {
                                triggerBiometricPrompt(
                                    onSuccess = { vm.markUnlocked() }
                                )
                            }
                        )
                    } else {
                        DailyAppMainScaffold(viewModel = vm)
                    }
                }
            }
        }
    }

    private fun triggerBiometricPrompt(onSuccess: () -> Unit) {
        val biometricManager = BiometricManager.from(this)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_WEAK or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        if (biometricManager.canAuthenticate(authenticators) != BiometricManager.BIOMETRIC_SUCCESS) {
            onSuccess()
            return
        }

        val executor = ContextCompat.getMainExecutor(this)
        val prompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }
            }
        )
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.biometric_prompt_title))
            .setSubtitle(getString(R.string.biometric_prompt_subtitle))
            .setAllowedAuthenticators(authenticators)
            .build()
        prompt.authenticate(promptInfo)
    }
}

@Composable
private fun BiometricLockGate(onRequestAuth: () -> Unit) {
    val s = LocalAppStrings.current
    LaunchedEffect(Unit) { onRequestAuth() }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = s.unlockAppTitle,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = s.unlockAppTitle,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = s.unlockAppSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(28.dp))
            Button(
                onClick = onRequestAuth,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("unlock_biometric_button"),
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Rounded.Fingerprint, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(s.unlockButton)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DailyAppMainScaffold(viewModel: DailyAppViewModel) {
    val s = LocalAppStrings.current
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val groceryItems by viewModel.groceryItems.collectAsStateWithLifecycle()
    val pantryItems by viewModel.pantryItems.collectAsStateWithLifecycle()
    val recipes by viewModel.recipes.collectAsStateWithLifecycle()
    val mealPlans by viewModel.mealPlans.collectAsStateWithLifecycle()
    val medications by viewModel.medications.collectAsStateWithLifecycle()
    val healthVault by viewModel.healthVault.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var currentTab by rememberSaveable { mutableIntStateOf(0) }
    var expensesInitialSubTab by remember { mutableIntStateOf(0) }
    var openScannerFlag by remember { mutableStateOf(false) }
    var openAddExpenseFlag by remember { mutableStateOf(false) }
    var openAddMedFlag by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvents.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    // Handle system Back button cleanly from secondary tabs back to Home
    BackHandler(enabled = currentTab != 0) {
        currentTab = 0
    }

    val tabs = listOf(
        NavTab(s.navHome, Icons.Rounded.Home, Icons.Outlined.Home, "nav_home"),
        NavTab(s.navExpenses, Icons.Rounded.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet, "nav_expenses"),
        NavTab(s.navMeals, Icons.Rounded.RestaurantMenu, Icons.Outlined.RestaurantMenu, "nav_meals"),
        NavTab(s.navHealth, Icons.Rounded.Favorite, Icons.Outlined.FavoriteBorder, "nav_health")
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = s.appTitle,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    actions = {
                        LanguageTogglePill(
                            isSinhala = s.isSinhala,
                            onToggle = { viewModel.toggleLanguage() }
                        )
                        Spacer(Modifier.width(4.dp))
                        IconButton(
                            onClick = { showSettingsSheet = true },
                            modifier = Modifier.testTag("settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = s.settingsTitle
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            bottomBar = {
                if (!useNavRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 4.dp
                    ) {
                        tabs.forEachIndexed { index, tab ->
                            val selected = currentTab == index
                            NavigationBarItem(
                                selected = selected,
                                onClick = { currentTab = index },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.label,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                modifier = Modifier.testTag(tab.tag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavRail) {
                    NavigationRail(
                        modifier = Modifier.fillMaxHeight(),
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        Spacer(Modifier.height(12.dp))
                        tabs.forEachIndexed { index, tab ->
                            val selected = currentTab == index
                            NavigationRailItem(
                                selected = selected,
                                onClick = { currentTab = index },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) },
                                modifier = Modifier.testTag(tab.tag)
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    when (currentTab) {
                        0 -> DashboardScreen(
                            expenses = expenses,
                            groceryItems = groceryItems,
                            mealPlans = mealPlans,
                            recipes = recipes,
                            medications = medications,
                            settings = settings,
                            parseTimes = viewModel.repository::parseStringList,
                            onToggleMedTaken = { med, date, slot ->
                                viewModel.toggleMedicationDoseTaken(med, date, slot)
                            },
                            onSpeakMed = { viewModel.speakMedicationAlert(it) },
                            onNavigateToExpenses = { scan, add ->
                                expensesInitialSubTab = 0
                                openScannerFlag = scan
                                openAddExpenseFlag = add
                                currentTab = 1
                            },
                            onNavigateToGrocery = {
                                expensesInitialSubTab = 1
                                currentTab = 1
                            },
                            onNavigateToMeals = { currentTab = 2 },
                            onNavigateToHealth = { addMed ->
                                openAddMedFlag = addMed
                                currentTab = 3
                            }
                        )

                        1 -> ExpensesGroceryScreen(
                            expenses = expenses,
                            groceryItems = groceryItems,
                            settings = settings,
                            initialSubTab = expensesInitialSubTab,
                            openScannerOnLaunch = openScannerFlag,
                            openAddExpenseOnLaunch = openAddExpenseFlag,
                            onConsumeLaunchFlags = {
                                openScannerFlag = false
                                openAddExpenseFlag = false
                            },
                            onAddExpense = { title, amount, cat, path, notes ->
                                viewModel.addExpense(title, amount, cat, receiptPath = path, notes = notes)
                            },
                            onDeleteExpense = viewModel::deleteExpense,
                            onProcessReceiptOffline = viewModel::processReceiptImageOffline,
                            onAddGrocery = viewModel::addGroceryItem,
                            onToggleGroceryBought = viewModel::toggleGroceryBought,
                            onDeleteGrocery = viewModel::deleteGroceryItem,
                            onLogBoughtGroceries = viewModel::logBoughtGroceriesToExpense
                        )

                        2 -> MealsScreen(
                            recipes = recipes,
                            mealPlans = mealPlans,
                            pantryItems = pantryItems,
                            onToggleFavorite = viewModel::toggleRecipeFavorite,
                            onAssignMealPlan = viewModel::assignMealPlan,
                            onRemoveMealPlan = viewModel::removeMealPlan,
                            onSyncMissingToGrocery = viewModel::syncRecipesToGrocery,
                            onAddPantryItem = viewModel::addPantryItem,
                            onDeletePantryItem = viewModel::deletePantryItem,
                            onAddCustomRecipe = viewModel::addCustomRecipe
                        )

                        3 -> HealthScreen(
                            medications = medications,
                            healthRecords = healthVault,
                            settings = settings,
                            openAddMedOnLaunch = openAddMedFlag,
                            onConsumeLaunchFlags = { openAddMedFlag = false },
                            parseStringList = viewModel.repository::parseStringList,
                            onAddMedication = viewModel::addMedication,
                            onToggleDoseTaken = viewModel::toggleMedicationDoseTaken,
                            onSpeakMedication = viewModel::speakMedicationAlert,
                            onDeleteMedication = viewModel::deleteMedication,
                            onSaveAttachmentLocally = viewModel::saveHealthAttachmentLocally,
                            onAddHealthRecord = viewModel::addHealthRecord,
                            onDeleteHealthRecord = viewModel::deleteHealthRecord
                        )
                    }
                }
            }
        }
    }

    if (showSettingsSheet) {
        SettingsBottomSheet(
            settings = settings,
            onDismiss = { showSettingsSheet = false },
            onSetLanguage = viewModel::setLanguage,
            onSetBudget = viewModel::setMonthlyBudget,
            onSetTheme = viewModel::setThemeMode,
            onSetBiometricLock = viewModel::setBiometricLock,
            onSetVoiceAlerts = viewModel::setVoiceAlertsEnabled,
            onExportBackup = viewModel::exportBackupToUri,
            onImportBackup = viewModel::importBackupFromUri
        )
    }
}
