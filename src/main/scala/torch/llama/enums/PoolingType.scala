package torch.llama.enums

enum PoolingType {
  case UNSPECIFIED, NONE, MEAN, CLS, LAST, RANK

  def getId: Int = this.ordinal

}
//object PoolingType extends Enumeration {
//  type PoolingType = Value
//  val UNSPECIFIED, NONE, MEAN, CLS, LAST, RANK = Value
//  private val id = 0
//  def this (value: Int) ={
//    this ()
//    this.id = value
//  }
//
//  def getId: Int = id
//}