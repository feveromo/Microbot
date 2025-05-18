package net.runelite.client.plugins.microbot.magic.aiomagic.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JewellerySelectionType {
    NONE("None", "No specific jewelry type selected"),
    
    // Rings
    RINGS_ALL("All Rings", "Enchant all ring types for current level"),
    RING_OPAL("Opal Ring", "Ring of Pursuit", 1),
    RING_SAPPHIRE("Sapphire Ring", "Ring of Recoil", 1),
    RING_JADE("Jade Ring", "Ring of Returning", 2),
    RING_EMERALD("Emerald Ring", "Ring of Dueling", 2),
    RING_TOPAZ("Topaz Ring", "Ring of Visibility", 3),
    RING_RUBY("Ruby Ring", "Ring of Forging", 3),
    RING_DIAMOND("Diamond Ring", "Ring of Life", 4),
    RING_DRAGONSTONE("Dragonstone Ring", "Ring of Wealth", 5),
    RING_ONYX("Onyx Ring", "Ring of Stone", 6),
    RING_ZENYTE("Zenyte Ring", "Ring of Suffering", 7),
    
    // Necklaces
    NECKLACES_ALL("All Necklaces", "Enchant all necklace types for current level"),
    NECKLACE_OPAL("Opal Necklace", "Dodgy Necklace", 1),
    NECKLACE_SAPPHIRE("Sapphire Necklace", "Games Necklace", 1),
    NECKLACE_JADE("Jade Necklace", "Necklace of Passage", 2),
    NECKLACE_EMERALD("Emerald Necklace", "Binding Necklace", 2),
    NECKLACE_TOPAZ("Topaz Necklace", "Burning Amulet", 3),
    NECKLACE_RUBY("Ruby Necklace", "Digsite Pendant", 3),
    NECKLACE_DIAMOND("Diamond Necklace", "Phoenix Necklace", 4),
    NECKLACE_DRAGONSTONE("Dragonstone Necklace", "Skills Necklace", 5),
    NECKLACE_ONYX("Onyx Necklace", "Berserker Necklace", 6),
    NECKLACE_ZENYTE("Zenyte Necklace", "Necklace of Anguish", 7),
    
    // Amulets
    AMULETS_ALL("All Amulets", "Enchant all amulet types for current level"),
    AMULET_OPAL("Opal Amulet", "Opal Amulet (e)", 1),
    AMULET_SAPPHIRE("Sapphire Amulet", "Amulet of Magic", 1),
    AMULET_JADE("Jade Amulet", "Jade Amulet (e)", 2),
    AMULET_EMERALD("Emerald Amulet", "Amulet of Defence", 2),
    AMULET_TOPAZ("Topaz Amulet", "Topaz Amulet (e)", 3),
    AMULET_RUBY("Ruby Amulet", "Amulet of Strength", 3),
    AMULET_DIAMOND("Diamond Amulet", "Amulet of Power", 4),
    AMULET_DRAGONSTONE("Dragonstone Amulet", "Amulet of Glory", 5),
    AMULET_ONYX("Onyx Amulet", "Amulet of Fury", 6),
    AMULET_ZENYTE("Zenyte Amulet", "Amulet of Torture", 7),
    
    // Bracelets
    BRACELETS_ALL("All Bracelets", "Enchant all bracelet types for current level"),
    BRACELET_OPAL("Opal Bracelet", "Expeditious Bracelet", 1),
    BRACELET_SAPPHIRE("Sapphire Bracelet", "Bracelet of Clay", 1),
    BRACELET_JADE("Jade Bracelet", "Flamtaer Bracelet", 2),
    BRACELET_EMERALD("Emerald Bracelet", "Castle Wars Bracelet", 2),
    BRACELET_TOPAZ("Topaz Bracelet", "Bracelet of Slaughter", 3),
    BRACELET_RUBY("Ruby Bracelet", "Inoculation Bracelet", 3),
    BRACELET_DIAMOND("Diamond Bracelet", "Abyssal Bracelet", 4),
    BRACELET_DRAGONSTONE("Dragonstone Bracelet", "Combat Bracelet", 5),
    BRACELET_ONYX("Onyx Bracelet", "Regeneration Bracelet", 6),
    BRACELET_ZENYTE("Zenyte Bracelet", "Tormented Bracelet", 7),
    
    // Groups by material
    MATERIAL_OPAL("All Opal Jewellery", "Enchant all opal jewelry", 1),
    MATERIAL_SAPPHIRE("All Sapphire Jewellery", "Enchant all sapphire jewelry", 1),
    MATERIAL_JADE("All Jade Jewellery", "Enchant all jade jewelry", 2),
    MATERIAL_EMERALD("All Emerald Jewellery", "Enchant all emerald jewelry", 2),
    MATERIAL_TOPAZ("All Topaz Jewellery", "Enchant all topaz jewelry", 3),
    MATERIAL_RUBY("All Ruby Jewellery", "Enchant all ruby jewelry", 3),
    MATERIAL_DIAMOND("All Diamond Jewellery", "Enchant all diamond jewelry", 4),
    MATERIAL_DRAGONSTONE("All Dragonstone Jewellery", "Enchant all dragonstone jewelry", 5),
    MATERIAL_ONYX("All Onyx Jewellery", "Enchant all onyx jewelry", 6),
    MATERIAL_ZENYTE("All Zenyte Jewellery", "Enchant all zenyte jewelry", 7);
    
    private final String name;
    private final String description;
    private final int enchantLevel;
    
    JewellerySelectionType(String name, String description) {
        this.name = name;
        this.description = description;
        this.enchantLevel = -1;  // For group selectors
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    public boolean isGroup() {
        return enchantLevel == -1 || name.contains("All");
    }
    
    public boolean isRing() {
        return name.contains("Ring");
    }
    
    public boolean isNecklace() {
        return name.contains("Necklace");
    }
    
    public boolean isAmulet() {
        return name.contains("Amulet");
    }
    
    public boolean isBracelet() {
        return name.contains("Bracelet");
    }
    
    public static JewellerySelectionType[] getTypesByEnchantLevel(int level) {
        return values();
    }
} 