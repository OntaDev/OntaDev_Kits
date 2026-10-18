// OntaDev_Kits Plugin
// Авторские права (c) 2026 OntaDev
// Лицензия: MIT

package com.ontadev.kits.service;

import com.ontadev.kits.config.Config;
import com.ontadev.kits.config.MessageConfig;
import com.ontadev.kits.storage.KitWipe;
import com.ontadev.kits.storage.KitWipeRepository;
import com.ontadev.libs.ioc.annotation.lifecycle.Shutdown;
import com.ontadev.libs.ioc.annotation.stereotype.Service;
import com.ontadev.libs.message.Placeholders;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Вайп: пока он включён, наборы не выдаются (кроме игроков с {@code kits.bypass.wipe}). Состояние лежит в БД
 * в той же области данных, что и получения, так что в GLOBAL вайп действует на все серверы БД сразу.
 */
@Service
public class WipeService {

    private static final long INDEFINITE = -1L;

    private final Plugin plugin;
    private final Config config;
    private final MessageConfig messages;
    private final KitWipeRepository repository;
    private final String scope;

    private final ExecutorService worker = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "ontadev-kits-wipe");
        thread.setDaemon(true);
        return thread;
    });

    /** Растёт при каждом локальном start/stop, чтобы запоздавший опрос БД не затёр свежее решение. */
    private final AtomicLong version = new AtomicLong();

    /** 0 - выключен, {@link #INDEFINITE} - без срока, иначе время окончания в мс. */
    private volatile long endsAt;
    private long lastAnnounced = Long.MAX_VALUE;

    public WipeService(Plugin plugin, Config config, MessageConfig messages, KitWipeRepository repository) {
        this.plugin = plugin;
        this.config = config;
        this.messages = messages;
        this.repository = repository;
        this.scope = config.getStorage().scope();

        endsAt = normalize(read());

        Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);

        int period = config.getStorage().getSyncSeconds();
        if (config.getStorage().isGlobal() && period > 0) {
            long ticks = period * 20L;
            Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> worker.execute(this::poll), ticks, ticks);
        }
    }

    public boolean isActive() {
        return endsAt != 0;
    }

    public boolean isIndefinite() {
        return endsAt == INDEFINITE;
    }

    public long remainingSeconds() {
        long end = endsAt;
        if (end <= 0) return 0;
        return Math.max(0, (end - System.currentTimeMillis() + 999) / 1000);
    }

    public void start(long seconds) {
        set(seconds > 0 ? System.currentTimeMillis() + seconds * 1000 : INDEFINITE);
    }

    public void stop() {
        set(0);
    }

    private void set(long value) {
        version.incrementAndGet();
        endsAt = value;
        lastAnnounced = Long.MAX_VALUE;
        persist(value);
    }

    private void persist(long value) {
        worker.execute(() -> {
            try {
                repository.save(new KitWipe(scope, value));
            } catch (Exception e) {
                plugin.getLogger().severe("Не удалось сохранить состояние вайпа: " + e.getMessage());
            }
        });
    }

    private long read() {
        try {
            return repository.findByScope(scope).map(KitWipe::getEndsAt).orElse(0L);
        } catch (Exception e) {
            plugin.getLogger().warning("Не удалось прочитать состояние вайпа: " + e.getMessage());
            return 0;
        }
    }

    private static long normalize(long value) {
        return value > 0 && value <= System.currentTimeMillis() ? 0 : value;
    }

    private void poll() {
        long seen = version.get();
        long fromDb = normalize(read());
        if (version.get() != seen) return;

        long previous = endsAt;
        if (previous == fromDb) return;

        endsAt = fromDb;
        lastAnnounced = Long.MAX_VALUE;
        if (fromDb == 0 && previous != 0) {
            Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getOnlinePlayers().forEach(p -> messages.getWipeStopped().send(p)));
        }
    }

    private void tick() {
        if (endsAt == 0) return;

        if (endsAt > 0 && System.currentTimeMillis() >= endsAt) {
            endsAt = 0;
            persist(0);
            messages.getWipeEnded().send(Bukkit.getConsoleSender());
            Bukkit.getOnlinePlayers().forEach(p -> messages.getWipeEnded().send(p));
            return;
        }
        if (endsAt == INDEFINITE) return;

        long remaining = remainingSeconds();
        Placeholders ph = Placeholders.of("time", config.formatTime(remaining));

        if (config.getWipe().isActionbar()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                messages.getWipeCountdown().sendActionBar(player, ph);
            }
        }

        if (remaining < lastAnnounced && config.getWipe().getBroadcastAt().contains((int) remaining)) {
            lastAnnounced = remaining;
            for (Player player : Bukkit.getOnlinePlayers()) {
                messages.getWipeBroadcast().send(player, ph);
            }
        }
    }

    @Shutdown
    public void shutdown() {
        worker.shutdown();
        try {
            if (!worker.awaitTermination(10, TimeUnit.SECONDS)) {
                plugin.getLogger().warning("Не все записи о вайпе успели завершиться");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
