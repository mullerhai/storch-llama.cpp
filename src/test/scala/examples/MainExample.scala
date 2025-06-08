package examples

import torch.llama.enums.MiroStat
import torch.llama.parameter.{InferenceParameters, ModelParameters}
import torch.llama.LlamaModel

import java.io.{BufferedReader, InputStreamReader}
import java.nio.charset.StandardCharsets

//@throws[IOException]
//@SuppressWarnings(Array("InfiniteLoopStatement")) 
object MainExample {

  //  @throws[IOException]
  def main(args: String*): Unit = {
    val modelParams = new ModelParameters().setModel("models/mistral-7b-instruct-v0.2.Q2_K.gguf").setGpuLayers(43)
    val system = "This is a conversation between User and Llama, a friendly chatbot.\n" + "Llama is helpful, kind, honest, good at writing, and never fails to answer any " + "requests immediately and with precision.\n\n" + "User: Hello Llama\n" + "Llama: Hello.  How may I help you today?"
    val reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))
    try {
      val model = new LlamaModel(modelParams)
      try {
        System.out.print(system)
        var prompt = system
        while (true) {
          prompt += "\nUser: "
          System.out.print("\nUser: ")
          val input = reader.readLine
          prompt += input
          System.out.print("Llama: ")
          prompt += "\nLlama: "
          val inferParams = new InferenceParameters(prompt).setTemperature(0.7f).setPenalizeNl(true).setMiroStat(MiroStat.V2).setStopStrings("User:")

          for (output <- model.generate(inferParams)) {
            System.out.print(output)
            prompt += output
          }
        }
      } finally if (model != null) model.close()
    }
  }
}