package committee.nova.neutron.server.player.storage.capability.api

import committee.nova.neutron.api.player.storage.{IHome, IPosWithDim}
import committee.nova.neutron.server.player.storage.{MuteStatus, StatsBeforeSuicide}
import committee.nova.neutron.util.collection.LimitedLinkedList

import scala.collection.mutable

trait INeutronCapability {
  def getRtpAccumulation: Int

  def setRtpAccumulation(acc: Int): Unit

  def getTpaCoolDown: Int

  def setTpaCoolDown(cd: Int): Unit

  def getHomes: mutable.LinkedHashSet[IHome]

  def setHomes(homes: mutable.LinkedHashSet[IHome]): Unit

  def getFormerPos: LimitedLinkedList[IPosWithDim]

  def setFormerPos(former: LimitedLinkedList[IPosWithDim]): Unit

  def getMuteStatus: MuteStatus

  def getStatsBeforeSuicide: StatsBeforeSuicide
}
