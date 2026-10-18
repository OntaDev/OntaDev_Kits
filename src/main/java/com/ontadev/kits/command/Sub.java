// OntaDev_Kits Plugin
// Авторские права (c) 2026 OntaDev
// Лицензия: MIT

package com.ontadev.kits.command;

import com.ontadev.kits.config.MessageConfig;
import com.ontadev.libs.command.SubCommand;
import com.ontadev.libs.message.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

final class Sub extends SubCommand {

    interface Executor {
        void run(CommandSender sender, String[] args);
    }

    interface Completer {
        List<String> complete(CommandSender sender, String[] args);
    }

    private final MessageConfig messages;
    private final String permission;
    private final String usage;
    private final Executor executor;
    private final Completer completer;

    Sub(MessageConfig messages, String name, String permission, String usage, Executor executor, Completer completer) {
        super(name);
        this.messages = messages;
        this.permission = permission;
        this.usage = usage;
        this.executor = executor;
        this.completer = completer;
    }

    Sub copyAs(String name) {
        return new Sub(messages, name, permission, usage, executor, completer);
    }

    @Override
    public void execute(CommandSender sender, Command command, String label, String... args) {
        executor.run(sender, args);
    }

    @Override
    public List<String> complete(CommandSender sender, String... args) {
        return completer == null ? Collections.emptyList() : completer.complete(sender, args);
    }

    @Override
    public String getPermission(CommandSender sender, Command command, String label, String... args) {
        return permission;
    }

    @Override
    public void noPermission(CommandSender sender, Command command, String label, String... args) {
        messages.getNoPermission().send(sender);
    }

    @Override
    public void sendUsage(CommandSender sender, Command command, String label, String... args) {
        new Message(usage).send(sender);
    }
}
