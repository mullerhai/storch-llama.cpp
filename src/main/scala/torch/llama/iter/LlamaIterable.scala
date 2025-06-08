package torch.llama.iter

import torch.llama.{LlamaModel, LlamaOutput}

/**
 * An iterable used by {@link LlamaModel# generate ( InferenceParameters )} that specifically returns a {@link LlamaIterator}.
 */
@FunctionalInterface
trait LlamaIterable extends Iterable[LlamaOutput] {
//    @NotNull
  override def iterator: LlamaIterator
}