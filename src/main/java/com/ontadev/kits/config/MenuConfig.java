// OntaDev_Kits Plugin
// Авторские права (c) 2026 OntaDev
// Лицензия: MIT

package com.ontadev.kits.config;

import com.google.gson.annotations.SerializedName;
import com.ontadev.libs.config.YamlConfig;
import com.ontadev.libs.ioc.annotation.stereotype.Config;
import com.ontadev.kits.model.KitLayout;
import com.ontadev.libs.message.Message;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Внешний вид меню (menu.yml). В названиях наборов работают {@code <name>}, {@code <id>}, {@code <description>},
 * {@code <cooldown>}, {@code <remaining>}, {@code <items>}, {@code <permission>}, {@code <commands>},
 * в заголовке главного меню еще {@code <page>} и {@code <pages>}.
 */
@Config
@Getter
@Setter
public class MenuConfig extends YamlConfig {

    private static final String LINE = "<#4B5563>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬";

    @SerializedName("state-on")
    private String stateOn = "<#34D399>включено";
    @SerializedName("state-off")
    private String stateOff = "<#F87171>выключено";
    private String none = "<#6B7280>нет";
    private String auto = "<#6B7280>авто";

    private Main main = new Main();
    private Preview preview = new Preview();
    private Editor editor = new Editor();

    @Override
    public String getFileName() {
        return "menu";
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Main {
        private Message title = new Message("<#F59E0B>✦ Наборы <#4B5563>(<page>/<pages>)");

        private int rows = 6;

        /** Слоты под наборы по порядку, остальные уходят на следующую страницу. */
        @SerializedName("kit-slots")
        private List<Integer> kitSlots = new ArrayList<>(Arrays.asList(
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43));

        @SerializedName("show-locked")
        private boolean showLocked = true;

        private ItemTemplate filler = new ItemTemplate(Material.BLACK_STAINED_GLASS_PANE, "<gray> ", -1);
        @SerializedName("filler-enabled")
        private boolean fillerEnabled = true;

        private ItemTemplate previous = new ItemTemplate(Material.ARROW, "<#F59E0B>« Назад", 46,
                "<#6B7280>Страница <page> из <pages>");
        private ItemTemplate next = new ItemTemplate(Material.ARROW, "<#F59E0B>Вперёд »", 52,
                "<#6B7280>Страница <page> из <pages>");
        private ItemTemplate close = new ItemTemplate(Material.BARRIER, "<#F87171>Закрыть", 8);
        private ItemTemplate info = new ItemTemplate(Material.BOOK, "<#F59E0B>Как это работает", 4,
                "<#CBD5E1>ЛКМ <#6B7280>- получить набор",
                "<#CBD5E1>ПКМ <#6B7280>- посмотреть содержимое");
        @SerializedName("admin-create")
        private ItemTemplate adminCreate = new ItemTemplate(Material.EMERALD, "<#34D399>+ Новый набор", 49,
                "<#CBD5E1>Создаст набор из твоего",
                "<#CBD5E1>текущего инвентаря",
                " ",
                "<#34D399>ЛКМ <#6B7280>- создать");
        @SerializedName("empty-page")
        private ItemTemplate emptyPage = new ItemTemplate(Material.STRUCTURE_VOID, "<#6B7280>Пока нет доступных наборов", 22);

        private Lore lore = new Lore();

        @SerializedName("glow-available")
        private boolean glowAvailable = true;

        @SerializedName("kit-name")
        private Message kitName = new Message("<name>");
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Lore {
        private Message available = new Message(
                LINE,
                "<description>",
                LINE,
                "<#CBD5E1>Статус: <#34D399>Доступно",
                "<#CBD5E1>Предметов: <#FBBF24><items> <#4B5563>| <#6B7280>ID: <#FBBF24><id>",
                "<#CBD5E1>Задержка: <#FBBF24><cooldown>",
                " ",
                "<#34D399>ЛКМ <#6B7280>- получить",
                "<#34D399>ПКМ <#6B7280>- предпросмотр");

        private Message cooldown = new Message(
                LINE,
                "<description>",
                LINE,
                "<#CBD5E1>Предметов: <#FBBF24><items>",
                "<#CBD5E1>Статус: <#F87171>Недоступно",
                "<#CBD5E1>Доступен через: <#FBBF24><remaining>",
                "<#6B7280>ID: <#FBBF24><id>",
                " ",
                "<#34D399>ПКМ <#6B7280>- предпросмотр");

        private Message claimed = new Message(
                LINE,
                "<description>",
                LINE,
                "<#CBD5E1>Предметов: <#FBBF24><items>",
                "<#CBD5E1>Статус: <#F87171>Недоступно",
                "<#F87171>Одноразовый набор уже получен",
                "<#6B7280>ID: <#FBBF24><id>",
                " ",
                "<#34D399>ПКМ <#6B7280>- предпросмотр");

        private Message locked = new Message(
                LINE,
                "<description>",
                LINE,
                "<#CBD5E1>Статус: <#F87171>Нет доступа",
                "<#6B7280>ID: <#FBBF24><id>",
                "<#6B7280>Нужно право: <permission>",
                " ",
                "<#34D399>ПКМ <#6B7280>- предпросмотр");

        private Message wipe = new Message(
                LINE,
                "<description>",
                LINE,
                "<#CBD5E1>Статус: <#F87171>Недоступно",
                "<#F87171>Идёт вайп. Выдача наборов откроется через <#FBBF24><wipe-time>",
                "<#6B7280>ID: <#FBBF24><id>",
                " ",
                "<#34D399>ПКМ <#6B7280>- предпросмотр");

        private Message disabled = new Message(
                LINE,
                "<description>",
                LINE,
                "<#F87171>Набор отключён <#6B7280>(виден только админам)");

        @SerializedName("admin-footer")
        private Message adminFooter = new Message(
                " ",
                "<#F59E0B>Shift+ПКМ <#6B7280>- редактировать",
                "<#F59E0B>Shift+ЛКМ <#6B7280>- вкл/выкл набор");
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Preview {
        private Message title = new Message("<#F59E0B>Предпросмотр: <#CBD5E1><name>");
        private ItemTemplate back = new ItemTemplate(Material.ARROW, "<#F59E0B>« К списку наборов", 45);
        private ItemTemplate claim = new ItemTemplate(Material.LIME_DYE, "<#34D399>Получить набор", 49,
                "<#CBD5E1>Предметов: <#FBBF24><items>",
                " ",
                "<#34D399>ЛКМ <#6B7280>- получить");
        private ItemTemplate filler = new ItemTemplate(Material.BLACK_STAINED_GLASS_PANE, "<gray> ", -1);
        /** Заглушка для пустых слотов брони и руки. */
        @SerializedName("empty-slot")
        private ItemTemplate emptySlot = new ItemTemplate(Material.LIGHT_BLUE_STAINED_GLASS_PANE, "<#6B7280>Пусто", -1);
        private ItemTemplate commands = new ItemTemplate(Material.COMMAND_BLOCK, "<#F59E0B>Дополнительно", 53,
                "<#CBD5E1>Вместе с набором выполнятся команды:",
                "<commands>");
        private ItemTemplate close = new ItemTemplate(Material.BARRIER, "<#F87171>Закрыть", 8);
        private ItemTemplate edit = new ItemTemplate(Material.ANVIL, "<#F59E0B>Редактировать", 47,
                "<#6B7280>Открыть редактор набора",
                " ",
                "<#34D399>ЛКМ <#6B7280>- открыть");
        private Slots slots = new Slots();

        /** Слоты брони, руки и сетки в предпросмотре и редакторе (0-53). */
        @Getter
        @Setter
        @NoArgsConstructor
        public static class Slots {
            private int head = 2;
            private int chest = 3;
            private int legs = 4;
            private int boots = 5;
            @SerializedName("left-hand")
            private int leftHand = 6;
            /** Первый слот сетки из 36 предметов (хотбар, потом хранилище). */
            @SerializedName("items-start")
            private int itemsStart = 9;

            public KitLayout layout() {
                return new KitLayout(head, chest, legs, boots, leftHand, itemsStart);
            }
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Editor {
        private Message title = new Message("<#F59E0B>Редактор: <#CBD5E1><name>");
        private ItemTemplate filler = new ItemTemplate(Material.GRAY_STAINED_GLASS_PANE, "<gray> ", -1);

        private ItemTemplate back = new ItemTemplate(Material.ARROW, "<#F59E0B>« К списку наборов", 45,
                "<#6B7280>Изменения содержимого",
                "<#6B7280>сохраняются автоматически",
                " ",
                "<#CBD5E1>Слоты 2-5 - броня, 6 - вторая рука,",
                "<#CBD5E1>9-44 - предметы (хотбар, потом хранилище).",
                "<#6B7280>Клади и забирай предметы как в сундуке.");
        private ItemTemplate close = new ItemTemplate(Material.BARRIER, "<#F87171>Закрыть", 8);
        private ItemTemplate name = new ItemTemplate(Material.NAME_TAG, "<#F59E0B>Название", 46,
                "<#6B7280>Сейчас: <#CBD5E1><name>", " ", "<#34D399>ЛКМ <#6B7280>- изменить");
        private ItemTemplate icon = new ItemTemplate(Material.ITEM_FRAME, "<#F59E0B>Иконка", 47,
                "<#6B7280>Сейчас: <#CBD5E1><icon>", " ",
                "<#34D399>Кликни по кнопке предметом", "<#34D399>с курсора <#6B7280>- сделать его иконкой");
        private ItemTemplate cooldown = new ItemTemplate(Material.CLOCK, "<#F59E0B>Задержка", 48,
                "<#6B7280>Сейчас: <#CBD5E1><cooldown>", " ", "<#34D399>ЛКМ <#6B7280>- изменить");
        private ItemTemplate oneTime = new ItemTemplate(Material.TRIPWIRE_HOOK, "<#F59E0B>Одноразовый", 49,
                "<#6B7280>Сейчас: <state>", " ", "<#34D399>ЛКМ <#6B7280>- переключить");
        private ItemTemplate permission = new ItemTemplate(Material.SHIELD, "<#F59E0B>Право доступа", 50,
                "<#6B7280>Сейчас: <#CBD5E1><permission>", " ", "<#34D399>ЛКМ <#6B7280>- изменить");
        private ItemTemplate commands = new ItemTemplate(Material.COMMAND_BLOCK, "<#F59E0B>Команды", 51,
                "<commands>", " ",
                "<#34D399>ЛКМ <#6B7280>- добавить",
                "<#34D399>ПКМ <#6B7280>- убрать последнюю",
                "<#34D399>Shift+ПКМ <#6B7280>- очистить");
        @SerializedName("load-inventory")
        private ItemTemplate loadInventory = new ItemTemplate(Material.CHEST_MINECART, "<#F59E0B>Взять из моего инвентаря", 52,
                "<#CBD5E1>Заменит содержимое набора", "<#CBD5E1>твоим текущим инвентарём", " ", "<#34D399>ЛКМ <#6B7280>- заменить");
        private ItemTemplate delete = new ItemTemplate(Material.TNT, "<#F87171>Удалить набор", 53,
                "<#F87171>Без возможности восстановления", " ", "<#F87171>Shift+ЛКМ <#6B7280>- подтвердить");
        private ItemTemplate enabled = new ItemTemplate(Material.LIME_DYE, "<#F59E0B>Включён", 0,
                "<#6B7280>Сейчас: <state>", " ", "<#34D399>ЛКМ <#6B7280>- переключить");
        private ItemTemplate description = new ItemTemplate(Material.WRITABLE_BOOK, "<#F59E0B>Описание", 1,
                "<description>", " ",
                "<#34D399>ЛКМ <#6B7280>- добавить строку",
                "<#34D399>ПКМ <#6B7280>- убрать последнюю");
        private ItemTemplate slot = new ItemTemplate(Material.COMPARATOR, "<#F59E0B>Слот в меню", 7,
                "<#6B7280>Слот: <#CBD5E1><slot>", "<#6B7280>Порядок: <#CBD5E1><order>", " ",
                "<#34D399>ЛКМ <#6B7280>- слот (0-53, -1 = авто)",
                "<#34D399>ПКМ <#6B7280>- порядок при авто-раскладке");
    }
}
