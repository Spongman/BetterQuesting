package betterquesting.client.gui.editors;

import java.util.ArrayList;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.opengl.GL11;
import betterquesting.api.client.gui.IGuiEmbedded;
import betterquesting.api.client.gui.INeedsRefresh;
import betterquesting.api.client.gui.IVolatileScreen;
import betterquesting.api.client.gui.QuestLineButtonTree;
import betterquesting.api.client.gui.premade.screens.GuiScreenThemed;
import betterquesting.api.client.toolbox.IToolboxTab;
import betterquesting.api.quests.IQuestLineContainer;
import betterquesting.api.utils.RenderUtils;
import betterquesting.client.gui.GuiQuestLinesEmbedded;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestLineDatabase;
import betterquesting.registry.ThemeRegistry;
import betterquesting.registry.ToolboxRegistry;

public class GuiQuestLineDesigner extends GuiScreenThemed implements IVolatileScreen, INeedsRefresh
{
	int qIndex = -1;
	IQuestLineContainer qLine;
	GuiQuestLinesEmbedded qlGui;
	int tabIndex = 0;
	IToolboxTab toolTab = null;
	IGuiEmbedded tabGui = null;
	
	public GuiQuestLineDesigner(GuiScreen parent, IQuestLineContainer qLine)
	{
		super(parent, "betterquesting.title.designer"); // This title won't be shown but for the sake of labels...
		this.qLine = qLine;
		this.qIndex = QuestLineDatabase.INSTANCE.getKey(qLine);
	}
	
	public GuiQuestLinesEmbedded getEmbeddedGui()
	{
		return qlGui;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui()
	{
		super.initGui();
		
		this.sizeX -= 96;
		((GuiButton)this.buttonList.get(0)).xPosition = guiLeft + sizeX/2 - 100;
		
		if(qIndex < 0 || qIndex >= QuestDatabase.questLines.size())
		{
			mc.displayGuiScreen(parent);
			return;
		} else
		{
			qLine = QuestDatabase.questLines.get(qIndex);
		}
		
		GuiQuestLinesEmbedded oldGui = qlGui;
		qlGui = new GuiQuestLinesEmbedded(guiLeft + 16, guiTop + 16, sizeX - 32, sizeY - 32);
		qlGui.setQuestLine(new QuestLineButtonTree(qLine));
		
		if(oldGui != null) // Preserve old settings
		{
			qlGui.copySettings(oldGui);
		}
		
		qlGui.clampScroll();
		
		ArrayList<ToolboxTab> tabList = ToolboxRegistry.getList();
		
		if(tabGui == null && tabList.size() > tabIndex)
		{
			toolTab = tabList.get(tabIndex);
			
			if(!toolTab.hasInit(this))
			{
				toolTab.init_(this);
			}
			
			tabGui = toolTab.getTabGui(this, guiLeft + sizeX + 16, guiTop + 32, 64, sizeY - 48);
			
			if(tabGui != null)
			{
				tabGui.refreshGui();
			}
		} else if(tabGui != null)
		{
			tabGui.refresh_(this, guiLeft + sizeX + 16, guiTop + 32, 64, sizeY - 48);
		}
		
		GuiButtonQuesting btnLeft = new GuiButtonQuesting(1, guiLeft + sizeX, guiTop + 16, 16, 16, "<");
		GuiButtonQuesting btnRight = new GuiButtonQuesting(2, guiLeft + sizeX + 80, guiTop + 16, 16, 16, ">");
		
		if(tabList.size() <= 1)
		{
			btnLeft.enabled = false;
			btnRight.enabled = false;
		}
		
		buttonList.add(btnLeft);
		buttonList.add(btnRight);
	}
	
	/**
	 * Modified version of super method to support extra toolbar
	 */
	public void drawScreen_(int mx, int my, float partialTick)
	{
		this.drawDefaultBackground();
		
		this.mc.renderEngine.bindTexture(currentTheme().getGuiTexture());
		
		for(int i = 0; i < 96; i += 16)
		{
			for(int j = 0; j < sizeY; j += 16)
			{
				int tx = 16;
				int ty = 16;
				
				if(i == 0)
				{
					tx -= 16;
				} else if(i == 80)
				{
					tx += 16;
				}
				
				if(j == 0)
				{
					ty -= 16;
				} else if(j == sizeY - 16)
				{
					ty += 16;
				}
				
				this.drawTexturedModalRect(guiLeft + sizeX + i, guiTop + j, tx, ty, 16, 16);
			}
		}
		
		for(int i = 0; i < this.sizeX; i += 16)
		{
			for(int j = 0; j < this.sizeY; j += 16)
			{
				int tx = 16;
				int ty = 16;
				
				if(i == 0)
				{
					tx -= 16;
				} else if(i == this.sizeX - 16)
				{
					tx += 16;
				}
				
				if(j == 0)
				{
					ty -= 16;
				} else if(j == this.sizeY - 16)
				{
					ty += 16;
				}
				
				this.drawTexturedModalRect(i + this.guiLeft, j + this.guiTop, tx, ty, 16, 16);
			}
		}
		
		String tmp = I18n.format("betterquesting.title.designer");
		this.fontRendererObj.drawString(EnumChatFormatting.BOLD + tmp, this.guiLeft + (sizeX/2) - this.fontRendererObj.getStringWidth(tmp)/2, this.guiTop + 18, getTextColor(), false);
		
        int k;
        
        for (k = 0; k < this.buttonList.size(); ++k)
        {
            ((GuiButton)this.buttonList.get(k)).drawButton(this.mc, mx, my);
        }
        
        for (k = 0; k < this.labelList.size(); ++k)
        {
            ((GuiLabel)this.labelList.get(k)).func_146159_a(this.mc, mx, my);
        }
		
		this.mc.renderEngine.bindTexture(currentTheme().getGuiTexture());
		GL11.glColor4f(1F, 1F, 1F, 1F);
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		drawScreen_(mx, my, partialTick);
		
		if(QuestDatabase.updateUI)
		{
			initGui();
			QuestDatabase.updateUI = false;
		}
		
		RenderUtils.DrawLine(guiLeft + sizeX + 16, guiTop + 32, guiLeft + sizeX + 80, guiTop + 32, partialTick, ThemeRegistry.curTheme().textColor());
		
		if(toolTab != null)
		{
			String tabTitle = EnumChatFormatting.UNDERLINE + toolTab.getDisplayName();
			this.fontRendererObj.drawString(tabTitle, guiLeft + sizeX + 48 - fontRendererObj.getStringWidth(tabTitle)/2, guiTop + 16 + 2, ThemeRegistry.curTheme().textColor().getRGB(), false);
		}
		
		if(tabGui != null)
		{
			tabGui.drawGui(mx, my, partialTick);
		}
		
		if(qlGui != null)
		{
			GL11.glPushMatrix();
			GL11.glColor4f(1F, 1F, 1F, 1f);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			qlGui.drawGui(mx, my, partialTick);
			GL11.glPopMatrix();
		}
		
		if(tabGui != null)
		{
			tabGui.drawOverlays(mx, my, partialTick);
		}
	}
	
	@Override
	public void refreshGui()
	{
		this.initGui();
	}
	
	@Override
	public void actionPerformed(GuiButton button)
	{
		super.actionPerformed(button);
		
		ArrayList<ToolboxTab> tabList = ToolboxRegistry.getList();
		int ts = tabList.size();
		
		if(ts > 1)
		{
			boolean flag = false;
			
			if(button.id == 1)
			{
				tabIndex = ((tabIndex - 1)%ts + ts)%ts;
				flag = true;
			} else if(button.id == 2)
			{
				tabIndex = (tabIndex + 1)%ts;
				flag = true;
			}
			
			if(flag)
			{
				toolTab = tabList.get(tabIndex);
				
				if(!toolTab.hasInit(this))
				{
					toolTab.init_(this);
				}
				
				tabGui = toolTab.getTabGui(this, guiLeft + sizeX + 16, guiTop + 32, 64, sizeY - 48);
				
				if(tabGui != null)
				{
					tabGui.refreshGui();
				}
			}
		}
	}
}
