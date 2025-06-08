package torch.llama.iter

import torch.llama.parameter.InferenceParameters
import torch.llama.{LlamaModel, LlamaOutput}

import java.lang.annotation.Native
import java.util

/**
 * This iterator is used by {@link LlamaModel# generate ( InferenceParameters )}. In addition to implementing {@link Iterator},
 * it allows to cancel ongoing inference (see {@link # cancel ( )}).
 */
final class LlamaIterator(val model: LlamaModel, parameters: InferenceParameters) extends Iterator[LlamaOutput] {

  parameters.setStream(true)
  taskId = model.requestCompletion(parameters.toString)
  final private var taskId = 0

  @Native
  @SuppressWarnings(Array("FieldMayBeFinal"))
  private var hasNexts = true

  override def hasNext(): Boolean = hasNexts

  override def next: LlamaOutput = {
    if (!hasNexts) throw new NoSuchElementException
    val output = model.receiveCompletion(taskId)
    hasNexts = !output.stop
    if (output.stop) model.releaseTask(taskId)
    output
  }

  /**
   * Cancel the ongoing generation process.
   */
  def cancel(): Unit = {
    model.cancelCompletion(taskId)
    hasNexts = false
  }
}