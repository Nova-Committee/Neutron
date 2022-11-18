package committee.nova.neutron.server.storage

import committee.nova.neutron.api.player.request.ITeleportRequest

import scala.collection.mutable

object ServerStorage {
  val teleportRequestSet: mutable.Set[ITeleportRequest] = new mutable.HashSet[ITeleportRequest]()

  def tick(): Unit = for (r <- teleportRequestSet) if (r.tick) teleportRequestSet.remove(r)

  def addRequest(request: ITeleportRequest): Boolean = teleportRequestSet.add(request)
  // TODO: Still not work as intended 
}
