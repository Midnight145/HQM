package com.hqm.hardcorequesting.quests;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;

import com.hqm.hardcorequesting.EventHandler;
import com.hqm.hardcorequesting.QuestingData;
import com.hqm.hardcorequesting.client.interfaces.GuiEditMenuItem;
import com.hqm.hardcorequesting.network.DataReader;

import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class QuestTaskItemsDetect extends QuestTaskItems {

    public QuestTaskItemsDetect(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);

        this.register(EventHandler.Type.CRAFTING, EventHandler.Type.PICK_UP, EventHandler.Type.OPEN_BOOK);
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected GuiEditMenuItem.Type getMenuTypeId() {
        return GuiEditMenuItem.Type.CRAFTING_TASK;
    }

    @Override
    public void onOpenBook(EventHandler.BookOpeningEvent event) {
        if (event.isRealName()) {
            this.countItems(event.getPlayer(), null);
        }
    }

    @Override
    public void onUpdate(EntityPlayer player, DataReader dr) {
        this.countItems(player, null);
    }

    @Override
    public void onItemPickUp(EntityItemPickupEvent event) {
        if (event.entityPlayer.inventory.inventoryChanged) {
            this.countItems(event.entityPlayer, event.item.getEntityItem());
        }
    }

    @Override
    public void onCrafting(PlayerEvent.ItemCraftedEvent event) {
        this.onCrafting(event.player, event.crafting, event.craftMatrix);
    }

    public void onCrafting(EntityPlayer player, ItemStack item, IInventory craftMatrix) {
        if (player != null) {
            item = item.copy();
            if (item.stackSize == 0) {
                item.stackSize = 1;
            }
            this.countItems(player, item);
        }
    }

    private void countItems(EntityPlayer player, ItemStack item) {
        if (player.worldObj.isRemote) {
            return;
        }

        ItemStack[] items;
        if (item == null) {
            items = player.inventory.mainInventory;
        } else {
            items = new ItemStack[player.inventory.mainInventory.length + 1];
            final ItemStack[] mainInventory = player.inventory.mainInventory;
            for (int i = 0; i < mainInventory.length; i++) {
                items[i] = mainInventory[i];
            }
            items[items.length - 1] = item;
        }
        this.countItems(items, (QuestDataTaskItems) this.getData(player), QuestingData.getUserName(player));
    }

    public void countItems(ItemStack[] itemsToCount, QuestDataTaskItems data, String playerName) {
        if (!this.parent.isAvailable(playerName)) {
            return;
        }

        boolean updated = false;

        for (int i = 0; i < this.items.length; i++) {
            final ItemRequirement item = this.items[i];
            if (!item.hasItem || item.required == data.progress[i]) {
                continue;
            }

            for (final ItemStack itemStack : itemsToCount) {
                if (item.getPrecision()
                    .areItemsSame(itemStack, item.getItem())) {
                    final int amount = Math.min(itemStack.stackSize, item.required - data.progress[i]);
                    data.progress[i] += amount;
                    updated = true;
                }
            }
        }

        if (updated) {
            this.doCompletionCheck(data, playerName);
        }
    }

    @Override
    protected void doCompletionCheck(QuestDataTaskItems data, String playerName) {
        boolean isDone = true;
        for (int i = 0; i < this.items.length; i++) {
            final ItemRequirement item = this.items[i];
            if (item.required > data.progress[i]) {
                data.progress[i] = 0; // Clear unfinished ones
                isDone = false;
            }
        }

        if (isDone) {
            this.completeTask(playerName);
        }
        this.parent.sendUpdatedDataToTeam(playerName);
    }

}
