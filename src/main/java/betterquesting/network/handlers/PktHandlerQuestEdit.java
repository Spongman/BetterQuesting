package betterquesting.network.handlers;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.network.IPacketHandler;
import betterquesting.api.network.PacketTypeNative;
import betterquesting.api.quests.IQuestContainer;
import betterquesting.api.quests.properties.QuestProperties;
import betterquesting.api.quests.tasks.ITaskBase;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;

public class PktHandlerQuestEdit implements IPacketHandler
{
	@Override
	public ResourceLocation getRegistryName()
	{
		return PacketTypeNative.QUEST_EDIT.GetLocation();
	}
	
	@Override
	public void handleServer(NBTTagCompound data, EntityPlayerMP sender)
	{
		if(sender == null)
		{
			return;
		}
		
		boolean isOp = MinecraftServer.getServer().getConfigurationManager().func_152596_g(sender.getGameProfile());
		
		if(!isOp)
		{
			return;
		}
		
		int aID = !data.hasKey("action")? -1 : data.getInteger("action");
		int qID = !data.hasKey("questID")? -1 : data.getInteger("questID");
		IQuestContainer quest = QuestDatabase.INSTANCE.getValue(qID);
		
		EnumPacketAction action = null;
		
		if(aID < 0 || aID >= EnumPacketAction.values().length)
		{
			return;
		}
		
		action = EnumPacketAction.values()[aID];
		
		if(action == EnumPacketAction.EDIT && quest != null)
		{
			quest.readPacket(data);
			PacketSender.INSTANCE.sendToAll(quest.getSyncPacket());
			return;
		} else if(action == EnumPacketAction.REMOVE)
		{
			if(quest == null || qID < 0)
			{
				BetterQuesting.logger.log(Level.ERROR, sender.getCommandSenderName() + " tried to delete non-existent quest with ID:" + qID);
				return;
			}
			
			BetterQuesting.logger.log(Level.INFO, "Player " + sender.getCommandSenderName() + " deleted quest " + quest.getUnlocalisedName());
			QuestDatabase.INSTANCE.remove(qID);
			PacketSender.INSTANCE.sendToAll(QuestDatabase.INSTANCE.getSyncPacket());
			return;
		} else if(action == EnumPacketAction.SET && quest != null) // Force Complete/Reset
		{
			if(data.getBoolean("state"))
			{
				quest.setComplete(sender.getUniqueID(), 0);
				
				int done = 0;
				
				if(!quest.getInfo().getProperty(QuestProperties.LOGIC_TASK).GetResult(done, quest.getTasks().size())) // Preliminary check
				{
					for(ITaskBase task : quest.getTasks().getAllValues())
					{
						task.setComplete(sender.getUniqueID());
						done += 1;
						
						if(quest.getInfo().getProperty(QuestProperties.LOGIC_TASK).GetResult(done, quest.getTasks().size()))
						{
							break; // Only complete enough quests to claim the reward
						}
					}
				}
			} else
			{
				quest.resetAll(true);
			}
			
			PacketSender.INSTANCE.sendToAll(quest.getSyncPacket());
			return;
		} else if(action == EnumPacketAction.ADD)
		{
			QuestDatabase.INSTANCE.add(new QuestInstance(), QuestDatabase.INSTANCE.nextID());
			PacketSender.INSTANCE.sendToAll(QuestDatabase.INSTANCE.getSyncPacket());
			return;
		}
	}

	@Override
	public void handleClient(NBTTagCompound data)
	{
		return;
	}
}
