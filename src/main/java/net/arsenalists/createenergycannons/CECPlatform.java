package net.arsenalists.createenergycannons;

import net.arsenalists.createenergycannons.config.CECConfig;
import net.arsenalists.createenergycannons.config.server.CECServerConfig;

public class CECPlatform {
    public static void registerConfig() {
        CECConfig.register();
    }

    public static CECServerConfig getServerConfig() {
        return CECConfig.server();
    }
}