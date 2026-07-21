package com.example.features.admin.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.CamsApplication
import com.example.core.navigation.AppRoutes
import com.example.core.network.LibraryBookDto
import com.example.core.network.LibraryIssueDto
import com.example.core.repository.AdminRepositoryImpl
import com.example.core.repository.LibraryRepositoryImpl
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.features.admin.models.AdminFeeStudent
import com.example.features.admin.providers.AdminLibraryViewModel2
import com.example.features.admin.providers.AdminLibraryViewModel2Factory
import com.example.features.admin.widgets.AdminBaseScreen

@Composable
fun AdminLibraryScreen(
    onNavigate: (String) -> Unit,
    viewModel: AdminLibraryViewModel2 = viewModel(
        factory = AdminLibraryViewModel2Factory(
            LibraryRepositoryImpl(CamsApplication.instance.container.apiService),
            AdminRepositoryImpl(CamsApplication.instance.container.apiService)
        )
    )
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var tab by remember { mutableStateOf(0) }
    val tabs = listOf("Books", "Issued")

    var showAddBook by remember { mutableStateOf(false) }
    var showIssueBook by remember { mutableStateOf(false) }
    var bookPendingDelete by remember { mutableStateOf<LibraryBookDto?>(null) }
    var issuePendingReturn by remember { mutableStateOf<LibraryIssueDto?>(null) }

    LaunchedEffect(uiState.saveSuccess, uiState.saveError) {
        if (uiState.saveSuccess) {
            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
            showAddBook = false; showIssueBook = false
            viewModel.clearSaveStatus()
        }
        uiState.saveError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearSaveStatus()
        }
    }

    AdminBaseScreen(
        title = "Library Management",
        subtitle = "Book catalogue and issue/return ledger",
        currentRoute = AppRoutes.ADMIN_LIBRARY,
        onNavigate = onNavigate,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { if (tab == 0) showAddBook = true else showIssueBook = true },
                containerColor = CamsNavy, contentColor = Color.White
            ) { Icon(Icons.Filled.Add, "Add") }
        }
    ) {
        Column(Modifier.fillMaxSize()) {
            TabRow(selectedTabIndex = tab, containerColor = MaterialTheme.colorScheme.surface, contentColor = CamsNavy) {
                tabs.forEachIndexed { i, t -> Tab(selected = tab == i, onClick = { tab = i }, text = { Text(t, fontWeight = FontWeight.Bold) }) }
            }
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                uiState.error?.let { Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp)) }
                if (uiState.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                } else if (tab == 0) {
                    BooksTab(uiState.books) { bookPendingDelete = it }
                } else {
                    IssuesTab(uiState.issues) { issuePendingReturn = it }
                }
            }
        }
    }

    if (showAddBook) {
        AddBookDialog(
            isSaving = uiState.isSaving,
            onDismiss = { showAddBook = false },
            onSubmit = { t, a, acc, isbn, cat, pub, tc -> viewModel.createBook(t, a, acc, isbn, cat, pub, tc) }
        )
    }
    if (showIssueBook) {
        IssueBookDialog(
            books = uiState.books.filter { it.availableCopies > 0 },
            students = uiState.studentResults,
            isSearching = uiState.isSearching,
            isSaving = uiState.isSaving,
            onSearch = { viewModel.searchStudents(it) },
            onDismiss = { showIssueBook = false },
            onSubmit = { bookId, memberId, dueOn -> viewModel.issueBook(bookId, memberId, dueOn) }
        )
    }

    bookPendingDelete?.let { book ->
        AlertDialog(
            onDismissRequest = { bookPendingDelete = null },
            title = { Text("Delete Book") },
            text = { Text("Delete \"${book.title}\"? This is only possible if it has no active issues.") },
            confirmButton = { TextButton(onClick = { viewModel.deleteBook(book.id); bookPendingDelete = null }) { Text("Delete", color = Color(0xFFB91C1C)) } },
            dismissButton = { TextButton(onClick = { bookPendingDelete = null }) { Text("Cancel") } }
        )
    }
    issuePendingReturn?.let { issue ->
        var fineAmount by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { issuePendingReturn = null },
            title = { Text("Return Book") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${issue.bookTitle ?: "Book"} — ${issue.memberName ?: "Member"}")
                    OutlinedTextField(
                        value = fineAmount, onValueChange = { fineAmount = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Fine Amount (₹, optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.returnBook(issue.id, fineAmount.toDoubleOrNull()); issuePendingReturn = null }) { Text("Return") }
            },
            dismissButton = { TextButton(onClick = { issuePendingReturn = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun BooksTab(books: List<LibraryBookDto>, onDelete: (LibraryBookDto) -> Unit) {
    if (books.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No books catalogued yet", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(books, key = { it.id }) { book ->
            CamsCard {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(book.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            "${book.author ?: "Unknown"} • ${book.accessionNo}${book.category?.let { " • $it" } ?: ""}",
                            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    val full = book.availableCopies <= 0
                    Text(
                        "${book.availableCopies}/${book.totalCopies}",
                        fontSize = 12.sp, fontWeight = FontWeight.Bold,
                        color = if (full) Color(0xFFB91C1C) else Color(0xFF047857),
                        modifier = Modifier.background(if (full) Color(0xFFFFE4E6) else Color(0xFFD1FAE5), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    IconButton(onClick = { onDelete(book) }) { Icon(Icons.Filled.Delete, "Delete book", tint = Color(0xFFB91C1C), modifier = Modifier.size(18.dp)) }
                }
            }
        }
    }
}

@Composable
private fun IssuesTab(issues: List<LibraryIssueDto>, onReturn: (LibraryIssueDto) -> Unit) {
    if (issues.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No issue records yet", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(issues, key = { it.id }) { issue ->
            CamsCard {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(issue.bookTitle ?: "Book", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("${issue.memberName ?: "Member"} • Due ${issue.dueOn}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (issue.fineAmount > 0) {
                            Text("Fine: ₹${issue.fineAmount.toInt()}", fontSize = 11.sp, color = Color(0xFFB91C1C))
                        }
                    }
                    if (issue.status == "RETURNED") {
                        Text("RETURNED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    } else {
                        val overdue = issue.isOverdue
                        Text(
                            if (overdue) "OVERDUE" else "ISSUED", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                            color = if (overdue) Color(0xFFB91C1C) else Color(0xFF4338CA),
                            modifier = Modifier.background(if (overdue) Color(0xFFFFE4E6) else Color(0xFFEEF2FF), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = { onReturn(issue) }) { Text("Return", fontSize = 12.sp) }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddBookDialog(
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (title: String, author: String, accessionNo: String, isbn: String, category: String, publisher: String, totalCopies: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var accessionNo by remember { mutableStateOf("") }
    var isbn by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var publisher by remember { mutableStateOf("") }
    var totalCopies by remember { mutableStateOf("1") }
    val tc = totalCopies.toIntOrNull() ?: 0
    val valid = title.isNotBlank() && accessionNo.isNotBlank() && tc > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Book") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = author, onValueChange = { author = it }, label = { Text("Author") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = accessionNo, onValueChange = { accessionNo = it }, label = { Text("Accession No.") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = isbn, onValueChange = { isbn = it }, label = { Text("ISBN") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = publisher, onValueChange = { publisher = it }, label = { Text("Publisher") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = totalCopies, onValueChange = { totalCopies = it.filter { c -> c.isDigit() } }, label = { Text("Total Copies") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        },
        confirmButton = {
            TextButton(enabled = valid && !isSaving, onClick = { onSubmit(title.trim(), author.trim(), accessionNo.trim(), isbn.trim(), category.trim(), publisher.trim(), tc) }) {
                Text(if (isSaving) "Adding..." else "Add")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun IssueBookDialog(
    books: List<LibraryBookDto>,
    students: List<AdminFeeStudent>,
    isSearching: Boolean,
    isSaving: Boolean,
    onSearch: (String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: (bookId: String, memberId: String, dueOn: String) -> Unit
) {
    var bookQuery by remember { mutableStateOf("") }
    var selectedBook by remember { mutableStateOf<LibraryBookDto?>(null) }
    var studentQuery by remember { mutableStateOf("") }
    var selectedStudent by remember { mutableStateOf<AdminFeeStudent?>(null) }
    var dueOn by remember { mutableStateOf("") }

    LaunchedEffect(studentQuery) {
        kotlinx.coroutines.delay(350)
        onSearch(studentQuery)
    }

    val filteredBooks = books.filter { it.title.contains(bookQuery, ignoreCase = true) || it.accessionNo.contains(bookQuery, ignoreCase = true) }
    val valid = selectedBook != null && selectedStudent != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Issue Book") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (selectedBook == null) {
                    OutlinedTextField(value = bookQuery, onValueChange = { bookQuery = it }, label = { Text("Search available book") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    filteredBooks.take(5).forEach { b ->
                        TextButton(onClick = { selectedBook = b }, modifier = Modifier.fillMaxWidth()) {
                            Text("${b.title} (${b.availableCopies} available)", modifier = Modifier.fillMaxWidth())
                        }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text(selectedBook!!.title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        TextButton(onClick = { selectedBook = null }) { Text("Change") }
                    }
                }

                if (selectedStudent == null) {
                    OutlinedTextField(
                        value = studentQuery, onValueChange = { studentQuery = it }, label = { Text("Search student") },
                        trailingIcon = { if (isSearching) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp) },
                        modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    students.take(5).forEach { s ->
                        TextButton(onClick = { selectedStudent = s }, modifier = Modifier.fillMaxWidth()) {
                            Text("${s.studentName} (${s.rollNo})", modifier = Modifier.fillMaxWidth())
                        }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("${selectedStudent!!.studentName} (${selectedStudent!!.rollNo})", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        TextButton(onClick = { selectedStudent = null }) { Text("Change") }
                    }
                }

                OutlinedTextField(value = dueOn, onValueChange = { dueOn = it }, label = { Text("Due Date (YYYY-MM-DD, optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid && !isSaving && selectedStudent?.userId != null,
                onClick = { onSubmit(selectedBook!!.id, selectedStudent!!.userId!!, dueOn.trim()) }
            ) {
                Text(if (isSaving) "Issuing..." else "Issue")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
