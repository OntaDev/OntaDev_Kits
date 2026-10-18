// OntaDev_Kits Plugin
// Авторские права (c) 2026 OntaDev
// Лицензия: MIT

package com.ontadev.kits.storage;

import com.ontadev.libs.orm.OrmRepository;
import com.ontadev.libs.orm.annotation.Modifying;
import com.ontadev.libs.orm.annotation.Query;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий над общим {@code DatabaseManager} из Libs, реализацию создаёт библиотека.
 * Все выборки ограничены областью {@code scope}.
 */
public interface KitClaimRepository extends OrmRepository<KitClaim, String> {

    @Query("SELECT * FROM kits_claims WHERE scope = ?")
    List<KitClaim> findAll(String scope);

    @Query("SELECT * FROM kits_claims WHERE scope = ? AND player_uuid = ?")
    List<KitClaim> findByPlayer(String scope, String playerUuid);

    @Query("SELECT * FROM kits_claims WHERE claim_key = ?")
    Optional<KitClaim> findByKey(String key);

    @Modifying("DELETE FROM kits_claims WHERE scope = ? AND player_uuid = ?")
    void deleteByPlayer(String scope, String playerUuid);

    @Modifying("DELETE FROM kits_claims WHERE claim_key = ?")
    void deleteByKey(String key);

    @Modifying("DELETE FROM kits_claims WHERE scope = ? AND kit_id = ?")
    void deleteByKit(String scope, String kitId);
}
