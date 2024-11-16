package com.hqm.hardcorequesting.tileentity;

import net.minecraft.entity.player.EntityPlayer;

import com.hqm.hardcorequesting.network.DataReader;
import com.hqm.hardcorequesting.network.DataWriter;

public interface IBlockSync {

    public void writeData(DataWriter dw, EntityPlayer player, boolean onServer, int id);

    public void readData(DataReader dr, EntityPlayer player, boolean onServer, int id);

    public int infoBitLength();
}
