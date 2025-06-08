package torch.llama.parameter

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

abstract class CliParameters {
  final private[llama] val parameters = new mutable.HashMap[String, String]

  override def toString: String = {
    val builder = new StringBuilder

    for (key <- parameters.keySet) {
      val value = parameters.get(key)
      builder.append(key).append(" ")
      if (value != null) builder.append(value).append(" ")
    }
    builder.toString
  }

  def toArray: Array[String] = {
    val result = new ListBuffer[String]
    result.append("") // c args contain the program name as the first argument, so we add an empty entry

    for (key <- parameters.keySet) {
      result.append(key)
      val value = parameters.getOrElse(key, null)
      if (value != null) then result.append(value)
    }
    result.toArray //(new Array[String](0))
  }
}