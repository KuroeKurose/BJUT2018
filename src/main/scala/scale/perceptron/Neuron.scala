package scale.perceptron

import scala.util.Random

object Neuron {
  val rnd = new Random()
}

class Neuron(val numInputs: Int) {
  //  val weights = Seq.fill(numInputs)(Neuron.rnd.nextDouble())
  val weights = Seq.fill(numInputs)(1.0)

  def forword(inputs: Seq[Double]): Double = {
    weights.zip(inputs).map { case (w, i) => w * i }.reduceLeft(_ + _)
  }
}

class Layer(val numInputs: Int, val numOutputs: Int) {
  val neurons = Seq.fill(numOutputs)(new Neuron(numInputs))

  def forword(inputs: Seq[Double]): Seq[Double] = {
    neurons.map(_.forword(inputs))
  }
}

class Perceptron(val numInputs: Int, val numHidden: Int, val numOutputs: Int) {
  val layer0 = new Layer(numInputs, numHidden)
  val layer1 = new Layer(numHidden, numOutputs)

  val layers = Seq(
    layer0, layer1
  )

  def forword(inputs: Seq[Double]): Seq[Double] = {
    layers.foldLeft(inputs) { case (is, layer) => layer.forword(is) }
  }
}

object Perceptron extends App {
  val perceptron = new Perceptron(1, 5, 1)
  val outputs = perceptron.forword(Seq(2.0))
  printf(s"outputs = $outputs\n")
}
