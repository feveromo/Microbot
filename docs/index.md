# Microbot Documentation

Welcome to the Microbot documentation! Microbot is an open-source automation framework for Old School RuneScape, built on top of the RuneLite client. This documentation will help you get started with creating your own scripts and understanding the API.

## Quick Links

- [**Comprehensive API Documentation**](api-documentation.md) - Detailed documentation of all APIs with examples
- [**Quick Reference Guide**](api-quick-reference.md) - Quick lookup for common operations
- [**RuneLite API Reference**](runelite-api-reference.md) - Core RuneLite APIs used in Microbot

## Getting Started

### Prerequisites

- Java JDK 11 or higher
- Maven
- IntelliJ IDEA (recommended)
- Basic knowledge of Java programming

### Installation

For detailed installation instructions:
- **Non-Jagex Accounts**: Watch this [setup video](https://www.youtube.com/watch?v=EbtdZnxq5iw)
- **Jagex Accounts**: Follow the [RuneLite Jagex Account guide](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts)

### Your First Script

Here's a simple example to get you started:

```java
public class MyFirstScript extends Script {
    public static double version = 1.0;

    public boolean run(MyConfig config) {
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!super.run()) return;
            
            try {
                // Find and chop a tree
                if (!Rs2Player.isAnimating()) {
                    GameObject tree = Rs2GameObject.findGameObject("Tree");
                    if (tree != null) {
                        Rs2GameObject.interact(tree, "Chop down");
                        Rs2Player.waitForAnimation();
                    }
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
        return true;
    }
}
```

## Key Concepts

### Utility Classes (Rs2 Prefix)

All Microbot utility classes are prefixed with `Rs2` for easy identification:

- `Rs2Player` - Player interactions and status
- `Rs2Inventory` - Inventory management
- `Rs2Bank` - Banking operations
- `Rs2Npc` - NPC interactions
- `Rs2GameObject` - Game object interactions
- `Rs2Walker` - Advanced pathfinding
- And many more...

### Script Components

Every script consists of four main components:

1. **Script** - Contains your main logic
2. **Config** - Configuration interface for script settings
3. **Plugin** - Manages script lifecycle
4. **Overlay** (optional) - Visual overlay for displaying information

### Threading and Execution

Scripts run on scheduled threads with configurable intervals:

```java
// Run every 600ms
scheduledExecutorService.scheduleWithFixedDelay(() -> {
    // Your script logic
}, 0, 600, TimeUnit.MILLISECONDS);
```

## Common Use Cases

### Skilling Scripts
- Woodcutting, Mining, Fishing
- Crafting, Fletching, Smithing
- Cooking, Firemaking

### Combat Scripts
- Monster killing with looting
- Prayer flicking
- Special attack management
- Safe spotting

### Money Making Scripts
- Grand Exchange flipping
- Resource gathering
- Processing items

### Utility Scripts
- Bank organizing
- Quest helpers
- Construction training

## Best Practices

1. **Always use null checks** when interacting with game objects
2. **Use conditional sleeps** instead of fixed delays
3. **Handle exceptions** gracefully in your main loop
4. **Clean up resources** in the shutdown method
5. **Add randomization** to avoid pattern detection
6. **Use appropriate polling intervals** based on your task

## Community and Support

- **Discord**: [Join our Discord server](https://discord.gg/zaGrfqFEWE)
- **AI Assistant**: [Microbot GPT](https://chatgpt.com/g/g-LM0fGeeXB-microbot-documentation)
- **GitHub**: [GitHub Repository](https://github.com/chsami/microbot)

## Contributing

We welcome contributions! When contributing:
- Follow the Rs2 naming convention for utilities
- Include comprehensive documentation
- Add examples for new features
- Write unit tests where applicable

## License

Microbot is licensed under the BSD 2-clause license, same as RuneLite.

---

Ready to start scripting? Check out the [API Documentation](api-documentation.md) for detailed information on all available APIs!