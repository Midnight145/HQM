package com.hqm.hardcorequesting.items;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.hqm.hardcorequesting.HardcoreQuesting;
import com.hqm.hardcorequesting.bag.BagTier;
import com.hqm.hardcorequesting.bag.Group;
import com.hqm.hardcorequesting.client.sounds.SoundHandler;
import com.hqm.hardcorequesting.client.sounds.Sounds;
import com.hqm.hardcorequesting.network.DataBitHelper;
import com.hqm.hardcorequesting.network.DataWriter;
import com.hqm.hardcorequesting.network.PacketHandler;
import com.hqm.hardcorequesting.network.PacketId;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemBag extends Item {

    public static boolean displayGui;

    public ItemBag() {
        super();
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setMaxStackSize(64);
        this.setCreativeTab(HardcoreQuesting.HQMTab);
        this.setUnlocalizedName(ItemInfo.LOCALIZATION_START + ItemInfo.BAG_UNLOCALIZED_NAME);
    }

    @Override
    public void registerIcons(IIconRegister register) {
        pickIcons(register);
    }

    private void pickIcons(IIconRegister register) {
        itemIcon = register.registerIcon(ItemInfo.TEXTURE_LOCATION + ":" + ItemInfo.BAG_ICON);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addInformation(ItemStack itemstack, EntityPlayer player, List tooltip, boolean extraInfo) {
        super.addInformation(itemstack, player, tooltip, extraInfo);

        int dmg = itemstack.getItemDamage();
        if (dmg >= 0 && dmg < BagTier.values().length) {
            BagTier tier = BagTier.values()[dmg];
            tooltip.add(tier.getColor() + tier.getName());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tabs, List stackList) {
        for (int i = 0; i < BagTier.values().length; i++) {
            stackList.add(new ItemStack(this, 1, i));
        }
    }

    @Override
    public ItemStack onItemRightClick(ItemStack item, World world, EntityPlayer player) {
        if (!world.isRemote) {
            int dmg = item.getItemDamage();
            if (dmg >= 0 && dmg < BagTier.values().length) {
                int totalWeight = 0;
                for (Group group : Group.getGroups()) {
                    if (group.isValid(player)) {
                        totalWeight += group.getTier()
                            .getWeights()[dmg];
                    }
                }
                if (totalWeight > 0) {
                    int rng = (int) (Math.random() * totalWeight);
                    List<Group> groups = Group.getGroups();
                    for (int i = 0; i < groups.size(); i++) {
                        Group group = groups.get(i);
                        if (group.isValid(player)) {
                            int weight = group.getTier()
                                .getWeights()[dmg];
                            if (rng < weight) {
                                group.open(player);
                                player.inventory.markDirty();
                                openClientInterface(player, i, dmg);
                                world.playSoundAtEntity(player, Sounds.BAG.getSound(), 1, 1);
                                break;
                            } else {
                                rng -= weight;
                            }
                        }
                    }
                }
            }

            // doing this makes sure the inventory is updated on the client, and the
            // creative mode thingy is already handled by the calling code
            // if(!player.capabilities.isCreativeMode) {
            --item.stackSize;
            // }
        }

        return item;
    }

    private void openClientInterface(EntityPlayer player, int id, int bag) {
        DataWriter dw = PacketHandler.getWriter(PacketId.BAG_INTERFACE);
        dw.writeData(id, DataBitHelper.GROUP_COUNT);
        dw.writeData(bag, DataBitHelper.BAG_TIER);
        for (Group group : Group.getGroups()) {
            if (group.getLimit() != 0) {
                dw.writeData(group.getRetrievalCount(player), DataBitHelper.LIMIT);
            }
        }

        if (ItemBag.displayGui) {
            PacketHandler.sendToRawPlayer(player, dw);
        }
        SoundHandler.play(Sounds.BAG, player);
    }
}
