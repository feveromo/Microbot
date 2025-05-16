package net.runelite.client.plugins.microbot.frankiesharks;

import javax.inject.Inject;

import com.google.inject.Provides;

import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
    name = "Micro Frankie Sharks",
    description = "Microbot script to buy sharks from Frankie, bank, and hop worlds.",
    tags = {"microbot", "fishing", "sharks", "frankie", "piscarilius"},
    enabledByDefault = false
)
public class FrankieSharksPlugin extends Plugin {
    @Inject
    private FrankieSharksConfig config;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private FrankieSharksOverlay frankieSharksOverlay;

    private FrankieSharksScript frankieSharksScript;

    @Provides
    FrankieSharksConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(FrankieSharksConfig.class);
    }

    @Override
    protected void startUp() throws Exception {
        if (frankieSharksScript == null) {
            frankieSharksScript = new FrankieSharksScript();
        }
        frankieSharksOverlay.setScript(frankieSharksScript);
        overlayManager.add(frankieSharksOverlay);
        frankieSharksScript.run(config);
    }

    @Override
    protected void shutDown() throws Exception {
        if (frankieSharksScript != null) {
            frankieSharksScript.shutdown();
            frankieSharksScript = null;
        }
        overlayManager.remove(frankieSharksOverlay);
    }
} 