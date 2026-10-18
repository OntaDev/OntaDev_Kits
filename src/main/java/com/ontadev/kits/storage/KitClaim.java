// OntaDev_Kits Plugin
// Авторские права (c) 2026 OntaDev
// Лицензия: MIT

package com.ontadev.kits.storage;

import com.ontadev.libs.orm.annotation.entity.Column;
import com.ontadev.libs.orm.annotation.entity.Entity;
import com.ontadev.libs.orm.annotation.entity.Id;
import com.ontadev.libs.orm.annotation.entity.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Запись о получении набора игроком. Поле {@code scope} разделяет серверы: {@code global} или {@code local:<server-id>}.
 */
@Entity
@Table("kits_claims")
@Getter
@Setter
@NoArgsConstructor
public class KitClaim {

    /** Составной ключ {@code <scope>:<uuid>:<kitId>}: ORM поддерживает только один {@code @Id}. */
    @Id
    @Column(value = "claim_key", length = 200)
    private String key;

    @Column(length = 100, indexed = true)
    private String scope;

    @Column(value = "player_uuid", indexed = true)
    private UUID playerUuid;

    @Column(value = "kit_id", length = 64)
    private String kitId;

    @Column("claimed_at")
    private Long claimedAt;

    public KitClaim(String scope, UUID playerUuid, String kitId, long claimedAt) {
        this.key = keyOf(scope, playerUuid, kitId);
        this.scope = scope;
        this.playerUuid = playerUuid;
        this.kitId = kitId;
        this.claimedAt = claimedAt;
    }

    public static String keyOf(String scope, UUID playerUuid, String kitId) {
        return scope + ":" + playerUuid + ":" + kitId;
    }
}
