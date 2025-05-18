package net.runelite.client.plugins.microbot.magic.aiomagic.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.client.plugins.microbot.util.magic.Rs2Spells;

@Getter
@RequiredArgsConstructor
public enum EnchantmentSpell {
    LVL_1_ENCHANT(Rs2Spells.ENCHANT_SAPPHIRE_JEWELLERY, "Sapphire", "Opal", 1, 7),
    LVL_2_ENCHANT(Rs2Spells.ENCHANT_EMERALD_JEWELLERY, "Emerald", "Jade", 2, 27),
    LVL_3_ENCHANT(Rs2Spells.ENCHANT_RUBY_JEWELLERY, "Ruby", "Topaz", 3, 49),
    LVL_4_ENCHANT(Rs2Spells.ENCHANT_DIAMOND_JEWELLERY, "Diamond", null, 4, 57),
    LVL_5_ENCHANT(Rs2Spells.ENCHANT_DRAGONSTONE_JEWELLERY, "Dragonstone", null, 5, 68),
    LVL_6_ENCHANT(Rs2Spells.ENCHANT_ONYX_JEWELLERY, "Onyx", null, 6, 87),
    LVL_7_ENCHANT(Rs2Spells.ENCHANT_ZENYTE_JEWELLERY, "Zenyte", null, 7, 93);
    
    private final Rs2Spells rs2Spell;
    private final String primaryGemName;
    private final String secondaryGemName;
    private final int level;
    private final int magicLevelRequired;
    
    @Override
    public String toString() {
        String displayName = rs2Spell != null ? rs2Spell.getName() : "None";
        String gems = secondaryGemName != null ? 
            primaryGemName + "/" + secondaryGemName : 
            primaryGemName;
        
        return displayName + " (" + gems + ")";
    }
} 