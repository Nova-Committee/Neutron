package committee.nova.neutron.server.player.storage

import committee.nova.neutron.api.player.storage.IHealthStats

class StatsBeforeSuicide extends IHealthStats {
  private var health = 20.0F
  private var foodLevel = 20
  private var saturation = 20.0F
  private var valid = false

  def this(health: Float, foodLevel: Int, saturation: Float) = {
    this
    this.health = health
    this.foodLevel = foodLevel
    this.saturation = saturation
  }

  override def getHealth: Float = health

  override def getFoodLevel: Int = foodLevel

  override def getSaturation: Float = saturation

  override def setHealth(health: Float): Unit = this.health = health

  override def setFoodLevel(foodLevel: Int): Unit = this.foodLevel = foodLevel

  override def setSaturation(saturation: Float): Unit = this.saturation = saturation

  def isValid: Boolean = valid

  def setValid(valid: Boolean): Unit = this.valid = valid
}
