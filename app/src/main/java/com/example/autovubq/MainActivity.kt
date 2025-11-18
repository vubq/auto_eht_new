package com.example.autovubq

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autovubq.ui.theme.AutoVubqTheme
import com.topjohnwu.superuser.Shell

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        if (Settings.canDrawOverlays(this)) {
            startFloatingService()
        } else {
            Toast.makeText(
                this,
                "Vui lòng cấp quyền hiển thị trên ứng dụng khác để sử dụng nút nổi",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Shell.enableVerboseLogging = true
        Shell.setDefaultBuilder(
            Shell.Builder.create()
                .setFlags(Shell.FLAG_REDIRECT_STDERR)
                .setTimeout(10)
        )

        // Kiểm tra root ngay khi khởi động
        Shell.getShell {
            checkRootAccess()  // Shell đã sẵn sàng
        }

        AutoInstance.autoADB = AutoADB()
        TelegramBotInstance.telegramBot.start()

        setContent {
            AutoVubqTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen(
                        onStartClick = { handleStartClick() }
                    )
                }
            }
        }
    }

    private fun checkRootAccess() {
        if (Shell.isAppGrantedRoot() != true) {
            Log.e(TAG, "Ứng dụng chưa có quyền root!")
            Toast.makeText(
                this,
                "Ứng dụng cần quyền root để hoạt động!",
                Toast.LENGTH_LONG
            ).show()
        } else {
            Log.i(TAG, "Đã có quyền root")
        }
    }

    private fun handleStartClick() {
        Log.d(TAG, "Loại: ${AutoConfig.selectedAutoType}, Kịch bản: ${AutoConfig.selectedScenario}, Tìm B: ${AutoConfig.findConfigB}")

        // Kiểm tra root
        if (Shell.isAppGrantedRoot() != true) {
            Toast.makeText(
                this,
                "Ứng dụng cần quyền root để hoạt động!",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // Kiểm tra quyền overlay
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            overlayPermissionLauncher.launch(intent)
        } else {
            startFloatingService()
        }
    }

    private fun startFloatingService() {
        try {
            startService(Intent(this, FloatingService::class.java))
            moveTaskToBack(true)

            AutoInstance.autoADB.start(
                loaiAuto = AutoConfig.selectedAutoType,
                kichBan = AutoConfig.selectedScenario,
                timKiemCaThietLapB = AutoConfig.findConfigB
            )
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khởi động service: ${e.message}", e)
            Toast.makeText(
                this,
                "Lỗi khởi động: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cleanup khi activity bị destroy
        if (isFinishing) {
            AutoInstance.autoADB.cleanup()
        }
    }
}

@Composable
fun MainScreen(onStartClick: () -> Unit) {
    val autoTypes = listOf(
        "Trang bị",
        "Cường hóa",
        "Tẩy thuộc tính",
        "Thú cưỡi",
        "Tính cách",
        "Rương boss",
        "Backup",
        "Restore"
    )

    var selectedAutoType by remember { mutableStateOf(autoTypes[0]) }
    var selectedScenario by remember { mutableStateOf("") }
    var findConfigB by remember { mutableStateOf(true) }

    val scenarioOptions = remember(selectedAutoType) {
        when (selectedAutoType) {
            "Trang bị" -> listOf("Giáp", "Găng", "Giày", "Dây chuyền", "Nhẫn", "Vũ khí")
            "Cường hóa", "Tẩy thuộc tính" -> List(8) { "Ô ${it + 1}" }
            else -> emptyList()
        }
    }

    // Cập nhật scenario khi auto type thay đổi
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
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.headlineMedium
                )

                ExposedDropdown(
                    label = "Loại Auto",
                    items = autoTypes,
                    selectedItem = selectedAutoType,
                    onItemSelected = { selectedAutoType = it }
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
                        Checkbox(
                            checked = findConfigB,
                            onCheckedChange = { findConfigB = it }
                        )
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
                        .height(50.dp),
                    enabled = selectedScenario.isNotEmpty() || scenarioOptions.isEmpty()
                ) {
                    Text("Bắt đầu", fontSize = 18.sp)
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
        onExpandedChange = { expanded = it }
    ) {
        TextField(
            value = selectedItem,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}