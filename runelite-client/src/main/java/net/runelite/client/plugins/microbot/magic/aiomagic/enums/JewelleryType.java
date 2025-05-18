package net.runelite.client.plugins.microbot.magic.aiomagic.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.ItemID;

@Getter
@RequiredArgsConstructor
public enum JewelleryType {
    // Level 1 enchantments (Sapphire/Opal)
    SAPPHIRE_RING(ItemID.SAPPHIRE_RING, ItemID.RING_OF_RECOIL, 1),
    SAPPHIRE_NECKLACE(ItemID.SAPPHIRE_NECKLACE, ItemID.GAMES_NECKLACE8, 1),
    SAPPHIRE_AMULET(ItemID.SAPPHIRE_AMULET, ItemID.AMULET_OF_MAGIC, 1),
    SAPPHIRE_BRACELET(ItemID.SAPPHIRE_BRACELET, ItemID.BRACELET_OF_CLAY, 1),
    OPAL_RING(ItemID.OPAL_RING, ItemID.RING_OF_PURSUIT, 1),
    OPAL_NECKLACE(ItemID.OPAL_NECKLACE, ItemID.DODGY_NECKLACE, 1),
    OPAL_BRACELET(ItemID.OPAL_BRACELET, 21000, 1),
    
    // Level 2 enchantments (Emerald/Jade)
    EMERALD_RING(ItemID.EMERALD_RING, ItemID.RING_OF_DUELING8, 2),
    EMERALD_NECKLACE(ItemID.EMERALD_NECKLACE, ItemID.BINDING_NECKLACE, 2),
    EMERALD_AMULET(ItemID.EMERALD_AMULET, ItemID.AMULET_OF_DEFENCE, 2),
    EMERALD_BRACELET(ItemID.EMERALD_BRACELET, ItemID.CASTLE_WARS_BRACELET3, 2),
    JADE_RING(ItemID.JADE_RING, ItemID.RING_OF_RETURNING5, 2),
    JADE_NECKLACE(ItemID.JADE_NECKLACE, ItemID.NECKLACE_OF_PASSAGE5, 2),
    JADE_BRACELET(ItemID.JADE_BRACELET, 21001, 2),
    
    // Level 3 enchantments (Ruby/Topaz)
    RUBY_RING(ItemID.RUBY_RING, ItemID.RING_OF_FORGING, 3),
    RUBY_NECKLACE(ItemID.RUBY_NECKLACE, ItemID.DIGSITE_PENDANT_5, 3),
    RUBY_AMULET(ItemID.RUBY_AMULET, ItemID.AMULET_OF_STRENGTH, 3),
    RUBY_BRACELET(ItemID.RUBY_BRACELET, ItemID.INOCULATION_BRACELET, 3),
    TOPAZ_RING(ItemID.TOPAZ_RING, ItemID.RING_OF_VISIBILITY, 3),
    TOPAZ_NECKLACE(ItemID.TOPAZ_NECKLACE, ItemID.BURNING_AMULET5, 3),
    TOPAZ_BRACELET(ItemID.TOPAZ_BRACELET, ItemID.BRACELET_OF_SLAUGHTER, 3),
    
    // Level 4 enchantments (Diamond)
    DIAMOND_RING(ItemID.DIAMOND_RING, ItemID.RING_OF_LIFE, 4),
    DIAMOND_NECKLACE(ItemID.DIAMOND_NECKLACE, ItemID.PHOENIX_NECKLACE, 4),
    DIAMOND_AMULET(ItemID.DIAMOND_AMULET, ItemID.AMULET_OF_POWER, 4),
    DIAMOND_BRACELET(ItemID.DIAMOND_BRACELET, ItemID.EXPEDITIOUS_BRACELET, 4),
    
    // Level 5 enchantments (Dragonstone)
    DRAGONSTONE_RING(ItemID.DRAGONSTONE_RING, ItemID.RING_OF_WEALTH_5, 5),
    DRAGONSTONE_NECKLACE(ItemID.DRAGONSTONE, ItemID.SKILLS_NECKLACE5, 5),
    DRAGONSTONE_AMULET(ItemID.DRAGONSTONE_AMULET, ItemID.AMULET_OF_GLORY5, 5),
    DRAGONSTONE_BRACELET(ItemID.DRAGONSTONE_BRACELET, ItemID.COMBAT_BRACELET5, 5),
    
    // Level 6 enchantments (Onyx)
    ONYX_RING(ItemID.ONYX_RING, ItemID.RING_OF_STONE, 6),
    ONYX_NECKLACE(ItemID.ONYX_NECKLACE, ItemID.BERSERKER_NECKLACE, 6),
    ONYX_AMULET(ItemID.ONYX_AMULET, ItemID.AMULET_OF_FURY, 6),
    ONYX_BRACELET(ItemID.ONYX_BRACELET, ItemID.REGEN_BRACELET, 6),
    
    // Level 7 enchantments (Zenyte)
    ZENYTE_RING(ItemID.ZENYTE_RING, ItemID.RING_OF_SUFFERING, 7),
    ZENYTE_NECKLACE(ItemID.ZENYTE_NECKLACE, ItemID.NECKLACE_OF_ANGUISH, 7),
    ZENYTE_AMULET(ItemID.ZENYTE_AMULET, ItemID.AMULET_OF_TORTURE, 7),
    ZENYTE_BRACELET(ItemID.ZENYTE_BRACELET, ItemID.TORMENTED_BRACELET, 7);
    
    private final int unenchantedItemId;
    private final int enchantedItemId;
    private final int enchantLevel;
    
    @Override
    public String toString() {
        String itemName = name().replace('_', ' ').toLowerCase();
        return itemName.substring(0, 1).toUpperCase() + itemName.substring(1);
    }
    
    public static JewelleryType getByItemId(int itemId) {
        for (JewelleryType jewellery : values()) {
            if (jewellery.getUnenchantedItemId() == itemId) {
                return jewellery;
            }
        }
        return null;
    }
} 