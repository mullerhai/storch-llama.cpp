package examples

import torch.llama.parameter.{InferenceParameters, ModelParameters}
import torch.llama.LlamaModel


object InfillExample {
  def main(args: String*): Unit = {
    val modelParams = new ModelParameters().setModel("models/codellama-7b.Q2_K.gguf").setGpuLayers(43)
    val prefix = "def remove_non_ascii(s: str) -> str:\n    \"\"\" "
    val suffix = "\n    return result\n"
    try {
      val model = new LlamaModel(modelParams)
      try {
        System.out.print(prefix)
        val inferParams = new InferenceParameters("").setInputPrefix(prefix).setInputSuffix(suffix)
        //        import scala.collection.JavaConversions.*
        for (output <- model.generate(inferParams)) {
          System.out.print(output)
        }
        System.out.print(suffix)
      } finally if (model != null) model.close()
    }
  }
}