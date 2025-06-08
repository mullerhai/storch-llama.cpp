package llama

import org.junit.{AfterClass, Assert, BeforeClass, Test}
import torch.llama.LlamaModel
import torch.llama.parameter.ModelParameters

object RerankingModelTest {
  private var model: LlamaModel = null

  @BeforeClass def setup(): Unit = {
    model = new LlamaModel(new ModelParameters().setCtxSize(128).setModel("models/jina-reranker-v1-tiny-en-Q4_0.gguf").setGpuLayers(43).enableReranking.enableLogTimestamps.enableLogPrefix)
  }

  @AfterClass def tearDown(): Unit = {
    if (model != null) model.close()
  }
}

class RerankingModelTest {
  private[llama] val query = "Machine learning is"
  private[llama] val TEST_DOCUMENTS = Array[String]("A machine is a physical system that uses power to apply forces and control movement to perform an action. The term is commonly applied to artificial devices, such as those employing engines or motors, but also to natural biological macromolecules, such as molecular machines.", "Learning is the process of acquiring new understanding, knowledge, behaviors, skills, values, attitudes, and preferences. The ability to learn is possessed by humans, non-human animals, and some machines; there is also evidence for some kind of learning in certain plants.", "Machine learning is a field of study in artificial intelligence concerned with the development and study of statistical algorithms that can learn from data and generalize to unseen data, and thus perform tasks without explicit instructions.", "Paris, capitale de la France, est une grande ville européenne et un centre mondial de l'art, de la mode, de la gastronomie et de la culture. Son paysage urbain du XIXe siècle est traversé par de larges boulevards et la Seine.")

  @Test
  def testReRanking(): Unit = {
    val llamaOutput = RerankingModelTest.model.rerank(query, TEST_DOCUMENTS(0), TEST_DOCUMENTS(1), TEST_DOCUMENTS(2), TEST_DOCUMENTS(3))
    val rankedDocumentsMap = llamaOutput.probabilities
    Assert.assertTrue(rankedDocumentsMap.size == TEST_DOCUMENTS.length)
    // Finding the most and least relevant documents
    var mostRelevantDoc: String = null
    var leastRelevantDoc: String = null
    var maxScore = Float.MinValue
    var minScore = Float.MaxValue

    for (entry <- rankedDocumentsMap) {
      if (entry._2 > maxScore) {
        maxScore = entry._2
        mostRelevantDoc = entry._1
      }
      if (entry._2 < minScore) {
        minScore = entry._2
        leastRelevantDoc = entry._1
      }
    }
    // Assertions
    Assert.assertTrue(maxScore > minScore)
    Assert.assertEquals("Machine learning is a field of study in artificial intelligence concerned with the development and study of statistical algorithms that can learn from data and generalize to unseen data, and thus perform tasks without explicit instructions.", mostRelevantDoc)
    Assert.assertEquals("Paris, capitale de la France, est une grande ville européenne et un centre mondial de l'art, de la mode, de la gastronomie et de la culture. Son paysage urbain du XIXe siècle est traversé par de larges boulevards et la Seine.", leastRelevantDoc)
  }

  @Test def testSortedReRanking(): Unit = {
    val rankedDocuments = RerankingModelTest.model.reranks(true, query, TEST_DOCUMENTS *)
    Assert.assertEquals(rankedDocuments.size, TEST_DOCUMENTS.length)
    // Check the ranking order: each score should be >= the next one
    for (i <- 0 until rankedDocuments.size - 1) {
      val currentScore = rankedDocuments(i).getValue
      val nextScore = rankedDocuments(i + 1).getValue
      Assert.assertTrue("Ranking order incorrect at index " + i, currentScore >= nextScore)
    }
  }
}