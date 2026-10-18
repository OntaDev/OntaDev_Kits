// OntaDev_Kits Plugin
// Авторские права (c) 2026 OntaDev
// Лицензия: MIT

package com.ontadev.kits.service;

import com.ontadev.kits.config.Config;
import com.ontadev.kits.storage.KitClaim;
import com.ontadev.kits.storage.KitClaimRepository;
import com.ontadev.libs.ioc.annotation.AutoListener;
import com.ontadev.libs.ioc.annotation.lifecycle.Shutdown;
import com.ontadev.libs.ioc.annotation.stereotype.Service;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Когда игрок последний раз брал каждый набор. Данные в БД, а в памяти кэш: задержки проверяются
 * из главного потока и в БД ходить не могут. Все операции с БД идут через один фоновый поток, чтобы
 * записи шли по порядку. В GLOBAL кэш подтягивает чужие изменения при входе, по таймеру и перед выдачей.
 */
@Service
@AutoListener
public class PlayerDataService implements Listener {

    private final Plugin plugin;
    private final KitClaimRepository repository;
    private final String scope;
    private final boolean global;
    private final Map<UUID, Map<String, Long>> claims = new ConcurrentHashMap<>();
    private final ExecutorService writer = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "ontadev-kits-db");
        thread.setDaemon(true);
        return thread;
    });

    public PlayerDataService(Plugin plugin, Config config, KitClaimRepository repository) {
        this.plugin = plugin;
        this.repository = repository;
        this.scope = config.getStorage().scope();
        this.global = config.getStorage().isGlobal();

        plugin.getLogger().info("Хранилище наборов: " + scope);
        applySnapshot(repository.findAll(scope), 0);

        int period = config.getStorage().getSyncSeconds();
        if (global && period > 0) {
            long ticks = period * 20L;
            Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> async(this::syncAll), ticks, ticks);
        }
    }

    public Long lastClaim(UUID player, String kitId) {
        Map<String, Long> map = claims.get(player);
        return map == null ? null : map.get(kitId);
    }

    public void mark(UUID player, String kitId) {
        long now = System.currentTimeMillis();
        claims.computeIfAbsent(player, k -> new ConcurrentHashMap<>()).put(kitId, now);
        async(() -> repository.save(new KitClaim(scope, player, kitId, now)));
    }

    /**
     * Синхронно тянет из БД запись об одном наборе. В GLOBAL нужно перед самой выдачей, чтобы другой
     * сервер не дал взять набор повторно. В LOCAL ничего не делает.
     */
    public void refresh(UUID player, String kitId) {
        if (!global) return;

        try {
            KitClaim claim = repository.findByKey(KitClaim.keyOf(scope, player, kitId)).orElse(null);
            if (claim == null) {
                Map<String, Long> map = claims.get(player);
                if (map != null) map.remove(kitId);
            } else {
                claims.computeIfAbsent(player, k -> new ConcurrentHashMap<>()).put(kitId, claim.getClaimedAt());
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Не удалось обновить данные из БД: " + e.getMessage());
        }
    }

    public void reset(UUID player, String kitId) {
        if (kitId == null) {
            claims.remove(player);
            async(() -> repository.deleteByPlayer(scope, player.toString()));
            return;
        }

        Map<String, Long> map = claims.get(player);
        if (map != null) map.remove(kitId);
        async(() -> repository.deleteByKey(KitClaim.keyOf(scope, player, kitId)));
    }

    public void forgetKit(String kitId) {
        claims.values().forEach(map -> map.remove(kitId));
        async(() -> repository.deleteByKit(scope, kitId));
    }

    /** Данные грузим ещё до входа, в асинхронном потоке логина. */
    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (!global) return;
        try {
            UUID uuid = event.getUniqueId();
            List<KitClaim> rows = repository.findByPlayer(scope, uuid.toString());
            Map<String, Long> fresh = new ConcurrentHashMap<>();
            for (KitClaim row : rows) {
                if (row.getKitId() != null && row.getClaimedAt() != null) {
                    fresh.put(row.getKitId(), row.getClaimedAt());
                }
            }
            claims.put(uuid, fresh);
        } catch (Exception e) {
            plugin.getLogger().warning("Не удалось загрузить данные игрока: " + e.getMessage());
        }
    }

    private void syncAll() {
        long startedAt = System.currentTimeMillis();
        try {
            applySnapshot(repository.findAll(scope), startedAt);
        } catch (Exception e) {
            plugin.getLogger().warning("Не удалось синхронизировать данные с БД: " + e.getMessage());
        }
    }

    /** Заменяет кэш данными из БД, но не трогает локальные изменения не старше {@code keepAfter}. */
    private void applySnapshot(List<KitClaim> rows, long keepAfter) {
        Map<UUID, Map<String, Long>> fresh = new HashMap<>();
        for (KitClaim row : rows) {
            if (row.getPlayerUuid() == null || row.getKitId() == null || row.getClaimedAt() == null) continue;
            fresh.computeIfAbsent(row.getPlayerUuid(), k -> new HashMap<>()).put(row.getKitId(), row.getClaimedAt());
        }

        claims.forEach((uuid, map) -> map.forEach((kitId, time) -> {
            if (time >= keepAfter && keepAfter > 0) {
                fresh.computeIfAbsent(uuid, k -> new HashMap<>()).merge(kitId, time, Math::max);
            }
        }));

        fresh.forEach((uuid, map) -> claims.put(uuid, new ConcurrentHashMap<>(map)));
        claims.keySet().removeIf(uuid -> !fresh.containsKey(uuid));
    }

    private void async(Runnable task) {
        writer.execute(() -> {
            try {
                task.run();
            } catch (Exception e) {
                plugin.getLogger().severe("Ошибка работы с БД: " + e.getMessage());
            }
        });
    }

    @Shutdown
    public void shutdown() {
        writer.shutdown();
        try {
            if (!writer.awaitTermination(10, TimeUnit.SECONDS)) {
                plugin.getLogger().warning("Не все записи в БД успели завершиться");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
