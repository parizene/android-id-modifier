package com.parizene.androididmodifier

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.parizene.androididmodifier.ui.theme.AndroidIdModifierTheme
import com.parizene.androididmodifier.xml.AppInfo
import com.parizene.androididmodifier.xml.SettingInfo
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidIdModifierTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val pairs by viewModel.appInfoList.observeAsState(listOf())
                    LazyColumn {
                        items(pairs) { appInfo ->
                            AppItem(appInfo)
                        }
                    }
                }
            }
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AppItem(pair: Pair<SettingInfo, AppInfo?>) {
        var showDialog by remember { mutableStateOf(false) }

        if (showDialog) {
            var value by remember { mutableStateOf(pair.first.value) }
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(pair.first.packageName) },
                text = {
                    Column {
                        TextField(value = value, onValueChange = { newValue ->
                            value = newValue
                        }, modifier = Modifier.padding(top = 4.dp))
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        showDialog = false
                        viewModel.handleUpdateValue(pair.first.packageName, value)
                    }) {
                        Text("OK")
                    }
                }
            )
        }

        Card(
            onClick = { showDialog = true }, modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                Box(
                    modifier = Modifier.size(48.dp)
                ) {
                    pair.second?.let {
                        Image(
                            bitmap = it.icon.toBitmap().asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(
                        text = pair.first.packageName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = pair.first.value,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
