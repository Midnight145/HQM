package hardcorequesting;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import hardcorequesting.items.ModItems;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class HQMTab extends CreativeTabs {

    public HQMTab() {
        super("hqm");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ItemStack getIconItemStack() {
        // the Icon of the creative Tab
        return new ItemStack(ModItems.book, 1, 0);
    }

    @Override
    public Item getTabIconItem() {
        return null;
    }
}
