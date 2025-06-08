package com.example.codea

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.codea.domain.ApkManager.ApkManager
import com.codea.domain.ApkManager.ToolsApkNames
import com.codea.domain.TerminalManager.BashCommandExecutor
import com.example.codea.ui.theme.CodeaTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val apkManager = ApkManager()

        var statusDialogVisible by mutableStateOf(true)
        var statusMessage by mutableStateOf("Installing Termux…")
        var showWebView by mutableStateOf(false)
        var showErrorDialog by mutableStateOf(false)
        var errorMessage by mutableStateOf("")
        var lastCommand by mutableStateOf(false)

        CoroutineScope(Dispatchers.Default).launch {
            val installResult = apkManager.installTools(this@MainActivity, ToolsApkNames.termux)
            installResult.onSuccess {

                statusMessage = "Termux is installed."

                val commands =
                    arrayOf(
                        "pkg update -y",
                        "pkg upgrade -y",
                        "pkg install tur-repo -y",
                        "pkg install code-server -y",
                        "code-server --auth none &"
                    )
                val installCodeServer = BashCommandExecutor(this@MainActivity)
                for (command in commands) {
                    launch {
                        /*installCodeServer.executeCommand(command).collect { line ->
                            Toast.makeText(this@MainActivity, line, Toast.LENGTH_SHORT)
                        }*/
                        //send to the aditional permission in android to set the Run commands in termux is enabled
                        statusMessage = "Running: $command"
                        val commandResult = installCodeServer.executeCommand(command)
                        commandResult.onSuccess {
                            statusMessage = "Success: $command"
                            if (command == commands.last()) {
                                lastCommand = true
                                statusDialogVisible = false
                                showWebView = true
                            }
                        }.onFailure {
//                            errorMessage = "Failure: ${throwable.message}"
                            errorMessage = "Failure:${it.message}"
                            statusDialogVisible = false
                            showErrorDialog = true

                            showWebView = false
                        }
                    }.join()
                }
            }.onFailure {
                errorMessage = "Error installing:${it.message}"

                Handler(Looper.getMainLooper()).postDelayed({
                    showWebView = false
                    statusDialogVisible = false
                    showErrorDialog = true
                }, 800)
            }

        }

        enableEdgeToEdge()
        setContent {
            CodeaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        if (showErrorDialog) {
                            ErrorDialog(
                                message = errorMessage,
                                onDismiss = { showErrorDialog = false }
                            )
                        }
                        // 2) Else if the status popup is visible, show that (no WebView yet)
                        else if (statusDialogVisible) {
                            StatusDialog(message = statusMessage)
                        }
                        // 3) Else if installation + commands succeeded, show the WebView
                        else if (showWebView) {
                            // Display WebView when installation succeeds
                            WebViewScreen(url = "http://127.0.0.1:8080 ")
                        } else {
                            // Initial screen
                            Greeting(
                                name = "Loading...",
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusDialog(message: String) {
    // A Compose Dialog that never dismisses by tapping outside (onDismissRequest = { })
    Dialog(onDismissRequest = { /* no-op, so it stays visible until we hide it */ }) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 8.dp,
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .padding(64.dp)
                    .width(IntrinsicSize.Max),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Please wait",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 60.dp)
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(10.dp)

                )
            }
        }
    }
}

@Composable
fun WebViewScreen(url: String) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                loadUrl(url)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun ErrorDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Error") },
        text = { Text(text = message) },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true, name = "preview1")
@Composable
fun GreetingPreview() {
    CodeaTheme {
        Greeting("Android")
    }
}