package betterquesting.api.client.gui;

import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.gui.controls.GuiButtonQuestInstance;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.questing.IQuestLineEntry;

/**
 * Builds a tree of connected buttons based on the given quest line.
 * Intended for use in the within the draggable quest line GUI.<br>
 * <b>WARNING:</b> Button IDs will all be initialized as 0
 */
@SideOnly(Side.CLIENT)
public class QuestLineButtonTree
{
	private IQuestLine line;
	private ArrayList<GuiButtonQuestInstance> buttonTree = new ArrayList<GuiButtonQuestInstance>();
	private int minX = Integer.MAX_VALUE;
	private int minY = Integer.MAX_VALUE;
	private int maxX = Integer.MIN_VALUE;
	private int maxY = Integer.MAX_VALUE;
	
	public QuestLineButtonTree(IQuestLine line)
	{
		this.line = line;
		RebuildTree();
	}

	public int getLeft()
	{
		return buttonTree.isEmpty() ? 0 : minX;
	}
	
	public int getTop()
	{
		return buttonTree.isEmpty() ? 0 : minY;
	}

	public int getWidth()
	{
		return buttonTree.isEmpty() ? 0 : (maxX - minX);
	}
	
	public int getHeight()
	{
		return buttonTree.isEmpty() ? 0 : (maxY - minY);
	}
	
	public IQuestLine getQuestLine()
	{
		return line;
	}
	
	public List<GuiButtonQuestInstance> getButtonTree()
	{
		return buttonTree;
	}
	
	public GuiButtonQuestInstance getButtonAt(int x, int y)
	{
		if(line == null)
		{
			return null;
		}
		
		int id = line.getQuestAt(x, y);
		IQuest quest = QuestingAPI.getAPI(ApiReference.QUEST_DB).getValue(id);
		
		if(quest == null)
		{
			return null;
		}
		
		for(GuiButtonQuestInstance btn : buttonTree)
		{
			if(btn.getQuest() == quest)
			{
				return btn;
			}
		}
		
		return null;
	}
	
	public void RebuildTree()
	{
		buttonTree.clear();
		
		minX = minY = Integer.MAX_VALUE;
		maxX = maxY = Integer.MIN_VALUE;
		
		if(line == null)
		{
			return;
		}
		
		for(int id : line.getAllKeys())
		{
			IQuest quest = QuestingAPI.getAPI(ApiReference.QUEST_DB).getValue(id);
			IQuestLineEntry entry = line.getValue(id);
			
			if(quest != null && entry != null)
			{
				buttonTree.add(new GuiButtonQuestInstance(0, entry.getPosX(), entry.getPosY(), entry.getSize(), entry.getSize(), quest));
			}
		}
		
		// Offset origin to 0,0 and establish bounds
		for(GuiButtonQuestInstance btn : buttonTree)
		{
			if(btn == null)
			{
				continue;
			}
			
			minX = Math.min(minX, btn.x);
			minY = Math.min(minY, btn.y);
			maxX = Math.max(maxX, btn.x + btn.width);
			maxY = Math.max(maxY, btn.y + btn.height);
			
			for(GuiButtonQuestInstance b2 : buttonTree)
			{
				if(b2 == null || btn == b2 || btn.getQuest() == null)
				{
					continue;
				}
				
				if(btn.getQuest().getPrerequisites().contains(b2.getQuest()))
				{
					btn.addParent(b2);
				}
			}
		}
	}
}
