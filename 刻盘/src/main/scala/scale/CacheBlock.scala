package scale

import chisel3._
import chisel3.util._

class CacheBlockRequest extends Bundle with Params {
  val read = Bool()
  val hit = Bool()
  val replacement = Bool()
}

class CacheBlockResponse extends Bundle with Params {
}

class CacheBlockIO extends Bundle {
  val request = Flipped(Decoupled(new CacheBlockRequest))
  val response = Decoupled(new CacheBlockResponse)

  val isCounter = Output(UInt(log2Ceil(Int.MaxValue).W))
  val siCounter = Output(UInt(log2Ceil(Int.MaxValue).W))
  val smCounter = Output(UInt(log2Ceil(Int.MaxValue).W))
  val msCounter = Output(UInt(log2Ceil(Int.MaxValue).W))
  val imCounter = Output(UInt(log2Ceil(Int.MaxValue).W))
  val miCounter = Output(UInt(log2Ceil(Int.MaxValue).W))
}

class CacheBlock extends Module with Params with CurrentCycle {
  val io = IO(new CacheBlockIO)

  val numStates = 9

  val sI :: sS :: sM :: sIS :: sSI :: sSM :: sMS :: sMI :: sIM :: Nil = Enum(numStates)

  val state = RegInit(sI)


  val isCounter = new Counter(Int.MaxValue)
  val siCounter = new Counter(Int.MaxValue)
  val smCounter = new Counter(Int.MaxValue)
  val msCounter = new Counter(Int.MaxValue)
  val imCounter = new Counter(Int.MaxValue)
  val miCounter = new Counter(Int.MaxValue)

  io.isCounter := isCounter.value
  io.siCounter := siCounter.value
  io.smCounter := smCounter.value
  io.msCounter := msCounter.value
  io.imCounter := imCounter.value
  io.miCounter := miCounter.value

  val tag = UInt(tagWidth.W)

  io.response.valid := false.B
  io.response.bits := DontCare
  io.request.ready := false.B

  switch(state) {
    is(sI) {
      io.request.ready := true.B

      when(io.request.valid) {
        state := Mux(io.request.bits.read, sIS, sIM)
      }
    }
    is(sS) {
      io.request.ready := true.B

      when(io.request.valid) {
        when(io.request.bits.hit) {
          when(io.request.bits.read) {
            //read hit: no action
            io.response.valid := true.B
          }.otherwise {
            state := sSI //write hit
          }
        }.otherwise {
          when(io.request.bits.replacement) {
            state := sSI // replacement
          }
        }
      }
    }
    is(sM) {
      io.request.ready := true.B

      when(io.request.valid) {
        when(io.request.bits.hit) {
          when(io.request.bits.read) {
            state := sMS //read hit
          }.otherwise {
            // write hit: no action
            io.response.valid := true.B
          }
        }.otherwise {
          when(io.request.bits.replacement) {
            state := sMI //replacement
          }
        }
      }
    }
    is(sIS) {
      state := sS
      io.response.valid := true.B
      isCounter.inc()
    }
    is(sSI) {
      state := sI
      io.response.valid := true.B
      siCounter.inc()
    }
    is(sSM) {
      state := sM
      io.response.valid := true.B
      smCounter.inc()
    }
    is(sMS) {
      state := sS
      io.response.valid := true.B
      msCounter.inc()
    }
    is(sMI) {
      state := sI
      io.response.valid := true.B
      miCounter.inc()
    }
    is(sIM) {
      state := sM
      io.response.valid := true.B
      imCounter.inc()
    }
  }

  printf(p"[$currentCycle] state = $state, request = ${io.request}, response = ${io.response}\n")
}

object CacheBlock extends App {
  Driver.execute(Array("-td", "source/"), () => new CacheBlock)
}