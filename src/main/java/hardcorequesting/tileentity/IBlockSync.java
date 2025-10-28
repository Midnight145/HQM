package hardcorequesting.tileentity;

import net.minecraft.entity.player.EntityPlayer;

import hardcorequesting.network.DataReader;
import hardcorequesting.network.DataWriter;

public interface IBlockSync {

    public void writeData(DataWriter dw, EntityPlayer player, boolean onServer, int id);

    public void readData(DataReader dr, EntityPlayer player, boolean onServer, int id);

    public int infoBitLength();
}
