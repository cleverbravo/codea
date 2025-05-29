package com.example.codea

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
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
//        var result: Result<InstallSession>

        var showWebView = true
        var showErrorDialog = false
        var errorMessage = ""

        CoroutineScope(Dispatchers.Default).launch {
            val installResult = apkManager.installTools(this@MainActivity, ToolsApkNames.termux)
//            result.onSuccess {
//                text = "terminado"
//            }.onFailure {
//                text = "falla ${it.message}"
//            }

            installResult.onSuccess {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(this@MainActivity, "Termux is installed.", Toast.LENGTH_SHORT)
                }
                val commands = arrayOf("pkg install tur-repo code-server -y")
                val installCodeServer = BashCommandExecutor(this@MainActivity)
                for (command in commands) {
                    launch {
                        /*installCodeServer.executeCommand(command).collect { line ->
                            Toast.makeText(this@MainActivity, line, Toast.LENGTH_SHORT)
                        }*/
                        //send to the aditional permission in android to set the Run commands in termux is enabled
                        val commandResult = installCodeServer.executeCommand(command)
                        commandResult.onSuccess {
                            Toast.makeText(
                                this@MainActivity,
                                "Sucess:${command}",
                                Toast.LENGTH_SHORT
                            )
                        }.onFailure {
                            errorMessage = "Failure:${it.message}"
                            showWebView = false
                            showErrorDialog = true
                        }
                    }
                }
            }.onFailure {
                Handler(Looper.getMainLooper()).post {
                    errorMessage = "Error installing:${it.message}"
                    showWebView = false
                    showErrorDialog = true
                }
            }

        }

        enableEdgeToEdge()
        setContent {
            CodeaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        if (showWebView) {
                            // Display WebView when installation succeeds
                            WebViewScreen(url = "https://www.example.com ")
                        } else {
                            // Initial screen
                            Greeting(
                                name = "Loading...",
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        // Error dialog
                        if (showErrorDialog) {
                            ErrorDialog(
                                message = errorMessage,
                                onDismiss = { showErrorDialog = false }
                            )
                        }
                    }
                }
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