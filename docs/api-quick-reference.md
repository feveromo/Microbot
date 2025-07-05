# Microbot API Quick Reference

## Most Common Operations

### Player
```java
// Health
Rs2Player.getHealthPercentage()
Rs2Player.eatAt(50)
Rs2Player.isFullHealth()

// Movement
Rs2Player.isMoving()
Rs2Player.waitForWalking()
Rs2Player.isAnimating()
Rs2Player.waitForAnimation()

// Location
Rs2Player.getWorldLocation()
Rs2Player.distanceTo(worldPoint)

// Combat
Rs2Player.isInCombat()
Rs2Player.attack(npcOrPlayer)

// Status
Rs2Player.isRunEnabled()
Rs2Player.toggleRunEnergy(true)
```

### Inventory
```java
Rs2Inventory.contains("item")
Rs2Inventory.count("item")
Rs2Inventory.isFull()
Rs2Inventory.isEmpty()
Rs2Inventory.use("item")
Rs2Inventory.drop("item")
Rs2Inventory.dropAll("item")
Rs2Inventory.combine("item1", "item2")
```

### Bank
```java
Rs2Bank.openBank()
Rs2Bank.isOpen()
Rs2Bank.depositAll()
Rs2Bank.depositAllExcept("item1", "item2")
Rs2Bank.withdrawX("item", amount)
Rs2Bank.closeBank()
```

### NPCs
```java
Rs2Npc.getNpc("name")
Rs2Npc.attack("name")
Rs2Npc.interact("name", "action")
Rs2Npc.exists("name")
```

### Objects
```java
Rs2GameObject.findGameObject("name")
Rs2GameObject.interact("name", "action")
Rs2GameObject.exists("name")
```

### Walking
```java
Rs2Walker.walkTo(worldPoint)
Rs2Walker.walkToArea(worldArea)
Rs2Walker.isAtLocation(area)
```

### Prayer
```java
Rs2Prayer.toggle(Prayer.PROTECT_FROM_MELEE)
Rs2Prayer.isActive(Prayer.PIETY)
Rs2Prayer.toggleQuickPrayer(true)
```

### Combat
```java
Rs2Combat.toggleSpecialAttack()
Rs2Combat.getSpecialAttackEnergy()
Rs2Combat.setAttackStyle("Aggressive")
```

### Ground Items
```java
Rs2GroundItem.take("item")
Rs2GroundItem.lootItemsAboveValue(1000)
Rs2GroundItem.get("item")
```

## Sleep Functions
```java
sleep(1000)                              // Sleep 1 second
sleep(600, 1200)                         // Random sleep 600-1200ms
sleepUntil(() -> condition, 5000)        // Sleep until condition or 5s
sleepUntilTrue(() -> condition, 100, 5000) // Check every 100ms for 5s max
```

## Script Template
```java
public class MyScript extends Script {
    public static double version = 1.0;

    public boolean run(MyConfig config) {
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!super.run()) return;
            try {
                // Your logic here
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }
}
```

## Common Patterns

### Safe Object Interaction
```java
GameObject obj = Rs2GameObject.findGameObject("Tree");
if (obj != null) {
    Rs2GameObject.interact(obj, "Chop down");
}
```

### Banking Routine
```java
if (!Rs2Bank.isOpen()) {
    Rs2Bank.openBank();
} else {
    Rs2Bank.depositAll();
    Rs2Bank.withdrawX("Food", 10);
    Rs2Bank.closeBank();
}
```

### Combat Loop
```java
if (Rs2Player.getHealthPercentage() < 50) {
    Rs2Player.eatAt(50);
} else if (!Rs2Player.isInCombat()) {
    Rs2Npc.attack("Monster");
}
```

### State Machine
```java
enum State { MINING, BANKING, WALKING }

State getState() {
    if (Rs2Inventory.isFull()) return State.BANKING;
    if (!Rs2GameObject.exists("Ore")) return State.WALKING;
    return State.MINING;
}
```