// OntaDev_Kits Plugin
// Авторские права (c) 2026 OntaDev
// Лицензия: MIT

package com.ontadev.kits.config;

import com.google.gson.annotations.SerializedName;
import com.ontadev.libs.config.YamlConfig;
import com.ontadev.kits.util.TimeFormat;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@com.ontadev.libs.ioc.annotation.stereotype.Config
@Getter
@Setter
public class Config extends YamlConfig {

    @SerializedName("auto-permission")
    private boolean autoPermission = false;

    @SerializedName("preserve-slots")
    private boolean preserveSlots = true;

    @SerializedName("drop-overflow")
    private boolean dropOverflow = true;

    @SerializedName("require-space")
    private boolean requireSpace = true;

    @SerializedName("default-cooldown")
    private long defaultCooldown = 86400;

    @SerializedName("open-menu-on-command")
    private boolean openMenuOnCommand = true;

    /** Дописывать в конфиги новые ключи при старте и reload, свои значения не трогаем. */
    @SerializedName("auto-update-configs")
    private boolean autoUpdateConfigs = true;

    private Storage storage = new Storage();

    private Wipe wipe = new Wipe();

    private Commands commands = new Commands();

    private Sounds sounds = new Sounds();

    @SerializedName("time-units")
    private TimeUnits timeUnits = new TimeUnits();

    @Override
    public String getFileName() {
        return "config";
    }

    public String formatTime(long seconds) {
        return TimeFormat.format(seconds, timeUnits.getDay(), timeUnits.getHour(), timeUnits.getMinute(), timeUnits.getSecond());
    }

    @Getter
    @Setter
    public static class Wipe {
        /** 0 - пока не выключат вручную. */
        @SerializedName("default-seconds")
        private long defaultSeconds = 0;

        @SerializedName("indefinite-text")
        private String indefiniteText = "бессрочно";

        private boolean actionbar = true;

        @SerializedName("broadcast-at")
        private List<Integer> broadcastAt = new ArrayList<>(Arrays.asList(
                3600, 1800, 600, 300, 60, 30, 10, 5, 4, 3, 2, 1));
    }

    @Getter
    @Setter
    public static class Commands {
        private List<String> kit = new ArrayList<>(Arrays.asList("kits", "наборы", "кит"));
        private List<String> admin = new ArrayList<>(Collections.singletonList("кадмин"));
    }

    /**
     * GLOBAL: все серверы одной БД делят задержки и одноразовые наборы.
     * LOCAL: у каждого сервера (по server-id) свои, но в той же БД.
     */
    @Getter
    @Setter
    public static class Storage {
        private String mode = "GLOBAL";

        @SerializedName("server-id")
        private String serverId = "server-1";

        /** 0 - не подтягивать чужие изменения. */
        @SerializedName("sync-seconds")
        private int syncSeconds = 30;

        public boolean isGlobal() {
            return !"LOCAL".equalsIgnoreCase(mode);
        }

        public String scope() {
            return isGlobal() ? "global" : "local:" + serverId;
        }
    }

    @Getter
    @Setter
    public static class Sounds {
        private boolean enabled = true;
        private String success = "ENTITY_PLAYER_LEVELUP";
        private String error = "ENTITY_VILLAGER_NO";
        private String click = "UI_BUTTON_CLICK";
        private float volume = 0.8f;
        private float pitch = 1.0f;
    }

    @Getter
    @Setter
    public static class TimeUnits {
        private String day = "д";
        private String hour = "ч";
        private String minute = "м";
        private String second = "с";
    }
}
