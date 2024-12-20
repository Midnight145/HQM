package com.hqm.hardcorequesting.items;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.hqm.hardcorequesting.HardcoreQuesting;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Created by Tim on 5/10/2015.
 */
public class ItemInvalid extends Item {

    public ItemInvalid() {
        super();
        this.setMaxStackSize(1);
        this.setCreativeTab(HardcoreQuesting.HQMTab);
        this.setUnlocalizedName(ItemInfo.LOCALIZATION_START + ItemInfo.INVALID_UNLOCALIZED_NAME);
    }

    @Override
    public String getUnlocalizedName(ItemStack itemStack) {
        return super.getUnlocalizedName(itemStack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        itemIcon = register.registerIcon(ItemInfo.TEXTURE_LOCATION + ":" + ItemInfo.INVALID_ICON);
    }
}
