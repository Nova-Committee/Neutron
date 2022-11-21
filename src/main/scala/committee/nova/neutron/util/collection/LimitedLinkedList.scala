package committee.nova.neutron.util.collection

import java.util

class LimitedLinkedList[T] extends util.LinkedList[T] {
  def addWithLimit(e: T, limit: Int): Boolean = {
    val l = if (limit < 0) Int.MaxValue else limit
    super.add(e)
    while (size() > l) super.remove()
    true
  }
}
