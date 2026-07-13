import re

filepath = 'D:/cams-app-upload/app/src/main/java/com/example/features/parent/screens/ParentNoticesScreen.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

# Add imports
imports = '''
import kotlinx.coroutines.delay
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
'''
if 'kotlinx.coroutines.delay' not in content:
    content = content.replace('import kotlinx.coroutines.launch', 'import kotlinx.coroutines.launch\n' + imports)

# Add search variables
search_state = '''
    var selectedCategory by remember { mutableStateOf("ALL") }
    var selectedPriority by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }
    var debouncedQuery by remember { mutableStateOf("") }
    
    LaunchedEffect(searchQuery) {
        delay(300)
        debouncedQuery = searchQuery
    }
'''
content = re.sub(r'var selectedCategory by remember \{ mutableStateOf\("ALL"\) \}\s*var selectedPriority by remember \{ mutableStateOf\("ALL"\) \}', search_state, content)

# Add Search Bar UI
search_ui = '''
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search notices...") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
'''
# Insert after "Filter Circulars" Text
content = content.replace('Text("Filter Circulars", fontWeight = FontWeight.Bold, color = CamsTextPrimary)', 'Text("Filter Circulars", fontWeight = FontWeight.Bold, color = CamsTextPrimary)' + search_ui)

# Update filter logic
filter_logic = '''
                val filteredNotices = uiState.notices.filter { notice ->
                    val matchCategory = selectedCategory == "ALL" || notice.category == selectedCategory
                    val matchPriority = selectedPriority == "ALL" || notice.priority.uppercase() == selectedPriority.uppercase()
                    val matchSearch = debouncedQuery.isEmpty() || 
                        notice.title.contains(debouncedQuery, ignoreCase = true) || 
                        notice.body.contains(debouncedQuery, ignoreCase = true)
                    matchCategory && matchPriority && matchSearch
                }
'''
content = re.sub(r'val filteredNotices = uiState\.notices\.filter \{.*?\n\s*\}', filter_logic.strip(), content, flags=re.DOTALL)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)

print("Added Search Debounce")
