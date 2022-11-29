package committee.nova.neutron.api.player.storage

trait IHealthStats {
  def getHealth: Float

  def setHealth(health: Float): Unit

  def getFoodLevel: Int

  def setFoodLevel(foodLevel: Int): Unit

  def getSaturation: Float

  def setSaturation(saturation: Float): Unit
}
