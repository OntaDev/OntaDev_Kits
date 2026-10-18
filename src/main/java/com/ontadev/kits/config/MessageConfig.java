// OntaDev_Kits Plugin
// Авторские права (c) 2026 OntaDev
// Лицензия: MIT

package com.ontadev.kits.config;

import com.google.gson.annotations.SerializedName;
import com.ontadev.libs.config.YamlConfig;
import com.ontadev.libs.ioc.annotation.stereotype.Config;
import com.ontadev.libs.message.Message;
import lombok.Getter;

@Config
@Getter
public class MessageConfig extends YamlConfig {

    private static final String OK = "<#4B5563>[<#34D399>✔<#4B5563>] <#CBD5E1>";
    private static final String ERR = "<#4B5563>[<#F87171>✘<#4B5563>] <#CBD5E1>";

    @SerializedName("only-players")
    Message onlyPlayers = new Message(ERR + "Команда доступна только игрокам");

    @SerializedName("no-permission")
    Message noPermission = new Message(ERR + "Недостаточно прав для этого действия");

    @SerializedName("kit-not-found")
    Message kitNotFound = new Message(ERR + "Набор <#FBBF24><kit> <#CBD5E1>не найден");

    @SerializedName("player-not-found")
    Message playerNotFound = new Message(ERR + "Игрок <#FBBF24><player> <#CBD5E1>не найден");

    @SerializedName("kit-exists")
    Message kitExists = new Message(ERR + "Набор <#FBBF24><kit> <#CBD5E1>уже существует");

    @SerializedName("invalid-id")
    Message invalidId = new Message(ERR + "Идентификатор может содержать только латиницу, цифры, <#FBBF24>_ <#CBD5E1>и <#FBBF24>-");

    @SerializedName("invalid-number")
    Message invalidNumber = new Message(ERR + "Нужно ввести число. Напиши ещё раз или <#FBBF24>отмена");

    @SerializedName("invalid-time")
    Message invalidTime = new Message(ERR + "Не понял время. Примеры: <#FBBF24>3600<#CBD5E1>, <#FBBF24>1d 2h 30m<#CBD5E1>, <#FBBF24>0 <#CBD5E1>(без задержки)");

    @SerializedName("empty-inventory")
    Message emptyInventory = new Message(ERR + "Твой инвентарь пуст - сохранять нечего");

    @SerializedName("kit-claimed")
    Message kitClaimed = new Message(OK + "Ты получил набор <#34D399><name>");

    @SerializedName("kit-given")
    Message kitGiven = new Message(OK + "Набор <#34D399><kit> <#CBD5E1>выдан игроку <#FBBF24><player>");

    @SerializedName("kit-received")
    Message kitReceived = new Message(OK + "Тебе выдан набор <#34D399><name>");

    @SerializedName("claim-disabled")
    Message claimDisabled = new Message(ERR + "Набор <#FBBF24><name> <#CBD5E1>сейчас отключён");

    @SerializedName("claim-no-permission")
    Message claimNoPermission = new Message(ERR + "У тебя нет доступа к набору <#FBBF24><name>");

    @SerializedName("claim-already")
    Message claimAlready = new Message(ERR + "Набор <#FBBF24><name> <#CBD5E1>одноразовый, ты его уже получал");

    @SerializedName("claim-cooldown")
    Message claimCooldown = new Message(ERR + "Набор <#FBBF24><name> <#CBD5E1>будет доступен через <#FBBF24><time>");

    @SerializedName("claim-no-space")
    Message claimNoSpace = new Message(ERR + "Недостаточно места в инвентаре для набора <#FBBF24><name>");

    @SerializedName("claim-wipe")
    Message claimWipe = new Message(ERR + "Идёт вайп - выдача наборов откроется через <#FBBF24><time>");

    @SerializedName("wipe-started")
    Message wipeStarted = new Message(OK + "Режим вайпа <#34D399>включён <#6B7280>(<time>). <#CBD5E1>Выдача наборов заблокирована");

    @SerializedName("wipe-stopped")
    Message wipeStopped = new Message(OK + "Режим вайпа <#F87171>выключен");

    @SerializedName("wipe-not-active")
    Message wipeNotActive = new Message(ERR + "Режим вайпа сейчас не включён");

    @SerializedName("wipe-status")
    Message wipeStatus = new Message(OK + "Вайп: <#FBBF24><time>");

    @SerializedName("wipe-countdown")
    Message wipeCountdown = new Message("<#F59E0B>⚠ Вайп: наборы откроются через <#FBBF24><time>");

    @SerializedName("wipe-broadcast")
    Message wipeBroadcast = new Message("<#4B5563>[<#F59E0B>Kits<#4B5563>] <#CBD5E1>До открытия наборов после вайпа: <#FBBF24><time>");

    @SerializedName("wipe-ended")
    Message wipeEnded = new Message("<#4B5563>[<#34D399>✔<#4B5563>] <#CBD5E1>Вайп завершён - наборы снова доступны!");

    @SerializedName("cooldown-set")
    Message cooldownSet = new Message(OK + "Задержка набора <#34D399><kit> <#CBD5E1>теперь <#FBBF24><time>");

    @SerializedName("overflow-dropped")
    Message overflowDropped = new Message("<#4B5563>[<#F59E0B>!<#4B5563>] <#CBD5E1>Часть предметов не поместилась и выпала под ноги");

    @SerializedName("kit-created")
    Message kitCreated = new Message(OK + "Набор <#34D399><kit> <#CBD5E1>создан из твоего инвентаря (<#FBBF24><count> <#CBD5E1>предм.). Открываю редактор");

    @SerializedName("kit-updated")
    Message kitUpdated = new Message(OK + "Содержимое набора <#34D399><kit> <#CBD5E1>обновлено из твоего инвентаря (<#FBBF24><count> <#CBD5E1>предм.)");

    @SerializedName("kit-deleted")
    Message kitDeleted = new Message(OK + "Набор <#34D399><kit> <#CBD5E1>удалён");

    @SerializedName("kit-reset")
    Message kitReset = new Message(OK + "Задержка сброшена: игрок <#FBBF24><player>, набор <#FBBF24><kit>");

    @SerializedName("plugin-reloaded")
    Message pluginReloaded = new Message(OK + "Конфигурация и наборы <#34D399>перезагружены <#6B7280>(наборов: <count>)");

    @SerializedName("editor-saved")
    Message editorSaved = new Message(OK + "Набор <#34D399><kit> <#CBD5E1>сохранён");

    @SerializedName("editor-changed")
    Message editorChanged = new Message(OK + "Изменено: <#FBBF24><what>");

    @SerializedName("editor-cancelled")
    Message editorCancelled = new Message(ERR + "Ввод отменён");

    @SerializedName("hand-empty")
    Message handEmpty = new Message(ERR + "Возьми предмет на курсор, чтобы сделать его иконкой");

    @SerializedName("prompt-new-id")
    Message promptNewId = new Message(OK + "Введи идентификатор нового набора (латиница, цифры, <#FBBF24>_ <#CBD5E1>и <#FBBF24>-<#CBD5E1>). Набор будет создан из твоего инвентаря. <#6B7280>отмена - выйти");

    @SerializedName("prompt-name")
    Message promptName = new Message(OK + "Введи новое название набора (разметка <#FBBF24><#RRGGBB> <#CBD5E1>и <#FBBF24>&c <#CBD5E1>работает). <#6B7280>отмена - выйти");

    @SerializedName("prompt-description")
    Message promptDescription = new Message(OK + "Введи строку описания набора. <#6B7280>отмена - выйти");

    @SerializedName("prompt-cooldown")
    Message promptCooldown = new Message(OK + "Введи задержку: секунды или <#FBBF24>1d 2h 30m<#CBD5E1>, <#FBBF24>0 <#CBD5E1>- без задержки. <#6B7280>отмена - выйти");

    @SerializedName("prompt-permission")
    Message promptPermission = new Message(OK + "Введи право набора (например <#FBBF24>kits.kit.vip<#CBD5E1>) или <#FBBF24>- <#CBD5E1>чтобы убрать. <#6B7280>отмена - выйти");

    @SerializedName("prompt-command")
    Message promptCommand = new Message(OK + "Введи консольную команду без <#FBBF24>/<#CBD5E1>, <#FBBF24><player> <#CBD5E1>заменится на ник. <#6B7280>отмена - выйти");

    @SerializedName("prompt-order")
    Message promptOrder = new Message(OK + "Введи порядок набора в меню (меньше - раньше) для автоматической раскладки. <#6B7280>отмена - выйти");

    @SerializedName("config-updated")
    Message configUpdated = new Message("<#4B5563>[<#F59E0B>!<#4B5563>] <#CBD5E1>В конфиги добавлено новых ключей: <#FBBF24><count>");

    @SerializedName("prompt-slot")
    Message promptSlot = new Message(OK + "Введи слот набора в меню (<#FBBF24>0-53<#CBD5E1>) или <#FBBF24>-1 <#CBD5E1>для автоматической раскладки. <#6B7280>отмена - выйти");

    @SerializedName("invalid-slot")
    Message invalidSlot = new Message(ERR + "Слот должен быть числом от <#FBBF24>0 <#CBD5E1>до <#FBBF24>53 <#CBD5E1>или <#FBBF24>-1");

    @SerializedName("help-header")
    Message helpHeader = new Message("<#4B5563>[<#F59E0B>Kits<#4B5563>] <#CBD5E1>Доступные команды:");

    @Override
    public String getFileName() {
        return "messages";
    }
}
