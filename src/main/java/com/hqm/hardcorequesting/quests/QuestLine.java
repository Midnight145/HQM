package com.hqm.hardcorequesting.quests;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import com.hqm.hardcorequesting.QuestingData;
import com.hqm.hardcorequesting.bag.Group;
import com.hqm.hardcorequesting.bag.GroupTier;
import com.hqm.hardcorequesting.client.interfaces.GuiColor;
import com.hqm.hardcorequesting.client.interfaces.GuiQuestBook;
import com.hqm.hardcorequesting.client.sounds.SoundHandler;
import com.hqm.hardcorequesting.network.DataBitHelper;
import com.hqm.hardcorequesting.network.DataReader;
import com.hqm.hardcorequesting.network.DataWriter;
import com.hqm.hardcorequesting.network.PacketHandler;
import com.hqm.hardcorequesting.network.PacketId;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class QuestLine {

    public QuestLine() {
        tiers.add(new GroupTier("Crap", GuiColor.RED, 50, 50, 50, 5, 0));
        tiers.add(new GroupTier("Plain", GuiColor.GRAY, 50, 50, 50, 30, 10));
        tiers.add(new GroupTier("Common", GuiColor.GREEN, 20, 30, 40, 30, 20));
        tiers.add(new GroupTier("Uncommon", GuiColor.BLUE, 5, 10, 15, 20, 25));
        tiers.add(new GroupTier("Rare", GuiColor.ORANGE, 3, 6, 12, 18, 21));
        tiers.add(new GroupTier("Unique", GuiColor.PURPLE, 1, 2, 3, 4, 30));
    }

    public List<QuestSet> questSets;
    public Map<Short, Quest> quests;
    public int questCount;
    public String mainDescription = "No description";
    public List<String> cachedMainDescription;
    public final List<GroupTier> tiers = new ArrayList<>();
    public final Map<Integer, Group> groups = new HashMap<>();
    public final List<Group> groupList = new ArrayList<>();
    public int groupCount;
    public String mainPath;
    @SideOnly(Side.CLIENT)
    public ResourceLocation front;

    private static QuestLine config = new QuestLine();
    private static QuestLine server;
    private static QuestLine world;

    public static QuestLine getActiveQuestLine() {
        return server != null ? server : world != null ? world : config;
    }

    private static boolean hasLoadedMainSound;
    public static boolean doServerSync;

    public static void receiveServerSync(DataReader dr) {
        if (!hasLoadedMainSound) {
            SoundHandler.loadLoreReading(config.mainPath);
            hasLoadedMainSound = true;
        }
        reset();
        GuiQuestBook.resetBookPosition();
        if (dr.readBoolean() && !Quest.isEditing) {
            server = new QuestLine();
            server.mainPath = config.mainPath;
            getActiveQuestLine().quests = new HashMap<>();
            getActiveQuestLine().questSets = new ArrayList<>();

            Quest.loadAll(dr, QuestingData.FILE_VERSION);
        } else {
            String path = dr.readString(DataBitHelper.SHORT);
            if (path != null && new File(path).exists()) {
                world = new QuestLine();
                Quest.init(path);
            }
        }
        SoundHandler.loadLoreReading(getActiveQuestLine().mainPath);
    }

    public static void reset() {
        server = null;
        world = null;
    }

    public static void sendServerSync(EntityPlayer player) {
        DataWriter dw = PacketHandler.getWriter(PacketId.QUEST_SYNC);
        dw.writeBoolean(doServerSync);
        if (doServerSync) {
            Quest.saveAll(dw);
        } else {
            String path = world == null ? null : world.mainPath;
            dw.writeString(path, DataBitHelper.SHORT);
        }
        PacketHandler.sendToRawPlayer(player, dw);
    }

    public static void loadWorldData(File worldPath) {
        String path = new File(worldPath, "HardcoreQuesting" + File.separator + "Quests").getAbsolutePath()
            + File.separator;
        if (new File(path).exists()) {
            world = new QuestLine();
            Quest.init(path);
        }
    }
}
