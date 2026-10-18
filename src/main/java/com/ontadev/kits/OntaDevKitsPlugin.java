// OntaDev_Kits Plugin
// Авторские права (c) 2026 OntaDev
// Лицензия: MIT

package com.ontadev.kits;

import com.ontadev.kits.config.Config;
import com.ontadev.kits.service.ConfigMigrator;
import com.ontadev.kits.service.KitAdminActions;
import com.ontadev.libs.ioc.PluginIoC;
import com.ontadev.libs.plugin.OntaDev_Template;
import org.bukkit.Bukkit;

public final class OntaDevKitsPlugin extends OntaDev_Template {

    @Override
    public void onPluginEnable(PluginIoC pluginIoC) {
        Config config = pluginIoC.get(Config.class);
        if (config.isAutoUpdateConfigs() && pluginIoC.get(ConfigMigrator.class).migrate() > 0) {
            pluginIoC.get(KitAdminActions.class).reload(Bukkit.getConsoleSender());
        }
        log.info("OntaDev_Kits enabled");
    }
}
