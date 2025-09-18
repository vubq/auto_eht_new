package com.example.autovubq

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.example.autovubq.ui.theme.AutoVubqTheme

class MainActivity : ComponentActivity() {
    companion object {
        const val REQUEST_CODE_OVERLAY = 1001
    }

    private fun checkStoragePermissionAndThen(actionIfGranted: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Toast.makeText(this, "Vui lòng cấp quyền truy cập bộ nhớ", Toast.LENGTH_SHORT)
                    .show()
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            } else {
                actionIfGranted()
            }
        } else {
            // Với Android < 11 (nếu cần), bạn có thể bổ sung xin READ_EXTERNAL_STORAGE
            actionIfGranted()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AutoInstance.autoADB = AutoADB(this)
        TelegramBotInstance.telegramBot.start()

        setContent {
            AutoVubqTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen(
                        onStartClick = {
                            Log.d(
                                "AUTO",
                                "Loại: ${AutoConfig.selectedAutoType}, Kịch bản: ${AutoConfig.selectedScenario}, Tìm B: ${AutoConfig.findConfigB}"
                            )

                            checkStoragePermissionAndThen {
                                if (!Settings.canDrawOverlays(this)) {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        "package:$packageName".toUri()
                                    )
                                    startActivityForResult(intent, REQUEST_CODE_OVERLAY)
                                } else {
                                    startService(Intent(this, FloatingService::class.java))
                                    moveTaskToBack(true)
                                    AutoInstance.autoADB.start(
                                        loaiAuto = AutoConfig.selectedAutoType,
                                        kichBan = AutoConfig.selectedScenario,
                                        timKiemCaThietLapB = AutoConfig.findConfigB
                                    )
                                }
                            }
                        },
                        onOpenGameClick = {
                            startService(Intent(this, FloatingService::class.java))
                            moveTaskToBack(true)
                            AutoInstance.autoADB.moGame()
                        }
                    )
                }
            }
        }
    }

    @Deprecated("Use registerForActivityResult instead")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_OVERLAY) {
            if (Settings.canDrawOverlays(this)) {
                startService(Intent(this, FloatingService::class.java))
                moveTaskToBack(true)
            } else {
                Toast.makeText(
                    this,
                    "Vui lòng cấp quyền hiển thị trên ứng dụng khác để sử dụng nút nổi",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}

@Composable
fun MainScreen(
    onStartClick: () -> Unit,
    onOpenGameClick: () -> Unit
) {
    val autoTypes = listOf(
        "Trang bị",
        "Cường hóa",
        "Tẩy thuộc tính",
        "Thú cưỡi",
        "Tính cách",
        "Rương boss",
        "Rương trang bị thú",
        "Test"
    )

    val initialScenarioOptions = when (autoTypes[0]) {
        "Trang bị" -> listOf("Giáp", "Găng", "Giày", "Dây chuyền", "Nhẫn", "Vũ khí")
        "Cường hóa", "Tẩy thuộc tính" -> List(8) { "Ô ${it + 1}" }
        else -> emptyList()
    }

    var selectedAutoType by remember { mutableStateOf(autoTypes[0]) }
    var selectedScenario by remember { mutableStateOf(initialScenarioOptions.firstOrNull() ?: "") }
    var findConfigB by remember { mutableStateOf(true) }

    val scenarioOptions = when (selectedAutoType) {
        "Trang bị" -> listOf("Giáp", "Găng", "Giày", "Dây chuyền", "Nhẫn", "Vũ khí")
        "Cường hóa", "Tẩy thuộc tính" -> List(8) { "Ô ${it + 1}" }
        else -> emptyList()
    }

    // Cập nhật lại selectedScenario mỗi khi loại auto thay đổi
    LaunchedEffect(selectedAutoType) {
        selectedScenario = scenarioOptions.firstOrNull() ?: ""
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(0.9f),
            elevation = CardDefaults.cardElevation(10.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    "Cấu hình Auto",
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                ExposedDropdown(
                    label = "Loại Auto",
                    items = autoTypes,
                    selectedItem = selectedAutoType,
                    onItemSelected = {
                        selectedAutoType = it
                    }
                )

                if (scenarioOptions.isNotEmpty()) {
                    ExposedDropdown(
                        label = "Kịch bản",
                        items = scenarioOptions,
                        selectedItem = selectedScenario,
                        onItemSelected = { selectedScenario = it }
                    )
                }

                if (selectedAutoType == "Trang bị") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = findConfigB, onCheckedChange = { findConfigB = it })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tìm cả thiết lập B")
                    }
                }

                Button(
                    onClick = {
                        AutoConfig.selectedAutoType = selectedAutoType
                        AutoConfig.selectedScenario = selectedScenario
                        AutoConfig.findConfigB = findConfigB

                        onStartClick()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Bắt đầu", fontSize = 18.sp)
                }

                Button(
                    onClick = onOpenGameClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Mở game", fontSize = 18.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExposedDropdown(
    label: String,
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            value = selectedItem,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        onItemSelected(selectionOption)
                        expanded = false
                    }
                )
            }
        }
    }
}
