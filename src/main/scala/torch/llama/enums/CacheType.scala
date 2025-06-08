package torch.llama.enums

enum CacheType {
  case F32, F16, BF16, Q8_0, Q4_0, Q4_1, IQ4_NL, Q5_0, Q5_1
}
//object CacheType extends Enumeration {
//  type CacheType = Value
//  val F32, F16, BF16, Q8_0, Q4_0, Q4_1, IQ4_NL, Q5_0, Q5_1 = Value
//}