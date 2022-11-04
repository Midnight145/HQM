package hardcorequesting.quests;

import java.util.Arrays;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import hardcorequesting.FileVersion;
import hardcorequesting.QuestingData;
import hardcorequesting.SaveHelper;
import hardcorequesting.client.EditMode;
import hardcorequesting.client.interfaces.GuiBase;
import hardcorequesting.client.interfaces.GuiEditMenuReputationSetting;
import hardcorequesting.client.interfaces.GuiQuestBook;
import hardcorequesting.client.interfaces.ResourceHelper;
import hardcorequesting.network.DataBitHelper;
import hardcorequesting.network.DataReader;
import hardcorequesting.network.DataWriter;
import hardcorequesting.reputation.Reputation;
import hardcorequesting.reputation.ReputationMarker;
import net.minecraft.entity.player.EntityPlayer;

public abstract class QuestTaskReputation extends QuestTask {
	// for this task to be completed, all reputation settings (up to 4) has to be
	// completed at the same time, therefore it's not saved whether you've completed
	// one of these reputation settings, just if you've completed it all

	private ReputationSetting[] settings = {};

	public ReputationSetting[] getSettings() { return this.settings; }

	public static class ReputationSetting {
		private final Reputation reputation;
		private ReputationMarker lower;
		private ReputationMarker upper;
		private final boolean inverted;

		public ReputationSetting(Reputation reputation, ReputationMarker lower, ReputationMarker upper,
				boolean inverted) {
			this.reputation = reputation;
			this.lower = lower;
			this.upper = upper;
			this.inverted = inverted;
		}

		public Reputation getReputation() { return this.reputation; }

		public ReputationMarker getLower() { return this.lower; }

		public ReputationMarker getUpper() { return this.upper; }

		public boolean isInverted() { return this.inverted; }

		public boolean isValid(String playerName) {
			if (this.getReputation() == null || !this.getReputation().isValid()) { return false; }
			final ReputationMarker current = this.getReputation()
					.getCurrentMarker(this.getReputation().getValue(playerName));

			return ((this.lower == null || this.lower.getValue() <= current.getValue())
					&& (this.upper == null || current.getValue() <= this.upper.getValue())) != this.inverted;
		}

		public void setLower(ReputationMarker lower) { this.lower = lower; }

		public void setUpper(ReputationMarker upper) { this.upper = upper; }
	}

	public void setSetting(int id, ReputationSetting setting) {
		if (id >= this.settings.length) {
			this.settings = Arrays.copyOf(this.settings, this.settings.length + 1);
			SaveHelper.add(SaveHelper.EditType.REPUTATION_TASK_CREATE);
		}
		else {
			SaveHelper.add(SaveHelper.EditType.REPUTATION_TASK_CHANGE);
		}

		this.settings[id] = setting;
	}

	public QuestTaskReputation(Quest parent, String description, String longDescription, int startOffsetY) {
		super(parent, description, longDescription);
		this.startOffsetY = startOffsetY;
	}

	protected boolean isPlayerInRange(EntityPlayer player) {
		if (this.settings.length > 0) {

			final QuestDataTask data = this.getData(player);
			if (!data.completed && !player.worldObj.isRemote) {
				final String name = QuestingData.getUserName(player);
				for (final ReputationSetting setting : this.settings) {
					if (!setting.isValid(name)) { return false; }
				}

				return true;
			}
		}
		return false;
	}

	@Override
	public void save(DataWriter dw) {
		dw.writeData(this.settings.length, DataBitHelper.REPUTATION_SETTING);
		for (final ReputationSetting setting : this.settings) {
			dw.writeData(setting.getReputation().getId(), DataBitHelper.REPUTATION);
			dw.writeBoolean(setting.lower != null);
			if (setting.lower != null) {
				dw.writeData(setting.lower.getId(), DataBitHelper.REPUTATION_MARKER);
			}
			dw.writeBoolean(setting.upper != null);
			if (setting.upper != null) {
				dw.writeData(setting.upper.getId(), DataBitHelper.REPUTATION_MARKER);
			}
			dw.writeBoolean(setting.inverted);
		}
	}

	@Override
	public void load(DataReader dr, FileVersion version) {
		final int count = dr.readData(DataBitHelper.REPUTATION_SETTING);
		this.settings = new ReputationSetting[count];
		for (int i = 0; i < count; i++) {
			final Reputation reputation = Reputation.getReputation(dr.readData(DataBitHelper.REPUTATION));
			final ReputationMarker lower = dr.readBoolean()
					? reputation.getMarker(dr.readData(DataBitHelper.REPUTATION_MARKER))
					: null;
			final ReputationMarker upper = dr.readBoolean()
					? reputation.getMarker(dr.readData(DataBitHelper.REPUTATION_MARKER))
					: null;
			final boolean inverted = dr.readBoolean();
			this.settings[i] = new ReputationSetting(reputation, lower, upper, inverted);
		}
	}

	private static final int OFFSET_Y = 27;
	private final int startOffsetY;

	@Override
	@SideOnly(Side.CLIENT)
	public void draw(GuiQuestBook gui, EntityPlayer player, int mX, int mY) {
		String info = null;
		final int size = Quest.isEditing
				? Math.min(this.settings.length + 1, DataBitHelper.REPUTATION_SETTING.getMaximum())
				: this.settings.length;
		for (int i = 0; i < size; i++) {
			gui.applyColor(0xFFFFFFFF);
			ResourceHelper.bindResource(GuiBase.MAP_TEXTURE);

			if (i >= this.settings.length) {
				gui.drawRect(START_X + Reputation.BAR_X, START_Y + this.startOffsetY + i * OFFSET_Y + Reputation.BAR_Y,
						Reputation.BAR_SRC_X, Reputation.BAR_SRC_Y, Reputation.BAR_WIDTH, Reputation.BAR_HEIGHT);
			}
			else {
				final ReputationSetting setting = this.settings[i];
				info = setting.reputation.draw(gui, START_X, START_Y + this.startOffsetY + i * OFFSET_Y, mX, mY, info,
						this.getPlayerForRender(player), true, setting.lower, setting.upper, setting.inverted, null,
						null, this.getData(player).completed);
			}
		}

		if (info != null) {
			gui.drawMouseOver(info, mX + gui.getLeft(), mY + gui.getTop());
		}
	}

	protected EntityPlayer getPlayerForRender(EntityPlayer player) { return player; }

	@Override
	@SideOnly(Side.CLIENT)
	public void onClick(GuiQuestBook gui, EntityPlayer player, int mX, int mY, int b) {
		if (Quest.isEditing && gui.getCurrentMode() != EditMode.NORMAL) {
			final int size = Math.min(this.settings.length + 1, DataBitHelper.REPUTATION_SETTING.getMaximum());
			for (int i = 0; i < size; i++) {
				if (gui.inBounds(START_X, START_Y + this.startOffsetY + i * OFFSET_Y, Reputation.BAR_WIDTH, 20, mX,
						mY)) {
					if (gui.getCurrentMode() == EditMode.REPUTATION_TASK) {
						gui.setEditMenu(new GuiEditMenuReputationSetting(gui, player, this, i,
								i >= this.settings.length ? null : this.settings[i]));
					}
					else if (gui.getCurrentMode() == EditMode.DELETE && i < this.settings.length) {
						this.removeSetting(i);
						SaveHelper.add(SaveHelper.EditType.REPUTATION_TASK_REMOVE);
					}
					break;
				}
			}
		}
	}

	public void removeSetting(int i) {
		int id = 0;
		final ReputationSetting[] settings = new ReputationSetting[this.settings.length - 1];
		for (int j = 0; j < this.settings.length; j++) {
			if (j != i) {
				settings[id] = this.settings[j];
				id++;
			}
		}
		this.settings = settings;
	}

	@Override
	public void onUpdate(EntityPlayer player, DataReader dr) {

	}

	@Override
	public float getCompletedRatio(String playerName) {
		final int count = this.settings.length;
		if (count == 0) { return 0; }

		int valid = 0;
		for (final ReputationSetting setting : this.settings) {
			if (setting.isValid(playerName)) {
				valid++;
			}
		}

		return (float) valid / count;
	}

	@Override
	public void mergeProgress(String playerName, QuestDataTask own, QuestDataTask other) {
		if (other.completed) {
			own.completed = true;
		}
	}

	@Override
	public void autoComplete(String playerName) { this.getData(playerName).completed = true; }
}
