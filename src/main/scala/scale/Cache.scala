package scale

import chisel3._
import chisel3.util._


trait CoreParams {
  val addrWidth = 32
}

class CacheRequest extends Bundle with CoreParams {
  val addr = UInt(addrWidth.W)
}

class CacheResponse extends Bundle with CacheParams {
  val data = UInt(blockSize.W)
}

class CacheIO extends Bundle {
  val req = Flipped(Valid(new CacheRequest))
  val resp = Valid(new CacheResponse)
}

trait CacheParams extends CoreParams {
  val capacity = 1 << 20 //1MB
  val blockSize = 1 << 9 //64Byte
  val assoc = 8
  val numSets = capacity / blockSize / assoc

  val blockOffsetWidth = log2Up(blockSize)
  val setIndexWidth = log2Up(numSets)
  val tagWidth = addrWidth - blockOffsetWidth - setIndexWidth
}

class CacheBlock extends Bundle with CacheParams {
  val valid = Bool()
  val tag = UInt(tagWidth.W)
  val data = UInt(blockSize.W)
}

class Cache extends Module with CacheParams {
  val io = IO(new CacheIO)

  val blocks = Vec(numSets, Vec(assoc, new CacheBlock))

  //  def compare

}