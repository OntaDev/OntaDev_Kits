// OntaDev_Kits Plugin
// Авторские права (c) 2026 OntaDev
// Лицензия: MIT

package com.ontadev.kits.service;

import com.ontadev.kits.config.Config;
import com.ontadev.kits.config.MessageConfig;
import com.ontadev.kits.model.ClaimResult;
import com.ontadev.kits.model.Kit;
import com.ontadev.libs.ioc.annotation.stereotype.Service;
import com.ontadev.libs.message.Placeholders;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

@Service
public class KitActions {

    private final KitService kitService;
    private final Config config;
    private final MessageConfig messages;
    private final WipeService wipe;

    public KitActions(KitService kitService, Config config, MessageConfig messages, WipeService wipe) {
        this.wipe = wipe;
        this.kitService = kitService;
        this.config = config;
        this.messages = messages;
    }

    public ClaimResult claim(Player player, Kit kit) {
        Placeholders ph = Placeholders.of("name", kit.getDisplayName()).add("kit", kit.getId());
        ClaimResult result = kitService.claim(player, kit);

        switch (result) {
            case SUCCESS:
                messages.getKitClaimed().send(player, ph);
                playSuccess(player);
                break;
            case DISABLED:
                messages.getClaimDisabled().send(player, ph);
                break;
            case NO_PERMISSION:
                messages.getClaimNoPermission().send(player, ph);
                break;
            case ALREADY_CLAIMED:
                messages.getClaimAlready().send(player, ph);
                break;
            case COOLDOWN:
                ph.add("time", formatTime(kitService.remainingCooldown(player, kit)));
                messages.getClaimCooldown().send(player, ph);
                break;
            case NO_SPACE:
                messages.getClaimNoSpace().send(player, ph);
                break;
            case WIPE:
                ph.add("time", wipeTime());
                messages.getClaimWipe().send(player, ph);
                break;
            default:
                break;
        }

        if (result != ClaimResult.SUCCESS) {
            playError(player);
        }
        return result;
    }

    public String formatTime(long seconds) {
        return config.formatTime(seconds);
    }

    public String wipeTime() {
        return wipe.isIndefinite() ? config.getWipe().getIndefiniteText() : formatTime(wipe.remainingSeconds());
    }

    public void playSuccess(Player player) {
        play(player, config.getSounds().getSuccess());
    }

    public void playError(Player player) {
        play(player, config.getSounds().getError());
    }

    public void playClick(Player player) {
        play(player, config.getSounds().getClick());
    }

    private void play(Player player, String soundName) {
        Config.Sounds sounds = config.getSounds();
        if (!sounds.isEnabled() || soundName == null || soundName.isEmpty()) return;
        try {
            player.playSound(player.getLocation(), Sound.valueOf(soundName.toUpperCase()), sounds.getVolume(), sounds.getPitch());
        } catch (IllegalArgumentException ignored) {
            // такого звука нет - ничего не играем
        }
    }
}
