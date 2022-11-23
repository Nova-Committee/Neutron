package committee.nova.neutron.server.ui.base

import committee.nova.neutron.implicits._
import committee.nova.neutron.util.Utilities
import committee.nova.neutron.util.reference.Tags
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP}
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack

import scala.collection.mutable


class InventoryInteraction(val player: EntityPlayerMP, val key: String) extends IInventory {
  private val invList = new mutable.MutableList[ItemStack]()

  override def getSizeInventory: Int = 36

  override def getStackInSlot(index: Int): ItemStack = invList.get(index).orNull

  override def decrStackSize(index: Int, number: Int): ItemStack = getStackInSlot(index)

  override def getStackInSlotOnClosing(index: Int): ItemStack = null

  override def setInventorySlotContents(index: Int, stack: ItemStack): Unit = {}

  override def getInventoryName: String = Utilities.L10n.getFromCurrentLang(key)

  override def hasCustomInventoryName: Boolean = false

  override def getInventoryStackLimit: Int = 64

  override def markDirty(): Unit = {}

  override def isUseableByPlayer(player: EntityPlayer): Boolean = true

  override def openInventory(): Unit = {}

  override def closeInventory(): Unit = {
    for (stack <- invList if !stack.getOrCreateTag.getBoolean(Tags.FOR_INTERACTION)) {
      val world = player.worldObj
      val item = new EntityItem(world, player.posX, player.posY, player.posZ, stack)
      world.spawnEntityInWorld(item)
    }
  }

  override def isItemValidForSlot(index: Int, stack: ItemStack): Boolean = false
}
