package betterquesting.network.handlers;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.network.IPacketHandler;
import betterquesting.api.network.PacketTypeNative;
import betterquesting.lives.LifeDatabase;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PktHandlerLives implements IPacketHandler
{
	@Override
	public ResourceLocation getRegistryName()
	{
		return PacketTypeNative.LIFE_DATABASE.GetLocation();
	}
	
	@Override
	public void handleServer(NBTTagCompound data, EntityPlayerMP sender)
	{
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void handleClient(NBTTagCompound data)
	{
		LifeDatabase.INSTANCE.readPacket(data);
	}
}
