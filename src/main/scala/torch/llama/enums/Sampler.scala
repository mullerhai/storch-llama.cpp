package torch.llama.enums

enum Sampler {
  case DRY, TOP_K, TOP_P, TYP_P, MIN_P, TEMPERATURE, XTC, INFILL, PENALTIES
}
//object Sampler extends Enumeration {
//  type Sampler = Value
//  val DRY, TOP_K, TOP_P, TYP_P, MIN_P, TEMPERATURE, XTC, INFILL, PENALTIES = Value
//}