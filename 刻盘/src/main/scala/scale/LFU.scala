package scale

import chisel3._
import chisel3.util._

class LFU(setIndex: Int, assoc: Int) extends CurrentCycle {
  val maxCounterValue = 32

  private val counters = Seq.fill(assoc)(new Counter(maxCounterValue))


  def hit(way: UInt) = {
    printf(p"[$currentCycle] cache.sets($setIndex).lfu.hit: way = $way\n")

    counters.zipWithIndex.foreach { case (counter, i) =>
      var hitValue = 0.U
      when(i.U === way) {
        hitValue = counter.value
      }
      counter.value := Mux(counter.value <= hitValue, counter.value + 1.U, counter.value)
      when(i.U === way) {
        counter.value := 0.U
      }
    }
  }

  def miss() = {
    var victimWay = assoc.U
    var victimCounterValue = 0.U(log2Ceil(maxCounterValue).W)

    counters.zipWithIndex.foreach { case (counter, i) =>
      victimWay = Mux(counter.value > victimCounterValue, i.U, victimWay)
      victimCounterValue = Mux(counter.value > victimCounterValue, counter.value, victimCounterValue)
    }

    assert(victimWay < assoc.U)

    counters.zipWithIndex.foreach { case (counter, i) =>
      when(i.U === victimWay) {
        counter.value := 0.U
      }.otherwise {
        counter.inc()
      }
    }

    printf(p"[$currentCycle] cache.sets($setIndex).lfu.miss: victimWay = $victimWay\n")

    victimWay
  }
}
