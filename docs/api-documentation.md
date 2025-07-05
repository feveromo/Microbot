# Microbot API Documentation

## Table of Contents

1. [Introduction](#introduction)
2. [Getting Started](#getting-started)
3. [Core Components](#core-components)
4. [Utility Classes](#utility-classes)
5. [Script Framework](#script-framework)
6. [Examples](#examples)
7. [Best Practices](#best-practices)

## Introduction

Microbot is an open-source automation framework for Old School RuneScape, built on top of RuneLite. It provides a comprehensive API for creating scripts using a plugin-based system.

## Getting Started

### Prerequisites

- Java JDK 11 or higher
- Maven
- IntelliJ IDEA (recommended)

### Basic Setup

1. Clone the repository
2. Import the project into IntelliJ IDEA as a Maven project
3. Follow the building guide at: https://github.com/chsami/microbot/wiki/Building-with-IntelliJ-IDEA

### Creating Your First Script

Every script in Microbot extends the `Script` class and follows this basic structure:

```java
public class MyScript extends Script {
    public static double version = 1.0;

    public boolean run(MyConfig config) {
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!super.run()) return;
            try {
                // Your script logic here
                
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 2000, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }
}
```

## Core Components

### Microbot Main Class

The central access point for all RuneLite functionality.

```java
// Access the RuneLite client
Client client = Microbot.getClient();

// Get local player
Player localPlayer = client.getLocalPlayer();

// Get current world location
WorldPoint location = localPlayer.getWorldLocation();

// Check login status
boolean isLoggedIn = Microbot.isLoggedIn();

// Access varbit values
int varbitValue = Microbot.getVarbitValue(Varbits.ANTIFIRE);

// Enable/disable auto run
Microbot.enableAutoRunOn = true;
```

### Client Thread Operations

Execute operations on the client thread safely:

```java
// Run on client thread with optional return
Optional<Integer> health = Microbot.getClientThread().runOnClientThreadOptional(() -> 
    Microbot.getClient().getLocalPlayer().getHealth()
);

// Schedule delayed execution
Microbot.getClientThread().runOnClientThread(() -> {
    // Code to run on client thread
}, 1000); // 1 second delay
```

## Utility Classes

All utility classes are prefixed with `Rs2` for easy identification. Here are the main utility categories:

### Rs2Player - Player Interaction Utilities

The `Rs2Player` class provides comprehensive player-related functionality.

#### Health and Status

```java
// Get health percentage
double healthPercent = Rs2Player.getHealthPercentage();

// Check if full health
boolean fullHealth = Rs2Player.isFullHealth();

// Eat food at specific health percentage
Rs2Player.eatAt(50); // Eats when health drops below 50%

// Check potion effects
boolean hasAntifire = Rs2Player.hasAntiFireActive();
boolean hasStamina = Rs2Player.hasStaminaBuffActive();
boolean hasAntiVenom = Rs2Player.hasAntiVenomActive();
boolean hasPrayer = Rs2Player.hasPrayerRegenerationActive();

// Check combat level boosts
boolean hasAttackBoost = Rs2Player.hasAttackActive(5); // 5+ boost
boolean hasStrengthBoost = Rs2Player.hasStrengthActive(5);
boolean hasMagicBoost = Rs2Player.hasMagicActive(5);
```

#### Movement and Animation

```java
// Check if moving
boolean isMoving = Rs2Player.isMoving();

// Wait for walking to complete
Rs2Player.waitForWalking();
Rs2Player.waitForWalking(5000); // Max 5 seconds

// Check animation state
boolean isAnimating = Rs2Player.isAnimating();
Rs2Player.waitForAnimation();

// Get current animation ID
int animationId = Rs2Player.getAnimation();

// Wait for XP drop
boolean gotXp = Rs2Player.waitForXpDrop(Skill.MINING, 5000);
```

#### Location and Distance

```java
// Get world location
WorldPoint worldLocation = Rs2Player.getWorldLocation();

// Get local location
LocalPoint localLocation = Rs2Player.getLocalLocation();

// Check distance to point
int distance = Rs2Player.distanceTo(targetWorldPoint);

// Check if near area
boolean isNear = Rs2Player.isNearArea(worldPoint, 10); // Within 10 tiles

// Check special locations
boolean inCave = Rs2Player.isInCave();
boolean inInstance = Rs2Player.IsInInstance();
boolean inMulti = Rs2Player.isInMulti();
```

#### Combat and PvP

```java
// Check combat status
boolean inCombat = Rs2Player.isInCombat();

// Get combat level
int combatLevel = Rs2Player.getCombatLevel();

// Attack player
Rs2PlayerModel target = Rs2Player.getPlayer("PlayerName");
Rs2Player.attack(target);

// Get players in combat range
List<Rs2PlayerModel> combatPlayers = Rs2Player.getPlayersInCombatLevelRange();

// Detect and logout if players nearby
Rs2Player.logoutIfPlayerDetected(1, 5000, 10); // 1 player, 5 seconds, 10 tiles

// Walk under player
Rs2Player.walkUnder(target);
```

#### Other Players

```java
// Get all players with filter
Stream<Rs2PlayerModel> players = Rs2Player.getPlayers(p -> 
    p.getCombatLevel() > 100
);

// Get specific player
Rs2PlayerModel player = Rs2Player.getPlayer("Username", true); // Exact match

// Check player equipment
boolean hasWeapon = Rs2Player.hasPlayerEquippedItem(player, "Dragon scimitar");

// Get player equipment map
Map<KitType, String> equipment = Rs2Player.getPlayerEquipmentNames(player);

// Calculate player health percentage
int healthPercent = Rs2Player.calculateHealthPercentage(player);
```

#### Consumables

```java
// Drink prayer potion at specific level
Rs2Player.drinkPrayerPotionAt(20);

// Drink combat potions
Rs2Player.drinkCombatPotionAt(Skill.ATTACK, true); // Super combat
Rs2Player.drinkCombatPotionAt(Skill.RANGED, false); // Regular ranging

// Drink protection potions
Rs2Player.drinkAntiPoisonPotion();
Rs2Player.drinkAntiFirePotion();
Rs2Player.drinkGoadingPotion();

// Use food
Rs2Player.useFood(); // Uses any food
Rs2Player.useFastFood(); // Uses karambwan
```

### Rs2Inventory - Inventory Management

```java
// Check inventory status
boolean isFull = Rs2Inventory.isFull();
boolean isEmpty = Rs2Inventory.isEmpty();
int emptySlots = Rs2Inventory.getEmptySlots();

// Get items
Rs2ItemModel item = Rs2Inventory.get("Shark");
List<Rs2ItemModel> allFood = Rs2Inventory.getAll(item -> 
    Rs2Food.isFood(item.getName())
);

// Use items
Rs2Inventory.use("Tinderbox");
Rs2Inventory.use("Logs");

// Drop items
Rs2Inventory.drop("Iron ore");
Rs2Inventory.dropAll("Iron ore");
Rs2Inventory.dropAllExcept("Pickaxe", "Hammer");

// Combine items
Rs2Inventory.combine("Knife", "Logs");

// Check item existence
boolean hasItem = Rs2Inventory.contains("Shark");
int sharkCount = Rs2Inventory.count("Shark");

// Use item on object/npc
Rs2Inventory.useItemOnGameObject("Bucket", "Well");
Rs2Inventory.useItemOnNpc("Raw chicken", "Fire");
```

### Rs2Bank - Banking Operations

```java
// Open bank
boolean opened = Rs2Bank.openBank();
Rs2Bank.openBankBooth();
Rs2Bank.openBankChest();

// Check bank status
boolean isOpen = Rs2Bank.isOpen();

// Deposit items
Rs2Bank.depositAll();
Rs2Bank.depositAll("Iron ore");
Rs2Bank.depositAllExcept("Pickaxe", "Hammer");

// Withdraw items
Rs2Bank.withdrawX("Shark", 10);
Rs2Bank.withdrawAll("Lobster");
Rs2Bank.withdrawAllAndEquip("Dragon scimitar");

// Search bank
boolean hasItem = Rs2Bank.contains("Shark");
int count = Rs2Bank.count("Shark");

// Bank presets
Rs2Bank.loadPreset(1);

// Close bank
Rs2Bank.closeBank();

// Banking with exact amounts
Rs2Bank.withdrawExact("Coins", 1000);
Rs2Bank.withdrawExact(Arrays.asList(
    new Rs2ItemModel("Shark", 10),
    new Rs2ItemModel("Prayer potion(4)", 5)
));
```

### Rs2GameObject - Game Object Interaction

```java
// Find game objects
Rs2GameObject object = Rs2GameObject.findGameObject("Tree");
Rs2GameObject nearest = Rs2GameObject.findGameObject("Bank booth", 10); // Within 10 tiles

// Interact with objects
Rs2GameObject.interact("Door", "Open");
Rs2GameObject.interact(12345, "Mine"); // By ID

// Get all objects with filter
List<GameObject> ores = Rs2GameObject.getGameObjects(obj -> 
    obj.getName().contains("ore")
);

// Check object existence
boolean exists = Rs2GameObject.exists("Tree");

// Get object at location
GameObject objectAtTile = Rs2GameObject.getGameObjectAtLocation(worldPoint);

// Advanced filtering
GameObject closest = Rs2GameObject.findGameObject(obj ->
    obj.getName().equals("Iron ore") && 
    obj.getWorldLocation().distanceTo(Rs2Player.getWorldLocation()) < 5
);
```

### Rs2Npc - NPC Interaction

```java
// Find NPCs
Rs2NpcModel npc = Rs2Npc.getNpc("Guard");
Rs2NpcModel nearest = Rs2Npc.getNearestNpc("Banker", 10);

// Attack NPC
Rs2Npc.attack("Guard");
Rs2Npc.attack(123); // By ID

// Interact with NPC
Rs2Npc.interact("Banker", "Bank");

// Get NPCs with filter
List<Rs2NpcModel> combatNpcs = Rs2Npc.getNpcs(npc -> 
    npc.getCombatLevel() > 50 && npc.getInteracting() == null
);

// Check NPC status
boolean exists = Rs2Npc.exists("Guard");
boolean hasAction = Rs2Npc.hasAction("Banker", "Bank");

// Talk to NPC
Rs2Npc.interact("Hans", "Talk-to");
```

### Rs2Walker - Advanced Pathfinding

```java
// Walk to location
Rs2Walker.walkTo(new WorldPoint(3200, 3200, 0));
Rs2Walker.walkFastCanvas(worldPoint);

// Walk to area
WorldArea bankArea = new WorldArea(3090, 3240, 10, 10, 0);
Rs2Walker.walkToArea(bankArea);

// Check if at location
boolean atBank = Rs2Walker.isAtLocation(bankArea);

// Set walk target
Rs2Walker.setTarget(worldPoint);

// Disable/enable teleports
Rs2Walker.disableTeleports = true;

// Web walking (uses shortest path)
Rs2Walker.walkTo(distantWorldPoint, 10); // With distance threshold

// Walk and interact
Rs2Walker.walkToAndInteract(Rs2GameObject.findGameObject("Tree"), "Chop down");
```

### Rs2Widget - Widget/Interface Interaction

```java
// Find widgets by text
Widget widget = Rs2Widget.getWidget("Click here to continue");
Rs2Widget.clickWidget("Click here to continue");

// Find by ID
Widget bankWidget = Rs2Widget.getWidget(WidgetInfo.BANK_CONTAINER);

// Check widget visibility
boolean isVisible = Rs2Widget.isWidgetVisible(widget);

// Click widget with action
Rs2Widget.clickWidget(widget, "Close");

// Get widget text
String text = Rs2Widget.getWidgetText(widget);

// Type in widget
Rs2Widget.typeInWidget(widgetId, "text to type");

// Find child widgets
Widget child = Rs2Widget.findWidget("Search", widget.getChildren());
```

### Rs2Combat - Combat Utilities

```java
// Toggle special attack
Rs2Combat.toggleSpecialAttack();
Rs2Combat.enableSpecialAttack();

// Check special attack
boolean specEnabled = Rs2Combat.isSpecialAttackEnabled();
int specEnergy = Rs2Combat.getSpecialAttackEnergy();

// Set attack style
Rs2Combat.setAttackStyle("Aggressive");

// Get current style
String style = Rs2Combat.getCurrentAttackStyle();

// Auto retaliate
Rs2Combat.toggleAutoRetaliate(true);
```

### Rs2Prayer - Prayer Management

```java
// Toggle prayers
Rs2Prayer.toggle(Prayer.PROTECT_FROM_MELEE);
Rs2Prayer.enable(Prayer.EAGLE_EYE);
Rs2Prayer.disable(Prayer.PROTECT_FROM_MAGIC);

// Check prayer status
boolean isActive = Rs2Prayer.isActive(Prayer.PIETY);

// Quick prayers
Rs2Prayer.toggleQuickPrayer(true);

// Get prayer points
int currentPrayer = Rs2Prayer.getPrayerPoints();
int maxPrayer = Rs2Prayer.getMaxPrayerPoints();

// Flick prayers
Rs2Prayer.flick(Prayer.PROTECT_FROM_MELEE, 600); // 600ms interval
```

### Rs2Equipment - Equipment Management

```java
// Check equipped items
boolean hasWeapon = Rs2Equipment.isWearing("Dragon scimitar");
boolean hasHelm = Rs2Equipment.isWearing("helm");

// Get equipped item
Rs2ItemModel weapon = Rs2Equipment.get(EquipmentInventorySlot.WEAPON);

// Equip items
Rs2Equipment.equip("Dragon scimitar");
Rs2Equipment.equipFromBank("Full helm");

// Unequip items
Rs2Equipment.unequip(EquipmentInventorySlot.WEAPON);

// Check equipment bonuses
int attackBonus = Rs2Equipment.getEquipmentBonus(EquipmentStat.ATTACK_STAB);
```

### Rs2Magic - Magic Spells

```java
// Cast spell on NPC
Rs2Magic.cast(MagicSpell.HIGH_LEVEL_ALCHEMY);
Rs2Magic.castOn(MagicSpell.TELEKINETIC_GRAB, "Wine of zamorak");

// Teleport spells
Rs2Magic.castTeleport(MagicSpell.VARROCK_TELEPORT);

// Check spell requirements
boolean canCast = Rs2Magic.canCast(MagicSpell.ICE_BARRAGE);

// Enchant items
Rs2Magic.enchant("Sapphire ring");

// Cast on player
Rs2Magic.castOn(MagicSpell.TELE_OTHER, playerModel);
```

### Rs2GrandExchange - Grand Exchange

```java
// Open GE
Rs2GrandExchange.open();

// Buy items
Rs2GrandExchange.buyItem("Shark", 100, 500); // 100 sharks at 500gp each

// Sell items
Rs2GrandExchange.sellItem("Iron ore", 1000, 50);

// Collect items
Rs2GrandExchange.collectAll();

// Check offer status
GrandExchangeOfferState state = Rs2GrandExchange.getOfferState(0); // Slot 0

// Cancel offer
Rs2GrandExchange.cancelOffer(0);
```

### Rs2Tab - Game Tabs

```java
// Switch tabs
Rs2Tab.switchToInventoryTab();
Rs2Tab.switchToPrayerTab();
Rs2Tab.switchToMagicTab();

// Get current tab
InterfaceTab currentTab = Rs2Tab.getCurrentTab();

// Check if tab is open
boolean isInventoryOpen = Rs2Tab.isOpen(InterfaceTab.INVENTORY);
```

### Rs2Camera - Camera Control

```java
// Set camera angle
Rs2Camera.turnTo(worldPoint);
Rs2Camera.turnTo(npc);
Rs2Camera.turnTo(gameObject);

// Adjust pitch
Rs2Camera.setPitch(270); // Look up

// Adjust yaw
Rs2Camera.setYaw(0); // Face north

// Reset camera
Rs2Camera.reset();

// Zoom
Rs2Camera.setZoom(500);
```

### Rs2Keyboard - Keyboard Input

```java
// Type text
Rs2Keyboard.typeString("Hello world");

// Press key
Rs2Keyboard.keyPress(KeyCode.KC_ENTER);

// Key combinations
Rs2Keyboard.keyHold(KeyCode.KC_SHIFT);
Rs2Keyboard.keyPress('A');
Rs2Keyboard.keyRelease(KeyCode.KC_SHIFT);

// Press space to continue
Rs2Keyboard.pressSpace();
```

### Rs2Mouse - Mouse Control

```java
// Click at location
Rs2Mouse.click(100, 200);
Rs2Mouse.click(point, true); // Right click

// Move mouse
Rs2Mouse.move(x, y);

// Hover over
Rs2Mouse.hover(widget);
Rs2Mouse.hover(npc);

// Get mouse position
Point mousePos = Rs2Mouse.getMousePosition();
```

### Rs2Settings - Game Settings

```java
// Toggle settings
Rs2Settings.toggleRun(true);
Rs2Settings.toggleSound(false);
Rs2Settings.toggleMusic(false);

// Brightness
Rs2Settings.setBrightness(4); // Max brightness

// Zoom
Rs2Settings.setZoom(50); // 0-100

// Camera
Rs2Settings.toggleRoofsHidden(true);
```

### Rs2Dialogue - Dialogue Handling

```java
// Check if in dialogue
boolean inDialogue = Rs2Dialogue.isInDialogue();

// Continue dialogue
Rs2Dialogue.continueDialogue();

// Select option
Rs2Dialogue.selectOption("Yes");
Rs2Dialogue.selectOption(2); // By index

// Check for specific text
boolean hasText = Rs2Dialogue.hasDialogueText("Would you like to");

// Click through all dialogues
Rs2Dialogue.clickContinue();
```

### Rs2GroundItem - Ground Item Interaction

```java
// Find ground items
Rs2GroundItem item = Rs2GroundItem.get("Coins");
Rs2GroundItem nearest = Rs2GroundItem.getNearest("Bones", 10);

// Take items
Rs2GroundItem.take("Coins");
Rs2GroundItem.take(995); // By ID

// Loot items above value
Rs2GroundItem.lootItemsAboveValue(1000);

// Get all ground items
List<Rs2GroundItem> allItems = Rs2GroundItem.getAll();

// Filter ground items
List<Rs2GroundItem> valuable = Rs2GroundItem.getAll(item -> 
    item.getGePrice() > 5000
);
```

## Script Framework

### Basic Script Structure

```java
public class MiningScript extends Script {
    public static double version = 1.0;
    
    private MiningConfig config;
    
    public boolean run(MiningConfig config) {
        this.config = config;
        Microbot.enableAutoRunOn = false;
        
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!super.run()) return;
            
            try {
                if (!Rs2Inventory.isFull()) {
                    mineOres();
                } else {
                    bankOres();
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
        
        return true;
    }
    
    private void mineOres() {
        GameObject ore = Rs2GameObject.findGameObject(config.oreType());
        if (ore != null && !Rs2Player.isAnimating()) {
            Rs2GameObject.interact(ore, "Mine");
            Rs2Player.waitForAnimation();
        }
    }
    
    private void bankOres() {
        if (!Rs2Bank.isOpen()) {
            Rs2Bank.openBank();
        } else {
            Rs2Bank.depositAll();
            Rs2Bank.closeBank();
        }
    }
    
    @Override
    public void shutdown() {
        super.shutdown();
    }
}
```

### Script Configuration

```java
@ConfigGroup("mining")
public interface MiningConfig extends Config {
    @ConfigItem(
        keyName = "oreType",
        name = "Ore Type",
        description = "Type of ore to mine"
    )
    default String oreType() {
        return "Iron ore";
    }
    
    @ConfigItem(
        keyName = "bankLocation",
        name = "Bank Location",
        description = "Where to bank"
    )
    default BankLocation bankLocation() {
        return BankLocation.VARROCK_WEST;
    }
}
```

### Script Plugin

```java
@PluginDescriptor(
    name = "Mining Script",
    description = "Mines ores and banks them",
    tags = {"mining", "skilling"},
    enabledByDefault = false
)
@Slf4j
public class MiningPlugin extends Plugin {
    @Inject
    private MiningConfig config;
    
    @Inject
    private OverlayManager overlayManager;
    
    @Inject
    private MiningOverlay miningOverlay;
    
    private MiningScript miningScript = new MiningScript();
    
    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(miningOverlay);
        }
        miningScript.run(config);
    }
    
    @Override
    protected void shutDown() {
        miningScript.shutdown();
        if (overlayManager != null) {
            overlayManager.remove(miningOverlay);
        }
    }
}
```

### Script Overlay

```java
public class MiningOverlay extends Overlay {
    private final MiningPlugin plugin;
    private final MiningScript script;
    
    @Inject
    public MiningOverlay(MiningPlugin plugin) {
        this.plugin = plugin;
        this.script = plugin.getMiningScript();
        setPosition(OverlayPosition.TOP_LEFT);
    }
    
    @Override
    public Dimension render(Graphics2D graphics) {
        PanelComponent panelComponent = new PanelComponent();
        panelComponent.setPreferredSize(new Dimension(200, 300));
        
        panelComponent.getChildren().add(TitleComponent.builder()
            .text("Mining Script v" + MiningScript.version)
            .color(Color.GREEN)
            .build());
            
        panelComponent.getChildren().add(LineComponent.builder()
            .left("Runtime:")
            .right(script.getRunTime().toString())
            .build());
            
        panelComponent.getChildren().add(LineComponent.builder()
            .left("Ores mined:")
            .right(String.valueOf(script.getOresMined()))
            .build());
            
        return panelComponent.render(graphics);
    }
}
```

## Examples

### Example 1: Simple Woodcutting Script

```java
public class WoodcuttingScript extends Script {
    
    public boolean run(WoodcuttingConfig config) {
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!super.run()) return;
            
            try {
                // Check if inventory is full
                if (Rs2Inventory.isFull()) {
                    dropLogs();
                    return;
                }
                
                // Find and chop tree
                if (!Rs2Player.isAnimating()) {
                    GameObject tree = Rs2GameObject.findGameObject("Tree");
                    if (tree != null) {
                        Rs2GameObject.interact(tree, "Chop down");
                        Rs2Player.waitForAnimation();
                    }
                }
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
        
        return true;
    }
    
    private void dropLogs() {
        Rs2Inventory.dropAll("Logs");
        sleep(600, 1200);
    }
}
```

### Example 2: Combat Script with Looting

```java
public class CombatScript extends Script {
    private static final String MONSTER_NAME = "Cow";
    private static final String[] LOOT_ITEMS = {"Cowhide", "Raw beef", "Bones"};
    
    public boolean run(CombatConfig config) {
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!super.run()) return;
            
            try {
                // Eat food if needed
                if (Rs2Player.getHealthPercentage() < 50) {
                    Rs2Player.eatAt(50);
                }
                
                // Loot items
                if (shouldLoot()) {
                    lootItems();
                    return;
                }
                
                // Attack monster
                if (!Rs2Player.isInCombat()) {
                    attackMonster();
                }
                
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
        
        return true;
    }
    
    private void attackMonster() {
        Rs2NpcModel monster = Rs2Npc.getNpc(npc -> 
            npc.getName().equals(MONSTER_NAME) && 
            !npc.isDead() && 
            npc.getInteracting() == null
        );
        
        if (monster != null) {
            Rs2Npc.attack(monster);
            sleepUntil(Rs2Player::isInCombat, 5000);
        }
    }
    
    private boolean shouldLoot() {
        return Rs2GroundItem.getAll(item -> 
            Arrays.asList(LOOT_ITEMS).contains(item.getName()) &&
            item.getLocation().distanceTo(Rs2Player.getWorldLocation()) <= 5
        ).size() > 0;
    }
    
    private void lootItems() {
        for (String item : LOOT_ITEMS) {
            Rs2GroundItem.take(item);
            sleep(600, 1200);
        }
    }
}
```

### Example 3: Banking Script

```java
public class BankingExample extends Script {
    
    public void bankingRoutine() {
        // Walk to bank
        WorldArea bankArea = new WorldArea(3092, 3242, 5, 5, 0);
        if (!Rs2Walker.isAtLocation(bankArea)) {
            Rs2Walker.walkTo(bankArea);
            sleepUntil(() -> Rs2Walker.isAtLocation(bankArea), 30000);
        }
        
        // Open bank
        if (!Rs2Bank.isOpen()) {
            Rs2Bank.openBank();
            sleepUntil(Rs2Bank::isOpen, 5000);
        }
        
        // Deposit all except essentials
        Rs2Bank.depositAllExcept("Pickaxe", "Coins");
        
        // Withdraw supplies
        Rs2Bank.withdrawX("Shark", 10);
        Rs2Bank.withdrawX("Prayer potion(4)", 5);
        
        // Close bank
        Rs2Bank.closeBank();
    }
}
```

### Example 4: Grand Exchange Trading

```java
public class GETrading extends Script {
    
    public void buySupplies() {
        // Open GE
        if (!Rs2GrandExchange.isOpen()) {
            Rs2GrandExchange.open();
            sleepUntil(Rs2GrandExchange::isOpen, 5000);
        }
        
        // Buy items
        Rs2GrandExchange.buyItem("Shark", 1000, 450);
        sleep(1000);
        
        // Wait for completion
        sleepUntil(() -> 
            Rs2GrandExchange.getOfferState(0) == GrandExchangeOfferState.BOUGHT, 
            30000
        );
        
        // Collect
        Rs2GrandExchange.collectAll();
    }
}
```

### Example 5: Prayer Flicking

```java
public class PrayerFlickingScript extends Script {
    
    public void fightWithPrayerFlicking() {
        // Enable protection prayer when under attack
        if (Rs2Player.isInCombat()) {
            // Get attacking NPC
            Actor interacting = Rs2Player.getInteracting();
            
            if (interacting instanceof NPC) {
                NPC attacker = (NPC) interacting;
                
                // Determine prayer based on combat style
                Prayer protectionPrayer = getProtectionPrayer(attacker);
                
                // Flick prayer
                if (protectionPrayer != null) {
                    Rs2Prayer.toggle(protectionPrayer);
                }
            }
        } else {
            // Disable all protection prayers when not in combat
            Rs2Prayer.disable(Prayer.PROTECT_FROM_MELEE);
            Rs2Prayer.disable(Prayer.PROTECT_FROM_MISSILES);
            Rs2Prayer.disable(Prayer.PROTECT_FROM_MAGIC);
        }
    }
    
    private Prayer getProtectionPrayer(NPC npc) {
        // Logic to determine prayer based on NPC
        String npcName = npc.getName();
        
        if (npcName.contains("mage")) {
            return Prayer.PROTECT_FROM_MAGIC;
        } else if (npcName.contains("ranger")) {
            return Prayer.PROTECT_FROM_MISSILES;
        } else {
            return Prayer.PROTECT_FROM_MELEE;
        }
    }
}
```

## Best Practices

### 1. Error Handling

Always wrap your main logic in try-catch blocks:

```java
try {
    // Your script logic
} catch (Exception ex) {
    System.out.println("Error: " + ex.getMessage());
}
```

### 2. Null Checks

Always check for null before using objects:

```java
GameObject tree = Rs2GameObject.findGameObject("Tree");
if (tree != null) {
    Rs2GameObject.interact(tree, "Chop down");
}
```

### 3. Sleep and Wait Conditions

Use conditional sleeps instead of fixed delays:

```java
// Good
sleepUntil(Rs2Bank::isOpen, 5000);

// Avoid
sleep(5000);
```

### 4. Resource Management

Always clean up resources in shutdown:

```java
@Override
public void shutdown() {
    super.shutdown();
    // Clean up any additional resources
}
```

### 5. State Management

Use state machines for complex scripts:

```java
enum State {
    MINING,
    WALKING_TO_BANK,
    BANKING,
    WALKING_TO_MINE
}

private State getCurrentState() {
    if (Rs2Inventory.isFull()) {
        if (Rs2Bank.isNear()) {
            return State.BANKING;
        } else {
            return State.WALKING_TO_BANK;
        }
    } else {
        if (Rs2GameObject.exists("Iron ore")) {
            return State.MINING;
        } else {
            return State.WALKING_TO_MINE;
        }
    }
}
```

### 6. Configuration

Make scripts configurable:

```java
@ConfigItem(
    keyName = "foodType",
    name = "Food Type",
    description = "Type of food to use"
)
default String foodType() {
    return "Shark";
}
```

### 7. Anti-Pattern Detection

Add randomization to avoid detection:

```java
// Random sleep between actions
sleep(600, 1200);

// Random mouse movements
if (Random.random(1, 10) == 1) {
    Rs2Mouse.move(Random.random(0, 765), Random.random(0, 502));
}
```

### 8. Performance

Use appropriate polling intervals:

```java
// High-frequency tasks (combat)
scheduleWithFixedDelay(task, 0, 100, TimeUnit.MILLISECONDS);

// Low-frequency tasks (banking)
scheduleWithFixedDelay(task, 0, 2000, TimeUnit.MILLISECONDS);
```

### 9. Logging

Use proper logging instead of System.out:

```java
@Slf4j
public class MyScript extends Script {
    
    public void doSomething() {
        log.info("Script started");
        log.error("Error occurred", exception);
    }
}
```

### 10. Thread Safety

Use thread-safe operations when needed:

```java
// Use concurrent collections for multi-threaded access
private final ConcurrentHashMap<String, Integer> itemCounts = new ConcurrentHashMap<>();

// Use atomic operations
private final AtomicInteger counter = new AtomicInteger(0);
```

## Additional Resources

- **Microbot GPT**: https://chatgpt.com/g/g-LM0fGeeXB-microbot-documentation
- **Discord**: https://discord.gg/zaGrfqFEWE
- **GitHub**: https://github.com/chsami/microbot
- **RuneLite Wiki**: https://github.com/runelite/runelite/wiki

## Contributing

When contributing to the API:

1. Follow the Rs2 prefix convention for utility classes
2. Document all public methods with Javadoc
3. Include examples in documentation
4. Write unit tests where applicable
5. Ensure thread safety for concurrent operations

---

This documentation covers the main public APIs and utilities available in Microbot. For specific implementation details or advanced features, refer to the source code or join the Discord community for support.