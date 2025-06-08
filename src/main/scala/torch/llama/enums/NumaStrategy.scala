package torch.llama.enums

enum NumaStrategy {
  case DISTRIBUTE, ISOLATE, NUMACTL
}
//object NumaStrategy extends Enumeration {
//  type NumaStrategy = Value
//  val DISTRIBUTE, ISOLATE, NUMACTL = Value
//}