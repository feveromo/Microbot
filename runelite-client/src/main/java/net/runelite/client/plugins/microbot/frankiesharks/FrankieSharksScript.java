package net.runelite.client.plugins.microbot.frankiesharks;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.runelite.api.ItemComposition;
import net.runelite.api.ItemID;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.security.Login;
import net.runelite.client.plugins.microbot.util.shop.Rs2Shop;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;

public class FrankieSharksScript extends Script {

    private static final int FRANKIE_NPC_ID_CUSTOM = 6963;
    private static final String FRANKIE_NPC_NAME = "Frankie";
    private static final int RAW_SHARK_ID = ItemID.RAW_SHARK;
    private static final int GP_ID = ItemID.COINS_995;
    private static final WorldPoint PISCARILIUS_BANK_LOCATION = new WorldPoint(1803, 3787, 0);
    private static final WorldPoint FRANKIE_LOCATION = new WorldPoint(1829, 3718, 0); // Frankie's approximate location
    private static final List<Integer> BANK_BOOTH_IDS = Arrays.asList(27718, 27719, 27720, 27721);
    private static final List<Integer> BANKER_NPC_IDS = Arrays.asList(6969, 6970);
    private static final int MIN_SHARKS_TO_BUY = 10;
    private static final int MAX_BUY_QUANTITY = 27; // 28 inv slots - 1 for GP
    private static final int FRANKIE_SHOP_MAX_QUANTITY = 25; // Maximum sharks Frankie has in shop
    
    // Statistics tracking
    private long startTime = 0;
    private int totalSharksBought = 0;
    
    // Stamina potion constants
    private static final List<Integer> STAMINA_POTION_IDS = Arrays.asList(
        ItemID.STAMINA_POTION1, ItemID.STAMINA_POTION2, 
        ItemID.STAMINA_POTION3, ItemID.STAMINA_POTION4
    );
    private static final int STAMINA_EFFECT_VARP = 25;

    private enum State {
        STARTING,
        CHECK_INITIAL_CONDITIONS,
        WALKING_TO_FRANKIE_AREA,
        WALKING_TO_FRANKIE,
        INTERACTING_WITH_FRANKIE,
        BUYING_SHARKS,
        WALKING_TO_BANK,
        BANKING_SHARKS,
        HOPPING_WORLD,
        STOPPED
    }

    private State currentState = State.STARTING;
    private FrankieSharksConfig config;
    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private boolean hasStaminaPotion = false;

    public String getCurrentState() {
        return currentState.toString();
    }
    
    // Methods to get statistics for overlay
    public int getTotalSharksBought() {
        return totalSharksBought;
    }
    
    public int getSharksPerHour() {
        if (startTime == 0) {
            return 0;
        }
        
        long timeRunning = System.currentTimeMillis() - startTime;
        // Avoid division by zero and ensure at least 1 minute has passed
        if (timeRunning < 60000) {
            return 0;
        }
        
        // Calculate sharks per hour: (sharks bought / time running in hours)
        // time in hours = time in ms / (1000 * 60 * 60)
        return (int) ((double) totalSharksBought / ((double) timeRunning / 3600000));
    }
    
    public String getRuntime() {
        if (startTime == 0) {
            return "00:00:00";
        }
        
        long timeRunning = System.currentTimeMillis() - startTime;
        long hours = timeRunning / (1000 * 60 * 60);
        long minutes = (timeRunning % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (timeRunning % (1000 * 60)) / 1000;
        
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public boolean run(FrankieSharksConfig config) {
        this.config = config;
        Microbot.enableAutoRunOn = true;
        startTime = System.currentTimeMillis();
        totalSharksBought = 0;
        mainScheduledFuture = executorService.schedule(this::loop, 0, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown() {
        super.shutdown();
        currentState = State.STOPPED;
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
        Microbot.log("Frankie Sharks script stopped.");
    }

    private void loop() {
        if (Microbot.isLoggedIn()) {
            currentState = State.CHECK_INITIAL_CONDITIONS;
        }

        while (isRunning() && currentState != State.STOPPED) {
            if (!Microbot.isLoggedIn()) {
                Microbot.log("Logged out, stopping script.");
                currentState = State.STOPPED;
                break;
            }
            
            // Check if we need to use stamina potion
            checkAndUseStaminaPotion();
            
            switch (currentState) {
                case CHECK_INITIAL_CONDITIONS:
                    handleCheckInitialConditions();
                    break;
                case WALKING_TO_FRANKIE_AREA:
                    handleWalkingToFrankieArea();
                    break;
                case WALKING_TO_FRANKIE:
                    handleWalkingToFrankie();
                    break;
                case INTERACTING_WITH_FRANKIE:
                    handleInteractingWithFrankie();
                    break;
                case BUYING_SHARKS:
                    handleBuyingSharks();
                    break;
                case WALKING_TO_BANK:
                    handleWalkingToBank();
                    break;
                case BANKING_SHARKS:
                    handleBankingSharks();
                    break;
                case HOPPING_WORLD:
                    handleHoppingWorld();
                    break;
                case STOPPED:
                    // Handled by loop condition
                    break;
            }
            sleep(100, 200);
        }
        if (!executorService.isShutdown()) {
             executorService.shutdownNow();
        }
    }
    
    /**
     * Checks if we have stamina potion and uses it if stamina effect is not active
     */
    private void checkAndUseStaminaPotion() {
        if (!config.useStaminaPotions() || !hasStaminaPotion) {
            return;
        }
        
        // Check if we already have stamina effect active
        boolean staminaActive = Microbot.getClient().getVarpValue(STAMINA_EFFECT_VARP) > 0;
        if (staminaActive) {
            return;
        }
        
        // Find stamina potion in inventory and use it
        for (int potionId : STAMINA_POTION_IDS) {
            if (Rs2Inventory.hasItem(potionId)) {
                Microbot.log("Using stamina potion");
                Rs2Inventory.interact(potionId, "Drink");
                sleep(600, 800);
                hasStaminaPotion = true;
                return;
            }
        }
        
        // If we reached here, we don't have a stamina potion anymore
        hasStaminaPotion = false;
    }

    private void handleCheckInitialConditions() {
        Microbot.log("State: Checking initial conditions");
        if (!Rs2Inventory.hasItem(GP_ID, 1)) {
            Microbot.showMessage("GP (Coins) not found in inventory. Stopping script.");
            currentState = State.STOPPED;
            return;
        }
        
        // Check if we need to get a stamina potion at startup
        if (config.useStaminaPotions() && !hasAnyStaminaPotion()) {
            Microbot.log("Stamina potions enabled but none in inventory. Checking bank...");
            // Go to bank if not there already
            if (Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(PISCARILIUS_BANK_LOCATION) > 5) {
                Microbot.log("Not at bank, walking to bank to get stamina potion.");
                Rs2Walker.walkTo(PISCARILIUS_BANK_LOCATION);
                sleepUntil(() -> Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(PISCARILIUS_BANK_LOCATION) <= 2, 5000);
            }
            
            // Open bank to get stamina potion
            if (!Rs2Bank.isOpen()) {
                boolean opened = Rs2Bank.openBank();
                if (opened) {
                    sleepUntil(Rs2Bank::isOpen, 3000);
                    if (Rs2Bank.isOpen()) {
                        // Try to find and withdraw any stamina potion
                        boolean foundStamina = false;
                        for (int potionId : STAMINA_POTION_IDS) {
                            if (Rs2Bank.hasItem(potionId)) {
                                Rs2Bank.withdrawItem(potionId);
                                Microbot.log("Withdrew stamina potion at startup");
                                sleep(600, 800);
                                hasStaminaPotion = true;
                                foundStamina = true;
                                break;
                            }
                        }
                        if (!foundStamina) {
                            Microbot.log("No stamina potions found in bank");
                        }
                        Rs2Bank.closeBank();
                        sleep(300, 500);
                    }
                }
            }
        }
        
        // Check if we're already near Frankie
        NPC frankie = Rs2Npc.getNpc(FRANKIE_NPC_ID_CUSTOM);
        if (frankie == null) {
            frankie = Rs2Npc.getNpc(FRANKIE_NPC_NAME);
        }
        
        // If we're already at Frankie and have enough inventory space, go straight to interaction
        if (frankie != null && Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(frankie.getWorldLocation()) <= 5) {
            Microbot.log("Already at Frankie's location, checking inventory space...");
            int requiredSpace = MIN_SHARKS_TO_BUY;
            if (config.useStaminaPotions() && !hasAnyStaminaPotion()) {
                requiredSpace += 1; // Reserve one more slot for stamina potion
            }
            
            if (Rs2Inventory.getEmptySlots() >= requiredSpace) {
                Microbot.log("We have enough space (" + Rs2Inventory.getEmptySlots() + "), proceeding directly to interaction");
                currentState = State.INTERACTING_WITH_FRANKIE;
                return;
            } else {
                Microbot.log("Not enough inventory space (" + Rs2Inventory.getEmptySlots() + "), need to bank first");
                // Continue to bank checks below
            }
        }
        
        // If we're not at Frankie or need to bank first, check bank location
        if (Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(PISCARILIUS_BANK_LOCATION) > 5) {
            Microbot.log("Not at bank, walking to bank first.");
            Rs2Walker.walkTo(PISCARILIUS_BANK_LOCATION);
            sleepUntil(() -> Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(PISCARILIUS_BANK_LOCATION) <= 2, 5000);
        }

        // Calculate max buy quantity based on stamina potion settings
        final int maxBuyQuantity = config.useStaminaPotions() ? MAX_BUY_QUANTITY - 1 : MAX_BUY_QUANTITY;
        
        if (Rs2Inventory.getEmptySlots() < maxBuyQuantity) {
            Microbot.log("Not enough inventory space ("+ Rs2Inventory.getEmptySlots() +"). Banking items...");
            if (!Rs2Bank.isOpen()) {
                boolean opened = Rs2Bank.openBank();
                if (opened) {
                    sleepUntil(Rs2Bank::isOpen, 3000);
                } else {
                     Microbot.log("Failed to open bank to clear inventory. Stopping.");
                     currentState = State.STOPPED;
                     return;
                }
            }
            if (Rs2Bank.isOpen()) {
                 // Check for and withdraw stamina potion if enabled
                 if (config.useStaminaPotions() && !hasAnyStaminaPotion()) {
                     // Try to find and withdraw any stamina potion
                     boolean foundStamina = false;
                     for (int potionId : STAMINA_POTION_IDS) {
                         if (Rs2Bank.hasItem(potionId)) {
                             Rs2Bank.withdrawItem(potionId);
                             Microbot.log("Withdrew stamina potion");
                             sleep(600, 800);
                             hasStaminaPotion = true;
                             foundStamina = true;
                             break;
                         }
                     }
                     if (!foundStamina) {
                         Microbot.log("No stamina potions found in bank");
                     }
                 }
                 
                 // Deposit all except coins and stamina potion
                 if (config.useStaminaPotions() && hasAnyStaminaPotion()) {
                     Integer[] keepItems = new Integer[]{GP_ID};
                     for (int potionId : STAMINA_POTION_IDS) {
                         if (Rs2Inventory.hasItem(potionId)) {
                             keepItems = new Integer[]{GP_ID, potionId};
                             break;
                         }
                     }
                     Rs2Bank.depositAllExcept(keepItems);
                 } else {
                     Rs2Bank.depositAllExcept(GP_ID);
                 }
                 
                 sleepUntil(() -> Rs2Inventory.getEmptySlots() >= maxBuyQuantity, 2000);
                 Rs2Bank.closeBank();
                 sleep(200,300);
            }
            if (Rs2Inventory.getEmptySlots() < maxBuyQuantity - (hasAnyStaminaPotion() ? 1 : 0)) {
                 Microbot.showMessage("Failed to free up inventory space ("+ Rs2Inventory.getEmptySlots() +"). Stopping script.");
                 currentState = State.STOPPED;
                 return;
            }
        }
        currentState = State.WALKING_TO_FRANKIE_AREA;
    }
    
    /**
     * Checks if player has any stamina potion in inventory
     */
    private boolean hasAnyStaminaPotion() {
        for (int potionId : STAMINA_POTION_IDS) {
            if (Rs2Inventory.hasItem(potionId)) {
                return true;
            }
        }
        return false;
    }

    private void handleWalkingToFrankieArea() {
        Microbot.log("State: Walking to Frankie's area");

        // Check if we're already in Frankie's area
        if (Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(FRANKIE_LOCATION) <= 10) {
            Microbot.log("Already in Frankie's area, moving to find Frankie");
            currentState = State.WALKING_TO_FRANKIE;
            return;
        }

        // Path to Frankie's general area from the bank
        Rs2Walker.walkTo(FRANKIE_LOCATION);
        boolean reached = sleepUntil(() -> 
            Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(FRANKIE_LOCATION) <= 10, 
            15000);

        if (reached) {
            Microbot.log("Reached Frankie's area");
            currentState = State.WALKING_TO_FRANKIE;
        } else {
            Microbot.log("Failed to reach Frankie's area. Retrying...");
            sleep(1000, 2000); // Wait briefly before retrying
            
            // Get current distance to target
            int distance = Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(FRANKIE_LOCATION);
            Microbot.log("Current distance to Frankie's area: " + distance);
            
            // If we're somewhat close, proceed anyway
            if (distance <= 20) {
                Microbot.log("Close enough to Frankie's area, proceeding");
                currentState = State.WALKING_TO_FRANKIE;
            }
        }
    }

    private void handleWalkingToFrankie() {
        Microbot.log("State: Finding and walking to Frankie");
        NPC frankieNpc = null; 
        
        // Try to find Frankie by ID first
        frankieNpc = Rs2Npc.getNpc(FRANKIE_NPC_ID_CUSTOM);
        
        // If not found by ID, try by name
        if (frankieNpc == null) {
            frankieNpc = Rs2Npc.getNpc(FRANKIE_NPC_NAME);
            if (frankieNpc != null) {
                Microbot.log("Found Frankie by name instead of ID. Actual ID: " + frankieNpc.getId());
            }
        }
        
        // Multiple attempts to find Frankie
        for (int i = 0; i < 3 && frankieNpc == null; i++) { 
            sleep(1000, 1500);
            frankieNpc = Rs2Npc.getNpc(FRANKIE_NPC_ID_CUSTOM);
            if (frankieNpc == null) {
                frankieNpc = Rs2Npc.getNpc(FRANKIE_NPC_NAME);
            }
            Microbot.log("Frankie not found on attempt " + (i + 1) + ". Waiting briefly...");
        }

        if (frankieNpc == null) {
            // If we're in Frankie's area but can't find him, try moving closer to exact spot
            if (Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(FRANKIE_LOCATION) <= 15) {
                Microbot.log("In Frankie's area but can't find him. Walking to exact location...");
                Rs2Walker.walkTo(FRANKIE_LOCATION);
                sleepUntil(() -> 
                    Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(FRANKIE_LOCATION) <= 3, 
                    10000);
                
                // Try one more time to find him
                frankieNpc = Rs2Npc.getNpc(FRANKIE_NPC_ID_CUSTOM);
                if (frankieNpc == null) {
                    frankieNpc = Rs2Npc.getNpc(FRANKIE_NPC_NAME);
                }
                
                if (frankieNpc == null) {
                    Microbot.log("Still can't find Frankie despite being at his location. Hopping world...");
                    currentState = State.HOPPING_WORLD;
                    return;
                }
            } else {
                Microbot.log("Not close enough to Frankie's location. Walking closer...");
                Rs2Walker.walkTo(FRANKIE_LOCATION);
                sleepUntil(() -> 
                    Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(FRANKIE_LOCATION) <= 10, 
                    10000);
                currentState = State.WALKING_TO_FRANKIE;
                return;
            }
        }

        final NPC targetFrankie = frankieNpc;
        Microbot.log("Found Frankie (ID: " + targetFrankie.getId() + ") at " + targetFrankie.getWorldLocation());

        // Check if we're already close to Frankie
        if (Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(targetFrankie.getWorldLocation()) < 3) {
            Microbot.log("Already close to Frankie, moving to interaction state");
            currentState = State.INTERACTING_WITH_FRANKIE;
            return;
        }

        // Walk to Frankie
        Rs2Walker.walkTo(targetFrankie.getWorldLocation());
        boolean reached = sleepUntil(() -> 
            Rs2Shop.isOpen() || 
            Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(targetFrankie.getWorldLocation()) < 3, 
            15000);

        if (Rs2Shop.isOpen()) {
            Microbot.log("Shop opened during walk to Frankie");
            currentState = State.BUYING_SHARKS;
        } else if (reached) {
            Microbot.log("Reached Frankie, moving to interaction");
            currentState = State.INTERACTING_WITH_FRANKIE;
        } else {
            Microbot.log("Failed to reach Frankie. Distance: " + 
                Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(targetFrankie.getWorldLocation()));
            // Try again rather than getting stuck
            if (Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(targetFrankie.getWorldLocation()) < 10) {
                currentState = State.INTERACTING_WITH_FRANKIE;
            }
        }
    }

    private void handleInteractingWithFrankie() {
        Microbot.log("State: Interacting with Frankie");
        if (Rs2Shop.isOpen()) {
            currentState = State.BUYING_SHARKS;
            return;
        }
        
        // Try to find Frankie by ID first, then by name
        NPC frankie = Rs2Npc.getNpc(FRANKIE_NPC_ID_CUSTOM);
        if (frankie == null) {
            frankie = Rs2Npc.getNpc(FRANKIE_NPC_NAME);
        }
        
        if (frankie == null) {
            Microbot.log("Frankie disappeared before interaction. Retrying walk.");
            currentState = State.WALKING_TO_FRANKIE;
            return;
        }
        
        Microbot.log("Attempting to trade with Frankie (ID: " + frankie.getId() + ")");
        
        // Try direct trade interaction
        boolean traded = Rs2Npc.interact(frankie, "Trade");
        if (!traded) {
            // If direct trade fails, try menu interaction alternative
            Microbot.log("Direct trade interaction failed, trying alternative approach");
            // Method doesn't exist, replace with standard interaction after small delay
            sleep(1000, 1500);
            traded = Rs2Npc.interact(frankie, "Trade");
        }
        
        if (traded) {
            Microbot.log("Trade interaction initiated, waiting for shop to open");
            boolean shopOpened = sleepUntil(Rs2Shop::isOpen, 5000);
            if (shopOpened) {
                Microbot.log("Shop opened successfully");
                currentState = State.BUYING_SHARKS;
            } else {
                Microbot.log("Failed to open Frankie's shop after interaction.");
                // Try one more time before giving up
                sleep(500, 1000);
                if (Rs2Shop.isOpen()) {
                    currentState = State.BUYING_SHARKS;
                } else {
                    currentState = State.WALKING_TO_FRANKIE;
                }
            }
        } else {
            Microbot.log("Failed to interact with Frankie (Trade). Retrying walk to re-acquire.");
            sleep(1000, 2000);
            currentState = State.WALKING_TO_FRANKIE;
        }
    }

    private void handleBuyingSharks() {
        Microbot.log("State: Buying sharks");
        if (!Rs2Shop.isOpen()) {
            Microbot.log("Shop is not open. Re-interacting with Frankie.");
            NPC frankie = Rs2Npc.getNpc(FRANKIE_NPC_ID_CUSTOM);
            if (frankie == null) {
                frankie = Rs2Npc.getNpc(FRANKIE_NPC_NAME);
            }
            if (frankie != null && Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(frankie.getWorldLocation()) < 5) {
                 currentState = State.INTERACTING_WITH_FRANKIE;
            } else {
                 currentState = State.WALKING_TO_FRANKIE;
            }
            return;
        }

        int currentSharkStock = Rs2Shop.shopItems.stream()
                .filter(item -> item.getId() == RAW_SHARK_ID)
                .mapToInt(Rs2ItemModel::getQuantity)
                .findFirst()
                .orElse(-1);

        Microbot.log("Frankie has " + (currentSharkStock == -1 ? "N/A" : currentSharkStock) + " sharks.");

        if (currentSharkStock == -1 || currentSharkStock < MIN_SHARKS_TO_BUY) {
            Microbot.log("Not enough sharks (< " + MIN_SHARKS_TO_BUY + "). Adding delay before closing shop.");
            // Add delay before closing shop when out of stock to prevent looping too quickly
            sleep(1500, 2500);
            
            Rs2Shop.closeShop();
            
            // Add slightly longer delay after closing shop to ensure it's fully closed
            sleep(1200, 1800);
            
            // Additional verification that shop is actually closed
            if (Rs2Shop.isOpen()) {
                Microbot.log("Shop is still open! Attempting to close again...");
                Rs2Shop.closeShop();
                sleep(1000, 1500);
            }
            
            // Check if we have sharks in inventory that need banking before hopping
            if (Rs2Inventory.hasItem(RAW_SHARK_ID)) {
                Microbot.log("Have sharks in inventory, need to bank before hopping.");
                currentState = State.WALKING_TO_BANK;
            } else {
                Microbot.log("No sharks in inventory, hopping worlds directly.");
                currentState = State.HOPPING_WORLD;
            }
            return;
        }

        int freeSlots = Rs2Inventory.getEmptySlots();
        // Calculate max purchase quantity
        final int maxPurchaseQuantity = config.useStaminaPotions() && hasAnyStaminaPotion() 
                ? MAX_BUY_QUANTITY - 1 
                : MAX_BUY_QUANTITY;
        
        int numToBuy = Math.min(currentSharkStock, freeSlots);
        numToBuy = Math.min(numToBuy, maxPurchaseQuantity);
        final int finalNumToBuy = numToBuy;

        if (finalNumToBuy > 0) {
            ItemComposition sharkComposition = Microbot.getClientThread().runOnClientThreadOptional(() -> Microbot.getClient().getItemDefinition(RAW_SHARK_ID)).orElse(null);
            if (sharkComposition == null) {
                Microbot.log("Could not find item definition for Raw Shark. Cannot proceed with buying.");
                Rs2Shop.closeShop();
                currentState = State.WALKING_TO_BANK;
                return;
            }
            String sharkName = sharkComposition.getName();
            
            // Check if we should buy all (50 is enough to buy all of Frankie's stock)
            if (finalNumToBuy >= MIN_SHARKS_TO_BUY && currentSharkStock <= FRANKIE_SHOP_MAX_QUANTITY) {
                Microbot.log("Attempting to buy all " + currentSharkStock + " " + sharkName + ".");
                int sharksInInventoryBeforeBuy = Rs2Inventory.count(RAW_SHARK_ID);
                
                // Try to buy in multiple batches to get all sharks
                try {
                    // Buy using multiple calls to get all sharks
                    Microbot.log("Buying sharks in multiple batches");
                    
                    // Buy in batches of 10 and 5
                    if (currentSharkStock >= 10) {
                        Rs2Shop.buyItemOptimally(sharkName, 10);
                        sleep(500, 800);
                    }
                    
                    if (currentSharkStock >= 15) {
                        Rs2Shop.buyItemOptimally(sharkName, 10);
                        sleep(500, 800);
                    }
                    
                    if (currentSharkStock >= 20) {
                        Rs2Shop.buyItemOptimally(sharkName, 5);
                        sleep(500, 800);
                    }
                    
                    // Buy any remaining sharks
                    int remainingToBuy = Math.min(freeSlots - (Rs2Inventory.count(RAW_SHARK_ID) - sharksInInventoryBeforeBuy), 
                                               currentSharkStock - Math.min(currentSharkStock, 20));
                    if (remainingToBuy > 0) {
                        Rs2Shop.buyItemOptimally(sharkName, remainingToBuy);
                    }
                } catch (Exception e) {
                    Microbot.log("Exception while buying sharks: " + e.getMessage());
                    // Fallback to standard buying
                    Rs2Shop.buyItemOptimally(sharkName, finalNumToBuy);
                }
                
                sleep(500, 1000); 
                
                boolean purchaseVerified = sleepUntil(() -> Rs2Inventory.count(RAW_SHARK_ID) >= sharksInInventoryBeforeBuy + MIN_SHARKS_TO_BUY, 5000 + (currentSharkStock * 300));
                
                int sharksBoughtThisTrip = Rs2Inventory.count(RAW_SHARK_ID) - sharksInInventoryBeforeBuy;
                totalSharksBought += sharksBoughtThisTrip;
                
                if (purchaseVerified) {
                    Microbot.log("Successfully purchased sharks. New count: " + Rs2Inventory.count(RAW_SHARK_ID) + 
                        " (+" + sharksBoughtThisTrip + ")");
                } else {
                    Microbot.log("Purchase of sharks might have failed or partially completed. Expected more than: " + 
                        (sharksInInventoryBeforeBuy + MIN_SHARKS_TO_BUY) + ", Got: " + Rs2Inventory.count(RAW_SHARK_ID) +
                        " (+" + sharksBoughtThisTrip + ")");
                }
            } else {
                Microbot.log("Attempting to buy " + finalNumToBuy + " " + sharkName + " using optimal buy.");
                int sharksInInventoryBeforeBuy = Rs2Inventory.count(RAW_SHARK_ID);
                Rs2Shop.buyItemOptimally(sharkName, finalNumToBuy);
                
                sleep(500, 1000);
                
                boolean purchaseVerified = sleepUntil(() -> Rs2Inventory.count(RAW_SHARK_ID) >= sharksInInventoryBeforeBuy + finalNumToBuy, 5000 + (finalNumToBuy * 300));
                
                int sharksBoughtThisTrip = Rs2Inventory.count(RAW_SHARK_ID) - sharksInInventoryBeforeBuy;
                totalSharksBought += sharksBoughtThisTrip;
                
                if (purchaseVerified) {
                    Microbot.log("Successfully purchased sharks. New count: " + Rs2Inventory.count(RAW_SHARK_ID));
                } else {
                    Microbot.log("Purchase of sharks might have failed or partially completed. Expected at least: " + 
                        (sharksInInventoryBeforeBuy + finalNumToBuy) + ", Got: " + Rs2Inventory.count(RAW_SHARK_ID));
                }
            }
        } else {
            Microbot.log("No space or no sharks to buy that meet criteria (numToBuy: " + finalNumToBuy + ", stock: " + currentSharkStock + ", freeSlots: " + freeSlots + ").");
        }

        Rs2Shop.closeShop();
        currentState = State.WALKING_TO_BANK;
    }

    private void handleWalkingToBank() {
        Microbot.log("State: Walking to bank");
        
        // Check if we're already at the bank
        if (Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(PISCARILIUS_BANK_LOCATION) < 3) {
            Microbot.log("Already at the bank, proceeding to banking state");
            currentState = State.BANKING_SHARKS;
            return;
        }
        
        Rs2Walker.walkTo(PISCARILIUS_BANK_LOCATION);
        sleepUntil(() -> Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(PISCARILIUS_BANK_LOCATION) < 3, 10000);
        
        if (Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(PISCARILIUS_BANK_LOCATION) < 3) {
            currentState = State.BANKING_SHARKS;
        } else {
            Microbot.log("Failed to reach bank. Retrying walk.");
        }
    }

    private void handleBankingSharks() {
        Microbot.log("State: Banking sharks");
        if (!Rs2Bank.isOpen()) {
            boolean bankOpened = Rs2Bank.openBank();
            if (bankOpened) {
                sleepUntil(Rs2Bank::isOpen, 5000);
            } else {
                Microbot.log("Failed to open bank. Retrying.");
                return; 
            }
        }

        if (Rs2Bank.isOpen()) {
            // Handle stamina potion checking if enabled
            if (config.useStaminaPotions() && !hasAnyStaminaPotion()) {
                // Try to find and withdraw any stamina potion
                for (int potionId : STAMINA_POTION_IDS) {
                    if (Rs2Bank.hasItem(potionId)) {
                        Rs2Bank.withdrawItem(potionId);
                        Microbot.log("Withdrew stamina potion");
                        sleep(600, 800);
                        hasStaminaPotion = true;
                        break;
                    }
                }
            }
            
            // Bank sharks but keep stamina potion if needed
            if (Rs2Inventory.hasItem(RAW_SHARK_ID)) {
                if (config.useStaminaPotions() && hasAnyStaminaPotion()) {
                    // Keep stamina potion if we have one
                    Integer staminaPotionId = null;
                    for (int potionId : STAMINA_POTION_IDS) {
                        if (Rs2Inventory.hasItem(potionId)) {
                            staminaPotionId = potionId;
                            break;
                        }
                    }
                    
                    if (staminaPotionId != null) {
                        Integer[] keepItems = {GP_ID, staminaPotionId};
                        Rs2Bank.depositAllExcept(keepItems);
                    } else {
                        Rs2Bank.depositAllExcept(GP_ID);
                    }
                } else {
                    Rs2Bank.depositAll(RAW_SHARK_ID);
                }
                sleepUntil(() -> !Rs2Inventory.hasItem(RAW_SHARK_ID), 3000);
            } else {
                Microbot.log("No sharks in inventory to bank.");
            }
            Rs2Bank.closeBank();
            Microbot.log("Sharks banked (or no sharks to bank).");
            currentState = State.HOPPING_WORLD;
        } else {
             Microbot.log("Bank is not open after attempting to open. Retrying.");
        }
    }

    private void handleHoppingWorld() {
        Microbot.log("State: Hopping world");

        int nextWorld = Login.getNextWorld(true, null);
        boolean hopInitiated = false;

        if (nextWorld != -1 && nextWorld != Microbot.getClient().getWorld()) {
            Microbot.hopToWorld(nextWorld);
            hopInitiated = true;
        } else if (nextWorld == -1) {
            Microbot.log("Could not find a suitable P2P world to hop to (getNextWorld returned -1).");
        } else {
            Microbot.log("Already on the world that would be hopped to, or no other P2P world found. Retrying logic or stopping if stuck.");
        }
        
        if(hopInitiated) {
            Microbot.log("World hop initiated to world " + nextWorld + ". Waiting for login...");
            sleepUntil(() -> Microbot.getClient().getLoginIndex() > 0, 8000);
            sleep(2000,3000);
            sleepUntil(Microbot::isLoggedIn, 15000);
             if(Microbot.isLoggedIn()){
                 Microbot.log("Successfully hopped worlds and logged in.");
                 
                 // Check if we've previously interacted with Frankie in this session
                 NPC frankie = Rs2Npc.getNpc(FRANKIE_NPC_ID_CUSTOM);
                 if (frankie == null) {
                     frankie = Rs2Npc.getNpc(FRANKIE_NPC_NAME);
                 }
                 
                 // If we're already near Frankie, go directly to interact with him
                 if (frankie != null && Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(frankie.getWorldLocation()) <= 10) {
                     Microbot.log("Already near Frankie after world hop, going directly to interaction");
                     currentState = State.WALKING_TO_FRANKIE;
                 } else {
                     currentState = State.CHECK_INITIAL_CONDITIONS;
                 }
             } else {
                 Microbot.log("Failed to log in after hopping. Stopping.");
                 currentState = State.STOPPED;
             }
        } else {
            sleep(2000, 3000);
        }
    }
} 