package betterquesting.client.gui2;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.gui.misc.INeedsRefresh;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.controls.IPanelButton;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.events.IPEventListener;
import betterquesting.api2.client.gui.events.PEventBroadcaster;
import betterquesting.api2.client.gui.events.PanelEvent;
import betterquesting.api2.client.gui.events.types.PEventButton;
import betterquesting.api2.client.gui.misc.*;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.CanvasTextured;
import betterquesting.api2.client.gui.panels.PanelLegacyEmbed;
import betterquesting.api2.client.gui.panels.bars.PanelVScrollBar;
import betterquesting.api2.client.gui.panels.content.PanelLine;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.panels.lists.CanvasScrolling;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetLine;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.client.gui.editors.GuiQuestEditor;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestDatabase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.util.vector.Vector4f;

public class GuiQuest extends GuiScreenCanvas implements IPEventListener, INeedsRefresh
{
    private final int questID;
    
    private IQuest quest;
    
    private PanelButton btnTaskLeft;
    private PanelButton btnTaskRight;
    private PanelButton btnRewardLeft;
    private PanelButton btnRewardRight;
    
    private PanelButton btnDetect;
    private PanelButton btnClaim;
    
    private PanelTextBox titleReward;
    private PanelTextBox titleTask;
    
    private CanvasEmpty cvInner;
    
    private IGuiRect rectReward;
    private IGuiRect rectTask;
    
    private PanelLegacyEmbed pnReward;
    private PanelLegacyEmbed pnTask;
    
    private int rewardIndex = 0;
    private int taskIndex = 0;
    
    // TODO: Replace this stupid variable when legacy tasks/rewards are less dumb
    private long autoRefreshTime = 0;
    
    public GuiQuest(GuiScreen parent, int questID)
    {
        super(parent);
        this.questID = questID;
        autoRefreshTime = System.currentTimeMillis();
    }
    
    @Override
    public void drawPanel(int mx, int my, float partialTick)
    {
        if(System.currentTimeMillis() - autoRefreshTime > 1000)
        {
            autoRefreshTime = System.currentTimeMillis();
            this.updateButtons();
        }
        
        super.drawPanel(mx, my, partialTick);
    }
    
    @Override
    public void initPanel()
    {
        super.initPanel();
        
        this.quest = QuestDatabase.INSTANCE.getValue(questID);
        
        if(quest == null)
        {
            mc.displayGuiScreen(this.parent);
            return;
        }
    
        PEventBroadcaster.INSTANCE.register(this, PEventButton.class);
        // TODO: Register quest updated event
    
        // Background panel
        CanvasTextured cvBackground = new CanvasTextured(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 0, 0), 0), PresetTexture.PANEL_MAIN.getTexture());
        this.addPanel(cvBackground);
    
        PanelTextBox panTxt = new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(0, 16, 0, -32), 0), I18n.format(quest.getUnlocalisedName())).setAlignment(1);
        panTxt.setColor(PresetColor.TEXT_HEADER.getColor());
        cvBackground.addPanel(panTxt);
    
        if(QuestingAPI.getAPI(ApiReference.SETTINGS).canUserEdit(mc.player))
        {
            cvBackground.addPanel(new PanelButton(new GuiTransform(GuiAlign.BOTTOM_CENTER, -100, -16, 100, 16, 0), 0, I18n.format("gui.back")));
            cvBackground.addPanel(new PanelButton(new GuiTransform(GuiAlign.BOTTOM_CENTER, 0, -16, 100, 16, 0), 1, I18n.format("betterquesting.btn.edit")));
        } else
        {
            cvBackground.addPanel(new PanelButton(new GuiTransform(GuiAlign.BOTTOM_CENTER, -100, -16, 200, 16, 0), 0, I18n.format("gui.back")));
        }
        
        cvInner = new CanvasEmpty(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(16, 32, 16, 24), 0));
        cvBackground.addPanel(cvInner);
        
        if(quest.getRewards().size() > 0)
        {
            CanvasScrolling cvDesc = new CanvasScrolling(new GuiTransform(new Vector4f(0F, 0F, 0.5F, 0.5F), new GuiPadding(0, 0, 16, 16), 0));
            cvInner.addPanel(cvDesc);
            PanelTextBox paDesc = new PanelTextBox(new GuiRectangle(0, 0, cvDesc.getTransform().getWidth(), 0), I18n.format(quest.getUnlocalisedDescription()), true);
            cvDesc.addPanel(paDesc);
    
            PanelVScrollBar paDescScroll = new PanelVScrollBar(new GuiTransform(GuiAlign.quickAnchor(GuiAlign.TOP_CENTER, GuiAlign.MID_CENTER), new GuiPadding(-16, 0, 8, 16), 0));
            cvInner.addPanel(paDescScroll);
            cvDesc.setScrollDriverY(paDescScroll);
    
            btnClaim = new PanelButton(new GuiTransform(new Vector4f(0F, 1F, 0.5F, 1F), new GuiPadding(16, -16, 24, 0), 0), 6, I18n.format("betterquesting.btn.claim"));
            cvInner.addPanel(btnClaim);
    
            btnRewardLeft = new PanelButton(new GuiTransform(GuiAlign.BOTTOM_LEFT, new GuiPadding(0, -16, -16, 0), 0), 2, "<");
            btnRewardLeft.setEnabled(rewardIndex > 0);
            cvInner.addPanel(btnRewardLeft);
    
            btnRewardRight = new PanelButton(new GuiTransform(new Vector4f(0.5F, 1F, 0.5F, 1F), new GuiPadding(-24, -16, 8, 0), 0), 3, ">");
            btnRewardRight.setEnabled(rewardIndex < quest.getRewards().size() - 1);
            cvInner.addPanel(btnRewardRight);
            
            rectReward = new GuiTransform(new Vector4f(0F, 0.5F, 0.5F, 1F), new GuiPadding(0, 0, 8, 16), 0);
            rectReward.setParent(cvInner.getTransform());
            
            titleReward = new PanelTextBox(new GuiTransform(new Vector4f(0F, 0.5F, 0.5F, 0.5F), new GuiPadding(0, -16, 8, 0), 0), "?");
            titleReward.setColor(PresetColor.TEXT_HEADER.getColor()).setAlignment(1);
            cvInner.addPanel(titleReward);
            
            refreshRewardPanel();
        } else
        {
            CanvasScrolling cvDesc = new CanvasScrolling(new GuiTransform(GuiAlign.HALF_LEFT, new GuiPadding(0, 0, 16, 0), 0));
            cvInner.addPanel(cvDesc);
            PanelTextBox paDesc = new PanelTextBox(new GuiRectangle(0, 0, cvDesc.getTransform().getWidth(), 0), I18n.format(quest.getUnlocalisedDescription()), true);
            cvDesc.addPanel(paDesc);
    
            PanelVScrollBar paDescScroll = new PanelVScrollBar(new GuiTransform(GuiAlign.quickAnchor(GuiAlign.TOP_CENTER, GuiAlign.BOTTOM_CENTER), new GuiPadding(-16, 0, 8, 0), 0));
            cvInner.addPanel(paDescScroll);
            cvDesc.setScrollDriverY(paDescScroll);
        }
        
        if(quest.getTasks().size() > 0)
        {
            btnDetect = new PanelButton(new GuiTransform(new Vector4f(0.5F, 1F, 1F, 1F), new GuiPadding(24, -16, 16, 0), 0), 7, I18n.format("betterquesting.btn.detect_submit"));
            cvInner.addPanel(btnDetect);
    
            btnTaskLeft = new PanelButton(new GuiTransform(new Vector4f(0.5F, 1F, 0.5F, 1F), new GuiPadding(8, -16, -24, 0), 0), 4, "<");
            btnTaskLeft.setEnabled(taskIndex > 0);
            cvInner.addPanel(btnTaskLeft);
    
            btnTaskRight = new PanelButton(new GuiTransform(GuiAlign.BOTTOM_RIGHT, new GuiPadding(-16, -16, 0, 0), 0), 5, ">");
            btnTaskRight.setEnabled(taskIndex < quest.getTasks().size() - 1);
            cvInner.addPanel(btnTaskRight);
            
            rectTask = new GuiTransform(GuiAlign.HALF_RIGHT, new GuiPadding(8, 16, 0, 16), 0);
            rectTask.setParent(cvInner.getTransform());
    
            titleTask = new PanelTextBox(new GuiTransform(new Vector4f(0.5F, 0F, 1F, 0F), new GuiPadding(8, 0, 0, -16), 0), "?");
            titleTask.setColor(PresetColor.TEXT_HEADER.getColor()).setAlignment(1);
            cvInner.addPanel(titleTask);
            
            refreshTaskPanel();
        }
    
        IGuiRect ls0 = new GuiTransform(GuiAlign.TOP_CENTER, 0, 0, 0, 0, 0);
        ls0.setParent(cvInner.getTransform());
        IGuiRect le0 = new GuiTransform(GuiAlign.BOTTOM_CENTER, 0, 0, 0, 0, 0);
        le0.setParent(cvInner.getTransform());
        PanelLine paLine0 = new PanelLine(ls0, le0, PresetLine.GUI_DIVIDER.getLine(), 1, PresetColor.GUI_DIVIDER.getColor(), 1);
        cvInner.addPanel(paLine0);
    }
    
    @Override
    public void refreshGui()
    {
        this.updateButtons(); // TODO: Replace with a proper event because this interface is dumb
    }
    
    @Override
    public boolean onMouseClick(int mx, int my, int click) // TODO: Replace these with events when legacy support is removed
    {
        if(super.onMouseClick(mx, my, click))
        {
            this.updateButtons();
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean onMouseScroll(int mx, int my, int scroll) // TODO: Replace these with events when legacy support is removed
    {
        if(super.onMouseScroll(mx, my, scroll))
        {
            this.updateButtons();
            return true;
        }
    
        return false;
    }
    
    @Override
    public boolean onKeyTyped(char c, int keycode) // TODO: Replace these with events when legacy support is removed
    {
        if(super.onKeyTyped(c, keycode))
        {
            this.updateButtons();
            return true;
        }
        
        return false;
    }
    
    @Override
    public void onPanelEvent(PanelEvent event)
    {
        if(event instanceof PEventButton)
        {
            onButtonPress((PEventButton)event);
        }
    }
    
    private void onButtonPress(PEventButton event)
    {
        IPanelButton btn = event.getButton();
        
        if(btn.getButtonID() == 0) // Exit
        {
            mc.displayGuiScreen(this.parent);
        } else if(btn.getButtonID() == 1) // Edit
        {
            mc.displayGuiScreen(new GuiQuestEditor(this, quest));
        } else if(btn.getButtonID() == 2) // Reward previous
        {
            rewardIndex = MathHelper.clamp(rewardIndex - 1, 0, quest.getRewards().size() - 1);
            refreshRewardPanel();
        } else if(btn.getButtonID() == 3) // Reward next
        {
            rewardIndex = MathHelper.clamp(rewardIndex + 1, 0, quest.getRewards().size() - 1);
            refreshRewardPanel();
        } else if(btn.getButtonID() == 4) // Task previous
        {
            taskIndex = MathHelper.clamp(taskIndex - 1, 0, quest.getTasks().size() - 1);
            refreshTaskPanel();
        } else if(btn.getButtonID() == 5) // Task next
        {
            taskIndex = MathHelper.clamp(taskIndex + 1, 0, quest.getTasks().size() - 1);
            refreshTaskPanel();
        } else if(btn.getButtonID() == 6) // Reward claim
        {
            NBTTagCompound tags = new NBTTagCompound();
            tags.setInteger("questID", QuestDatabase.INSTANCE.getKey(quest));
            PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.CLAIM.GetLocation(), tags));
        } else if(btn.getButtonID() == 7) // Task detect/submit
        {
            NBTTagCompound tags = new NBTTagCompound();
            tags.setInteger("questID", QuestDatabase.INSTANCE.getKey(quest));
            PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.DETECT.GetLocation(), tags));
        }
    }
    
    private void refreshRewardPanel()
    {
        if(pnReward != null)
        {
            cvInner.removePanel(pnReward);
        }
        
        if(rewardIndex < 0 || rewardIndex >= quest.getRewards().size())
        {
            if(rectReward != null && quest.getRewards().size() == 0)
            {
                this.initPanel();
            } else
            {
                titleReward.setText("?");
                updateButtons();
            }
            
            return;
        } else if(rectReward == null)
        {
            this.initPanel();
            return;
        }
        
        IReward rew = quest.getRewards().getAllValues().get(rewardIndex);
        
        pnReward = new PanelLegacyEmbed<>(rectReward, rew.getRewardGui(rectReward.getX(), rectReward.getY(), rectReward.getWidth(), rectReward.getHeight(), quest));
        cvInner.addPanel(pnReward);
    
        titleReward.setText(I18n.format(rew.getUnlocalisedName()));
        
        updateButtons();
    }
    
    private void refreshTaskPanel()
    {
        if(pnTask != null)
        {
            cvInner.removePanel(pnTask);
        }
        
        if(taskIndex < 0 || taskIndex >= quest.getTasks().size())
        {
            if(rectTask != null && quest.getTasks().size() == 0)
            {
                this.initPanel();
            } else
            {
                titleTask.setText("?");
                updateButtons();
            }
            
            return;
        }
        
        ITask tsk = quest.getTasks().getAllValues().get(taskIndex);
        
        pnTask = new PanelLegacyEmbed<>(rectTask, tsk.getTaskGui(rectTask.getX(), rectTask.getY(), rectTask.getWidth(), rectTask.getHeight(), quest));
        cvInner.addPanel(pnTask);
        
        titleTask.setText(I18n.format(tsk.getUnlocalisedName()));
        
        updateButtons();
    }
    
    private void updateButtons()
    {
        Minecraft mc = Minecraft.getMinecraft();
        
        if(btnRewardLeft != null && btnRewardRight != null && btnClaim != null)
        {
            btnRewardLeft.setEnabled(rewardIndex > 0);
            btnRewardRight.setEnabled(rewardIndex < quest.getRewards().size() - 1);
            
            // Claim button state
            btnClaim.setEnabled(quest.getRewards().size() > 0 && quest.canClaim(mc.player));
        }
        
        if(btnTaskLeft != null && btnTaskRight != null && btnDetect != null)
        {
            btnTaskLeft.setEnabled(taskIndex > 0);
            btnTaskRight.setEnabled(taskIndex < quest.getTasks().size() - 1);
    
            // Detect/submit button state
            btnDetect.setEnabled(quest.canSubmit(mc.player));
        }
    }
}
