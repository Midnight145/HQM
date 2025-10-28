package hardcorequesting.client.interfaces;

import net.minecraft.client.gui.GuiScreen;

import cpw.mods.fml.client.config.GuiConfig;
import hardcorequesting.ModInformation;
import hardcorequesting.config.ModConfig;

public class HQMConfigGui extends GuiConfig {

    public HQMConfigGui(GuiScreen parentScreen) {
        super(
            parentScreen,
            ModConfig.getConfigElements(),
            ModInformation.ID,
            false,
            false,
            GuiConfig.getAbridgedConfigPath(ModConfig.config.toString()));
    }
}
