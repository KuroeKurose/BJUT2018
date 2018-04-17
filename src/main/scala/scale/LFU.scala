package scale

import chisel3._
import chisel3.util._

class LFU(setIndex: Int, assoc: Int) extends CurrentCycle {
  private val counters = Seq.fill(assoc)(new Counter(32))

  private def access(way: UInt) = {
    counters.zipWithIndex.foreach { case (counter, i) =>
      when(i.U === way) {
        counter.value := 0.U
      }.otherwise {
        counter.inc()
      }
    }

    counters.zipWithIndex.foreach { case (counter, i) =>
      printf(p"[$currentCycle] cache.sets($setIndex).lfu.access: i = $i, way = $way, (i.U === way) = ${i.U === way}, counter.value = ${counter.value}\n")
    }
  }

  def hit(way: UInt) = {
    printf(p"[$currentCycle] cache.sets($setIndex).lfu.hit: way = $way\n")

    access(way)
  }

  def miss() = {
    var victimWay = (assoc - 1).U
    var maxCounterValue = 0.U(5.W)

    counters.zipWithIndex.foreach { case (counter, i) =>
      victimWay = Mux(counter.value > maxCounterValue, i.U, victimWay)
      maxCounterValue = Mux(counter.value > maxCounterValue, counter.value, maxCounterValue)
    }

    assert(victimWay < assoc.U)

    printf(p"[$currentCycle] cache.sets($setIndex).lfu.miss: victimWay = $victimWay\n")

    access(victimWay)

    victimWay
  }
}
