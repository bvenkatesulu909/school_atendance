package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.ClassesScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.MarkAttendanceScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.StudentDetailScreen
import com.example.ui.screens.StudentDirectoryScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AttendanceViewModel

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : BottomNavItem("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Mark : BottomNavItem("mark_attendance", "Mark", Icons.Default.HowToReg)
    object Students : BottomNavItem("students", "Students", Icons.Default.Group)
    object Classes : BottomNavItem("classes", "Classes", Icons.Default.Class)
    object Reports : BottomNavItem("reports", "Reports", Icons.Default.BarChart)
}

class MainActivity : ComponentActivity() {

    private val viewModel: AttendanceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                AttendanceApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AttendanceApp(viewModel: AttendanceViewModel) {
    val navController = rememberNavController()
    val navItems = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Mark,
        BottomNavItem.Students,
        BottomNavItem.Classes,
        BottomNavItem.Reports
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in navItems.map { it.route } ||
            currentRoute?.startsWith("mark_attendance") == true

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    navItems.forEach { item ->
                        val selected = currentRoute == item.route ||
                                (item.route == "mark_attendance" && currentRoute?.startsWith("mark_attendance") == true)

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            modifier = Modifier.testTag("nav_item_${item.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToMarkAttendance = { classId ->
                        navController.navigate("mark_attendance?classId=$classId")
                    },
                    onNavigateToStudents = {
                        navController.navigate(BottomNavItem.Students.route)
                    },
                    onNavigateToClasses = {
                        navController.navigate(BottomNavItem.Classes.route)
                    },
                    onNavigateToReports = {
                        navController.navigate(BottomNavItem.Reports.route)
                    }
                )
            }

            composable(
                route = "mark_attendance?classId={classId}",
                arguments = listOf(
                    navArgument("classId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { backStackEntry ->
                val classIdArg = backStackEntry.arguments?.getLong("classId")
                val initialClassId = if (classIdArg != null && classIdArg != -1L) classIdArg else null
                MarkAttendanceScreen(
                    viewModel = viewModel,
                    initialClassId = initialClassId
                )
            }

            composable(BottomNavItem.Students.route) {
                StudentDirectoryScreen(
                    viewModel = viewModel,
                    onStudentClick = { studentId ->
                        navController.navigate("student_detail/$studentId")
                    }
                )
            }

            composable(
                route = "student_detail/{studentId}",
                arguments = listOf(
                    navArgument("studentId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val studentId = backStackEntry.arguments?.getLong("studentId") ?: 0L
                StudentDetailScreen(
                    viewModel = viewModel,
                    studentId = studentId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(BottomNavItem.Classes.route) {
                ClassesScreen(
                    viewModel = viewModel,
                    onClassClick = { classId ->
                        navController.navigate("mark_attendance?classId=$classId")
                    }
                )
            }

            composable(BottomNavItem.Reports.route) {
                ReportsScreen(
                    viewModel = viewModel,
                    onStudentClick = { studentId ->
                        navController.navigate("student_detail/$studentId")
                    }
                )
            }
        }
    }
}
