package com.example.features.parent.widgets

import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.core.ui.CamsScreen
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.example.CamsApplication

@Composable
fun ParentBaseScreen(
    title: String,
    subtitle: String? = null,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onBackClick: (() -> Unit)? = null,
    floatingActionButton: @Composable (() -> Unit)? = null,
    scrollable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ParentDrawer(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    scope.launch { drawerState.close() }
                    onNavigate(route)
                }
            )
        }
    ) {
        CamsScreen(
            title = title,
            subtitle = subtitle,
            onBackClick = onBackClick,
            navigationIcon = if (onBackClick == null) {
                {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                }
            } else null,
            actions = {
                val context = LocalContext.current
                val repository = (context.applicationContext as CamsApplication).container.parentRepository
                ChildSwitcher(repository)
                IconButton(onClick = { onNavigate("LOGOUT") }) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = Color.White)
                }
            },
            floatingActionButton = floatingActionButton,
            scrollable = scrollable,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content
        )
    }
}

@Composable
fun ChildSwitcher(repository: com.example.core.repository.ParentRepository) {
    val scope = rememberCoroutineScope()
    var childrenList by remember { mutableStateOf<List<com.example.features.parent.models.ChildSummary>>(emptyList()) }
    var selectedId by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        childrenList = repository.getChildrenList()
        repository.selectedChildId.collect { id ->
            if (id == null && childrenList.isNotEmpty()) {
                repository.setSelectedChildId(childrenList.first().id)
            } else {
                selectedId = id
            }
        }
    }

    // A parent with a single child gets no picker — there is nothing to switch to.
    if (childrenList.size < 2) return

    val selectedChild = childrenList.find { it.id == selectedId }

    Box {
        TextButton(onClick = { expanded = true }) {
            Text(
                text = selectedChild?.fullName ?: "Select Child",
                color = Color.White
            )
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = Color.White)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            childrenList.forEach { child ->
                DropdownMenuItem(
                    text = { Text(child.fullName) },
                    onClick = {
                        scope.launch {
                            repository.setSelectedChildId(child.id)
                        }
                        expanded = false
                    }
                )
            }
        }
    }
}
