// OntaDev_Kits Plugin
// Авторские права (c) 2026 OntaDev
// Лицензия: MIT

package com.ontadev.kits.storage;

import com.ontadev.libs.orm.OrmRepository;
import com.ontadev.libs.orm.annotation.Query;

import java.util.Optional;

public interface KitWipeRepository extends OrmRepository<KitWipe, String> {

    @Query("SELECT * FROM kits_wipe WHERE scope = ?")
    Optional<KitWipe> findByScope(String scope);
}
