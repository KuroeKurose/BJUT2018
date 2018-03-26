package scale

import chisel3._
import chisel3.util._


abstract trait CoreParams {
  val addrLength = 32
}

class CacheRequest extends Bundle with CoreParams{
  val addr = UInt(addrLength.W)
}

class CacheRespond extends Bundle with CoreParams{
  val data = UInt(addrLength.W)
}

class CacheIO extends Bundle{
  val req   = Flipped(Valid(new CacheRequest))
  val resp  = Valid(new CacheRespond)
}

trait CacheParams extends CoreParams{
  val cacheCap = 1<<20 //1MB
  val blockSize = 1<<9 //64Byte
  val assoc = 8
  val numSets = cacheCap / blockSize / assoc

  val blockoffsetLength = log2Up(blockSize)
  val setLength = log2Up(numSets)
  val tagLength = addrLength - (blockoffsetLength + setLength)
}

class IndexItem extends Bundle with CacheParams{
  val v = Bool()
  val tag = UInt(tagLength.W)
}

class Cache extends Module with CacheParams {
  val io = IO(new CacheIO)
  val indexTable = Mem(numSets,Vec(assoc,new IndexItem))
  val cacheMem = Vec(cacheCap / blockSize,UInt(blockSize.W))



}