package torch.llama

import java.util.Objects
import scala.jdk.CollectionConverters.*

class Pair[K, V](val key: K, val value: V) {
  def getKey: K = key

  def getValue: V = value

  override def hashCode: Int = Objects.hash(key, value)

  override def equals(obj: Any): Boolean = {
    if (this eq obj.asInstanceOf[Pair[?, ?]]) return true
    if (obj == null) return false
    if (getClass ne obj.getClass) return false
    val other = obj.asInstanceOf[Pair[?, ?]]
    Objects.equals(key, other.key) && Objects.equals(value, other.value)
  }

  override def toString: String = "Pair [key=" + key + ", value=" + value + "]"
}