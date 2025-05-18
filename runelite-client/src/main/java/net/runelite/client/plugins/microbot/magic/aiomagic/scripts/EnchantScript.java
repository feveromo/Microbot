package net.runelite.client.plugins.microbot.magic.aiomagic.scripts;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import net.runelite.api.Skill;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.magic.aiomagic.AIOMagicPlugin;
import net.runelite.client.plugins.microbot.magic.aiomagic.enums.JewellerySelectionType;
import net.runelite.client.plugins.microbot.magic.aiomagic.enums.JewelleryType;
import net.runelite.client.plugins.microbot.magic.aiomagic.enums.MagicState;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.antiban.enums.Activity;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;

public class EnchantScript extends Script {

    private MagicState state = MagicState.CASTING;
    private final AIOMagicPlugin plugin;

    @Inject
    public EnchantScript(AIOMagicPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean run() {
        Microbot.enableAutoRunOn = false;
        Rs2Antiban.resetAntibanSettings();
        Rs2Antiban.antibanSetupTemplates.applyGeneralBasicSetup();
        Rs2AntibanSettings.simulateAttentionSpan = true;
        Rs2AntibanSettings.nonLinearIntervals = true;
        Rs2AntibanSettings.contextualVariability = true;
        Rs2AntibanSettings.usePlayStyle = true;
        Rs2Antiban.setActivity(Activity.ENCHANTING);
        
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                long startTime = System.currentTimeMillis();

                if (hasStateChanged()) {
                    state = updateState();
                }

                if (state == null) {
                    Microbot.showMessage("Unable to evaluate state");
                    shutdown();
                    return;
                }

                // Check if we have the required magic level for the selected enchantment spell
                if (!plugin.getEnchantmentSpell().getRs2Spell().hasRequiredLevel()) {
                    Microbot.showMessage("You do not have the required magic level (" + 
                            plugin.getEnchantmentSpell().getMagicLevelRequired() + 
                            ") for this enchantment spell");
                    shutdown();
                    return;
                }

                switch (state) {
                    case BANKING:
                        if (!Rs2Bank.isNearBank(15)) {
                            Rs2Bank.walkToBankAndUseBank();
                            return;
                        }
                        
                        if (!Rs2Bank.isOpen()) {
                            Rs2Bank.useBank();
                            return;
                        }

                        // Deposit all enchanted jewellery
                        List<JewelleryType> jewelryToProcess = getJewelleryForCurrentConfig();
                        
                        for (JewelleryType jewellery : jewelryToProcess) {
                            if (Rs2Inventory.hasItem(jewellery.getEnchantedItemId())) {
                                Rs2Bank.depositAll(jewellery.getEnchantedItemId());
                            }
                        }

                        // Check and withdraw cosmic runes if needed
                        if (!Rs2Inventory.hasItemAmount("Cosmic rune", 500)) {
                            if (!Rs2Bank.hasBankItem("Cosmic rune")) {
                                Microbot.showMessage("Out of cosmic runes");
                                shutdown();
                                return;
                            }
                            Rs2Bank.withdrawX("Cosmic rune", 500);
                        }

                        // Check and withdraw the other runes needed based on the enchantment level
                        switch (plugin.getEnchantmentSpell().getLevel()) {
                            case 1: // Water runes for level 1 enchant
                                if (!Rs2Inventory.hasItemAmount("Water rune", 500)) {
                                    if (!Rs2Bank.hasBankItem("Water rune")) {
                                        Microbot.showMessage("Out of water runes");
                                        shutdown();
                                        return;
                                    }
                                    Rs2Bank.withdrawX("Water rune", 500);
                                }
                                break;
                            case 2: // Air runes for level 2 enchant
                                if (!Rs2Inventory.hasItemAmount("Air rune", 1500)) {
                                    if (!Rs2Bank.hasBankItem("Air rune")) {
                                        Microbot.showMessage("Out of air runes");
                                        shutdown();
                                        return;
                                    }
                                    Rs2Bank.withdrawX("Air rune", 1500);
                                }
                                break;
                            case 3: // Fire runes for level 3 enchant
                                if (!Rs2Inventory.hasItemAmount("Fire rune", 2500)) {
                                    if (!Rs2Bank.hasBankItem("Fire rune")) {
                                        Microbot.showMessage("Out of fire runes");
                                        shutdown();
                                        return;
                                    }
                                    Rs2Bank.withdrawX("Fire rune", 2500);
                                }
                                break;
                            case 4: // Earth runes for level 4 enchant
                                if (!Rs2Inventory.hasItemAmount("Earth rune", 5000)) {
                                    if (!Rs2Bank.hasBankItem("Earth rune")) {
                                        Microbot.showMessage("Out of earth runes");
                                        shutdown();
                                        return;
                                    }
                                    Rs2Bank.withdrawX("Earth rune", 5000);
                                }
                                break;
                            case 5: // Water runes for level 5 enchant
                                if (!Rs2Inventory.hasItemAmount("Water rune", 7500)) {
                                    if (!Rs2Bank.hasBankItem("Water rune")) {
                                        Microbot.showMessage("Out of water runes");
                                        shutdown();
                                        return;
                                    }
                                    Rs2Bank.withdrawX("Water rune", 7500);
                                }
                                break;
                            case 6: // Fire runes for level 6 enchant
                                if (!Rs2Inventory.hasItemAmount("Fire rune", 10000)) {
                                    if (!Rs2Bank.hasBankItem("Fire rune")) {
                                        Microbot.showMessage("Out of fire runes");
                                        shutdown();
                                        return;
                                    }
                                    Rs2Bank.withdrawX("Fire rune", 10000);
                                }
                                break;
                            case 7: // Soul and Blood runes for level 7 enchant
                                if (!Rs2Inventory.hasItemAmount("Soul rune", 1000)) {
                                    if (!Rs2Bank.hasBankItem("Soul rune")) {
                                        Microbot.showMessage("Out of soul runes");
                                        shutdown();
                                        return;
                                    }
                                    Rs2Bank.withdrawX("Soul rune", 1000);
                                }
                                if (!Rs2Inventory.hasItemAmount("Blood rune", 1000)) {
                                    if (!Rs2Bank.hasBankItem("Blood rune")) {
                                        Microbot.showMessage("Out of blood runes");
                                        shutdown();
                                        return;
                                    }
                                    Rs2Bank.withdrawX("Blood rune", 1000);
                                }
                                break;
                        }

                        // Calculate remaining space and withdraw unenchanted jewellery
                        int emptySpace = Rs2Inventory.getEmptySlots();
                        if (emptySpace > 0) {
                            // Try to withdraw specific unenchanted jewellery based on the selection
                            boolean foundJewellery = false;
                            
                            for (JewelleryType jewellery : jewelryToProcess) {
                                if (Rs2Bank.hasBankItem(jewellery.getUnenchantedItemId(), 1)) {
                                    Rs2Bank.withdrawAll(jewellery.getUnenchantedItemId());
                                    foundJewellery = true;
                                    break;
                                }
                            }
                            
                            if (!foundJewellery) {
                                String selectedType = plugin.getJewelleryType() == JewellerySelectionType.NONE ? 
                                        "any" : plugin.getJewelleryType().getName();
                                
                                Microbot.showMessage("No compatible " + selectedType + " jewellery found for enchantment level " + 
                                        plugin.getEnchantmentSpell().getLevel());
                                shutdown();
                                return;
                            }
                        }

                        Rs2Bank.closeBank();
                        break;
                        
                    case CASTING:
                        // Find an unenchanted jewellery item to enchant that matches our selection
                        Rs2ItemModel jewelleryItem = findMatchingJewelleryItem();
                        
                        if (jewelleryItem == null) {
                            Microbot.log("No more jewellery to enchant");
                            return;
                        }
                        
                        if (!Rs2Magic.hasRequiredRunes(plugin.getEnchantmentSpell().getRs2Spell())) {
                            Microbot.log("Missing required runes for enchantment");
                            return;
                        }
                        
                        // Cast the enchantment spell on the jewellery item
                        Rs2Magic.cast(plugin.getEnchantmentSpell().getRs2Spell().getAction());
                        sleep(600, 800);
                        Rs2Inventory.interact(jewelleryItem);
                        Rs2Player.waitForXpDrop(Skill.MAGIC, 5000, false);
                        break;
                }

                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown() {
        Rs2Antiban.resetAntibanSettings();
        super.shutdown();
    }

    private boolean hasStateChanged() {
        if (state == null) return true;
        if (state == MagicState.BANKING && hasUnenchantedJewellery()) return true;
        if (state == MagicState.CASTING && !hasUnenchantedJewellery()) return true;
        return false;
    }

    private MagicState updateState() {
        if (state == null) {
            return hasUnenchantedJewellery() ? MagicState.CASTING : MagicState.BANKING;
        }
        if (state == MagicState.BANKING && hasUnenchantedJewellery()) {
            return MagicState.CASTING;
        }
        if (state == MagicState.CASTING && !hasUnenchantedJewellery()) {
            return MagicState.BANKING;
        }
        return state;
    }
    
    private boolean hasUnenchantedJewellery() {
        return findMatchingJewelleryItem() != null;
    }
    
    private Rs2ItemModel findMatchingJewelleryItem() {
        List<JewelleryType> jewelryToProcess = getJewelleryForCurrentConfig();
        
        for (Rs2ItemModel item : Rs2Inventory.items()) {
            for (JewelleryType jewellery : jewelryToProcess) {
                if (item.getId() == jewellery.getUnenchantedItemId()) {
                    return item;
                }
            }
        }
        return null;
    }
    
    private List<JewelleryType> getJewelleryForCurrentConfig() {
        List<JewelleryType> result = new ArrayList<>();
        JewellerySelectionType selectedType = plugin.getJewelleryType();
        int enchantLevel = plugin.getEnchantmentSpell().getLevel();
        
        // If no specific type selected, return all jewellery types matching the enchant level
        if (selectedType == JewellerySelectionType.NONE) {
            for (JewelleryType jewellery : JewelleryType.values()) {
                if (jewellery.getEnchantLevel() == enchantLevel) {
                    result.add(jewellery);
                }
            }
            return result;
        }
        
        // Handle category-based selections
        if (selectedType.isGroup()) {
            for (JewelleryType jewellery : JewelleryType.values()) {
                if (jewellery.getEnchantLevel() == enchantLevel) {
                    // Filter by jewelry category
                    if (selectedType == JewellerySelectionType.RINGS_ALL && jewellery.name().contains("RING")) {
                        result.add(jewellery);
                    } else if (selectedType == JewellerySelectionType.NECKLACES_ALL && jewellery.name().contains("NECKLACE")) {
                        result.add(jewellery);
                    } else if (selectedType == JewellerySelectionType.AMULETS_ALL && jewellery.name().contains("AMULET")) {
                        result.add(jewellery);
                    } else if (selectedType == JewellerySelectionType.BRACELETS_ALL && jewellery.name().contains("BRACELET")) {
                        result.add(jewellery);
                    }
                    // Filter by material
                    else if (selectedType.name().startsWith("MATERIAL_")) {
                        String material = selectedType.name().substring(9); // Remove "MATERIAL_" prefix
                        if (jewellery.name().contains(material)) {
                            result.add(jewellery);
                        }
                    }
                }
            }
            return result;
        }
        
        // Handle individual jewelry selections
        for (JewelleryType jewellery : JewelleryType.values()) {
            if (jewellery.getEnchantLevel() == enchantLevel) {
                String jewelryNameLower = jewellery.name().toLowerCase();
                String selectedNameLower = selectedType.name().toLowerCase().replace("ring_", "").replace("necklace_", "")
                        .replace("amulet_", "").replace("bracelet_", "");
                
                // Match the specific jewelry type
                if (jewelryNameLower.contains(selectedNameLower.toLowerCase())) {
                    if ((selectedType.isRing() && jewellery.name().contains("RING")) ||
                        (selectedType.isNecklace() && jewellery.name().contains("NECKLACE")) ||
                        (selectedType.isAmulet() && jewellery.name().contains("AMULET")) ||
                        (selectedType.isBracelet() && jewellery.name().contains("BRACELET"))) {
                        result.add(jewellery);
                    }
                }
            }
        }
        
        return result;
    }
} 