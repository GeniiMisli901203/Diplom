package com.example.ks1compose

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ks1compose.Screens.AccountScreen
import com.example.ks1compose.Screens.AddIdeaScreen
import com.example.ks1compose.Screens.AddScheduleScreen
import com.example.ks1compose.Screens.AdminScheduleScreen
import com.example.ks1compose.Screens.EditProfileScreen
import com.example.ks1compose.Screens.IdeaScreen
import com.example.ks1compose.Screens.LoginScreen
import com.example.ks1compose.Screens.RegistrationScreen
import com.example.ks1compose.Screens.ScheduleScreen
import com.example.ks1compose.Screens.SearchScreen
import com.example.ks1compose.repositories.ScheduleViewModel
import com.example.ks1compose.ui.theme.DarkGrey
import com.example.ks1compose.ui.theme.KS1ComposeTheme
import com.example.ks1compose.ui.theme.LightGrey

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        setContent {
            var darkTheme by rememberSaveable { mutableStateOf(false) }
            val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val savedToken = sharedPreferences.getString("token", null)
            val savedUserLogin = sharedPreferences.getString("userLogin", null)

            KS1ComposeTheme(darkTheme = darkTheme) {
                AppContent(
                    darkTheme = darkTheme,
                    onThemeChange = { isDarkTheme -> darkTheme = isDarkTheme },
                    initialToken = savedToken,
                    initialUserLogin = savedUserLogin
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(navController: NavController) {
    val currentRoute by navController.currentBackStackEntryFlow.collectAsState(initial = null)

    TopAppBar(
        modifier = Modifier
            .clip(RoundedCornerShape(bottomEnd = 20.dp, bottomStart = 20.dp))
            .background(DarkGrey),
        title = {
            Text(text = getScreenTitle(currentRoute?.destination?.route), color = Color.White)
        },
        actions = {},
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent(
    darkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    initialToken: String?,
    initialUserLogin: String?
) {
    val navController = rememberNavController()
    var token by rememberSaveable { mutableStateOf(initialToken) }
    var userLogin by rememberSaveable { mutableStateOf(initialUserLogin) }
    val scheduleViewModel: ScheduleViewModel = viewModel()
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

    Scaffold(
        topBar = {
            val currentRoute = navController.currentDestination?.route
            if (currentRoute != "login" && currentRoute != "registration") {
                AppBar(navController)
            }
        },
        bottomBar = {
            AppBottomBar(navController, token, userLogin)
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = if (token != null) "ideas" else "login",
            modifier = Modifier.padding(padding)
        ) {
            composable("login") {
                LoginScreen(
                    onNavigateToIdeas = { newToken, newLogin ->
                        token = newToken
                        userLogin = newLogin
                        // Сохраняем информацию о пользователе
                        sharedPreferences.edit()
                            .putString("token", newToken)
                            .putString("userLogin", newLogin)
                            .apply()
                        navController.navigate("ideas")
                    },
                    onNavigateToRegistration = { navController.navigate("registration") },
                    token = token ?: ""
                )
            }
            composable("registration") {
                RegistrationScreen(
                    onNavigateToIdeas = { newLogin ->
                        userLogin = newLogin.toString()
                        // Сохраняем информацию о пользователе
                        sharedPreferences.edit()
                            .putString("userLogin", newLogin.toString())
                            .apply()
                        navController.navigate("ideas")
                    },
                    login = ""
                )
            }
            composable("ideas") {
                IdeaScreen(
                    onNavigateToSearch = { navController.navigate("search") },
                    currentUserId = userLogin ?: ""
                )
            }

            composable("search") {
                SearchScreen(
                    onNavigateBack = { navController.navigateUp() },
                    currentUserId = userLogin ?: ""
                )
            }
            composable("schedule") {
                ScheduleScreen(viewModel = scheduleViewModel, userLogin = userLogin ?: "")
            }
            composable(
                "account/{userLogin}",
                arguments = listOf(navArgument("userLogin") { type = NavType.StringType })
            ) { backStackEntry ->
                val login = backStackEntry.arguments?.getString("userLogin") ?: ""
                AccountScreen(
                    onAddSchedule = { navController.navigate("addSchedule") },
                    onAddIdea = { navController.navigate("addIdea") },
                    onViewAllSchedules = { navController.navigate("adminSchedule") },
                    onEditProfile = { navController.navigate("editProfile/$login") },
                    onLogout = {
                        token = null
                        userLogin = null
                        // Очищаем информацию о пользователе при выходе
                        sharedPreferences.edit()
                            .remove("token")
                            .remove("userLogin")
                            .apply()
                        navController.navigate("login") {
                            popUpTo(0)
                        }
                    },
                    userLogin = login,
                    token = token ?: "",
                    darkTheme = darkTheme,
                    onThemeChange = onThemeChange
                )
            }
            composable("addSchedule") {
                AddScheduleScreen(
                    onNavigateBack = { navController.navigateUp() }
                )
            }
            composable("addIdea") {
                AddIdeaScreen(
                    onIdeaAdded = { navController.navigateUp() },
                    token = token ?: ""
                )
            }
            composable("adminSchedule") {
                AdminScheduleScreen()
            }
            composable(
                "editProfile/{userLogin}",
                arguments = listOf(navArgument("userLogin") { type = NavType.StringType })
            ) { backStackEntry ->
                val login = backStackEntry.arguments?.getString("userLogin") ?: ""
                EditProfileScreen(
                    onProfileUpdated = { navController.navigateUp() },
                    userLogin = login
                )
            }
        }
    }
}


@Composable
private fun AppBottomBar(navController: NavController, token: String?, userLogin: String?) {
    val currentRoute = navController.currentDestination?.route
    BottomAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp)
            .clip(RoundedCornerShape(topEnd = 20.dp, topStart = 20.dp))
            .background(LightGrey),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = Icons.Default.Menu,
                label = "Новости",
                isSelected = currentRoute == "ideas",
                onClick = { navController.navigate("ideas") }
            )

            BottomNavItem(
                icon = Icons.Default.DateRange,
                label = "Расписание",
                isSelected = currentRoute == "schedule",
                onClick = { navController.navigate("schedule") }
            )
            BottomNavItem(
                icon = Icons.Default.AccountCircle,
                label = "Аккаунт",
                isSelected = currentRoute == "account/{userLogin}",
                onClick = {
                    if (userLogin != null) {
                        navController.navigate("account/$userLogin")
                    }
                }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .clickable(
                onClick = onClick,
                indication = LocalIndication.current,
                interactionSource = remember { MutableInteractionSource() },
            )
            .padding(8.dp)
            .size(64.dp)
            .clip(RoundedCornerShape(25.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            label,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun getScreenTitle(route: String?): String {
    return when (route) {
        "ideas" -> "Новости"
        "account" -> "Аккаунт"
        "schedule" -> "Расписание"
        "addSchedule" -> "Добавить расписание"
        "addIdea" -> "Добавить новость"
        else -> ""
    }
}
