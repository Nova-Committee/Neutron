package committee.nova.neutron.server.ui.container

import committee.nova.neutron.implicits._
import committee.nova.neutron.server.event.impl.InteractableItemClickEvent
import committee.nova.neutron.util.reference.Tags
import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP, InventoryPlayer}
import net.minecraft.inventory.{ClickType, Container, IInventory, Slot}
import net.minecraft.item.ItemStack
import net.minecraftforge.common.MinecraftForge

import scala.util.Try

class ContainerInteractable(player: EntityPlayerMP, playerInv: InventoryPlayer, extraInv: IInventory) extends Container {
  private val numRows = extraInv.getSizeInventory / 9
  private val i: Int = (numRows - 4) * 8

  extraInv.openInventory(player)
  for (j <- 0 until numRows) for (k <- 0 until 9) this.addSlotToContainer(new Slot(extraInv, k + j * 9, 8 + k * 18, 18 + j * 18))
  for (j <- 0 until 3) for (k <- 0 until 9) this.addSlotToContainer(new Slot(playerInv, k + j * 9 + 9, 8 + k * 18, 103 + j * 18 + i))
  for (j <- 0 until 9) this.addSlotToContainer(new Slot(playerInv, j, 8 + j * 18, 161 + i))

  override def canInteractWith(player: EntityPlayer): Boolean = true

  override def transferStackInSlot(player: EntityPlayer, index: Int): ItemStack = inventorySlots.get(index).getStack

  override def slotClick(index: Int, i1: Int, i2: ClickType, player: EntityPlayer): ItemStack = {
    if (player.world.isRemote) return ItemStack.EMPTY
    val mp = player.asInstanceOf[EntityPlayerMP]
    Try(getSlot(index).getStack).foreach(stack => {
      mp.sendContainerToPlayer(mp.inventoryContainer)
      mp.sendContainerToPlayer(mp.openContainer)
      if (stack.isEmpty || !stack.hasTagCompound || !stack.getTagCompound.hasKey(Tags.INTERACTABLE)) return ItemStack.EMPTY
      MinecraftForge.EVENT_BUS.post(InteractableItemClickEvent(mp, stack))
    })
    ItemStack.EMPTY
  }

  override def mergeItemStack(stack: ItemStack, i1: Int, i2: Int, b: Boolean): Boolean = false

  // It's important somehow
  override def getCanCraft(entity: EntityPlayer): Boolean = true
}
