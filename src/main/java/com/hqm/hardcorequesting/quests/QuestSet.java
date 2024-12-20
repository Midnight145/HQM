package com.hqm.hardcorequesting.quests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.hqm.hardcorequesting.OPBookHelper;
import com.hqm.hardcorequesting.SaveHelper;
import com.hqm.hardcorequesting.Translator;
import com.hqm.hardcorequesting.client.EditMode;
import com.hqm.hardcorequesting.client.interfaces.GuiBase;
import com.hqm.hardcorequesting.client.interfaces.GuiColor;
import com.hqm.hardcorequesting.client.interfaces.GuiEditMenuItem;
import com.hqm.hardcorequesting.client.interfaces.GuiEditMenuParentCount;
import com.hqm.hardcorequesting.client.interfaces.GuiEditMenuRepeat;
import com.hqm.hardcorequesting.client.interfaces.GuiEditMenuTextEditor;
import com.hqm.hardcorequesting.client.interfaces.GuiEditMenuTrigger;
import com.hqm.hardcorequesting.client.interfaces.GuiQuestBook;
import com.hqm.hardcorequesting.client.interfaces.ResourceHelper;
import com.hqm.hardcorequesting.client.interfaces.ScrollBar;
import com.hqm.hardcorequesting.network.DataBitHelper;
import com.hqm.hardcorequesting.reputation.ReputationBar;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class QuestSet {

    private String name;
    private String description;
    private List<String> cachedDescription;
    private final List<Quest> quests;
    private final List<ReputationBar> reputationBars;
    private int id;

    public QuestSet(String name, String description) {
        this.name = name;
        this.description = description;
        this.quests = new ArrayList<>();
        this.reputationBars = new ArrayList<>();
        this.id = Quest.getQuestSets()
            .size();
    }

    public List<Quest> getQuests() {
        return this.quests;
    }

    public List<ReputationBar> getReputationBars() {
        this.validateBars();
        return this.reputationBars;
    }

    private void validateBars() {
        final List<ReputationBar> toRemove = new ArrayList<>();
        for (final ReputationBar reputationBar : this.reputationBars) {
            if (!reputationBar.isValid()) {
                toRemove.add(reputationBar);
            }
        }
        this.reputationBars.removeAll(toRemove);
    }

    public String getName() {
        return this.name;
    }

    public String getName(int i) {
        return i + 1 + ". " + this.name;
    }

    @SideOnly(Side.CLIENT)
    public List<String> getDescription(GuiBase gui) {
        if (this.cachedDescription == null) {
            this.cachedDescription = gui.getLinesFromText(this.description, 0.7F, 130);
        }

        return this.cachedDescription;
    }

    public boolean isEnabled(EntityPlayer player) {
        return this.isEnabled(player, new HashMap<Quest, Boolean>(), new HashMap<Quest, Boolean>());
    }

    private boolean isEnabled(EntityPlayer player, Map<Quest, Boolean> isVisibleCache,
        Map<Quest, Boolean> isLinkFreeCache) {
        if (this.quests.isEmpty()) {
            return false;
        }

        for (final Quest quest : this.quests) {
            if (quest.isEnabled(player, isVisibleCache, isLinkFreeCache)) {
                return true;
            }
        }

        return false;
    }

    public boolean isCompleted(EntityPlayer player) {
        if (this.quests.isEmpty()) {
            return false;
        }

        for (final Quest quest : this.quests) {
            if (!quest.isCompleted(player)) {
                return false;
            }
        }

        return true;
    }

    public void removeQuest(Quest quest) {
        this.quests.remove(quest);
    }

    public void addQuest(Quest quest) {
        this.quests.add(quest);
    }

    public void removeRepBar(ReputationBar repBar) {
        this.reputationBars.remove(repBar);
    }

    public void addRepBar(ReputationBar repBar) {
        if (repBar == null) {
            return;
        }
        repBar.setQuestSet(this.id);
        this.reputationBars.add(repBar);
    }

    public int getCompletedCount(EntityPlayer player) {
        return this.getCompletedCount(player, new HashMap<Quest, Boolean>(), new HashMap<Quest, Boolean>());
    }

    private int getCompletedCount(EntityPlayer player, Map<Quest, Boolean> isVisibleCache,
        Map<Quest, Boolean> isLinkFreeCache) {
        int count = 0;
        for (final Quest quest : this.quests) {
            if (quest.isCompleted(player) && quest.isEnabled(player, isVisibleCache, isLinkFreeCache)) {
                count++;
            }
        }

        return count;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.cachedDescription = null;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return this.id;
    }

    public void decreaseId() {
        this.id--;
        for (final ReputationBar reputationBar : this.reputationBars) {
            reputationBar.setQuestSet(this.id);
        }
    }

    private static final int LINE_2_X = 10;
    private static final int LINE_2_Y = 12;
    private static final int INFO_Y = 100;

    @SideOnly(Side.CLIENT)
    public static void drawOverview(GuiQuestBook gui, ScrollBar setScroll, ScrollBar descriptionScroll, int x, int y) {
        final EntityPlayer player = gui.getPlayer();
        final List<QuestSet> questSets = Quest.getQuestSets();
        final int start = setScroll.isVisible(gui) ? Math.round(
            (Quest.getQuestSets()
                .size() - GuiQuestBook.VISIBLE_SETS) * setScroll.getScroll())
            : 0;

        final HashMap<Quest, Boolean> isVisibleCache = new HashMap<>();
        final HashMap<Quest, Boolean> isLinkFreeCache = new HashMap<>();
        for (int i = start; i < Math.min(start + GuiQuestBook.VISIBLE_SETS, questSets.size()); i++) {
            final QuestSet questSet = questSets.get(i);

            final int setY = GuiQuestBook.LIST_Y + (i - start) * (GuiQuestBook.TEXT_HEIGHT + GuiQuestBook.TEXT_SPACING);

            final int total = questSet.getQuests()
                .size();
            final boolean enabled = questSet.isEnabled(player, isVisibleCache, isLinkFreeCache);
            final int completedCount = enabled ? questSet.getCompletedCount(player, isVisibleCache, isLinkFreeCache)
                : 0; // no
            // need
            // to
            // check
            // for
            // the
            // completed
            // count
            // if
            // it's
            // not
            // enabled

            boolean completed = true;
            int unclaimed = 0;
            for (final Quest quest : questSet.getQuests()) {
                if (completed && !quest.isCompleted(player) && quest.isLinkFree(player, isLinkFreeCache)) {
                    completed = false;
                }
                if (quest.isCompleted(player) && quest.hasReward(player)) {
                    unclaimed++;
                }
            }
            final boolean selected = questSet == GuiQuestBook.selectedSet;
            final boolean inBounds = gui.inBounds(
                GuiQuestBook.LIST_X,
                setY,
                gui.getStringWidth(questSet.getName(i)),
                GuiQuestBook.TEXT_HEIGHT,
                x,
                y);
            final int color = gui.modifyingQuestSet == questSet ? 0x4040DD
                : enabled
                    ? completed ? selected ? inBounds ? 0x40BB40 : 0x40A040 : inBounds ? 0x10A010 : 0x107010
                        : selected ? inBounds ? 0xAAAAAA : 0x888888 : inBounds ? 0x666666 : 0x404040
                    : 0xDDDDDD;
            gui.drawString(questSet.getName(i), GuiQuestBook.LIST_X, setY, color);

            String info;
            if (enabled) {
                if (completed) {
                    info = Translator.translate("hqm.questBook.allQuests");
                } else {
                    info = Translator.translate("hqm.questBook.percentageQuests", completedCount * 100 / total);
                }
            } else {
                info = Translator.translate("hqm.questBook.locked");
            }
            gui.drawString(info, GuiQuestBook.LIST_X + LINE_2_X, setY + LINE_2_Y, 0.7F, color);
            if (enabled && unclaimed != 0) {
                final String toClaim = GuiColor.PURPLE.toString()
                    + Translator.translate(unclaimed != 1, "hqm.questBook.unclaimedRewards", unclaimed);
                gui.drawString(toClaim, GuiQuestBook.LIST_X + LINE_2_X, setY + LINE_2_Y + 8, 0.7F, 0xFFFFFFFF);
            }
        }

        if (Quest.isEditing && gui.getCurrentMode() == EditMode.CREATE) {
            gui.drawString(
                gui.getLinesFromText(Translator.translate("hqm.questBook.createNewSet"), 0.7F, 130),
                GuiQuestBook.DESCRIPTION_X,
                GuiQuestBook.DESCRIPTION_Y,
                0.7F,
                0x404040);
        } else {
            if (GuiQuestBook.selectedSet != null) {
                final int startLine = descriptionScroll.isVisible(
                    gui) ? Math.round((GuiQuestBook.selectedSet.getDescription(gui)
                        .size() - GuiQuestBook.VISIBLE_DESCRIPTION_LINES) * descriptionScroll.getScroll()) : 0;
                gui.drawString(
                    GuiQuestBook.selectedSet.getDescription(gui),
                    startLine,
                    GuiQuestBook.VISIBLE_DESCRIPTION_LINES,
                    GuiQuestBook.DESCRIPTION_X,
                    GuiQuestBook.DESCRIPTION_Y,
                    0.7F,
                    0x404040);
            }

            drawQuestInfo(
                gui,
                GuiQuestBook.selectedSet,
                GuiQuestBook.DESCRIPTION_X,
                GuiQuestBook.selectedSet == null ? GuiQuestBook.DESCRIPTION_Y : INFO_Y,
                isVisibleCache,
                isLinkFreeCache);
        }

    }

    @SideOnly(Side.CLIENT)
    public void draw(GuiQuestBook gui, int x0, int y0, int x, int y) {
        if (gui.isOpBook) {
            gui.drawString(
                gui.getLinesFromText(Translator.translate("hqm.questBook.shiftSetReset"), 0.7F, 130),
                184,
                192,
                0.7F,
                0x707070);
        }

        final EntityPlayer player = gui.getPlayer();

        for (final ReputationBar bar : this.getReputationBars()) {
            bar.draw(gui, x, y, player);
        }

        final HashMap<Quest, Boolean> isVisibleCache = new HashMap<>();
        final HashMap<Quest, Boolean> isLinkFreeCache = new HashMap<>();

        for (final Quest child : this.getQuests()) {
            if (Quest.isEditing || child.isVisible(player, isVisibleCache, isLinkFreeCache)) {
                for (final Quest parent : child.getRequirement()) {
                    if ((Quest.isEditing || parent.isVisible(player, isVisibleCache, isLinkFreeCache))
                        && parent.hasSameSetAs(child)) {
                        final int color = Quest.isEditing && (!child.isVisible(player, isVisibleCache, isLinkFreeCache)
                            || !parent.isVisible(player, isVisibleCache, isLinkFreeCache)) ? 0x55404040 : 0xFF404040;
                        gui.drawLine(
                            gui.getLeft() + parent.getGuiCenterX(),
                            gui.getTop() + parent.getGuiCenterY(),
                            gui.getLeft() + child.getGuiCenterX(),
                            gui.getTop() + child.getGuiCenterY(),
                            5,
                            color);
                    }
                }
            }
        }
        if (Quest.isEditing) {
            for (final Quest child : this.getQuests()) {
                for (final Quest parent : child.getOptionLinks()) {
                    if (parent.hasSameSetAs(child)) {
                        final int color = !child.isVisible(player, isVisibleCache, isLinkFreeCache)
                            || !parent.isVisible(player, isVisibleCache, isLinkFreeCache) ? 0x554040DD : 0xFF4040DD;
                        gui.drawLine(
                            gui.getLeft() + parent.getGuiCenterX(),
                            gui.getTop() + parent.getGuiCenterY(),
                            gui.getLeft() + child.getGuiCenterX(),
                            gui.getTop() + child.getGuiCenterY(),
                            5,
                            color);
                    }
                }
            }
        }

        for (final Quest quest : this.getQuests()) {
            if (Quest.isEditing || quest.isVisible(player, isVisibleCache, isLinkFreeCache)) {

                GL11.glPushMatrix();
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                gui.applyColor(quest == gui.modifyingQuest ? 0xFFBBFFBB : quest.getColorFilter(player, gui.getTick()));
                ResourceHelper.bindResource(GuiBase.MAP_TEXTURE);
                gui.drawRect(
                    quest.getGuiX(),
                    quest.getGuiY(),
                    quest.getGuiU(),
                    quest.getGuiV(player, x, y),
                    quest.getGuiW(),
                    quest.getGuiH());

                int iconX = quest.getGuiCenterX() - 8;
                int iconY = quest.getGuiCenterY() - 8;

                if (quest.useBigIcon()) {
                    iconX++;
                    iconY++;
                }

                gui.drawItem(quest.getIcon(), iconX, iconY, true);
                GL11.glPopMatrix();
                // ResourceHelper.bindResource(QUEST_ICONS);
                // drawRect(quest.getIconX(), quest.getIconY(), quest.getIconU(),
                // quest.getIconV(), quest.getIconSize(), quest.getIconSize());
            }
        }

        for (final Quest quest : this.getQuests()) {
            final boolean editing = Quest.isEditing && !GuiScreen.isCtrlKeyDown();
            if ((editing || quest.isVisible(player, isVisibleCache, isLinkFreeCache)) && quest.isMouseInObject(x, y)) {
                boolean shouldDrawText = false;
                final boolean enabled = quest.isEnabled(player, isVisibleCache, isLinkFreeCache);
                String txt = "";

                if (enabled || editing) {
                    txt += quest.getName();
                }

                if (!enabled) {
                    if (editing) {
                        txt += "\n";
                    }
                    txt += GuiColor.GRAY + Translator.translate("hqm.questBook.lockedQuest");
                }

                if (!enabled || editing) {
                    int totalParentCount = 0;
                    int totalCompletedCount = 0;
                    int parentCount = 0;
                    int completed = 0;
                    final List<Quest> externalQuests = new ArrayList<>();
                    for (final Quest parent : quest.getRequirement()) {
                        totalParentCount++;
                        final boolean isCompleted = parent.isCompleted(player);
                        if (isCompleted) {
                            totalCompletedCount++;
                        }
                        if (!parent.hasSameSetAs(quest)) {
                            externalQuests.add(parent);
                            parentCount++;
                            if (isCompleted) {
                                completed++;
                            }
                        }

                    }

                    if (editing && totalParentCount > 0) {
                        txt += "\n" + GuiColor.GRAY
                            + Translator.translate(
                                totalParentCount != 1,
                                "hqm.questBook.parentCount",
                                totalParentCount - totalCompletedCount,
                                totalParentCount);

                        if (Keyboard.isKeyDown(Keyboard.KEY_R)) {
                            txt += " [" + Translator.translate("hqm.questBook.holding", "R") + "]";
                            for (final Quest parent : quest.getRequirement()) {
                                txt += "\n" + GuiColor.GRAY + parent.getName();
                                if (parent.isCompleted(player)) {
                                    txt += " " + GuiColor.WHITE
                                        + " ["
                                        + Translator.translate("hqm.questBook.completed")
                                        + "]";
                                }
                            }
                        } else {
                            txt += " [" + Translator.translate("hqm.questBook.hold", "R") + "]";
                        }
                    }

                    final int allowedUncompleted = quest
                        .getUseModifiedParentRequirement()
                            ? Math.max(
                                0,
                                quest.getRequirement()
                                    .size() - quest.getParentRequirementCount())
                            : 0;
                    if (parentCount - completed > allowedUncompleted || editing && parentCount > 0) {
                        txt += "\n" + GuiColor.PINK
                            + Translator.translate(
                                totalParentCount != 1,
                                "hqm.questBook.parentCountElsewhere",
                                totalParentCount - totalCompletedCount,
                                totalParentCount);
                        shouldDrawText = true;
                        if (editing) {
                            if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
                                txt += " [" + Translator.translate("hqm.questBook.holding", "E") + "]";
                                for (final Quest parent : externalQuests) {
                                    txt += "\n" + GuiColor.PINK
                                        + parent.getName()
                                        + " ("
                                        + parent.getQuestSet()
                                            .getName()
                                        + ")";
                                    if (parent.isCompleted(player)) {
                                        txt += " " + GuiColor.WHITE
                                            + " ["
                                            + Translator.translate("hqm.questBook.completed")
                                            + "]";
                                    }
                                }
                            } else {
                                txt += " [" + Translator.translate("hqm.questBook.hold", "E") + "]";
                            }
                        }
                    }

                    if (editing && quest.getUseModifiedParentRequirement()) {
                        txt += "\n" + GuiColor.MAGENTA;
                        final int amount = quest.getParentRequirementCount();
                        if (amount < quest.getRequirement()
                            .size()) {
                            txt += Translator.translate(amount != 1, "hqm.questBook.reqOnly", amount);
                        } else if (amount > quest.getRequirement()
                            .size()) {
                                txt += Translator.translate(amount != 1, "hqm.questBook.reqMore", amount);
                            } else {
                                txt += Translator.translate(amount != 1, "hqm.questBook.reqAll", amount);
                            }

                    }
                }

                if (enabled || editing) {
                    if (quest.isCompleted(player)) {
                        txt += "\n" + GuiColor.GREEN + Translator.translate("hqm.questBook.completed");
                    }
                    if (quest.hasReward(player)) {
                        txt += "\n" + GuiColor.PURPLE + Translator.translate("hqm.questBook.unclaimedReward");
                    }

                    final String repeatMessage = enabled ? quest.getRepeatInfo()
                        .getMessage(quest, player)
                        : quest.getRepeatInfo()
                            .getShortMessage();
                    if (repeatMessage != null) {
                        txt += "\n" + repeatMessage;
                    }

                    if (editing) {
                        int totalTasks = 0;
                        int completedTasks = 0;
                        for (final QuestTask task : quest.getTasks()) {
                            totalTasks++;
                            if (task.isCompleted(player)) {
                                completedTasks++;
                            }
                        }

                        if (totalTasks == 0) {
                            txt += "\n" + GuiColor.RED + Translator.translate("hqm.questBook.noTasks");
                        } else {
                            txt += "\n" + GuiColor.CYAN
                                + Translator.translate("hqm.questBook.completedTasks", completedTasks, totalTasks);

                            if (Keyboard.isKeyDown(Keyboard.KEY_T)) {
                                txt += " [" + Translator.translate("hqm.questBook.holding", "T") + "]";
                                for (final QuestTask task : quest.getTasks()) {
                                    txt += "\n" + GuiColor.CYAN + task.getDescription();
                                    if (task.isCompleted(player)) {
                                        txt += GuiColor.WHITE + " ["
                                            + Translator.translate("hqm.questBook.completed")
                                            + "]";
                                    }
                                }
                            } else {
                                txt += " [" + Translator.translate("hqm.questBook.holding", "T") + "]";
                            }
                        }

                        final String triggerMessage = quest.getTriggerType()
                            .getMessage(quest);
                        if (triggerMessage != null) {
                            txt += "\n" + triggerMessage;
                        }

                        if (!quest.isVisible(player, isVisibleCache, isLinkFreeCache)) {
                            String invisibilityMessage;
                            if (quest.isLinkFree(player, isLinkFreeCache)) {
                                boolean parentInvisible = false;
                                for (final Quest parent : quest.getRequirement()) {
                                    if (!parent.isVisible(player, isVisibleCache, isLinkFreeCache)) {
                                        parentInvisible = true;
                                        break;
                                    }
                                }

                                switch (quest.getTriggerType()) {
                                    case ANTI_TRIGGER:
                                        invisibilityMessage = Translator.translate("hqm.questBook.invisLocked");
                                        break;
                                    case QUEST_TRIGGER:
                                        invisibilityMessage = Translator.translate("hqm.questBook.invisPerm");
                                        parentInvisible = false;
                                        break;
                                    case TASK_TRIGGER:
                                        invisibilityMessage = Translator.translate(
                                            quest.getTriggerTasks() != 1,
                                            "hqm.questBook.invisCount",
                                            quest.getTriggerTasks());
                                        break;
                                    default:
                                        invisibilityMessage = null;
                                }

                                if (parentInvisible) {
                                    final String parentText = Translator.translate("hqm.questBook.invisInherit");
                                    if (invisibilityMessage == null) {
                                        invisibilityMessage = parentText;
                                    } else {
                                        invisibilityMessage = parentText + " "
                                            + Translator.translate("hqm.questBook.and")
                                            + " "
                                            + invisibilityMessage;
                                    }
                                }

                            } else {
                                invisibilityMessage = Translator.translate("hqm.questBook.invisOption");
                            }

                            if (invisibilityMessage != null) {
                                txt += "\n" + GuiColor.LIGHT_BLUE + invisibilityMessage;
                            }
                        }

                        final List<Integer> ids = new ArrayList<>();
                        for (final Quest option : quest.getOptionLinks()) {
                            ids.add((int) option.getId());
                        }
                        for (final Quest option : quest.getReversedOptionLinks()) {
                            final int id = option.getId();
                            if (!ids.contains(id)) {
                                ids.add(id);
                            }
                        }
                        final int optionLinks = ids.size();
                        if (optionLinks > 0) {
                            txt += "\n" + GuiColor.BLUE
                                + Translator.translate(optionLinks != 1, "hqm.questBook.optionLinks", optionLinks);

                            if (Keyboard.isKeyDown(Keyboard.KEY_O)) {
                                txt += " [" + Translator.translate("hqm.questBook.holding", "O") + "]";
                                for (final int id : ids) {
                                    final Quest option = Quest.getQuest(id);
                                    txt += "\n" + GuiColor.BLUE + option.getName();
                                    if (!option.hasSameSetAs(quest)) {
                                        txt += " (" + option.getQuestSet()
                                            .getName() + ")";
                                    }
                                }
                            } else {
                                txt += " [" + Translator.translate("hqm.questBook.hold", "O") + "]";
                            }
                        }

                    }

                    final List<Quest> externalQuests = new ArrayList<>();
                    int childCount = 0;
                    for (final Quest child : quest.getReversedRequirement()) {
                        if (!quest.hasSameSetAs(child)) {
                            childCount++;
                            externalQuests.add(child);
                        }
                    }

                    if (childCount > 0) {
                        txt += "\n" + GuiColor.PINK
                            + Translator.translate(childCount != 1, "hqm.questBook.childUnlocks", childCount);
                        if (editing) {
                            if (Keyboard.isKeyDown(Keyboard.KEY_U)) {
                                txt += " [" + Translator.translate("hqm.questBook.holding", "U") + "]";
                                for (final Quest child : externalQuests) {
                                    txt += "\n" + GuiColor.PINK
                                        + child.getName()
                                        + " ("
                                        + child.getQuestSet()
                                            .getName()
                                        + ")";
                                }
                            } else {
                                txt += " [" + Translator.translate("hqm.questBook.hold", "U") + "]";
                            }
                        }
                    }
                    shouldDrawText = true;

                }

                if (editing) {
                    txt += "\n\n" + GuiColor.GRAY + Translator.translate("hqm.questBook.ctrlNonEditor");
                }

                if (gui.isOpBook && GuiScreen.isShiftKeyDown()) {
                    if (quest.isCompleted(player)) {
                        txt += "\n\n" + GuiColor.RED + Translator.translate("hqm.questBook.resetQuest");
                    } else {
                        txt += "\n\n" + GuiColor.ORANGE + Translator.translate("hqm.questBook.completeQuest");
                    }
                }

                if (shouldDrawText) {
                    gui.drawMouseOver(txt, x0, y0);
                }
                break;
            }
        }

    }

    @SideOnly(Side.CLIENT)
    public static void drawQuestInfo(GuiQuestBook gui, QuestSet set, int x, int y) {
        drawQuestInfo(gui, set, x, y, new HashMap<Quest, Boolean>(), new HashMap<Quest, Boolean>());
    }

    @SideOnly(Side.CLIENT)
    private static void drawQuestInfo(GuiQuestBook gui, QuestSet set, int x, int y,
        HashMap<Quest, Boolean> isVisibleCache, HashMap<Quest, Boolean> isLinkFreeCache) {
        int completed = 0;
        int reward = 0;
        int enabled = 0;
        int total = 0;
        int realTotal = 0;

        final EntityPlayer player = gui.getPlayer();

        for (final Quest quest : Quest.getQuests()) {
            if (set == null || quest.hasSet(set)) {
                realTotal++;
                if (quest.isVisible(player, isVisibleCache, isLinkFreeCache)) {
                    total++;
                    if (quest.isEnabled(player, isVisibleCache, isLinkFreeCache)) {
                        enabled++;
                        if (quest.isCompleted(player)) {
                            completed++;
                            if (quest.hasReward(player)) {
                                reward++;
                            }
                        }
                    }
                }
            }
        }

        final List<String> info = new ArrayList<>();
        info.add(GuiColor.GRAY.toString() + Translator.translate(total != 1, "hqm.questBook.totalQuests", total));
        info.add(
            GuiColor.CYAN.toString() + Translator.translate(enabled != 1, "hqm.questBook.unlockedQuests", enabled));
        info.add(
            GuiColor.GREEN.toString()
                + Translator.translate(completed != 1, "hqm.questBook.completedQuests", completed));
        info.add(
            GuiColor.LIGHT_BLUE.toString()
                + Translator.translate(enabled - completed != 1, "hqm.questBook.totalQuests", enabled - completed));
        if (reward > 0) {
            info.add(
                GuiColor.PURPLE.toString()
                    + Translator.translate(reward != 1, "hqm.questBook.unclaimedQuests", reward));
        }
        if (Quest.isEditing && !GuiScreen.isCtrlKeyDown()) {
            info.add(
                GuiColor.LIGHT_GRAY.toString()
                    + Translator.translate(realTotal != 1, "hqm.questBook.inclInvisiQuests", realTotal));
        }
        gui.drawString(info, x, y, 0.7F, 0x404040);
    }

    @SideOnly(Side.CLIENT)
    public static void mouseClickedOverview(GuiQuestBook gui, ScrollBar setScroll, int x, int y) {
        final List<QuestSet> questSets = Quest.getQuestSets();
        final int start = setScroll.isVisible(gui) ? Math.round(
            (Quest.getQuestSets()
                .size() - GuiQuestBook.VISIBLE_SETS) * setScroll.getScroll())
            : 0;

        final HashMap<Quest, Boolean> isVisibleCache = new HashMap<>();
        final HashMap<Quest, Boolean> isLinkFreeCache = new HashMap<>();

        for (int i = start; i < Math.min(start + GuiQuestBook.VISIBLE_SETS, questSets.size()); i++) {
            final QuestSet questSet = questSets.get(i);

            final int setY = GuiQuestBook.LIST_Y + (i - start) * (GuiQuestBook.TEXT_HEIGHT + GuiQuestBook.TEXT_SPACING);
            if (gui.inBounds(
                GuiQuestBook.LIST_X,
                setY,
                gui.getStringWidth(questSet.getName(i)),
                GuiQuestBook.TEXT_HEIGHT,
                x,
                y)) {
                switch (gui.getCurrentMode()) {
                    case DELETE:
                        if (questSet.getQuests()
                            .isEmpty()) {
                            for (int j = questSet.getId() + 1; j < Quest.getQuestSets()
                                .size(); j++) {
                                Quest.getQuestSets()
                                    .get(j)
                                    .decreaseId();
                            }
                            Quest.getQuestSets()
                                .remove(questSet);
                            SaveHelper.add(SaveHelper.EditType.SET_REMOVE);
                        }
                        break;
                    case SWAP_SELECT:
                        gui.modifyingQuestSet = gui.modifyingQuestSet == questSet ? null : questSet;
                        break;
                    case RENAME:
                        gui.setEditMenu(new GuiEditMenuTextEditor(gui, gui.getPlayer(), questSet, true));
                        break;
                    default:
                        if (!(!Quest.isEditing
                            && questSet.isEnabled(gui.getPlayer(), isVisibleCache, isLinkFreeCache))) {
                            break;
                        }
                    case NORMAL:
                        GuiQuestBook.selectedSet = GuiQuestBook.selectedSet == questSet ? null : questSet;
                        break;
                }
                break;
            }
        }

        if ((Quest.isEditing && gui.getCurrentMode() == EditMode.RENAME) && gui.inBounds(
            GuiQuestBook.DESCRIPTION_X,
            GuiQuestBook.DESCRIPTION_Y,
            130,
            (int) (GuiQuestBook.VISIBLE_DESCRIPTION_LINES * GuiQuestBook.TEXT_HEIGHT * 0.7F),
            x,
            y)) {
            gui.setEditMenu(new GuiEditMenuTextEditor(gui, gui.getPlayer(), GuiQuestBook.selectedSet, false));
        }
    }

    @SideOnly(Side.CLIENT)
    public void mouseClicked(GuiQuestBook gui, int x, int y) {
        final EntityPlayer player = gui.getPlayer();
        if (Quest.isEditing
            && (gui.getCurrentMode() == EditMode.CREATE || gui.getCurrentMode() == EditMode.REP_BAR_CREATE)) {
            switch (gui.getCurrentMode()) {
                case CREATE:
                    if (x > 0 && Quest.size() < DataBitHelper.QUESTS.getMaximum()) {
                        int i = 0;
                        for (final Quest quest : this.getQuests()) {
                            if (quest.getName()
                                .startsWith("Unnamed")) {
                                i++;
                            }
                        }
                        final Quest newQuest = new Quest(
                            Quest.size(),
                            "Unnamed" + (i == 0 ? "" : i),
                            "Unnamed quest",
                            0,
                            0,
                            false);
                        newQuest.setGuiCenterX(x);
                        newQuest.setGuiCenterY(y);
                        newQuest.setQuestSet(this);
                        SaveHelper.add(SaveHelper.EditType.QUEST_CREATE);
                    }
                    break;
                case REP_BAR_CREATE:
                    gui.setEditMenu(new ReputationBar.EditGui(gui, player, x, y, this.getId()));
                    break;
                default:
                    break;
            }
        } else {
            final HashMap<Quest, Boolean> isVisibleCache = new HashMap<>();
            final HashMap<Quest, Boolean> isLinkFreeCache = new HashMap<>();
            for (final Quest quest : this.getQuests()) {
                if ((Quest.isEditing || quest.isVisible(player, isVisibleCache, isLinkFreeCache))
                    && quest.isMouseInObject(x, y)) {
                    if (Quest.isEditing && gui.getCurrentMode() != EditMode.NORMAL) {
                        switch (gui.getCurrentMode()) {
                            case MOVE:
                                gui.modifyingQuest = quest;
                                SaveHelper.add(SaveHelper.EditType.QUEST_MOVE);
                                break;
                            case REQUIREMENT:
                                if (gui.modifyingQuest == quest) {
                                    if (GuiScreen.isShiftKeyDown()) {
                                        gui.modifyingQuest.clearRequirements();
                                    }
                                    gui.modifyingQuest = null;
                                } else if (gui.modifyingQuest == null) {
                                    gui.modifyingQuest = quest;
                                } else {
                                    gui.modifyingQuest.addRequirement(quest.getId());
                                }
                                break;
                            case SIZE:
                                final int cX = quest.getGuiCenterX();
                                final int cY = quest.getGuiCenterY();
                                quest.setBigIcon(!quest.useBigIcon());
                                quest.setGuiCenterX(cX);
                                quest.setGuiCenterY(cY);
                                SaveHelper.add(SaveHelper.EditType.QUEST_SIZE_CHANGE);
                                break;
                            case ITEM:
                                gui.setEditMenu(
                                    new GuiEditMenuItem(
                                        gui,
                                        player,
                                        quest.getIcon(),
                                        quest.getId(),
                                        GuiEditMenuItem.Type.QUEST_ICON,
                                        1,
                                        ItemPrecision.PRECISE));
                                break;
                            case DELETE:
                                Quest.removeQuest(quest);
                                SaveHelper.add(SaveHelper.EditType.QUEST_REMOVE);
                                break;
                            case SWAP:
                                if (gui.modifyingQuestSet != null && gui.modifyingQuestSet != this) {
                                    quest.setQuestSet(gui.modifyingQuestSet);
                                    SaveHelper.add(SaveHelper.EditType.QUEST_CHANGE_SET);
                                }
                                break;
                            case REPEATABLE:
                                gui.setEditMenu(new GuiEditMenuRepeat(gui, player, quest));
                                break;
                            case REQUIRED_PARENTS:
                                gui.setEditMenu(new GuiEditMenuParentCount(gui, player, quest));
                                break;
                            case QUEST_SELECTION:
                                Quest.selectedQuestId = quest.getId();
                                break;
                            case QUEST_OPTION:
                                if (gui.modifyingQuest == quest) {
                                    if (GuiScreen.isShiftKeyDown()) {
                                        gui.modifyingQuest.clearOptionLinks();
                                    }
                                    gui.modifyingQuest = null;
                                } else if (gui.modifyingQuest == null) {
                                    gui.modifyingQuest = quest;
                                } else {
                                    gui.modifyingQuest.addOptionLink(quest.getId());
                                }
                                break;
                            case TRIGGER:
                                gui.setEditMenu(new GuiEditMenuTrigger(gui, player, quest));
                                break;
                            default:
                                break;
                        }
                    } else {
                        if (gui.isOpBook && GuiScreen.isShiftKeyDown()) {
                            OPBookHelper.reverseQuestCompletion(quest, player);
                        } else {
                            GuiQuestBook.selectedQuest = quest;
                            quest.onOpen(gui, player);
                        }
                    }
                    break;
                }
            }
        }

        if (Quest.isEditing) {
            for (final ReputationBar reputationBar : new ArrayList<>(this.getReputationBars())) {
                reputationBar.mouseClicked(gui, x, y);
            }
        }
    }
}
