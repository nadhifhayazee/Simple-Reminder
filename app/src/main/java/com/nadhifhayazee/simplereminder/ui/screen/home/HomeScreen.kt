package com.nadhifhayazee.simplereminder.ui.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nadhifhayazee.simplereminder.ui.component.EmptyState
import com.nadhifhayazee.simplereminder.ui.component.LoadingScreen
import com.nadhifhayazee.simplereminder.ui.screen.home.components.QuickAddReminderBar
import com.nadhifhayazee.simplereminder.ui.screen.home.components.ReminderFilterBar
import com.nadhifhayazee.simplereminder.ui.screen.home.components.ReminderItem
import com.nadhifhayazee.simplereminder.ui.screen.home.components.ReminderSearchBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onEditReminder: (Int) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var isSearchExpanded by remember { mutableStateOf(false) }

    val hasActiveFilters = state.searchQuery.isNotBlank() ||
            state.statusFilter != null ||
            state.dateFilterStartDate != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchExpanded) {
                        ReminderSearchBar(
                            query = state.searchQuery,
                            onQueryChanged = { query ->
                                viewModel.handleIntent(HomeIntent.SearchQueryChanged(query))
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text("Simple Reminder")
                    }
                },
                actions = {
                    if (!isSearchExpanded) {
                        IconButton(onClick = { isSearchExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search reminders"
                            )
                        }
                    } else {
                        IconButton(onClick = {
                            isSearchExpanded = false
                            viewModel.handleIntent(HomeIntent.SearchQueryChanged(""))
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close search"
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            QuickAddReminderBar(
                onAddReminder = { name ->
                    viewModel.handleIntent(HomeIntent.AddReminder(name))
                },
                modifier = Modifier.imePadding()
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Filter bar
            AnimatedVisibility(visible = state.reminders.isNotEmpty()) {
                ReminderFilterBar(
                    selectedStatus = state.statusFilter,
                    dateFilterStartDate = state.dateFilterStartDate,
                    dateFilterEndDate = state.dateFilterEndDate,
                    onStatusFilterChanged = { status ->
                        viewModel.handleIntent(HomeIntent.StatusFilterChanged(status))
                    },
                    onDateFilterChanged = { start, end ->
                        viewModel.handleIntent(HomeIntent.DateFilterChanged(start, end))
                    },
                    onClearFilters = {
                        viewModel.handleIntent(HomeIntent.ClearFilters)
                    },
                    hasActiveFilters = hasActiveFilters
                )
            }

            when {
                state.isLoading -> LoadingScreen()
                state.filteredReminders.isEmpty() && hasActiveFilters -> {
                    EmptyState(message = "No reminders match your filters")
                }
                state.reminders.isEmpty() -> EmptyState(message = "No reminders yet")
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.filteredReminders) { reminder ->
                            ReminderItem(
                                reminder = reminder,
                                onStatusChange = { newStatus ->
                                    viewModel.handleIntent(
                                        HomeIntent.UpdateReminder(
                                            reminder.copy(status = newStatus)
                                        )
                                    )
                                },
                                onClick = { onEditReminder(reminder.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
