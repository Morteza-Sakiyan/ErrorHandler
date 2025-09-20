package com.sepanta.controlkit.errorhandler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sepanta.controlkit.errorhandler.examples.ContactForm
import com.sepanta.controlkit.errorhandler.examples.FormValidationExample
import com.sepanta.controlkit.errorhandler.examples.NetworkErrorExample
import com.sepanta.controlkit.errorhandler.examples.SimpleApiExample
import com.sepanta.controlkit.errorhandler.examples.mvvm.UserViewModel
import com.sepanta.controlkit.errorhandler.ui.theme.ErrorHandlerTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ErrorHandlerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ErrorHandlerExamples()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErrorHandlerExamples() {
    val scope = rememberCoroutineScope()
    var selectedExample by remember { mutableStateOf("") }
    var logMessage by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ErrorHandler Examples",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Simple API Example
        ExampleCard(
            title = "1. Simple API Error Handling",
            description = "Simple API error handling example"
        ) {
            scope.launch {
                selectedExample = "Simple API"
                logMessage = "Testing Simple API Error Handling..."
                
                val simpleExample = SimpleApiExample()
                simpleExample.fetchUser("404") { result ->
                    result.fold(
                        onSuccess = { user ->
                            logMessage = "Success: User found - ${user.name}"
                        },
                        onFailure = { error ->
                            logMessage = "Error: ${error.message}"
                        }
                    )
                }
            }
        }
        
        // MVVM Example
        ExampleCard(
            title = "2. MVVM Architecture Example",
            description = "Example usage in MVVM architecture"
        ) {
            scope.launch {
                selectedExample = "MVVM"
                logMessage = "Testing MVVM Error Handling..."
                
                val viewModel = UserViewModel()
                viewModel.login("invalid@example.com", "password")
                
                // Simulate receiving state
                logMessage = "MVVM: Testing login with invalid credentials"
            }
        }
        
        // Form Validation Example
        ExampleCard(
            title = "3. Form Validation Example",
            description = "Form validation error handling example"
        ) {
            scope.launch {
                selectedExample = "Form Validation"
                logMessage = "Testing Form Validation..."
                
                val formExample = FormValidationExample()
                val form = ContactForm(
                    name = "",
                    email = "invalid-email",
                    phone = "123",
                    message = "short",
                    age = 16
                )
                
                formExample.submitContactForm(form) { result ->
                    result.fold(
                        onSuccess = { contact ->
                            logMessage = "Success: Contact created - ${contact.name}"
                        },
                        onFailure = { error ->
                            logMessage = "Validation Error: ${error.message}"
                        }
                    )
                }
            }
        }
        
        // Network Error Example
        ExampleCard(
            title = "4. Network Error Handling",
            description = "Comprehensive network error handling example"
        ) {
            scope.launch {
                selectedExample = "Network Error"
                logMessage = "Testing Network Error Handling with Retry..."
                
                val networkExample = NetworkErrorExample()
                networkExample.fetchPostsWithRetry { result ->
                    result.fold(
                        onSuccess = { posts ->
                            logMessage = "Success: ${posts.size} posts fetched"
                        },
                        onFailure = { error ->
                            logMessage = "Network Error: ${error.message}"
                        }
                    )
                }
            }
        }
        
        // Multiple Error Types Example
        ExampleCard(
            title = "5. Multiple Error Types",
            description = "Example testing different types of errors"
        ) {
            scope.launch {
                selectedExample = "Multiple Errors"
                logMessage = "Testing Multiple Error Types..."
                
                val simpleExample = SimpleApiExample()
                val errorTypes = listOf("404", "500", "timeout", "network", "success")
                
                errorTypes.forEachIndexed { index, errorType ->
                    delay(1000)
                    simpleExample.fetchUser(errorType) { result ->
                        result.fold(
                            onSuccess = { user ->
                                logMessage = "Test ${index + 1}: Success - ${user.name}"
                            },
                            onFailure = { error ->
                                logMessage = "Test ${index + 1}: Error - ${error.message}"
                            }
                        )
                    }
                }
            }
        }
        
        // Log Display
        if (logMessage.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Log Output:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = logMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ExampleCard(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Run Example")
            }
        }
    }
}
