// OntaDev_Kits Plugin
// Авторские права (c) 2026 OntaDev
// Лицензия: MIT

package com.ontadev.kits.command;

import com.ontadev.kits.config.Config;
import com.ontadev.kits.config.MessageConfig;
import com.ontadev.kits.model.Kit;
import com.ontadev.kits.service.KitActions;
import com.ontadev.kits.service.KitAdminActions;
import com.ontadev.kits.service.KitService;
import com.ontadev.kits.service.MenuState;
import com.ontadev.libs.command.AbstractCommand;
import com.ontadev.libs.ioc.annotation.stereotype.RegisterCommand;
import com.ontadev.libs.message.Message;
import com.ontadev.libs.message.Placeholders;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RegisterCommand
public class KitCommand extends AbstractCommand {

    private static final String USE = "kits.use";
    private static final String ADMIN = "kits.admin";

    private final Config config;
    private final MessageConfig messages;
    private final KitService kitService;
    private final KitActions actions;
    private final KitAdminActions admin;
    private final MenuState state;

    public KitCommand(Config config, MessageConfig messages, KitService kitService, KitActions actions,
                      KitAdminActions admin, MenuState state) {
        this.config = config;
        this.messages = messages;
        this.kitService = kitService;
        this.actions = actions;
        this.admin = admin;
        this.state = state;

        register(new Sub(messages, "menu", USE, "<#6B7280>» <#F59E0B>/kit menu <#4B5563>- <#CBD5E1>открыть меню наборов",
                (sender, args) -> {
                    Player player = admin.playerOf(sender);
                    if (player != null) state.openMain(player);
                }, null));

        register(new Sub(messages, "list", USE, "<#6B7280>» <#F59E0B>/kit list <#4B5563>- <#CBD5E1>список наборов",
                (sender, args) -> {
                    List<String> names = kitService.all().stream()
                            .filter(kit -> kit.isEnabled() || sender.hasPermission(ADMIN))
                            .map(Kit::getId)
                            .collect(Collectors.toList());
                    new Message("<#4B5563>[<#F59E0B>Kits<#4B5563>] <#CBD5E1>Наборы (" + names.size() + "): <#FBBF24>"
                            + String.join("<#6B7280>, <#FBBF24>", names)).send(sender);
                }, null), "список");

        register(new Sub(messages, "get", USE, "<#6B7280>» <#F59E0B>/kit получить <набор> [игрок] <#4B5563>- <#CBD5E1>получить набор (игроку - без проверок, kits.give)",
                (sender, args) -> get(sender, args),
                (sender, args) -> {
                    if (args.length == 1) return kitIds();
                    if (args.length == 2 && sender.hasPermission("kits.give")) return onlineNames();
                    return Collections.emptyList();
                }), "получить");

        register(new Sub(messages, "preview", USE, "<#6B7280>» <#F59E0B>/kit посмотреть <набор> <#4B5563>- <#CBD5E1>посмотреть содержимое",
                (sender, args) -> admin.withKit(sender, args, (player, kit) -> state.openPreview(player, kit.getId())),
                (sender, args) -> args.length == 1 ? kitIds() : Collections.emptyList()), "посмотреть");

        register(new Sub(messages, "reset", ADMIN, "<#6B7280>» <#F59E0B>/kit сброс <набор|*> [игрок] <#4B5563>- <#CBD5E1>сбросить задержку",
                (sender, args) -> admin.reset(sender, args),
                (sender, args) -> {
                    if (args.length == 1) return kitIds();
                    if (args.length == 2) return onlineNames();
                    return Collections.emptyList();
                }), "сброс");
    }

    private void register(Sub sub, String... aliases) {
        registerSubCommand(sub);
        for (String alias : aliases) {
            registerSubCommand(sub.copyAs(alias));
        }
    }

    @Override
    public @NotNull String getName() {
        return "kit";
    }

    @Override
    public @NotNull List<String> getAliases() {
        return new ArrayList<>(config.getCommands().getKit());
    }

    @Override
    protected void handle(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (!sender.hasPermission(USE)) {
            messages.getNoPermission().send(sender);
            return;
        }

        if (args.length == 0) {
            if (config.isOpenMenuOnCommand() && sender instanceof Player) {
                state.openMain((Player) sender);
            } else {
                sendHelp(sender, command, commandLabel);
            }
            return;
        }

        Kit kit = kitService.get(args[0]);
        if (kit == null) {
            messages.getKitNotFound().send(sender, Placeholders.of("kit", args[0]));
            return;
        }

        Player player = admin.playerOf(sender);
        if (player != null) {
            actions.claim(player, kit);
        }
    }

    @Override
    public List<String> complete(CommandSender sender, String label, String... args) {
        if (args.length == 1) {
            List<String> result = new ArrayList<>(super.complete(sender, label, args));
            result.addAll(kitIds());
            return result;
        }
        return super.complete(sender, label, args);
    }

    private void get(CommandSender sender, String[] args) {
        if (args.length > 1) {
            if (!sender.hasPermission("kits.give")) {
                messages.getNoPermission().send(sender);
                return;
            }
            admin.give(sender, args);
            return;
        }

        Kit kit = admin.findKit(sender, args, 0);
        Player player = kit == null ? null : admin.playerOf(sender);
        if (player != null) {
            actions.claim(player, kit);
        }
    }

    private void sendHelp(CommandSender sender, Command command, String label) {
        messages.getHelpHeader().send(sender);
        new Message("<#6B7280>» <#F59E0B>/kit [набор] <#4B5563>- <#CBD5E1>открыть меню / получить набор").send(sender);
        subCommands.values().stream().distinct().forEach(sub -> {
            if (sub.getName().chars().allMatch(c -> c < 128) && sub.hasPermission(sender, command, label)) {
                sub.sendUsage(sender, command, label);
            }
        });
        if (sender.hasPermission(ADMIN)) {
            new Message("<#6B7280>» <#F59E0B>/kitadmin <#4B5563>- <#CBD5E1>команды администратора").send(sender);
        }
    }

    private List<String> kitIds() {
        return new ArrayList<>(kitService.ids());
    }

    private List<String> onlineNames() {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }
}
