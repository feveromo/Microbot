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
    private static final int STAMINA_EFFECT_VARP = 25;
    
    @Inject
    private FrankieSharksConfig config;

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
                    .text("Frankie Sharks v1.0.4")
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
                    
            // Add runtime display
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Runtime:")
                    .right(script.getRuntime())
                    .rightColor(Color.WHITE)
                    .build());
                    
            // Add statistics for sharks bought and sharks per hour
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Sharks Bought:")
                    .right(String.valueOf(script.getTotalSharksBought()))
                    .rightColor(Color.GREEN)
                    .build());
                    
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Sharks Per Hour:")
                    .right(String.valueOf(script.getSharksPerHour()))
                    .rightColor(Color.CYAN)
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

            // If bank is open, simplify to just show bank status
            if (Rs2Bank.isOpen()) {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Bank:")
                        .right("Open")
                        .rightColor(Color.CYAN)
                        .build());
            }
            
            // Add stamina potion status
            if (config.useStaminaPotions()) {
                boolean staminaActive = Microbot.getClient().getVarpValue(STAMINA_EFFECT_VARP) > 0;
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Stamina:")
                        .right(staminaActive ? "Active" : "Inactive")
                        .rightColor(staminaActive ? Color.GREEN : Color.RED)
                        .build());
                
                // Check for stamina potions in inventory
                boolean hasStaminaInv = hasAnyStaminaPotion();
                if (hasStaminaInv) {
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left("Potion:")
                            .right("In inventory")
                            .rightColor(Color.GREEN)
                            .build());
                }
            }

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return super.render(graphics);
    }
    
    /**
     * Checks if player has any stamina potion in inventory
     */
    private boolean hasAnyStaminaPotion() {
        return Rs2Inventory.hasItem(ItemID.STAMINA_POTION1) ||
               Rs2Inventory.hasItem(ItemID.STAMINA_POTION2) ||
               Rs2Inventory.hasItem(ItemID.STAMINA_POTION3) ||
               Rs2Inventory.hasItem(ItemID.STAMINA_POTION4);
    }
} 