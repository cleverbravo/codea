package com.example.codea

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.codea.JSEventsBridge
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

        var statusDialogVisible by mutableStateOf(true)
        var statusMessage by mutableStateOf("Installing Termux…")
        var showWebView by mutableStateOf(false)
        var showErrorDialog by mutableStateOf(false)
        var errorMessage by mutableStateOf("")
        var lastCommand by mutableStateOf(false)

        val apkManager = ApkManager()
        apkManager.statusMessage = statusMessage

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
                        "code-server --auth none &",
                        "sleep 1",
//                        "pkg install termux-services -y",
//                        "echo \"#!/data/data/com.termux/files/usr/bin/sh\" > code-service.sh",
//                        "echo \"code-server --auth none\" >> code-service.sh",
//                        "chmod +x code-service.sh",
//                        "sv-enable code-service",
//                        "sv start code-service",
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
                launch {
//                    installCodeServer.executeCommand("code-server --auth none")
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
                        } else if (statusDialogVisible) {
                            StatusDialog(message = statusMessage)
                        } else if (showWebView) {
                            WebViewScreen(
                                url = "http://127.0.0.1:8080",
                                onSwipeDown = { },
                                this@MainActivity
                            )
                        } else {
                            //???
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
    Dialog(onDismissRequest = { /*stays visible until we hide it*/ }) {
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
fun WebViewScreen(url: String, onSwipeDown: () -> Unit, context: Context) {
    var webView: WebView? by remember { mutableStateOf(null) }
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                addJavascriptInterface(JSEventsBridge({ this.reload() }, context), "WebViewBridge")
                webViewClient = object : WebViewClient() {
                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        if (request?.isForMainFrame == true) {
//                            val errorHtml = """
//                                <html>
//                                    <body style="display:flex;justify-content:center;align-items:center;height:100vh;flex-direction:column;">
//                                    <br/> <br/> <br/>
//                                        <p>Error webResource loading page: ${'$'}{error?.description}</p>
//                                        <!--<button onclick="WebViewBridge.retryAndReload()">Reload</button>-->
//                                        <button onclick="window.location.href='$url'">Reload</button>
//                                    </body>
//                                </html>
//                            """.trimIndent()
                            val errorHtml = """
                                <!DOCTYPE html>
                                <html lang="en">
                                    <head>
                                    <meta charset="UTF-8">
                                    <title>Auto Reload Page</title>
                                    <meta http-equiv="refresh" content="1;url=$url"> <!-- Refresh every 5 seconds -->
                                </head>
                                <body>
                                    <h1>Reloading UI...</h1>
                                </body>
                                </html>
                            """.trimIndent()
                            view?.loadDataWithBaseURL(null, errorHtml, "text/html", "UTF-8", null)
//                            webView?.loadUrl(url)
                        }
                    }

                    override fun onReceivedHttpError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        errorResponse: WebResourceResponse?
                    ) {
                        if (request?.isForMainFrame == true && errorResponse != null) {
                            val errorHtml = """
                                <html>
                                    <body style="display:flex;justify-content:center;align-items:center;height:100vh;flex-direction:column;">
                                    <br/> <br/> <br/>
                                        <p>HTTP Error ${'$'}{errorResponse.statusCode}</p>
                                        <!--<button onclick="WebViewBridge.retryAndReload()">Reload</button>-->
                                        <button onclick="window.location.href='$url'">Reload</button>
                                    </body>
                                </html>
                            """.trimIndent()
//                            webView?.loadUrl(url)
                            view?.loadDataWithBaseURL(null, errorHtml, "text/html", "UTF-8", null)
                        }
                    }
                }
                loadUrl(url)
                webView = this
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures { change, dragAmount ->
                    if (dragAmount > 0) {
                        onSwipeDown()
                        webView?.clearCache(true)
//                        webView?.reload()
                        webView?.loadUrl(url)
                        change.consume()
                    }
                }
            }
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