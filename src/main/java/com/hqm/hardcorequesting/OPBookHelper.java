package com.hqm.hardcorequesting;

import net.minecraft.entity.player.EntityPlayer;

import com.hqm.hardcorequesting.commands.CommandHandler;
import com.hqm.hardcorequesting.network.DataBitHelper;
import com.hqm.hardcorequesting.network.DataReader;
import com.hqm.hardcorequesting.network.DataWriter;
import com.hqm.hardcorequesting.network.PacketHandler;
import com.hqm.hardcorequesting.network.PacketId;
import com.hqm.hardcorequesting.quests.Quest;

public final class OPBookHelper {

    private OPBookHelper() {}

    public static void handlePacket(EntityPlayer player, DataReader dr) {
        if (CommandHandler.isOwnerOrOp(player)) {
            OpAction action = OpAction.values()[dr.readData(DataBitHelper.OP_ACTION)];
            switch (action) {
                case RESET:
                    QuestingData.getQuestingData(player)
                        .getTeam()
                        .clearProgress();
                    break;
                case QUEST_COMPLETION:
                    Quest quest = Quest.getQuest(dr.readData(DataBitHelper.QUESTS));
                    if (quest != null) {
                        if (quest.isCompleted(player)) {
                            QuestingData.getQuestingData(player)
                                .getTeam()
                                .resetProgress(quest);
                        } else {
                            quest.completeQuest(player);
                        }
                        quest.sendUpdatedDataToTeam(player);
                    }
                    break;
            }
        }
    }

    private static DataWriter getWriter(OpAction action) {
        DataWriter dw = PacketHandler.getWriter(PacketId.OP_BOOK);
        dw.writeData(action.ordinal(), DataBitHelper.OP_ACTION);
        return dw;
    }

    public static void reverseQuestCompletion(Quest quest, EntityPlayer player) {
        DataWriter dw = getWriter(OpAction.QUEST_COMPLETION);
        dw.writeData(quest.getId(), DataBitHelper.QUESTS);
        PacketHandler.sendToServer(dw);
    }

    private enum OpAction {
        RESET,
        QUEST_COMPLETION
    }

    public static void reset(EntityPlayer player) {
        PacketHandler.sendToServer(getWriter(OpAction.RESET));
    }
}
