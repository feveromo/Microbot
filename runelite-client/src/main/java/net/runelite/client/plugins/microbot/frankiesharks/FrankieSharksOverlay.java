package net.runelite.client.plugins.microbot.frankiesharks;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

import javax.inject.Inject;

import net.runelite.api.ItemID;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

public class FrankieSharksOverlay extends OverlayPanel {
    private FrankieSharksScript script;
    private static final int FRANKIE_NPC_ID = 6963;
    private static final String FRANKIE_NPC_NAME = "Frankie";
    private static final WorldPoint FRANKIE_LOCATION = new WorldPoint(1829, 3718, 0);
    private static final int RAW_SHARK_ID = ItemID.RAW_SHARK;

    @Inject
    FrankieSharksOverlay(FrankieSharksPlugin plugin) {
        super(plugin);
        setPosition(OverlayPosition.TOP_LEFT);
        setNaughty();
    }
    
    public void setScript(FrankieSharksScript script) {
        this.script = script;
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        try {
            panelComponent.setPreferredSize(new Dimension(220, 300));
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Frankie Sharks v1.0.3")
                    .color(Color.GREEN)
                    .build());

            if (script == null) {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Status:")
                        .right("Script not started")
                        .rightColor(Color.RED)
                        .build());
                return super.render(graphics);
            }

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Current State:")
                    .right(script.getCurrentState())
                    .build());
                    
            // Add distance to Frankie's location
            int distanceToFrankieLocation = Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(FRANKIE_LOCATION);
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Distance to Area:")
                    .right(String.valueOf(distanceToFrankieLocation))
                    .rightColor(distanceToFrankieLocation <= 10 ? Color.GREEN : (distanceToFrankieLocation <= 20 ? Color.YELLOW : Color.RED))
                    .build());
                    
            // Check if Frankie is found (helpful for debugging)
            NPC frankie = Rs2Npc.getNpc(FRANKIE_NPC_ID);
            if (frankie == null) {
                frankie = Rs2Npc.getNpc(FRANKIE_NPC_NAME);
            }
            
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Frankie Found:")
                    .right(frankie != null ? "Yes (ID: " + (frankie != null ? frankie.getId() : "N/A") + ")" : "No")
                    .rightColor(frankie != null ? Color.GREEN : Color.RED)
                    .build());
                    
            if (frankie != null) {
                int distance = Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(frankie.getWorldLocation());
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Distance to Frankie:")
                        .right(String.valueOf(distance))
                        .rightColor(distance < 3 ? Color.GREEN : (distance < 10 ? Color.YELLOW : Color.RED))
                        .build());
            }
            
            // Count sharks in inventory
            int sharksInInventory = Rs2Inventory.count(RAW_SHARK_ID);
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Inventory Sharks:")
                    .right(String.valueOf(sharksInInventory))
                    .rightColor(sharksInInventory > 0 ? Color.GREEN : Color.WHITE)
                    .build());
                    
            // Inventory space available
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Free Slots:")
                    .right(String.valueOf(Rs2Inventory.getEmptySlots()))
                    .rightColor(Rs2Inventory.getEmptySlots() >= 10 ? Color.GREEN : 
                               (Rs2Inventory.getEmptySlots() > 0 ? Color.YELLOW : Color.RED))
                    .build());

            // If bank is open, simplify to just show bank status
            if (Rs2Bank.isOpen()) {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Bank:")
                        .right("Open")
                        .rightColor(Color.CYAN)
                        .build());
            }

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return super.render(graphics);
    }
} 