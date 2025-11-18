package com.example.autovubq

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class AutoADB {
    companion object {
        private const val TAG = "AutoADB"
        private const val PATH_DATA = "/storage/emulated/0/AutoEHT/"
    }

    private val isRunning = AtomicBoolean(false)
    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Cache text recognizer
    private val textRecognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    /**
     * Bắt đầu auto với các tham số
     */
    fun start(loaiAuto: String, kichBan: String, timKiemCaThietLapB: Boolean) {
        if (isRunning.get()) {
            Log.w(TAG, "Auto đã đang chạy")
            return
        }

        // Kiểm tra root access
        if (Shell.isAppGrantedRoot() != true) {
            Log.e(TAG, "Không có quyền root!")
            return
        }

        isRunning.set(true)

        job = scope.launch {
            try {
                when (loaiAuto) {
                    "Trang bị" -> equip(kichBan, timKiemCaThietLapB)
                    "Cường hóa" -> strengthen(kichBan)
                    "Tẩy thuộc tính" -> eraseAttribute(kichBan)
                    "Rương boss" -> bossChest()
                    "Thú cưỡi" -> ridingAnimal()
                    "Tính cách" -> character()
                    "Backup" -> backupAppData()
                    "Restore" -> restoreAppData()
                    else -> Log.w(TAG, "Loại auto không hợp lệ: $loaiAuto")
                }
            } catch (e: CancellationException) {
                Log.i(TAG, "Auto đã bị dừng")
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi khi chạy auto: ${e.message}", e)
            } finally {
                isRunning.set(false)
            }
        }
    }

    /**
     * Dừng auto
     */
    fun stop() {
        isRunning.set(false)
        job?.cancel()
        Log.i(TAG, "Đã dừng auto")
    }

    /**
     * Đọc file từ storage
     */
    fun readFile(fileName: String): String {
        return try {
            val file = File("$PATH_DATA$fileName.txt")
            if (!file.exists()) {
                "Không có file!"
            } else {
                file.readText()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi đọc file: ${e.message}", e)
            "Lỗi đọc file: ${e.message}"
        }
    }

    /**
     * Xóa nội dung file
     */
    fun clearFile(fileName: String): String {
        return try {
            val file = File("$PATH_DATA$fileName.txt")
            if (!file.exists()) {
                "Không có file!"
            } else {
                file.writeText("")
                "Đã clear file!"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi clear file: ${e.message}", e)
            "Lỗi clear file: ${e.message}"
        }
    }

    /**
     * Thực thi lệnh shell với libsu
     */
    private suspend fun executeShell(command: String, delay: Long = 0): Shell.Result {
        return withContext(Dispatchers.IO) {
            if (!isRunning.get()) {
                throw CancellationException("Auto đã bị dừng")
            }

            val result = Shell.cmd(command).exec()

            if (!result.isSuccess) {
                Log.w(TAG, "Command failed: $command, code: ${result.code}")
            }

            if (delay > 0) {
                delay(delay)
            }

            result
        }
    }

    /**
     * Mở ứng dụng
     */
    private suspend fun openApp(packageName: String, delay: Long = 500) {
        executeShell("monkey -p $packageName -c android.intent.category.LAUNCHER 1", delay)
    }

    /**
     * Click tại tọa độ
     */
    private suspend fun click(x: Int, y: Int, delay: Long = 0) {
        executeShell("input tap $x $y", delay)
    }

    /**
     * Swipe từ điểm này sang điểm khác
     */
    private suspend fun swipe(x1: Int, y1: Int, x2: Int, y2: Int, speed: Int = 500, delay: Long = 0) {
        executeShell("input swipe $x1 $y1 $x2 $y2 $speed", delay)
    }

    /**
     * Chụp màn hình
     */
    private suspend fun screenCapture(fileName: String, delay: Long = 0) {
        val filePath = "$PATH_DATA$fileName.png"
        executeShell("screencap -p $filePath", delay)
    }

    /**
     * Điều chỉnh độ sáng màn hình
     */
    private suspend fun adjustBrightness(brightness: Int, delay: Long = 0) {
        executeShell("settings put system screen_brightness $brightness", delay)
    }

    /**
     * Lấy thời gian hiện tại
     */
    private fun getCurrentDateTime(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return formatter.format(Date())
    }

    /**
     * Chuyển đổi Unicode tiếng Việt
     */
    private fun normalizeVietnamese(text: String): String {
        val diacriticMap = mapOf(
            'á' to 'a', 'à' to 'a', 'ả' to 'a', 'ã' to 'a', 'ạ' to 'a',
            'ă' to 'a', 'ắ' to 'a', 'ằ' to 'a', 'ẳ' to 'a', 'ẵ' to 'a', 'ặ' to 'a',
            'â' to 'a', 'ấ' to 'a', 'ầ' to 'a', 'ẩ' to 'a', 'ẫ' to 'a', 'ậ' to 'a',
            'é' to 'e', 'è' to 'e', 'ẻ' to 'e', 'ẽ' to 'e', 'ẹ' to 'e',
            'ê' to 'e', 'ế' to 'e', 'ề' to 'e', 'ể' to 'e', 'ễ' to 'e', 'ệ' to 'e',
            'í' to 'i', 'ì' to 'i', 'ỉ' to 'i', 'ĩ' to 'i', 'ị' to 'i',
            'ó' to 'o', 'ò' to 'o', 'ỏ' to 'o', 'õ' to 'o', 'ọ' to 'o',
            'ô' to 'o', 'ố' to 'o', 'ồ' to 'o', 'ổ' to 'o', 'ỗ' to 'o', 'ộ' to 'o',
            'ơ' to 'o', 'ớ' to 'o', 'ờ' to 'o', 'ở' to 'o', 'ỡ' to 'o', 'ợ' to 'o',
            'ú' to 'u', 'ù' to 'u', 'ủ' to 'u', 'ũ' to 'u', 'ụ' to 'u',
            'ư' to 'u', 'ứ' to 'u', 'ừ' to 'u', 'ử' to 'u', 'ữ' to 'u', 'ự' to 'u',
            'ý' to 'y', 'ỳ' to 'y', 'ỷ' to 'y', 'ỹ' to 'y', 'ỵ' to 'y',
            'Đ' to 'D', 'đ' to 'd'
        )
        return text.map { diacriticMap[it] ?: it }.joinToString("")
    }

    /**
     * Nhận dạng text từ ảnh
     */
    private suspend fun recognizeText(imagePath: String): String {
        return withContext(Dispatchers.Default) {
            try {
                val file = File(imagePath)
                if (!file.exists()) {
                    Log.w(TAG, "File không tồn tại: $imagePath")
                    return@withContext ""
                }

                val bitmap = BitmapFactory.decodeFile(imagePath)
                    ?: return@withContext ""

                val image = InputImage.fromBitmap(bitmap, 0)
                val visionText = textRecognizer.process(image).await()

                normalizeVietnamese(visionText.text)
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi nhận dạng text: ${e.message}", e)
                ""
            }
        }
    }

    /**
     * Kiểm tra text từ ảnh có chứa các từ khóa
     */
    private suspend fun checkTextInImage(
        fileName: String,
        keywords: List<String>,
        attempt: Int
    ): Boolean {
        return withContext(Dispatchers.Default) {
            try {
                val imagePath = "$PATH_DATA$fileName.png"
                val text = recognizeText(imagePath)

                if (text.isEmpty()) {
                    Log.w(TAG, "Không nhận dạng được text từ ảnh")
                    return@withContext false
                }

                val found = keywords.any { keyword ->
                    text.contains(keyword, ignoreCase = true)
                }

                // Ghi log
                val logText = "Lần $attempt: $text - $found - ${getCurrentDateTime()}"
                Log.d(TAG, logText)

                // Lưu vào file
                withContext(Dispatchers.IO) {
                    try {
                        val logFile = File("$PATH_DATA$fileName.txt")
                        logFile.parentFile?.mkdirs()
                        logFile.appendText("$logText\n")
                    } catch (e: Exception) {
                        Log.e(TAG, "Lỗi ghi log: ${e.message}", e)
                    }
                }

                found
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi kiểm tra text: ${e.message}", e)
                false
            }
        }
    }

    /**
     * Cắt ảnh
     */
    private suspend fun cropImage(fileName: String, x: Int, y: Int, width: Int, height: Int) {
        withContext(Dispatchers.IO) {
            try {
                val filePath = "$PATH_DATA$fileName.png"
                val file = File(filePath)

                if (!file.exists()) {
                    Log.w(TAG, "File không tồn tại: $filePath")
                    return@withContext
                }

                val bitmap = BitmapFactory.decodeFile(filePath)
                    ?: return@withContext

                // Kiểm tra bounds
                if (x < 0 || y < 0 || x + width > bitmap.width || y + height > bitmap.height) {
                    Log.w(TAG, "Tọa độ crop không hợp lệ")
                    return@withContext
                }

                val croppedBitmap = Bitmap.createBitmap(bitmap, x, y, width, height)

                FileOutputStream(file).use { output ->
                    croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)
                    output.flush()
                }

                if (bitmap != croppedBitmap) {
                    bitmap.recycle()
                }
                croppedBitmap.recycle()
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi crop ảnh: ${e.message}", e)
            }
        }
    }

    /**
     * Test chụp màn hình
     */
    fun screenCapture() {
        scope.launch {
            try {
                screenCapture("test", 0)
                Log.i(TAG, "Đã chụp màn hình test")
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi chụp màn hình: ${e.message}", e)
            }
        }
    }

    /**
     * Khởi tạo app
     */
    private suspend fun initAuto() {
        // Mở App Backup
        openApp("com.machiav3lli.backup", 500)

        // Nhấn khôi phục
        click(841, 1958, 500)

        // Nhấn OK
        click(942, 1517, 5000)

        // Mở EHT
        openApp("com.superplanet.evilhunter", 13000)

        // Nhấn Touch To Start
        click(505, 1995, 29000)

        // Nhấn đóng
        click(530, 1800, 500)
    }

    /**
     * Backup dữ liệu
     */
    private suspend fun backup() {
        // Mở App Backup
        openApp("com.machiav3lli.backup", 500)

        // Nhấn sao lưu
        click(257, 1374, 500)

        // Nhấn dữ liệu phương tiện
        click(124, 1468, 500)

        // Nhấn OK
        click(935, 1640, 8000)
    }

    /**
     * Auto trang bị
     */
    private suspend fun equip(kichBan: String, timKiemCaThietLapB: Boolean) {
        while (isRunning.get()) {
            try {
                initAuto()

                // Nhấn chọn lò rèn hoặc kim hoàn
                when (kichBan) {
                    "Dây chuyền", "Nhẫn" -> click(735, 1486, 500) // Kim hoàn
                    else -> click(432, 1361, 500) // Lò rèn
                }

                // Nhấn chọn loại đồ
                when (kichBan) {
                    "Giáp", "Nhẫn" -> click(286, 929, 500)
                    "Găng" -> click(387, 933, 500)
                    "Giày" -> click(491, 929, 500)
                }

                // Nhấn chọn đồ
                if (kichBan == "Vũ khí") {
                    repeat(3) { swipe(390, 1510, 390, 985, 500, 0) }
                    swipe(390, 1510, 390, 985, 500, 500)
                    click(527, 1471, 500)
                } else {
                    swipe(390, 1510, 390, 985, 500, 500)
                    click(248, 1465, 500)
                }

                // Kéo đầy thanh
                swipe(241, 1786, 965, 1786, 500, 500)

                // Nhấn điều chế
                click(364, 1977, 7000)

                // Nhấn tìm thuộc tính
                click(520, 910, 500)

                // Nhấn thiết lập sẵn A
                click(183, 527, 500)

                // Nhấn tìm kiếm
                click(335, 2045, 2000)

                // Chụp và kiểm tra
                screenCapture("Equip", 0)
                cropImage("Equip", 85, 865, 623, 107)

                val keywords = listOf("4 thuoc tinh co hieu luc")
                if (checkTextInImage("Equip", keywords, 1)) {
                    Log.i(TAG, "Đã tìm thấy trang bị phù hợp (A)")
                    break
                }

                if (!timKiemCaThietLapB) continue

                // Kiểm tra thiết lập B
                click(527, 2084, 500)
                click(520, 910, 500)
                click(455, 530, 500)
                click(335, 2045, 2000)

                screenCapture("Equip", 0)
                cropImage("Equip", 85, 865, 623, 107)

                if (checkTextInImage("Equip", keywords, 2)) {
                    Log.i(TAG, "Đã tìm thấy trang bị phù hợp (B)")
                    break
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi trong equip: ${e.message}", e)
                delay(1000)
            }
        }
    }

    /**
     * Auto cường hóa
     */
    private suspend fun strengthen(kichBan: String) {
        while (isRunning.get()) {
            try {
                initAuto()

                // Nhấn chọn cường hóa thần
                click(535, 990, 500)

                // Nhấn chọn ô
                val positions = mapOf(
                    "Ô 1" to Pair(198, 1746),
                    "Ô 2" to Pair(292, 1746),
                    "Ô 3" to Pair(389, 1746),
                    "Ô 4" to Pair(483, 1746),
                    "Ô 5" to Pair(584, 1746),
                    "Ô 6" to Pair(678, 1746),
                    "Ô 7" to Pair(779, 1746),
                    "Ô 8" to Pair(873, 1746)
                )

                positions[kichBan]?.let { (x, y) -> click(x, y, 500) }

                // Kiểm tra đã max chưa
                screenCapture("StrengthenMax", 0)
                cropImage("StrengthenMax", 109, 1262, 966 - 109, 1360 - 1262)

                if (checkTextInImage("StrengthenMax", listOf("Khong the cuong hoa than them nua"), 1)) {
                    Log.i(TAG, "Đã cường hóa max")
                    break
                }

                // Nhấn cường hóa
                click(303, 2002, 7000)

                // Kiểm tra thành công
                screenCapture("Strengthen", 0)
                cropImage("Strengthen", 186, 762, 881 - 186, 876 - 762)

                if (checkTextInImage("Strengthen", listOf("Cuong Hoa Thanh Cong"), 1)) {
                    backup()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi trong strengthen: ${e.message}", e)
                delay(1000)
            }
        }
    }

    /**
     * Auto tẩy thuộc tính
     */
    private suspend fun eraseAttribute(kichBan: String) {
        while (isRunning.get()) {
            try {
                initAuto()

                // Nhấn chọn loại bỏ thuộc tính
                click(535, 990, 500)

                // Nhấn chọn ô
                val positions = mapOf(
                    "Ô 1" to Pair(198, 1746),
                    "Ô 2" to Pair(292, 1746),
                    "Ô 3" to Pair(389, 1746),
                    "Ô 4" to Pair(483, 1746),
                    "Ô 5" to Pair(584, 1746),
                    "Ô 6" to Pair(678, 1746),
                    "Ô 7" to Pair(779, 1746),
                    "Ô 8" to Pair(873, 1746)
                )

                positions[kichBan]?.let { (x, y) -> click(x, y, 500) }

                // Kiểm tra còn thuộc tính âm không
                screenCapture("EraseAttributeMax", 0)
                cropImage("EraseAttributeMax", 125, 1490, 817, 87)

                if (checkTextInImage("EraseAttributeMax", listOf("Khong co thuoc tinh am de loai bo"), 1)) {
                    Log.i(TAG, "Không còn thuộc tính âm")
                    break
                }

                // Nhấn loại bỏ
                click(303, 2002, 7000)

                // Kiểm tra thành công
                screenCapture("EraseAttribute", 0)
                cropImage("EraseAttribute", 206, 783, 663, 85)

                if (checkTextInImage("EraseAttribute", listOf("Da loai bo"), 1)) {
                    backup()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi trong eraseAttribute: ${e.message}", e)
                delay(1000)
            }
        }
    }

    /**
     * Auto thú cưỡi
     */
    private suspend fun ridingAnimal() {
        while (isRunning.get()) {
            try {
                initAuto()

                click(1005, 910, 5000)
                click(190, 2265, 500)
                click(377, 1672, 500)
                click(274, 1517, 2000)

                screenCapture("RidingAnimal", 0)
                cropImage("RidingAnimal", 98, 754, 266, 68)

                if (checkTextInImage("RidingAnimal", listOf("LEO S", "BLUBEE S", "PINIA S", "INFERNO S"), 1)) {
                    backup()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi trong ridingAnimal: ${e.message}", e)
                delay(1000)
            }
        }
    }

    /**
     * Auto rương boss
     */
    private suspend fun bossChest() {
        while (isRunning.get()) {
            try {
                initAuto()

                // Nhấn mở rương
                click(751, 2315, 500)

                // Nhấn tab đồ đặc biệt
                click(666, 1368, 500)

                // Kéo xuống
                repeat(4) {
                    swipe(430, 2181, 430, 1429, 500, 500)
                }

                // Nhấn vào rương boss
                click(938, 1701, 500)

                // Nhấn sử dụng
                click(277, 1640, 7000)

                // Kiểm tra item đầu tiên
                screenCapture("BossChest", 0)
                cropImage("BossChest", 389, 1197, 682 - 389, 1299 - 1197)

                if (!checkTextInImage("BossChest", listOf("Tinh Chat Vua"), 1)) {
                    Log.i(TAG, "Không phải Tinh Chất Vua")
                    break
                }

                // Kiểm tra 11 item tiếp theo
                repeat(11) {
                    click(877, 1127, 2000)

                    screenCapture("BossChest", 0)
                    cropImage("BossChest", 389, 1197, 682 - 389, 1299 - 1197)

//                    if (!checkTextInImage("BossChest", listOf("Tinh Chat Vua"), 1)) {
//                        Log.i(TAG, "Tìm thấy item khác Tinh Chất Vua")
//                        break
//                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi trong bossChest: ${e.message}", e)
                delay(1000)
            }
        }
    }

    /**
     * Auto tính cách
     */
    private suspend fun character() {
        while (isRunning.get()) {
            try {
                initAuto()

                // Nhấn mở rương
                click(751, 2315, 500)

                // Nhấn tab đồ đặc biệt
                click(666, 1368, 500)

                // Kéo xuống
                repeat(4) {
                    swipe(430, 2181, 430, 1429, 500, 500)
                }

                // Nhấn vào bình
                click(747, 1500, 500)

                // Nhấn sử dụng
                click(355, 1611, 500)

                // Chọn hunter
                click(293, 1011, 500)

                // Nhấn thay đổi
                click(371, 1731, 4000)

                // Nhấn thợ săn
                click(543, 2308, 1000)

                // Chọn thợ săn đầu
                click(121, 1763, 4000)

                // Kiểm tra tính cách
                screenCapture("Character", 0)
                cropImage("Character", 202, 404, 867 - 202, 498 - 404)

                if (checkTextInImage("Character", listOf("Nhanh Nhen"), 1)) {
                    Log.i(TAG, "Đã có tính cách Nhanh Nhẹn")
                    break
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi trong character: ${e.message}", e)
                delay(1000)
            }
        }
    }

    private val mmShell by lazy {
        Shell.Builder.create()
            .setFlags(Shell.FLAG_MOUNT_MASTER)   // tương đương su -mm
            .build()
    }

    private suspend fun backupAppData() {
        withContext(Dispatchers.IO) {

            val src = "/data/data/com.superplanet.evilhunter"
            val dst = "/storage/emulated/0/AutoEHT/backup_evilhunter.tar"

            // Đảm bảo thư mục
            Shell.cmd("mkdir -p /storage/emulated/0/AutoEHT").exec()

            val cmd = """
            cd /data/data
            tar -cf "$dst" "com.superplanet.evilhunter"
        """.trimIndent()

            val result = mmShell.newJob().add(cmd).exec()

            if (!result.isSuccess) {
                Log.e("AutoADB", "Backup FAILED: exitCode = ${result.code}")
            } else {
                Log.i("AutoADB", "Backup completed: $dst")
            }
        }
    }

    private suspend fun restoreAppData() {
        withContext(Dispatchers.IO) {

            val src = "/storage/emulated/0/AutoEHT/backup_evilhunter.tar"
            val dst = "/data/data"

            // Dừng app trước khi restore
            mmShell.newJob().add("am force-stop com.superplanet.evilhunter").exec()

            val cmd = """
            cd "$dst"
            rm -rf com.superplanet.evilhunter
            tar -xf "$src"
            chown -R u0_a349:u0_a349 com.superplanet.evilhunter
        """.trimIndent()

            val result = mmShell.newJob().add(cmd).exec()

            if (!result.isSuccess) {
                Log.e("AutoADB", "Restore FAILED: exitCode = ${result.code}")
            } else {
                Log.i("AutoADB", "Restore completed successfully")
            }
        }
    }


    /**
     * Cleanup khi không còn sử dụng
     */
    fun cleanup() {
        stop()
        scope.cancel()
        textRecognizer.close()
    }
}