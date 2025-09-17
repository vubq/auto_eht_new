package com.example.autovubq

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.io.BufferedWriter
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AutoADB(private val context: Context) {

    private var pathData: String = context.getExternalFilesDir(null)?.absolutePath + "/"
    private var auto = false
    private var loaiAuto = "Trang bị"
    private var kichBan = "Giáp"
    private var timKiemCaThietLapB = true

    private var job: Job? = null

    fun start(loaiAuto: String, kichBan: String, timKiemCaThietLapB: Boolean) {
        if (auto) return
        this.loaiAuto = loaiAuto
        this.kichBan = kichBan
        this.timKiemCaThietLapB = timKiemCaThietLapB
        auto = true

        job = CoroutineScope(Dispatchers.Default).launch {
            when (loaiAuto) {
                "Trang bị" -> trangBi()
                "Cường hóa" -> cuongHoa()
                "Thú cưỡi" -> thuCuoi()
                "Tẩy thuộc tính" -> tayThuocTinh()
                else -> {}
            }
        }
    }

    fun stop() {
        auto = false
        job?.cancel()
    }

    fun moGame() {
        job = CoroutineScope(Dispatchers.Default).launch {
            auto = true
            Thread {
                while (auto) {
                    "com.superplanet.evilhunter".openApp(0)
                    auto = false
                }
            }.start()
        }
        job?.cancel()
    }

    fun dongGame() {
        job = CoroutineScope(Dispatchers.Default).launch {
            auto = true
            Thread {
                while (auto) {
                    println("dosngasdasdasfd ")
                    "com.superplanet.evilhunter".closeApp(0)
                    auto = false
                }
            }.start()
        }
        job?.cancel()
    }

    fun docFile(fileName: String): String {
        val file = File("$pathData$fileName.txt")
        if (!file.exists()) {
            return "Không có file"
        } else {
            if (file.readText().isEmpty()) {
                return "Chưa có dữ liệu"
            }
            return file.readText()
        }
    }

    fun xoaFile(fileName: String): String {
        val file = File("$pathData$fileName.txt")
        if (!file.exists()) {
            return "Không có file"
        } else {
            file.writeText("")
            return "Đã xóa dữ liệu"
        }
    }

    private fun String.adbExecution(delay: Long) {
        if (!auto) return
        val process = Runtime.getRuntime().exec(arrayOf("su", "-c", this))
        process.waitFor()
        Thread.sleep(delay)
    }

    private fun String.openApp(delay: Long) {
        "monkey -p $this -c android.intent.category.LAUNCHER 1".adbExecution(delay)
    }

    private fun String.closeApp(delay: Long) {
        "am force-stop $this".adbExecution(delay)
    }

    private fun click(x: Int, y: Int, delay: Long) {
        "input tap $x $y".adbExecution(delay)
    }

    private fun swipe(x1: Int, y1: Int, x2: Int, y2: Int, speed: Int = 500, delay: Long) {
        "input swipe $x1 $y1 $x2 $y2 $speed".adbExecution(delay)
    }

    private fun String.screenCapture(delay: Long) {
        "screencap -p $pathData$this.png".adbExecution(delay)
    }

    private fun adjustBrightness(i: Int, delay: Long) {
        "shell settings put system screen_brightness $i".adbExecution(delay)
    }

    private fun getCurrentDateTime(): String {
        val calendar = Calendar.getInstance()
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return formatter.format(calendar.time)
    }

    private fun unicode(text: String): String {
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

    private suspend fun recognizeText(image: String): String {
        val inputStream: InputStream = File(image).inputStream()
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val visionText = recognizer.process(InputImage.fromBitmap(bitmap, 0)).await()
        return unicode(visionText.text)
    }

    private fun getTextFromImage(
        fileName: String,
        comparativeWords: List<String>,
        bout: Int,
    ): Boolean = runBlocking {
        val text: String = recognizeText("$pathData$fileName.png")

        if (text.isEmpty()) return@runBlocking false

        val exist = comparativeWords.any { text.contains(it, ignoreCase = true) }

//        telegramBot.sendMessage("$text - $exist")

        val textToAppend = "Lần $bout: $text - $exist" + " - " + getCurrentDateTime()

        BufferedWriter(FileWriter("$pathData$fileName.txt", true)).use { writer ->
            writer.write(textToAppend)
            writer.newLine()
        }

        return@runBlocking exist
    }

    private fun cropImage(fileName: String, x: Int, y: Int, width: Int, height: Int) {
        try {
            val file = File(pathData, "$fileName.png")

            if (!file.exists()) {
                throw FileNotFoundException("File không tồn tại: ${file.absolutePath}")
            }

            val bitmap = file.inputStream().use { BitmapFactory.decodeStream(it) }
                ?: throw IllegalArgumentException("Không thể đọc bitmap từ file")

            val safeWidth = minOf(width, bitmap.width - x)
            val safeHeight = minOf(height, bitmap.height - y)
            if (safeWidth <= 0 || safeHeight <= 0) throw IllegalArgumentException("Kích thước crop không hợp lệ")

            val croppedBitmap = Bitmap.createBitmap(bitmap, x, y, safeWidth, safeHeight)

            FileOutputStream(file).use {
                croppedBitmap.compress(
                    Bitmap.CompressFormat.PNG,
                    100,
                    it
                )
            }

        } catch (e: Exception) {
            TelegramBotInstance.telegramBot.sendMessage(e.message.toString())
        }
    }

    private fun initAuto() {
        //Dong eht
        "com.superplanet.evilhunter".closeApp(500)

        //Mo titanium backup
        "com.keramidas.TitaniumBackup".openApp(500)

        //Chon eht
        click(337, 339, 500)

        //Nhan restore
        click(124, 481, 500)

        //Nhan data only
        click(150, 793, 2000)

        //Mở EHT
        "com.superplanet.evilhunter".openApp(10000)

        //Nhan touch to start
        click(345, 1090, 18000)

        //Nhan dong
        click(359, 972, 500)
    }

    private fun backup() {
        //Mo titanium backup
        "com.keramidas.TitaniumBackup".openApp(500)

        //Chon eht
        click(266, 716, 500)

        //Nhan backup
        click(147, 284, 8000)
    }

    private fun trangBi() {
        auto = true
        TelegramBotInstance.telegramBot.sendMessage("Bắt đầu auto: Trang bị")
        Thread {
            while (auto) {
                initAuto()

                //Nhấn chọn lò rèn hoặc kim hoàn
                if (kichBan == "Dây chuyền" || kichBan == "Nhẫn") {
                    //Kim hoàn
                    click(464, 749, 500)
                } else {
                    //Lò rèn
                    click(305, 685, 500)
                }

                //Nhấn chọn loại đồ
                if (kichBan == "Giáp" || kichBan == "Nhẫn") {
                    //Giáp or nhẫn
                    click(206, 448, 500)
                }
                if (kichBan == "Găng") {
                    //Găng
                    click(268, 448, 500)
                }
                if (kichBan == "Giày") {
                    //Giày
                    click(327, 448, 500)
                }

                //Nhấn chọn đồ
                if (kichBan == "Vũ khí") {
                    swipe(287, 783, 254, 481, 500, 0)
                    swipe(280, 776, 290, 479, 500, 0)
                    swipe(280, 776, 290, 479, 500, 0)
                    swipe(280, 776, 290, 479, 500, 500)

                    //Vũ khí
                    click(353, 577, 500)
                } else {
                    //Các đồ khác
                    if (kichBan == "Dây chuyền" || kichBan == "Nhẫn") {
                        //Hỗn độn
                        click(522, 632, 500)
                    } else {
                        //Vực thẳm
                        swipe(280, 776, 290, 479, 500, 500)
                        click(187, 771, 500)
                    }
                }

                //Kéo đầy thanh
                swipe(182, 984, 684, 984, 500, 500)

                //Nhấn điều chế
                click(259, 1098, 4000)

                //Nhấn tìm thuộc tính
                click(350, 432, 500)

                //Nhấn thiết lập sẵn A
                click(150, 201, 500)

                //Nhấn tìm kiếm
                click(240, 1126, 2000)

                "trangbi".screenCapture(0)

                if (!auto) break
                cropImage("trangbi", 87, 415, 466 - 87, 467 - 415)

                if (!auto) break
                val comparativeWords = listOf("4 thuoc tinh co hieu luc")
                val isTrue = getTextFromImage("trangbi", comparativeWords, 1)

                if (!auto) break
                if (isTrue) {
                    auto = false
                    TelegramBotInstance.telegramBot.sendMessage("Đã tìm thấy trang bị [Thiết lập A]")
                    break
                }

                if (!timKiemCaThietLapB) continue

                //Nhấn xác nhận
                click(357, 1147, 500)

                //Nhấn tìm thuộc tính
                click(350, 432, 500)

                //Nhấn thiết lập sẵn B
                click(305, 202, 500)

                //Nhấn tìm kiếm
                click(240, 1126, 2000)

                "trangbi".screenCapture(0)

                if (!auto) break
                cropImage("trangbi", 87, 415, 466 - 87, 467 - 415)

                if (!auto) break
                val isTrue2 = getTextFromImage("trangbi", comparativeWords, 2)

                if (!auto) break
                if (isTrue2) {
                    auto = false
                    TelegramBotInstance.telegramBot.sendMessage("Đã tìm thấy trang bị [Thiết lập B]")
                    break
                }
            }
        }.start()
    }

    private fun cuongHoa() {
        auto = true
        TelegramBotInstance.telegramBot.sendMessage("Bắt đầu auto: Cường hóa")
        Thread {
            while (auto) {
                initAuto()

                //Nhấn chọn cường hóa thần
                click(356, 511, 500)

                //Nhấn chọn ô
                if (kichBan == "Ô 1") click(151, 960, 500)
                if (kichBan == "Ô 2") click(209, 960, 500)
                if (kichBan == "Ô 3") click(268, 960, 500)
                if (kichBan == "Ô 4") click(327, 960, 500)
                if (kichBan == "Ô 5") click(386, 960, 500)
                if (kichBan == "Ô 6") click(445, 960, 500)
                if (kichBan == "Ô 7") click(505, 960, 500)
                if (kichBan == "Ô 8") click(563, 960, 500)

                "cuonghoamax".screenCapture(0)

                if (!auto) break
                cropImage("cuonghoamax", 101, 657, 612 - 101, 705 - 657)

                if (!auto) break
                val isTrue =
                    getTextFromImage(
                        "cuonghoamax",
                        listOf("Khong the cuong hoa than them nua"),
                        1
                    )

                if (!auto) break
                if (isTrue) {
                    auto = false
                    TelegramBotInstance.telegramBot.sendMessage("Đã cường hóa max")
                    break
                }

                //Nhấn cường hóa
                click(250, 1123, 3000)

                "cuonghoa".screenCapture(0)

                if (!auto) break
                cropImage("cuonghoa", 153, 354, 565 - 153, 407 - 354)

                if (!auto) break
                val isTrue2 = getTextFromImage("cuonghoa", listOf("Cuong Hoa Thanh Cong"), 1)

                if (!auto) break
                if (isTrue2) backup()
            }
        }.start()
    }

    private fun tayThuocTinh() {
        auto = true
        TelegramBotInstance.telegramBot.sendMessage("Bắt đầu auto: Tẩy thuộc tính")
        Thread {
            while (auto) {
                initAuto()

                //Nhấn chọn loại bỏ thuộc tính
                click(356, 511, 500)

                //Nhấn chọn ô
                if (kichBan == "Ô 1") click(151, 960, 500)
                if (kichBan == "Ô 2") click(209, 960, 500)
                if (kichBan == "Ô 3") click(268, 960, 500)
                if (kichBan == "Ô 4") click(327, 960, 500)
                if (kichBan == "Ô 5") click(386, 960, 500)
                if (kichBan == "Ô 6") click(445, 960, 500)
                if (kichBan == "Ô 7") click(505, 960, 500)
                if (kichBan == "Ô 8") click(563, 960, 500)

                "taythuoctinhmax".screenCapture(0)

                if (!auto) break
                cropImage("taythuoctinhmax", 109, 787, 614 - 109, 840 - 787)

                if (!auto) break
                val isTrue =
                    getTextFromImage(
                        "taythuoctinhmax",
                        listOf("Khong co thuoc tinh am de loai bo"),
                        1
                    )

                if (!auto) break
                if (isTrue) {
                    auto = false
                    TelegramBotInstance.telegramBot.sendMessage("Đã tẩy thuộc tính max")
                    break
                }

                //Nhấn loại bỏ
                click(246, 1100, 3000)

                "taythuoctinh".screenCapture(0)

                if (!auto) break
                cropImage("taythuoctinh", 260, 354, 458 - 260, 406 - 354)

                if (!auto) break
                val isTrue2 =
                    getTextFromImage(
                        "taythuoctinh",
                        listOf("Da loai bo"),
                        1
                    )

                if (!auto) break
                if (isTrue2) backup()
            }
        }.start()
    }

    private fun thuCuoi() {
        auto = true
        Thread {
            while (auto) {
                initAuto()

                //Nhan chon thuyen
                click(597, 460, 3500)

                //Nhan trieu hoi
                click(153, 1197, 500)

                //Nhan bo qua hoat canh
                click(262, 896, 500)

                //Nhan 1 lan
                click(192, 798, 1500)

                "thucuoi".screenCapture(0)

                if (!auto) break
                cropImage("thucuoi", 89, 337, 254 - 89, 379 - 337)

                if (!auto) break
                val isTrue2 =
                    getTextFromImage(
                        "thucuoi",
//                        listOf("LEO S", "BLUBEE S", "PINIA S", "INFERNO S"),
                        listOf(
                            "WANG WANG A",
                            "DUN DUN A",
                            "TUCAN A",
                            "PYRO A",
                            "GRIZZLY A",
                            "GRAY A"
                        ),
                        1
                    )

                if (!auto) break
                if (isTrue2) backup()
            }
        }.start()
    }

    private fun ruongBoss() {
        auto = true
        Thread {
            while (auto) {
                initAuto()

                //Nhấn mở rương
                click(751, 2315, 500)

                //Nhấn tab đồ đặc biệt
                click(666, 1368, 500)

                //Kéo
                swipe(430, 2181, 430, 1429, 500, 500)
                swipe(430, 2181, 430, 1429, 500, 500)
                swipe(430, 2181, 430, 1429, 500, 500)
                swipe(430, 2181, 430, 1429, 500, 500)

                //Nhấn vào rương boss
                click(938, 1701, 500)

                //Nhấn sử dụng
//                click(540, 1640, 7000)
                click(277, 1640, 7000)

                //Chụp ảnh
                "BossCheat".screenCapture(0)

                //Cắt ảnh
                if (!auto) break
                cropImage("BossCheat", 389, 1197, 682 - 389, 1299 - 1197)

                if (!auto) break
                val isTrue =
                    getTextFromImage(
                        "BossCheat",
                        listOf("Tinh Chat Vua"),
                        1
                    )

                if (!auto) break
                if (!isTrue) {
                    auto = false
                    break
                }

                for (i in 1..11) {
                    //Item tiếp theo
                    click(877, 1127, 2000)

                    //Chụp ảnh
                    "BossCheat".screenCapture(0)

                    //Cắt ảnh
                    if (!auto) break
                    cropImage("BossCheat", 389, 1197, 682 - 389, 1299 - 1197)

                    if (!auto) break
                    val isTrue2 =
                        getTextFromImage(
                            "BossCheat",
                            listOf("Tinh Chat Vua"),
                            1
                        )

                    if (!auto) break
                    if (!isTrue2) {
                        auto = false
                        break
                    }
                }
            }
        }.start()
    }

    private fun tinhCach() {
        auto = true
        Thread {
            while (auto) {
                initAuto()

                //Nhấn mở rương
                click(751, 2315, 500)

                //Nhấn tab đồ đặc biệt
                click(666, 1368, 500)

                //Kéo
                swipe(430, 2181, 430, 1429, 500, 500)
                swipe(430, 2181, 430, 1429, 500, 500)
                swipe(430, 2181, 430, 1429, 500, 500)
                swipe(430, 2181, 430, 1429, 500, 500)

                //Nhấn vào bình
                click(747, 1500, 500)

                //Nhấn sử dụng
                click(355, 1611, 500)

                //Chọn hunter
                click(293, 1011, 500)

                //Nhấn thay đổi
                click(371, 1731, 4000)

                //Nhấn thợ săn
                click(543, 2308, 1000)

                //Chọn thợ săn đầu
                click(121, 1763, 4000)

                //Chụp ảnh
                "Character".screenCapture(0)

                //Cắt ảnh
                if (!auto) break
                cropImage("Character", 202, 404, 867 - 202, 498 - 404)

                if (!auto) break
                val isTrue =
                    getTextFromImage(
                        "Character",
                        listOf("Nhanh Nhen"),
                        1
                    )

                if (!auto) break
                if (isTrue) {
                    auto = false
                    break
                }
            }
        }.start()
    }
}