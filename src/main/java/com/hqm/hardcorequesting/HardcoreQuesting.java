package com.hqm.hardcorequesting;

import java.io.File;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;

import com.hqm.hardcorequesting.blocks.ModBlocks;
import com.hqm.hardcorequesting.commands.CommandHandler;
import com.hqm.hardcorequesting.config.ConfigHandler;
import com.hqm.hardcorequesting.items.ModItems;
import com.hqm.hardcorequesting.network.PacketHandler;
import com.hqm.hardcorequesting.proxies.CommonProxy;
import com.hqm.hardcorequesting.quests.Quest;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.NetworkRegistry;

@Mod(
    modid = ModInformation.ID,
    name = ModInformation.NAME,
    version = ModInformation.VERSION,
    guiFactory = "com.hqm.hardcorequesting.client.interfaces.HQMModGuiFactory")
public class HardcoreQuesting {

    @Instance(ModInformation.ID)
    public static HardcoreQuesting instance;

    @SidedProxy(
        clientSide = "com.hqm.hardcorequesting.proxies.ClientProxy",
        serverSide = "com.hqm.hardcorequesting.proxies.CommonProxy")
    public static CommonProxy proxy;
    public static CreativeTabs HQMTab = new HQMTab();

    public static String path;

    public static File configDir;

    public static FMLEventChannel packetHandler;

    private static EntityPlayer commandUser;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        new com.hqm.hardcorequesting.EventHandler();
        packetHandler = NetworkRegistry.INSTANCE.newEventDrivenChannel(ModInformation.CHANNEL);

        path = event.getModConfigurationDirectory()
            .getAbsolutePath() + File.separator
            + ModInformation.CONFIG_LOC_NAME.toLowerCase()
            + File.separator;
        configDir = new File(path);
        ConfigHandler.initModConfig(path);
        ConfigHandler.initEditConfig(path);

        proxy.init();
        proxy.initRenderers();
        proxy.initSounds(path);

        ModItems.init();

        ModBlocks.init();
        ModBlocks.registerBlocks();
        ModBlocks.registerTileEntities();

        // Quest.init(this.path);
    }

    @EventHandler
    public void load(FMLInitializationEvent event) {
        new File(configDir + File.separator + "QuestFiles").mkdir();
        FMLCommonHandler.instance()
            .bus()
            .register(instance);

        packetHandler.register(new PacketHandler());
        new WorldEventListener();
        new PlayerDeathEventListener();
        new PlayerTracker();

        ModItems.registerRecipes();
        ModBlocks.registerRecipes();

        FMLInterModComms.sendMessage("Waila", "register", "hardcorequesting.waila.Provider.callbackRegister");
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        Quest.init(path);
    }

    @EventHandler
    public void modsLoaded(FMLPostInitializationEvent event) {

    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(CommandHandler.instance);
    }

    @EventHandler
    public void serverAboutToStart(FMLServerAboutToStartEvent event) {

    }

    @EventHandler
    public void serverAboutToStart(FMLServerStoppingEvent event) {

    }

    public static EntityPlayer getPlayer() {
        return commandUser;
    }

    public static void setPlayer(EntityPlayer player) {
        commandUser = player;
    }
}
