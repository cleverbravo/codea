package com.example.codea

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.codea.domain.ApkManager.ApkManager
import com.codea.domain.ApkManager.InstallSession
import com.codea.domain.ApkManager.ToolsApkNames
import com.example.codea.ui.theme.CodeaTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val apkManager = ApkManager()
        var result: Result<InstallSession>
        var text: String = ""
        CoroutineScope(Dispatchers.Default).launch {
            result = apkManager.installTools(this@MainActivity, ToolsApkNames.termux)
            result.onSuccess {
                text = "terminado"
            }.onFailure {
                text = "falla ${it.message}"
            }
        }

        enableEdgeToEdge()
        setContent {
            CodeaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = text,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
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