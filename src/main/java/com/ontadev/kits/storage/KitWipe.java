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

@Entity
@Table("kits_wipe")
@Getter
@Setter
@NoArgsConstructor
public class KitWipe {

    @Id
    @Column(length = 100)
    private String scope;

    /** 0 - выключен, -1 - без срока, иначе время окончания в мс. */
    @Column("ends_at")
    private Long endsAt;

    public KitWipe(String scope, long endsAt) {
        this.scope = scope;
        this.endsAt = endsAt;
    }
}
