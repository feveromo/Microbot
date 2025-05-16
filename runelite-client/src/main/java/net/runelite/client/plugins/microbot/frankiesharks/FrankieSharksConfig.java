package net.runelite.client.plugins.microbot.frankiesharks;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("frankiesharks")
public interface FrankieSharksConfig extends Config {
    @ConfigItem(
            keyName = "guide",
            name = "Script Guide",
            description = "Detailed instructions for the Frankie's Sharks script",
            position = 0
    )
    default String guide() {
        return "Buys sharks from Frankie in Port Piscarilius, banks, and hops worlds.\n" +
               "Ensure you start at Port Piscarilius bank with GP in your first inventory slot.";
    }
} 