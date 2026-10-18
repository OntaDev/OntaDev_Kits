// OntaDev_Kits Plugin
// Авторские права (c) 2026 OntaDev
// Лицензия: MIT

package com.ontadev.kits.command;

import com.ontadev.kits.config.Config;
import com.ontadev.kits.config.MessageConfig;
import com.ontadev.kits.service.KitAdminActions;
import com.ontadev.kits.service.KitService;
import com.ontadev.libs.command.AbstractCommand;
import com.ontadev.libs.ioc.annotation.stereotype.RegisterCommand;
import com.ontadev.libs.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RegisterCommand
public class KitAdminCommand extends AbstractCommand {

    private static final String ADMIN = "kits.admin";

    private final Config config;
    private final MessageConfig messages;
    private final KitService kitService;

    public KitAdminCommand(Config config, MessageConfig messages, KitService kitService, KitAdminActions admin) {
        this.config = config;
        this.messages = messages;
        this.kitService = kitService;

        Set<String> reserved = new HashSet<>(Arrays.asList("menu", "list", "get", "preview", "reset"));

        register(new Sub(messages, "create", ADMIN, "<#6B7280>» <#F59E0B>/kitadmin create <id> [слот] <#4B5563>- <#CBD5E1>создать набор из своего инвентаря",
                (sender, args) -> admin.create(sender, args, reserved), null), "создать");

        register(new Sub(messages, "update", ADMIN, "<#6B7280>» <#F59E0B>/kitadmin update <набор> <#4B5563>- <#CBD5E1>перезаписать набор своим инвентарём",
                admin::update, kits()), "обновить");

        register(new Sub(messages, "edit", ADMIN, "<#6B7280>» <#F59E0B>/kitadmin edit <набор> <#4B5563>- <#CBD5E1>GUI-редактор",
                admin::edit, kits()), "редактор");

        register(new Sub(messages, "delete", ADMIN, "<#6B7280>» <#F59E0B>/kitadmin delete <набор> <#4B5563>- <#CBD5E1>удалить набор",
                admin::delete, kits()), "удалить");

        register(new Sub(messages, "give", "kits.give", "<#6B7280>» <#F59E0B>/kitadmin give <набор> [игрок] <#4B5563>- <#CBD5E1>выдать набор без проверок",
                admin::give, (sender, args) -> {
                    if (args.length == 1) return kitIds();
                    if (args.length == 2) return onlineNames();
                    return Collections.emptyList();
                }), "выдать");

        register(new Sub(messages, "setcooldown", ADMIN, "<#6B7280>» <#F59E0B>/kitadmin setcooldown <набор> <время> <#4B5563>- <#CBD5E1>задать задержку (3600, 1d 2h)",
                admin::setCooldown, kits()), "задержка");

        register(new Sub(messages, "resetcooldown", ADMIN, "<#6B7280>» <#F59E0B>/kitadmin resetcooldown <набор|*> [игрок] <#4B5563>- <#CBD5E1>сбросить задержку",
                admin::reset, (sender, args) -> {
                    if (args.length == 1) {
                        List<String> list = kitIds();
                        list.add("*");
                        return list;
                    }
                    if (args.length == 2) return onlineNames();
                    return Collections.emptyList();
                }), "сбросить");

        register(new Sub(messages, "wipe", ADMIN, "<#6B7280>» <#F59E0B>/kitadmin wipe on [время]|off|status <#4B5563>- <#CBD5E1>режим вайпа",
                admin::wipe, (sender, args) -> args.length == 1
                        ? Arrays.asList("on", "off", "status") : Collections.emptyList()), "вайп");

        register(new Sub(messages, "reload", "kits.reload", "<#6B7280>» <#F59E0B>/kitadmin reload <#4B5563>- <#CBD5E1>перезагрузить конфиги и наборы",
                (sender, args) -> admin.reload(sender), null), "перезагрузка");
    }

    private Sub.Completer kits() {
        return (sender, args) -> args.length == 1 ? kitIds() : Collections.emptyList();
    }

    private void register(Sub sub, String... aliases) {
        registerSubCommand(sub);
        for (String alias : aliases) {
            registerSubCommand(sub.copyAs(alias));
        }
    }

    @Override
    public @NotNull String getName() {
        return "kitadmin";
    }

    @Override
    public @NotNull List<String> getAliases() {
        return new ArrayList<>(config.getCommands().getAdmin());
    }

    @Override
    protected void handle(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (!sender.hasPermission(ADMIN)) {
            messages.getNoPermission().send(sender);
            return;
        }
        messages.getHelpHeader().send(sender);
        subCommands.values().stream().distinct().forEach(sub -> {
            if (sub.getName().chars().allMatch(c -> c < 128) && sub.hasPermission(sender, command, commandLabel)) {
                sub.sendUsage(sender, command, commandLabel);
            }
        });
    }

    private List<String> kitIds() {
        return new ArrayList<>(kitService.ids());
    }

    private List<String> onlineNames() {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }
}
