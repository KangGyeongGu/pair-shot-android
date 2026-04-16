package com.pairshot.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.pairshot.ui.camera.CameraScreen
import com.pairshot.ui.compare.CompareScreen
import com.pairshot.ui.export.ExportScreen
import com.pairshot.ui.gallery.GalleryScreen
import com.pairshot.ui.pairing.PairingScreen
import com.pairshot.ui.project.ProjectListScreen
import com.pairshot.ui.settings.SettingsScreen
import kotlinx.serialization.Serializable

@Serializable
data object ProjectList

@Serializable
data class ProjectDetail(
    val projectId: Long,
)

@Serializable
data class Camera(
    val projectId: Long,
)

@Serializable
data class Pairing(
    val projectId: Long,
    val initialPairId: Long? = null,
)

@Serializable
data class Compare(
    val pairId: Long,
)

@Serializable
data class Export(
    val projectId: Long,
)

@Serializable
data object Settings

@Composable
fun PairShotNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = ProjectList) {
        composable<ProjectList> {
            ProjectListScreen(
                onNavigateToSettings = { navController.navigate(Settings) },
                onNavigateToProject = { projectId -> navController.navigate(ProjectDetail(projectId)) },
            )
        }
        composable<ProjectDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<ProjectDetail>()
            GalleryScreen(
                projectId = route.projectId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCamera = { navController.navigate(Camera(route.projectId)) },
                onNavigateToPairing = { pairId -> navController.navigate(Pairing(route.projectId, pairId)) },
                onNavigateToCompare = { pairId -> navController.navigate(Compare(pairId)) },
                onNavigateToExport = { navController.navigate(Export(route.projectId)) },
            )
        }
        composable<Camera> { backStackEntry ->
            val route = backStackEntry.toRoute<Camera>()
            CameraScreen(
                projectId = route.projectId,
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable<Pairing> { backStackEntry ->
            val route = backStackEntry.toRoute<Pairing>()
            PairingScreen(
                projectId = route.projectId,
                initialPairId = route.initialPairId,
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable<Compare> { backStackEntry ->
            val route = backStackEntry.toRoute<Compare>()
            CompareScreen(
                pairId = route.pairId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPairing = { projectId, pairId ->
                    navController.navigate(Pairing(projectId, pairId))
                },
            )
        }
        composable<Export> { backStackEntry ->
            val route = backStackEntry.toRoute<Export>()
            ExportScreen(
                projectId = route.projectId,
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable<Settings> {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
