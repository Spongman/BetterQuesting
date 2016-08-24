package betterquesting.commands.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import betterquesting.api.quests.IQuestContainer;
import betterquesting.api.quests.properties.QuestProperties;
import betterquesting.api.quests.tasks.ITaskBase;
import betterquesting.commands.QuestCommandBase;
import betterquesting.network.PacketSender;
import betterquesting.quests.QuestDatabase;

public class QuestCommandComplete extends QuestCommandBase
{
	public String getUsageSuffix()
	{
		return "<quest_id> [username|uuid]";
	}
	
	public boolean validArgs(String[] args)
	{
		return args.length == 3;
	}
	
	@SuppressWarnings("unchecked")
	public List<String> autoComplete(ICommandSender sender, String[] args)
	{
		ArrayList<String> list = new ArrayList<String>();
		
		if(args.length == 2)
		{
			for(int i : QuestDatabase.INSTANCE.getAllKeys())
			{
				list.add("" + i);
			}
		} else if(args.length == 3)
		{
			return CommandBase.getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
		}
		
		return list;
	}
	
	@Override
	public String getCommand()
	{
		return "complete";
	}
	
	@Override
	public void runCommand(CommandBase command, ICommandSender sender, String[] args)
	{
		UUID uuid = null;
		EntityPlayerMP player = null;
		
		player = MinecraftServer.getServer().getConfigurationManager().func_152612_a(args[2]);
		
		if(player == null)
		{
			try
			{
				uuid = UUID.fromString(args[2]);
			} catch(Exception e)
			{
				throw getException(command);
			}
		} else
		{
			uuid = player.getUniqueID();
		}
		
		try
		{
			int id = Integer.parseInt(args[1].trim());
			IQuestContainer quest = QuestDatabase.INSTANCE.getValue(id);
			quest.setComplete(uuid, 0);
			
			int done = 0;
			
			if(!quest.getInfo().getProperty(QuestProperties.LOGIC_TASK).GetResult(done, quest.getTasks().size())) // Preliminary check
			{
				for(ITaskBase task : quest.getTasks().getAllValues())
				{
					task.setComplete(uuid);
					done += 1;
					
					if(quest.getInfo().getProperty(QuestProperties.LOGIC_TASK).GetResult(done, quest.getTasks().size()))
					{
						break; // Only complete enough quests to claim the reward
					}
				}
			}
			
			sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.complete", new ChatComponentTranslation(quest.getUnlocalisedName()), player.getCommandSenderName()));
		} catch(Exception e)
		{
			throw getException(command);
		}
		
		PacketSender.INSTANCE.sendToAll(QuestDatabase.INSTANCE.getSyncPacket());
	}
}
