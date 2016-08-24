package betterquesting.api.quests.tasks;

import java.util.UUID;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.client.gui.IGuiEmbedded;
import betterquesting.api.database.IJsonSaveLoad;
import betterquesting.api.quests.IQuestContainer;
import com.google.gson.JsonObject;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public interface ITaskBase extends IJsonSaveLoad<JsonObject>
{
	public String getUnlocalisedName();
	public ResourceLocation getFactoryID();
	
	public void update(EntityPlayer player, IQuestContainer quest);
	public void detect(EntityPlayer player, IQuestContainer quest);
	
	public boolean isComplete(UUID uuid);
	public void setComplete(UUID uuid);
	
	public void resetUser(UUID uuid);
	public void resetAll();
	
	@SideOnly(Side.CLIENT)
	public IGuiEmbedded getTaskGui(int x, int y, int w, int h, IQuestContainer quest);
	
	@SideOnly(Side.CLIENT)
	public GuiScreen getTaskEditor(GuiScreen parent, IQuestContainer quest);
}
