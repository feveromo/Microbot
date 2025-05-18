package net.runelite.client.plugins.microbot.magic.aiomagic;

import java.awt.Dimension;
import java.awt.Graphics2D;

import javax.inject.Inject;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.magic.aiomagic.enums.MagicActivity;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.util.ColorUtil;

public class AIOMagicOverlay extends OverlayPanel {

    private final AIOMagicPlugin plugin;
    @Inject
    private AIOMagicConfig config;

    @Inject
    AIOMagicOverlay(AIOMagicPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
        setPosition(OverlayPosition.TOP_LEFT);
        setNaughty();
    }
    
    @Override
    public Dimension render(Graphics2D graphics) {
        try {
            panelComponent.setPreferredSize(new Dimension(200, 300));
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("AIO Magic V" + AIOMagicPlugin.version)
                    .color(ColorUtil.fromHex("0077B6"))
                    .build());
            
            panelComponent.getChildren().add(LineComponent.builder().build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left(Microbot.status)
                    .build());
                    
            // Display additional information based on activity
            if (config.magicActivity() == MagicActivity.ENCHANTING) {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Spell:")
                        .right(plugin.getEnchantmentSpell().toString())
                        .build());
                        
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Jewellery:")
                        .right(plugin.getJewelleryType().getName())
                        .build());
            }

        } catch(Exception ex) {
            System.out.println(ex.getMessage());
        }
        return super.render(graphics);
    }
}
