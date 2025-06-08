package torch.llama.enums

enum RopeScalingType:
  case UNSPECIFIED, NONE, LINEAR, YARN2, LONGROPE, MAX_VALUE

  def getId: Int = ordinal
//object RopeScalingType extends Enumeration {
//  type RopeScalingType = Value
//  val UNSPECIFIED, NONE, LINEAR, YARN2, LONGROPE, MAX_VALUE = Value
//  private val id = 0
//  def this (value: Int) ={
//    this ()
//    this.id = value
//  }
//
//  def getId: Int = id
//}