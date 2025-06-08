package torch.llama

import torch.llama.parameter.InferenceParameters

import java.nio.charset.StandardCharsets
import scala.collection.mutable

/**
 * An output of the LLM providing access to the generated text and the associated probabilities. You have to configure
 * {@link InferenceParameters# setNProbs ( int )} in order for probabilities to be returned.
 */
final class LlamaOutput(generated: Array[Byte],
                        val probabilities: mutable.HashMap[String, Float],
                        val stop: Boolean) {
  this.text = new String(generated, StandardCharsets.UTF_8)
  /**
   * The last bit of generated text that is representable as text (i.e., cannot be individual utf-8 multibyte code
   * points).
   */
  //  @NotNull 
  final var text: String = null

  override def toString: String = text
}