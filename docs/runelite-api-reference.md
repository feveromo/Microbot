# RuneLite API Reference for Microbot

This document covers the most commonly used RuneLite API classes and interfaces when developing Microbot scripts.

## Client Interface

The main interface for accessing game state.

```java
Client client = Microbot.getClient();

// Player information
Player localPlayer = client.getLocalPlayer();
int combatLevel = client.getLocalCombatLevel();

// Game state
GameState gameState = client.getGameState();
boolean isLoggedIn = gameState == GameState.LOGGED_IN;

// World information
int currentWorld = client.getWorld();
int gameTick = client.getTickCount();

// Skills
int currentHp = client.getBoostedSkillLevel(Skill.HITPOINTS);
int maxHp = client.getRealSkillLevel(Skill.HITPOINTS);
int experience = client.getSkillExperience(Skill.MINING);

// Energy
int runEnergy = client.getEnergy();

// Inventory
ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
ItemContainer bank = client.getItemContainer(InventoryID.BANK);
ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);

// NPCs and Players
List<NPC> npcs = client.getNpcs();
List<Player> players = client.getPlayers();

// Scene
Scene scene = client.getScene();
Tile[][][] tiles = scene.getTiles();

// Varbits and Varps
int varbitValue = client.getVarbitValue(varbits.ANTIFIRE);
int varpValue = client.getVarpValue(VarPlayer.SPECIAL_ATTACK_PERCENT);
```

## Player and Actor Classes

### Player
```java
Player player = client.getLocalPlayer();

// Basic info
String name = player.getName();
int combatLevel = player.getCombatLevel();
PlayerComposition appearance = player.getPlayerComposition();

// Location
WorldPoint worldLocation = player.getWorldLocation();
LocalPoint localLocation = player.getLocalLocation();

// Animation and movement
int animation = player.getAnimation();
int poseAnimation = player.getPoseAnimation();
boolean isMoving = player.getPoseAnimation() != player.getIdlePoseAnimation();

// Combat
Actor interacting = player.getInteracting();
boolean isInteracting = player.isInteracting();
int health = player.getHealth();

// Overhead icons
SkullIcon skull = player.getSkullIcon();
OverheadIcon overhead = player.getOverheadIcon();
```

### NPC
```java
NPC npc = getNpc();

// Basic info
String name = npc.getName();
int id = npc.getId();
int combatLevel = npc.getCombatLevel();
NPCComposition composition = npc.getComposition();

// Status
boolean isDead = npc.isDead();
int health = npc.getHealth();

// Actions
String[] actions = composition.getActions();
boolean canAttack = Arrays.asList(actions).contains("Attack");
```

## WorldPoint and Coordinates

```java
// Create world points
WorldPoint point = new WorldPoint(3200, 3200, 0);
WorldPoint current = player.getWorldLocation();

// Distance calculations
int distance = current.distanceTo(point);
int dx = Math.abs(current.getX() - point.getX());
int dy = Math.abs(current.getY() - point.getY());

// Local to world conversion
LocalPoint localPoint = LocalPoint.fromWorld(client, worldPoint);
WorldPoint worldPoint = WorldPoint.fromLocal(client, localPoint);

// Areas
WorldArea area = new WorldArea(3090, 3240, 10, 10, 0);
boolean inArea = area.contains(worldPoint);
WorldPoint center = area.toWorldPoint();
```

## Items and Inventory

### Item
```java
Item item = inventory.getItem(slot);

// Basic info
int id = item.getId();
int quantity = item.getQuantity();

// Item definition
ItemComposition itemDef = client.getItemDefinition(id);
String name = itemDef.getName();
boolean stackable = itemDef.isStackable();
int noteId = itemDef.getNote();
boolean isNoted = noteId != -1;
```

### ItemContainer
```java
ItemContainer container = client.getItemContainer(InventoryID.INVENTORY);

// Get items
Item[] items = container.getItems();
Item item = container.getItem(slot);

// Check contents
boolean contains = container.contains(itemId);
int count = container.count(itemId);

// Find items
int slot = container.find(itemId);
```

## Game Objects

```java
// Find objects in scene
Tile tile = scene.getTiles()[plane][x][y];
GameObject[] gameObjects = tile.getGameObjects();
GroundObject groundObject = tile.getGroundObject();
WallObject wallObject = tile.getWallObject();
DecorativeObject decorObject = tile.getDecorativeObject();

// Object properties
GameObject object = gameObjects[0];
int id = object.getId();
ObjectComposition objectDef = client.getObjectDefinition(id);
String name = objectDef.getName();
String[] actions = objectDef.getActions();
WorldPoint location = object.getWorldLocation();
```

## Widgets

```java
// Get widgets
Widget widget = client.getWidget(WidgetInfo.BANK_CONTAINER);
Widget widget = client.getWidget(groupId, childId);

// Widget properties
String text = widget.getText();
boolean hidden = widget.isHidden();
Rectangle bounds = widget.getBounds();
Point canvasLocation = widget.getCanvasLocation();
int itemId = widget.getItemId();
int itemQuantity = widget.getItemQuantity();

// Widget actions
String[] actions = widget.getActions();
Widget[] children = widget.getChildren();
Widget parent = widget.getParent();

// Common widget IDs
WidgetInfo.BANK_CONTAINER
WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER
WidgetInfo.INVENTORY
WidgetInfo.MINIMAP_ORB_RUN
WidgetInfo.COMBAT_SPECIAL_ATTACK_CLICKBOX
```

## Varbits and VarPlayer

```java
// Varbits (game settings stored as bits)
int antifire = client.getVarbitValue(Varbits.ANTIFIRE);
int questProgress = client.getVarbitValue(Varbits.QUEST_THE_RESTLESS_GHOST);

// Common Varbits
Varbits.ANTIFIRE
Varbits.SUPER_ANTIFIRE
Varbits.STAMINA_EFFECT
Varbits.TELEBLOCK
Varbits.PRAYER_RAPID_HEAL
Varbits.QUICK_PRAYER

// VarPlayer (player-specific variables)
int specEnergy = client.getVarpValue(VarPlayer.SPECIAL_ATTACK_PERCENT);
int attackStyle = client.getVarpValue(VarPlayer.ATTACK_STYLE);

// Common VarPlayers
VarPlayer.SPECIAL_ATTACK_PERCENT
VarPlayer.SPECIAL_ATTACK_ENABLED
VarPlayer.PRAYER_POINTS
VarPlayer.POISON
VarPlayer.MEMBERSHIP_DAYS
```

## Animations

```java
// Common player animations
AnimationID.IDLE              // -1
AnimationID.WOODCUTTING_BRONZE // 879
AnimationID.WOODCUTTING_IRON   // 877
AnimationID.WOODCUTTING_STEEL  // 875
AnimationID.WOODCUTTING_MITHRIL // 873
AnimationID.WOODCUTTING_ADAMANT // 871
AnimationID.WOODCUTTING_RUNE   // 869
AnimationID.WOODCUTTING_DRAGON // 2846
AnimationID.MINING_BRONZE_PICKAXE // 625
AnimationID.FISHING_POLE       // 623
AnimationID.COOKING_FIRE       // 897
AnimationID.FLETCHING_BOW_CUTTING // 6702

// Check animation
boolean isWoodcutting = player.getAnimation() == AnimationID.WOODCUTTING_RUNE;
```

## Prayers

```java
// Prayer enum values
Prayer.PROTECT_FROM_MAGIC
Prayer.PROTECT_FROM_MISSILES
Prayer.PROTECT_FROM_MELEE
Prayer.EAGLE_EYE
Prayer.RIGOUR
Prayer.PIETY
Prayer.AUGURY

// Prayer usage (through Rs2Prayer utility)
Rs2Prayer.toggle(Prayer.PROTECT_FROM_MELEE);
```

## Skills

```java
// Skill enum values
Skill.ATTACK
Skill.DEFENCE
Skill.STRENGTH
Skill.HITPOINTS
Skill.RANGED
Skill.PRAYER
Skill.MAGIC
Skill.COOKING
Skill.WOODCUTTING
Skill.FLETCHING
Skill.FISHING
Skill.FIREMAKING
Skill.CRAFTING
Skill.SMITHING
Skill.MINING
Skill.HERBLORE
Skill.AGILITY
Skill.THIEVING
Skill.SLAYER
Skill.FARMING
Skill.RUNECRAFT
Skill.HUNTER
Skill.CONSTRUCTION

// Get skill info
int level = client.getRealSkillLevel(Skill.MINING);
int boosted = client.getBoostedSkillLevel(Skill.MINING);
int xp = client.getSkillExperience(Skill.MINING);
```

## Menu Entries

```java
// Create menu entry
MenuEntry entry = new MenuEntry(
    "Attack",           // option
    "Guard",           // target
    123,               // identifier (NPC id)
    MenuAction.NPC_SECOND_OPTION.getId(),
    0,                 // param0
    0                  // param1
);

// Common MenuActions
MenuAction.WALK
MenuAction.ITEM_USE_ON_GAME_OBJECT
MenuAction.GAME_OBJECT_FIRST_OPTION
MenuAction.GAME_OBJECT_SECOND_OPTION
MenuAction.NPC_FIRST_OPTION
MenuAction.NPC_SECOND_OPTION
MenuAction.GROUND_ITEM_THIRD_OPTION
MenuAction.WIDGET_TARGET
```

## Events

Common events to subscribe to in plugins:

```java
@Subscribe
public void onGameTick(GameTick event) {
    // Called every game tick (600ms)
}

@Subscribe
public void onMenuOptionClicked(MenuOptionClicked event) {
    // Called when a menu option is clicked
}

@Subscribe
public void onChatMessage(ChatMessage event) {
    // Called when a chat message is received
    String message = event.getMessage();
    ChatMessageType type = event.getType();
}

@Subscribe
public void onItemContainerChanged(ItemContainerChanged event) {
    // Called when an item container changes
    int containerId = event.getContainerId();
    ItemContainer container = event.getItemContainer();
}

@Subscribe
public void onAnimationChanged(AnimationChanged event) {
    // Called when an actor's animation changes
    Actor actor = event.getActor();
    int animation = actor.getAnimation();
}

@Subscribe
public void onGameStateChanged(GameStateChanged event) {
    // Called when game state changes
    GameState newState = event.getGameState();
}

@Subscribe
public void onVarbitChanged(VarbitChanged event) {
    // Called when a varbit value changes
    int varbitId = event.getVarbitId();
    int value = event.getValue();
}
```

## Utility Constants

### InventoryID
```java
InventoryID.INVENTORY
InventoryID.EQUIPMENT
InventoryID.BANK
InventoryID.PUZZLE_BOX
InventoryID.BARROWS_PUZZLE
InventoryID.MONKEY_MADNESS_PUZZLE
InventoryID.GRAND_EXCHANGE_OFFER
```

### EquipmentInventorySlot
```java
EquipmentInventorySlot.HEAD
EquipmentInventorySlot.CAPE
EquipmentInventorySlot.AMULET
EquipmentInventorySlot.WEAPON
EquipmentInventorySlot.BODY
EquipmentInventorySlot.SHIELD
EquipmentInventorySlot.LEGS
EquipmentInventorySlot.GLOVES
EquipmentInventorySlot.BOOTS
EquipmentInventorySlot.RING
EquipmentInventorySlot.AMMO
```

### ChatMessageType
```java
ChatMessageType.GAMEMESSAGE
ChatMessageType.PUBLICCHAT
ChatMessageType.PRIVATECHAT
ChatMessageType.FRIENDSCHAT
ChatMessageType.BROADCAST
ChatMessageType.SPAM
```

## Best Practices

1. **Null Checks**: Always check for null when accessing game objects
```java
Player player = client.getLocalPlayer();
if (player != null) {
    // Safe to use player
}
```

2. **Client Thread**: Game state should only be accessed on the client thread
```java
Microbot.getClientThread().runOnClientThread(() -> {
    // Access game state here
});
```

3. **Caching**: Cache expensive lookups when possible
```java
// Bad - looks up definition every time
for (Item item : items) {
    String name = client.getItemDefinition(item.getId()).getName();
}

// Good - cache the definition
ItemComposition def = client.getItemDefinition(itemId);
String name = def.getName();
```

4. **Constants**: Use provided constants instead of magic numbers
```java
// Bad
if (animation == 829) { }

// Good
if (animation == AnimationID.FISHING_POLE) { }
```

This reference covers the most commonly used RuneLite API elements. For complete API documentation, refer to the [RuneLite API JavaDocs](https://runelite.net/api/).