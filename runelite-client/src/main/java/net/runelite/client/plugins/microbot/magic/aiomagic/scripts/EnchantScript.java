package net.runelite.client.plugins.microbot.magic.aiomagic.scripts;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.inject.Inject;

import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.Skill;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.globval.enums.InterfaceTab;
import net.runelite.client.plugins.microbot.magic.aiomagic.AIOMagicPlugin;
import net.runelite.client.plugins.microbot.magic.aiomagic.enums.JewellerySelectionType;
import net.runelite.client.plugins.microbot.magic.aiomagic.enums.JewelleryType;
import net.runelite.client.plugins.microbot.magic.aiomagic.enums.MagicState;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.antiban.enums.Activity;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.magic.Rs2Staff;
import net.runelite.client.plugins.microbot.util.magic.Runes;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.tabs.Rs2Tab;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

public class EnchantScript extends Script {

    private MagicState state = MagicState.CASTING;
    private final AIOMagicPlugin plugin;
    
    // Safety tracking variables
    private int bankFailureCount = 0;
    private final int maxBankFailures = 5;
    private int consecutiveEmptyWithdrawals = 0;
    private final int maxConsecutiveEmptyWithdrawals = 3;

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
        
        // Reset counters when script starts
        bankFailureCount = 0;
        consecutiveEmptyWithdrawals = 0;
        
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
                
                // Safety check - if we've had too many bank failures, shut down
                if (bankFailureCount >= maxBankFailures) {
                    Microbot.showMessage("Shutting down due to repeated banking failures");
                    shutdown();
                    return;
                }
                
                // Safety check - if we've had too many consecutive empty withdrawals, shut down
                if (consecutiveEmptyWithdrawals >= maxConsecutiveEmptyWithdrawals) {
                    Microbot.showMessage("Shutting down due to repeated empty jewelry withdrawals");
                    shutdown();
                    return;
                }

                switch (state) {
                    case BANKING:
                        if (!Rs2Bank.isOpen()) {
                            // Add retry mechanism for bank opening
                            int maxRetries = 3;
                            int retries = 0;
                            boolean bankOpened = false;
                            
                            while (!bankOpened && retries < maxRetries) {
                                Microbot.status = "Opening bank, attempt " + (retries + 1);
                                
                                if (Rs2Bank.isNearBank(15)) {
                                    bankOpened = Rs2Bank.useBank();
                                } else {
                                    bankOpened = Rs2Bank.walkToBankAndUseBank();
                                }
                                
                                if (!bankOpened) {
                                    retries++;
                                    bankFailureCount++; // Track total banking failures across attempts
                                    sleep(500, 800);
                                } else {
                                    bankFailureCount = 0; // Reset on successful bank open
                                }
                            }
                            
                            // Wait for bank interface to appear
                            if (!sleepUntil(() -> Rs2Bank.isOpen(), 3000)) {
                                Microbot.status = "Failed to open bank after " + maxRetries + " attempts";
                                return;
                            }
                        }

                        // Deposit all enchanted jewellery
                        List<JewelleryType> jewelryToProcess = getJewelleryForCurrentConfig();
                        boolean depositedSomething = false;
                        for (JewelleryType jewellery : jewelryToProcess) {
                            if (Rs2Inventory.hasItem(jewellery.getEnchantedItemId())) {
                                Rs2Bank.depositAll(jewellery.getEnchantedItemId());
                                sleep(100, 200); 
                                depositedSomething = true;
                            }
                        }

                        if (depositedSomething) {
                            sleep(200, 400); 
                            // Wait for inventory to update after depositing
                            Rs2Inventory.waitForInventoryChanges(1000);
                        }

                        // Check and withdraw cosmic runes
                        if (!Rs2Inventory.hasItemAmount("Cosmic rune", 500)) {
                            checkAndWithdrawRunes("Cosmic rune", "cosmic rune", 500);
                        }

                        // Check and withdraw the other runes needed based on the enchantment level
                        switch (plugin.getEnchantmentSpell().getLevel()) {
                            case 1: // Water runes for level 1 enchant
                                if (!Rs2Inventory.hasItemAmount("Water rune", 500)) {
                                    checkAndWithdrawRunes("Water rune", "water rune", 500);
                                }
                                break;
                            case 2: // Air runes for level 2 enchant
                                if (!Rs2Inventory.hasItemAmount("Air rune", 1500)) {
                                    checkAndWithdrawRunes("Air rune", "air rune", 1500);
                                }
                                break;
                            case 3: // Fire runes for level 3 enchant
                                if (!Rs2Inventory.hasItemAmount("Fire rune", 2500)) {
                                    checkAndWithdrawRunes("Fire rune", "fire rune", 2500);
                                }
                                break;
                            case 4: // Earth runes for level 4 enchant
                                if (!Rs2Inventory.hasItemAmount("Earth rune", 5000)) {
                                    checkAndWithdrawRunes("Earth rune", "earth rune", 5000);
                                }
                                break;
                            case 5: // Water runes for level 5 enchant
                                if (!Rs2Inventory.hasItemAmount("Water rune", 7500)) {
                                    checkAndWithdrawRunes("Water rune", "water rune", 7500);
                                }
                                break;
                            case 6: // Fire runes for level 6 enchant
                                if (!Rs2Inventory.hasItemAmount("Fire rune", 10000)) {
                                    checkAndWithdrawRunes("Fire rune", "fire rune", 10000);
                                }
                                break;
                            case 7: // Soul and Blood runes for level 7 enchant
                                if (!Rs2Inventory.hasItemAmount("Soul rune", 1000)) {
                                    checkAndWithdrawRunes("Soul rune", "soul rune", 1000);
                                }
                                if (!Rs2Inventory.hasItemAmount("Blood rune", 1000)) {
                                    checkAndWithdrawRunes("Blood rune", "blood rune", 1000);
                                }
                                break;
                        }

                        // Calculate remaining space and withdraw unenchanted jewellery
                        int emptySpace = Rs2Inventory.getEmptySlots();
                        if (emptySpace > 0) {
                            // Try to withdraw specific unenchanted jewellery based on the selection
                            boolean foundJewelleryToWithdraw = false;
                            int startingItemCount = Rs2Inventory.count();
                            
                            for (JewelleryType jewellery : jewelryToProcess) {
                                if (Rs2Bank.hasBankItem(jewellery.getUnenchantedItemId(), 1)) {
                                    // Verify the item is really there - sometimes hasBankItem can be inaccurate due to synchronization
                                    Rs2ItemModel bankItem = Rs2Bank.getBankItem(jewellery.getUnenchantedItemId());
                                    if (bankItem == null || bankItem.getQuantity() < 1) {
                                        // Item not actually in bank, continue to next type
                                        continue;
                                    }
                                    
                                    // First try withdrawing all
                                    Rs2Bank.withdrawAll(jewellery.getUnenchantedItemId());
                                    sleep(300, 500);
                                    
                                    // Verify that items were actually withdrawn
                                    if (Rs2Inventory.count() > startingItemCount) {
                                        foundJewelleryToWithdraw = true;
                                        consecutiveEmptyWithdrawals = 0; // Reset counter on successful withdrawal
                                        break;
                                    }
                                    
                                    // If withdrawAll failed, try explicit withdraw with X=28
                                    Rs2Bank.withdrawX(jewellery.getUnenchantedItemId(), Math.min(28, emptySpace));
                                    sleep(300, 500);
                                    
                                    // Check again if withdrawal succeeded
                                    if (Rs2Inventory.count() > startingItemCount) {
                                        foundJewelleryToWithdraw = true;
                                        consecutiveEmptyWithdrawals = 0; // Reset counter on successful withdrawal
                                        break;
                                    }
                                }
                            }
                            
                            // Wait longer for inventory to update after withdrawal attempt
                            sleep(500, 700);
                            
                            if (!foundJewelleryToWithdraw || !hasJewelleryToEnchant(jewelryToProcess)) {
                                consecutiveEmptyWithdrawals++; // Increment counter on failed withdrawal
                                
                                if (consecutiveEmptyWithdrawals >= maxConsecutiveEmptyWithdrawals) {
                                    String selectedType = plugin.getJewelleryType() == JewellerySelectionType.NONE ? 
                                            "any" : plugin.getJewelleryType().getName();
                                    
                                    Microbot.showMessage("No compatible " + selectedType + " jewellery found for enchantment level " + 
                                            plugin.getEnchantmentSpell().getLevel());
                                    shutdown();
                                    return;
                                }
                                
                                // Try closing and reopening the bank
                                Rs2Bank.closeBank();
                                sleep(500, 700);
                                return; // Return to try again next cycle
                            }
                        }

                        Rs2Bank.closeBank();
                        sleepUntil(() -> !Rs2Bank.isOpen(), 2000);
                        break;
                        
                    case CASTING:
                        // Find an unenchanted jewellery item to enchant that matches our selection
                        Rs2ItemModel jewelleryItem = findMatchingJewelleryItem();
                        
                        if (jewelleryItem == null) {
                            Microbot.log("No more jewellery to enchant");
                            return;
                        }
                        
                        // Check for the required runes using the default method
                        if (!Rs2Magic.hasRequiredRunes(plugin.getEnchantmentSpell().getRs2Spell())) {
                            // Double-check with our more tolerant case-insensitive method before giving up
                            if (!hasEnchantmentRunes()) {
                                Microbot.log("Missing required runes for enchantment");
                                return;
                            } else {
                                // We found the runes via our custom check
                                Microbot.status = "Found runes, continuing with enchantment...";
                            }
                        }
                        
                        // Properly cast the enchantment spell on the jewellery item
                        if (Rs2Tab.getCurrentTab() != InterfaceTab.MAGIC) {
                            Rs2Tab.switchToMagicTab();
                            sleep(300, 500);
                        }
                        
                        // Clear previous status
                        Microbot.status = "Enchanting jewelry";
                        
                        // First open the enchantment interface if needed
                        if (!Rs2Widget.hasWidgetText("Jewellery Enchantments", 218, 3, true)) {
                            // Click the enchantment button to open the interface
                            if (Rs2Widget.clickWidget("Jewellery Enchantments", Optional.of(218), 3, true)) {
                                sleep(500, 700);
                            }
                        }
                        
                        // Now cast the specific enchantment spell - using the most direct method
                        Rs2Magic.cast(plugin.getEnchantmentSpell().getRs2Spell().getAction());
                        sleep(600, 800);
                        
                        // Make sure spell is selected before clicking the item
                        if (Microbot.getClient().isWidgetSelected()) {
                            // Simple interact with the item - more reliable
                            Rs2Inventory.interact(jewelleryItem, "Cast");
                            Rs2Player.waitForXpDrop(Skill.MAGIC, 3000, false);
                        } else {
                            Microbot.status = "Failed to select spell, retrying...";
                            return;
                        }
                        break;
                }

                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 150, TimeUnit.MILLISECONDS);
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
        // Convert stream to list for iteration
        List<Rs2ItemModel> inventoryItems = Rs2Inventory.items().collect(Collectors.toList());
        
        for (Rs2ItemModel item : inventoryItems) {
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

    /**
     * Checks if the inventory contains any unenchanted jewellery from the provided list.
     * 
     * @param jewelryList List of jewellery types to check for
     * @return true if at least one unenchanted jewellery item is found
     */
    private boolean hasJewelleryToEnchant(List<JewelleryType> jewelryList) {
        // Convert stream to list for iteration
        List<Rs2ItemModel> inventoryItems = Rs2Inventory.items().collect(Collectors.toList());
        for (Rs2ItemModel item : inventoryItems) {
            for (JewelleryType jewellery : jewelryList) {
                if (item.getId() == jewellery.getUnenchantedItemId()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Helper method to check for runes in both uppercase and lowercase versions,
     * and withdraw them if not found in inventory
     * 
     * @param properName Properly capitalized rune name
     * @param lowercaseName Lowercase rune name for alternative check
     * @param amount Amount needed
     */
    private void checkAndWithdrawRunes(String properName, String lowercaseName, int amount) {
        // Check if the rune is provided by an equipped staff first
        boolean isRuneProvided = false;
        
        // Convert rune name to Runes enum type
        Runes runeType = null;
        if (properName.equalsIgnoreCase("Air rune")) runeType = Runes.AIR;
        else if (properName.equalsIgnoreCase("Water rune")) runeType = Runes.WATER;
        else if (properName.equalsIgnoreCase("Earth rune")) runeType = Runes.EARTH;
        else if (properName.equalsIgnoreCase("Fire rune")) runeType = Runes.FIRE;
        
        // Check if the rune is provided by equipped staff
        Rs2ItemModel equippedWeapon = Rs2Equipment.get(EquipmentInventorySlot.WEAPON);
        if (equippedWeapon != null && runeType != null) {
            Rs2Staff equippedStaff = Rs2Magic.getRs2Staff(equippedWeapon.getId());
            if (equippedStaff != Rs2Staff.NONE && equippedStaff.getRunes().contains(runeType)) {
                isRuneProvided = true;
                Microbot.status = "Equipped staff provides infinite " + lowercaseName;
            }
        }
        
        // Only withdraw if we need the rune (not provided by staff and not already in inventory)
        if (!isRuneProvided && !Rs2Inventory.hasItem(lowercaseName) && !Rs2Inventory.hasItem(properName)) {
            if (!Rs2Bank.hasBankItem(properName) && !Rs2Bank.hasBankItem(lowercaseName)) {
                Microbot.showMessage("Out of " + lowercaseName);
                shutdown();
                return;
            }
            Rs2Bank.withdrawX(properName, amount);
            sleep(150, 250); 
        } else {
            Microbot.status = isRuneProvided ? 
                "Staff provides " + lowercaseName + ", no need to withdraw" : 
                "Have " + lowercaseName + ", continuing...";
        }
    }

    /**
     * Custom method to check if we have runes for enchantment spell, handling case sensitivity
     * and checking for equipped staffs
     */
    private boolean hasEnchantmentRunes() {
        int level = plugin.getEnchantmentSpell().getLevel();
        
        // All enchantment spells require cosmic runes
        if (!Rs2Inventory.hasItem("Cosmic rune") && !Rs2Inventory.hasItem("cosmic rune")) {
            return false;
        }
        
        // Check for equipped staff that provides runes
        Rs2ItemModel equippedWeapon = Rs2Equipment.get(EquipmentInventorySlot.WEAPON);
        Rs2Staff equippedStaff = null;
        
        if (equippedWeapon != null) {
            equippedStaff = Rs2Magic.getRs2Staff(equippedWeapon.getId());
        }
        
        // Check for other runes based on enchantment level
        switch (level) {
            case 1: // Water runes for level 1 enchant
                return (equippedStaff != null && equippedStaff != Rs2Staff.NONE && equippedStaff.getRunes().contains(Runes.WATER)) ||
                       Rs2Inventory.hasItem("Water rune") || Rs2Inventory.hasItem("water rune");
            case 2: // Air runes for level 2 enchant
                return (equippedStaff != null && equippedStaff != Rs2Staff.NONE && equippedStaff.getRunes().contains(Runes.AIR)) ||
                       Rs2Inventory.hasItem("Air rune") || Rs2Inventory.hasItem("air rune");
            case 3: // Fire runes for level 3 enchant
                return (equippedStaff != null && equippedStaff != Rs2Staff.NONE && equippedStaff.getRunes().contains(Runes.FIRE)) ||
                       Rs2Inventory.hasItem("Fire rune") || Rs2Inventory.hasItem("fire rune");
            case 4: // Earth runes for level 4 enchant
                return (equippedStaff != null && equippedStaff != Rs2Staff.NONE && equippedStaff.getRunes().contains(Runes.EARTH)) ||
                       Rs2Inventory.hasItem("Earth rune") || Rs2Inventory.hasItem("earth rune");
            case 5: // Water runes for level 5 enchant
                return (equippedStaff != null && equippedStaff != Rs2Staff.NONE && equippedStaff.getRunes().contains(Runes.WATER)) ||
                       Rs2Inventory.hasItem("Water rune") || Rs2Inventory.hasItem("water rune");
            case 6: // Fire runes for level 6 enchant
                return (equippedStaff != null && equippedStaff != Rs2Staff.NONE && equippedStaff.getRunes().contains(Runes.FIRE)) ||
                       Rs2Inventory.hasItem("Fire rune") || Rs2Inventory.hasItem("fire rune");
            case 7: // Soul and Blood runes for level 7 enchant
                boolean hasSoulRunes = (Rs2Inventory.hasItem("Soul rune") || Rs2Inventory.hasItem("soul rune"));
                boolean hasBloodRunes = (Rs2Inventory.hasItem("Blood rune") || Rs2Inventory.hasItem("blood rune"));
                
                // For level 7, we need both Soul and Blood runes - no staff provides both
                return hasSoulRunes && hasBloodRunes;
            default:
                return false;
        }
    }
} 