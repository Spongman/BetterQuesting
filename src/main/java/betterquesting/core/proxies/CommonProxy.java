package betterquesting.core.proxies;

import net.minecraftforge.common.MinecraftForge;
import betterquesting.api.IQuestingExpansion;
import betterquesting.client.UpdateNotification;
import betterquesting.core.BetterQuesting;
import betterquesting.core.ExpansionLoader;
import betterquesting.core.ParentAPI;
import betterquesting.handlers.EventHandler;
import betterquesting.handlers.GuiHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.NetworkRegistry;

public class CommonProxy
{
	public boolean isClient()
	{
		return false;
	}
	
	public void registerHandlers()
	{
		EventHandler handler = new EventHandler();
		MinecraftForge.EVENT_BUS.register(handler);
		MinecraftForge.TERRAIN_GEN_BUS.register(handler);
		FMLCommonHandler.instance().bus().register(handler);
		
		FMLCommonHandler.instance().bus().register(new UpdateNotification());
		
		NetworkRegistry.INSTANCE.registerGuiHandler(BetterQuesting.instance, new GuiHandler());
	}
	
	public void registerExpansions()
	{
		for(IQuestingExpansion exp : ExpansionLoader.INSTANCE.getAllExpansions())
		{
			exp.registerCommon(ParentAPI.API);
		}
	}
}
