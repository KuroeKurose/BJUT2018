package scale

import chisel3._
import chisel3.util._

class CacheIO extends Bundle {
  val req = Flipped(Valid(new CacheRequest))
  val resp = Valid(new CacheResponse)
}
