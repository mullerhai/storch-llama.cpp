package examples

import torch.llama.parameter.{InferenceParameters, ModelParameters}
import torch.llama.LlamaModel

object GrammarExample {
  def main(args: String*): Unit = {
    val grammar = "root  ::= (expr \"=\" term \"\\n\")+\n" + "expr  ::= term ([-+*/] term)*\n" + "term  ::= [0-9]"
    val modelParams = new ModelParameters().setModel("models/mistral-7b-instruct-v0.2.Q2_K.gguf")
    val inferParams = new InferenceParameters("").setGrammar(grammar)
    try {
      val model = new LlamaModel(modelParams)
      try

        for (output <- model.generate(inferParams)) {
          System.out.print(output)
        }
      finally {
        if (model != null) model.close()
      }
    }
  }
}