package committee.nova.neutron.server.event.handler

import committee.nova.neutron.implicits._
import committee.nova.neutron.server.config.ServerConfig
import committee.nova.neutron.server.storage.ServerStorage
import cpw.mods.fml.common.FMLCommonHandler
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.gameevent.TickEvent.{Phase, PlayerTickEvent, ServerTickEvent}
import cpw.mods.fml.relauncher.Side

object FMLEventHandler {
  def init(): Unit = FMLCommonHandler.instance().bus().register(new FMLEventHandler)
}

class FMLEventHandler {
  @SubscribeEvent
  def onServerTick(e: ServerTickEvent): Unit = if (e.phase == Phase.END) ServerStorage.tick()

  @SubscribeEvent
  def onPlayerTick(e: PlayerTickEvent): Unit = {
    if (e.side == Side.CLIENT) return
    val player = e.player
    //for (index <- 0 until player.inventory.getSizeInventory) {
    //  val stack = player.inventory.getStackInSlot(index)
    //  if (stack != null && stack.hasTagCompound && stack.getTagCompound.hasKey(Tags.INTERACTABLE)) {
    //    MinecraftForge.EVENT_BUS.post(InteractableItemClickEvent(player.asInstanceOf[EntityPlayerMP], stack))
    //    player.inventory.setInventorySlotContents(index, null)
    //  }
    //}
    if (e.phase == Phase.START) return
    //val currentStack = player.inventory.getItemStack
    //if (currentStack != null && currentStack.hasTagCompound && currentStack.getTagCompound.hasKey(Tags.INTERACTABLE)) {
    //  MinecraftForge.EVENT_BUS.post(InteractableItemClickEvent(player.asInstanceOf[EntityPlayerMP], currentStack))
    //  player.inventory.setItemStack(null)
    //  player.inventory.markDirty()
    //}
    // RTP Accumulation
    if (player.getRtpAccumulation > 0) {
      val max = ServerConfig.getMaxRtpChances * ServerConfig.getRtpChancesRecoveryTime
      if (player.getRtpAccumulation > max) player.setRtpAccumulation(max)
      player.setRtpAccumulation(player.getRtpAccumulation - 1)
    }
    // Teleportation CD
    if (player.getTpaCoolDown > 0) {
      val max = ServerConfig.getTpCoolDown
      if (player.getTpaCoolDown > max) player.setTpaCoolDown(max)
      player.setTpaCoolDown(player.getTpaCoolDown - 1)
    }
  }
}
