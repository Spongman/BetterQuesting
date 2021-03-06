package betterquesting.api2.client.gui.themes.presets;

import betterquesting.api2.client.gui.resources.colors.GuiColorPulse;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import net.minecraft.util.ResourceLocation;
import betterquesting.api2.client.gui.themes.ThemeRegistry;
import betterquesting.core.BetterQuesting;

public enum PresetColor
{
	TEXT_HEADER("text_header"),
	TEXT_MAIN("text_main"),
	TEXT_AUX_0("text_aux_0"),
	TEXT_AUX_1("text_aux_1"),
	
	GUI_DIVIDER("gui_divider"),
	
	GRID_MAJOR("grid_major"),
	GRID_MINOR("grid_minor"),
	
	BTN_DISABLED("btn_disabled"),
	BTN_IDLE("btn_idle"),
	BTN_HOVER("btn_hover"),
	
	QUEST_LINE_LOCKED("quest_line_locked"),
	QUEST_LINE_UNLOCKED("quest_line_unlocked"),
	QUEST_LINE_PENDING("quest_line_pending"),
	QUEST_LINE_COMPLETE("quest_line_complete"),
	
	QUEST_ICON_LOCKED("quest_icon_locked"),
	QUEST_ICON_UNLOCKED("quest_icon_unlocked"),
	QUEST_ICON_PENDING("quest_icon_pending"),
	QUEST_ICON_COMPLETE("quest_icon_complete");
	
	private final ResourceLocation key;
	
	private PresetColor(String key)
	{
		this.key = new ResourceLocation(BetterQuesting.MODID, key);
	}
	
	public IGuiColor getColor()
	{
		return ThemeRegistry.INSTANCE.getColor(this.key);
	}
	
	public ResourceLocation getKey()
	{
		return this.key;
	}
	
	public static void registerColors(ThemeRegistry reg)
	{
		reg.setDefaultColor(TEXT_HEADER.key, new GuiColorStatic(0, 0, 0, 255)); // Headers
		reg.setDefaultColor(TEXT_MAIN.key, new GuiColorStatic(0, 0, 0, 255)); // Paragraphs
		reg.setDefaultColor(TEXT_AUX_0.key, new GuiColorStatic(255, 255, 255, 255)); // Dark panels
		reg.setDefaultColor(TEXT_AUX_1.key, new GuiColorStatic(0, 0, 0, 255)); // Light panels
		
		reg.setDefaultColor(GUI_DIVIDER.key, new GuiColorStatic(0, 0, 0, 255));
		
		reg.setDefaultColor(GRID_MAJOR.key, new GuiColorStatic(0, 0, 0, 255));
		reg.setDefaultColor(GRID_MINOR.key, new GuiColorStatic(0, 0, 0, 255));
		
		reg.setDefaultColor(BTN_DISABLED.key, new GuiColorStatic(128, 128, 128, 255));
		reg.setDefaultColor(BTN_IDLE.key, new GuiColorStatic(255, 255, 255, 255));
		reg.setDefaultColor(BTN_HOVER.key, new GuiColorStatic(16777120));
		
		reg.setDefaultColor(QUEST_LINE_LOCKED.key, new GuiColorStatic(192, 0, 0, 255));
		reg.setDefaultColor(QUEST_LINE_UNLOCKED.key, new GuiColorPulse(quickMix(255, 255, 0, 255), quickMix(128, 128, 0, 255), 1F, 0F));
		reg.setDefaultColor(QUEST_LINE_PENDING.key, new GuiColorStatic(0, 255, 0, 255));
		reg.setDefaultColor(QUEST_LINE_COMPLETE.key, new GuiColorStatic(0, 255, 0, 255));
		
		reg.setDefaultColor(QUEST_ICON_LOCKED.key, new GuiColorStatic(128, 128, 128, 255));
		reg.setDefaultColor(QUEST_ICON_UNLOCKED.key, new GuiColorPulse(quickMix(192, 0, 0, 255), quickMix(96, 0, 0, 255), 1F, 0F));
		reg.setDefaultColor(QUEST_ICON_PENDING.key, new GuiColorPulse(quickMix(0, 255, 255, 255), quickMix(0, 128, 128, 255), 1F, 0F));
		reg.setDefaultColor(QUEST_ICON_COMPLETE.key, new GuiColorStatic(0, 255, 0, 255));
	}
	
	private static int quickMix(int red, int green, int blue, int alpha)
	{
		return ((alpha & 255) << 24) | ((red & 255) << 16) | ((green & 255) << 8) | (blue & 255);
	}
}
