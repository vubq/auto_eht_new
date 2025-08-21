package com.example.autovubq

import android.annotation.SuppressLint

object AutoConfig {
    var selectedAutoType: String = "Trang bị"
    var selectedScenario: String = "Giáp"
    var findConfigB: Boolean = true
}

object AutoInstance {
    @SuppressLint("StaticFieldLeak")
    lateinit var autoADB: AutoADB
}

object TelegramBotInstance {
    val telegramBot = TelegramBot()
}
