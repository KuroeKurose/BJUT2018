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
}

class CacheBlock extends Module with Params {
  val io = IO(new CacheBlockIO)

  val numStates = 9

  val sI :: sS :: sM :: sIS :: sSI :: sSM :: sMS :: sMI :: sIM :: Nil = Enum(numStates)

  val state = RegInit(sI)

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
    }
    is(sSI) {
      state := sI
    }
    is(sSM) {
      state := sM
    }
    is(sMS) {
      state := sS
    }
    is(sMI) {
      state := sI
    }
    is(sIM) {
      state := sM
    }
  }
}

object CacheBlock extends App {
  Driver.execute(Array("-td", "source/"), () => new CacheBlock)
}