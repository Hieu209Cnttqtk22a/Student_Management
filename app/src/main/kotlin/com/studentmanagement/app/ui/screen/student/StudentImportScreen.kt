package com.studentmanagement.app.ui.screen.student

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.studentmanagement.app.service.ParseResult
import com.studentmanagement.app.ui.viewmodel.ImportSummary
import com.studentmanagement.app.ui.viewmodel.ImportViewModel

/**
 * Screen for importing students from CSV/Excel files.
 * Requirements: 1.1, 2.4, 2.5
 */
@Composable
fun StudentImportScreen(
    navController: NavController,
    classId: Long,
    viewModel: ImportViewModel = hiltViewModel()
) {
    val parseResult by viewModel.parseResult.collectAsState()
    val importProgress by viewModel.importProgress.collectAsState()
    val importSummary by viewModel.importSummary.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    // Requirement 1.1: File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.selectFile(it) }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import Students") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Requirement 1.1: File selection button
            if (parseResult == null && importSummary == null) {
                Button(
                    onClick = { 
                        filePickerLauncher.launch("*/*") // Accept all file types, will validate in parser
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Upload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select File (CSV, Excel)")
                }
                
                Text(
                    text = "Supported formats: .csv, .xls, .xlsx",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            // Requirement 4.1: Error message display
            errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            // Requirements 2.4, 2.5: Preview and column selection
            parseResult?.let { result ->
                Spacer(modifier = Modifier.height(16.dp))
                NameColumnSelector(
                    parseResult = result,
                    onConfirm = { columnIndex ->
                        viewModel.confirmImport(classId, columnIndex)
                    },
                    onCancel = { viewModel.cancelImport() }
                )
            }
            
            // Requirement 5.1: Import progress indicator
            if (importProgress.isImporting) {
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Importing: ${importProgress.current} / ${importProgress.total}")
                }
            }
            
            // Requirement 5.3: Import summary
            importSummary?.let { summary ->
                Spacer(modifier = Modifier.height(16.dp))
                ImportSummaryCard(
                    summary = summary,
                    onDone = { navController.popBackStack() }
                )
            }
        }
    }
}

/**
 * Component for selecting the name column from parsed file.
 * Requirements: 2.4, 2.5
 */
@Composable
fun NameColumnSelector(
    parseResult: ParseResult,
    onConfirm: (Int) -> Unit,
    onCancel: () -> Unit
) {
    // Requirement 2.1: Auto-detect name column or default to first column
    var selectedColumn by remember { 
        mutableStateOf(
            parseResult.detectedNameColumns.fullNameColumn 
                ?: parseResult.detectedNameColumns.firstNameColumn 
                ?: 0
        )
    }
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Select Name Column",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Requirement 2.4: Column selection dropdown (using radio buttons)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                parseResult.headers.forEachIndexed { index, header ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedColumn = index }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedColumn == index,
                            onClick = { selectedColumn = index }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = header)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Requirement 2.5: Preview of detected names
        Text(
            text = "Preview (first 5 rows):",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                parseResult.rows.take(5).forEach { row ->
                    Text(
                        text = row.getOrNull(selectedColumn) ?: "(empty)",
                        modifier = Modifier.padding(vertical = 4.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Requirement 1.1: Confirm and cancel buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }
            
            Button(
                onClick = { onConfirm(selectedColumn) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Import")
            }
        }
    }
}

/**
 * Component displaying import summary after completion.
 * Requirement 5.3
 */
@Composable
fun ImportSummaryCard(
    summary: ImportSummary,
    onDone: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Import Complete",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text("Students added: ${summary.added}")
            Text("Students skipped: ${summary.skipped}")
            
            // Requirement 4.1: Display errors if any
            if (summary.errors.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Errors:",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                LazyColumn(
                    modifier = Modifier.height(100.dp)
                ) {
                    items(summary.errors) { error ->
                        Text(
                            text = "• $error",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Done")
            }
        }
    }
}
