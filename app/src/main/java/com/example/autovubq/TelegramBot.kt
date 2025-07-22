package com.example.autovubq

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.CallbackQuery
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.Message

class TelegramBot {

    private val allowedUserIds = setOf(5543802102)

    private val bot = bot {
        token = "7763714639:AAHcpyRIvgBNaoIwFcUot0l6rdL9d7NEZ24"
        dispatch {
            text {
                handleTextCommand(text, message)
            }

            callbackQuery {
                handleCallBackQuery(callbackQuery)
            }
        }
    }

    private fun handleTextCommand(command: String, message: Message) {
        if (!isUserAllowed(message)) {
            bot.sendMessage(ChatId.fromId(message.chat.id), "Bạn không có quyền sử dụng bot này!")
        }
        val commands = command.split("_");
        when (commands[0]) {
            "/start" -> {
                menuStart(message.chat.id)
            }

            "/check" -> {
                bot.sendMessage(ChatId.fromId(message.chat.id), "Bot đang hoạt động!")
            }

            "/stop" -> {
                AutoInstance.autoADB.stop()
            }

            "/docfile" -> {
                val fullText = AutoInstance.autoADB.docFile(commands[1])
                val maxLength = 4096

                var index = 0
                while (index < fullText.length) {
                    val chunk = fullText.substring(index, (index + maxLength).coerceAtMost(fullText.length))
                    bot.sendMessage(ChatId.fromId(message.chat.id), chunk)
                    index += maxLength
                }
            }

            "/xoafile" -> {
                bot.sendMessage(
                    ChatId.fromId(message.chat.id),
                    AutoInstance.autoADB.xoaFile(commands[1])
                )
            }

//            "/auto" -> {
//                when {
//                    commands[1].equals("Equip", ignoreCase = true) -> {
//                        ehtBot.setAutoType(AutoType.EQUIP)
//                        when {
//                            commands[2].equals("Armor", ignoreCase = true) -> {
//                                ehtBot.setEquipmentType(EquipmentType.ARMOR)
//                            }
//
//                            commands[2].equals("Gloves", ignoreCase = true) -> {
//                                ehtBot.setEquipmentType(EquipmentType.GLOVES)
//                            }
//
//                            commands[2].equals("Shoe", ignoreCase = true) -> {
//                                ehtBot.setEquipmentType(EquipmentType.SHOE)
//                            }
//
//                            commands[2].equals("Necklace", ignoreCase = true) -> {
//                                ehtBot.setEquipmentType(EquipmentType.NECKLACE)
//                            }
//
//                            commands[2].equals("Ring", ignoreCase = true) -> {
//                                ehtBot.setEquipmentType(EquipmentType.RING)
//                            }
//
//                            commands[2].equals("Weapon", ignoreCase = true) -> {
//                                ehtBot.setEquipmentType(EquipmentType.WEAPON)
//                            }
//
//                            else -> ehtBot.setEquipmentType(EquipmentType.NULL)
//                        }
//                        when {
//                            commands[3].equals("B", ignoreCase = true) -> {
//                                ehtBot.setPresetB(true)
//                            }
//
//                            else -> ehtBot.setPresetB(false)
//                        }
//                        ehtBot.equip()
//                    }
//
//                    commands[1].equals("Strengthen", ignoreCase = true) -> {
//                        ehtBot.setAutoType(AutoType.STRENGTHEN)
//                        when {
//                            commands[2].equals("1", ignoreCase = true) -> {
//                                ehtBot.setStrengthenPlace(1)
//                            }
//
//                            commands[2].equals("2", ignoreCase = true) -> {
//                                ehtBot.setStrengthenPlace(2)
//                            }
//
//                            commands[2].equals("3", ignoreCase = true) -> {
//                                ehtBot.setStrengthenPlace(3)
//                            }
//
//                            commands[2].equals("4", ignoreCase = true) -> {
//                                ehtBot.setStrengthenPlace(4)
//                            }
//
//                            commands[2].equals("5", ignoreCase = true) -> {
//                                ehtBot.setStrengthenPlace(5)
//                            }
//
//                            commands[2].equals("6", ignoreCase = true) -> {
//                                ehtBot.setStrengthenPlace(6)
//                            }
//
//                            commands[2].equals("7", ignoreCase = true) -> {
//                                ehtBot.setStrengthenPlace(7)
//                            }
//
//                            commands[2].equals("8", ignoreCase = true) -> {
//                                ehtBot.setStrengthenPlace(8)
//                            }
//
//                            else -> ehtBot.setStrengthenPlace(null)
//                        }
//                        ehtBot.strengthen()
//                    }
//
//                    commands[1].equals("EraseAttribute", ignoreCase = true) -> {
//                        ehtBot.setAutoType(AutoType.ERASE_ATTRIBUTE)
//                        when {
//                            commands[2].equals("1", ignoreCase = true) -> {
//                                ehtBot.setEraseAttributePlace(1)
//                            }
//
//                            commands[2].equals("2", ignoreCase = true) -> {
//                                ehtBot.setEraseAttributePlace(2)
//                            }
//
//                            commands[2].equals("3", ignoreCase = true) -> {
//                                ehtBot.setEraseAttributePlace(3)
//                            }
//
//                            commands[2].equals("4", ignoreCase = true) -> {
//                                ehtBot.setEraseAttributePlace(4)
//                            }
//
//                            commands[2].equals("5", ignoreCase = true) -> {
//                                ehtBot.setEraseAttributePlace(5)
//                            }
//
//                            commands[2].equals("6", ignoreCase = true) -> {
//                                ehtBot.setEraseAttributePlace(6)
//                            }
//
//                            commands[2].equals("7", ignoreCase = true) -> {
//                                ehtBot.setEraseAttributePlace(7)
//                            }
//
//                            commands[2].equals("8", ignoreCase = true) -> {
//                                ehtBot.setEraseAttributePlace(8)
//                            }
//
//                            else -> ehtBot.setEraseAttributePlace(null)
//                        }
//                        ehtBot.eraseAttribute()
//                    }
//
//                    commands[1].equals("RidingAnimal", ignoreCase = true) -> {
//                        ehtBot.setAutoType(AutoType.RIDING_ANIMAL)
//                        ehtBot.ridingAnimal()
//                    }
//
//                    commands[1].equals("BossCheat", ignoreCase = true) -> {
//                        ehtBot.setAutoType(AutoType.BOSS_CHEAT)
//                        ehtBot.bossChest()
//                    }
//
//                    commands[1].equals("Character", ignoreCase = true) -> {
//                        ehtBot.setAutoType(AutoType.CHARACTER)
//                        ehtBot.character()
//                    }
//
//                    else -> ehtBot.setAutoType(AutoType.NULL)
//                }
//            }

            else -> {
                menuStart(message.chat.id)
            }
        }
    }

    private fun handleCallBackQuery(callbackQuery: CallbackQuery) {
        bot.sendMessage(ChatId.fromId(callbackQuery.from.id), "Call back")
    }

    private fun isUserAllowed(message: Message): Boolean {
        val userId = message.from?.id
        return userId != null && allowedUserIds.contains(userId)
    }

    private fun menuStart(id: Long) {
        bot.sendMessage(
            ChatId.fromId(id),
            "Bot auto EHT by Quang Vũ"
//            "Bot đã được khởi động! \n\n" +
//                    "Command: \n\n" +
//                    "Trang bị: \n" +
//                    "/auto_Equip_Armor_B \n" +
//                    "/auto_Equip_Gloves_B \n" +
//                    "/auto_Equip_Shoe_B \n" +
//                    "/auto_Equip_Necklace_B \n" +
//                    "/auto_Equip_Ring_B \n" +
//                    "/auto_Equip_Weapon_B \n" +
//                    "/auto_Equip_Armor_A \n" +
//                    "/auto_Equip_Gloves_A \n" +
//                    "/auto_Equip_Shoe_A \n" +
//                    "/auto_Equip_Necklace_A \n" +
//                    "/auto_Equip_Ring_A \n" +
//                    "/auto_Equip_Weapon_A \n\n" +
//                    "Cường hóa: \n" +
//                    "/auto_Strengthen_1 \n" +
//                    "/auto_Strengthen_2 \n" +
//                    "/auto_Strengthen_3 \n" +
//                    "/auto_Strengthen_4 \n" +
//                    "/auto_Strengthen_5 \n" +
//                    "/auto_Strengthen_6 \n" +
//                    "/auto_Strengthen_7 \n" +
//                    "/auto_Strengthen_8 \n\n" +
//                    "Tẩy thuộc tính: \n" +
//                    "/auto_EraseAttribute_1 \n" +
//                    "/auto_EraseAttribute_2 \n" +
//                    "/auto_EraseAttribute_3 \n" +
//                    "/auto_EraseAttribute_4 \n" +
//                    "/auto_EraseAttribute_5 \n" +
//                    "/auto_EraseAttribute_6 \n" +
//                    "/auto_EraseAttribute_7 \n" +
//                    "/auto_EraseAttribute_8 \n\n" +
//                    "Thú cưỡi: \n" +
//                    "/auto_RidingAnimal \n\n" +
//                    "Rương boss: \n" +
//                    "/auto_BossCheat \n\n" +
//                    "Tính cách: \n" +
//                    "/auto_Character \n\n" +
//                    "Đọc file txt: \n" +
//                    "/readFile_Equip \n" +
//                    "/readFile_Strengthen \n" +
//                    "/readFile_StrengthenMax \n" +
//                    "/readFile_EraseAttribute \n" +
//                    "/readFile_EraseAttributeMax \n" +
//                    "/readFile_RidingAnimal \n" +
//                    "/readFile_BossCheat \n" +
//                    "/readFile_Character \n\n" +
//                    "Clear file txt: \n" +
//                    "/clearFile_Equip \n" +
//                    "/clearFile_Strengthen \n" +
//                    "/clearFile_StrengthenMax \n" +
//                    "/clearFile_EraseAttribute \n" +
//                    "/clearFile_EraseAttributeMax \n" +
//                    "/clearFile_RidingAnimal \n" +
//                    "/clearFile_BossCheat \n" +
//                    "/clearFile_Character \n\n" +
//                    "Dừng: \n" +
//                    "/stopAuto \n\n"
        )
    }

    fun sendMessage(message: String) {
        bot.sendMessage(ChatId.fromId(5543802102), message)
    }

    fun start() {
        bot.startPolling()
    }
}