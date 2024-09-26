package xyz.stratalab.sdk.syntax

import xyz.stratalab.sdk.MockHelpers
import xyz.stratalab.sdk.common.ContainsEvidence
import xyz.stratalab.sdk.common.ContainsImmutable
import xyz.stratalab.sdk.common.ContainsSignable
import xyz.stratalab.sdk.common.ContainsSignable.instances.ioTransactionSignable
import co.topl.brambl.models.TransactionId
import co.topl.brambl.models.common.ImmutableBytes
import co.topl.brambl.models.transaction.IoTransaction

class TransactionSyntaxSpec extends munit.FunSuite with MockHelpers {

  test("TransactionSyntax creates and embeds IDs") {
    val transaction = dummyTx
    assertEquals(transaction.transactionId, None)
    val expectedId = {
      implicit val immutableContainsImmutable: ContainsImmutable[ImmutableBytes] = identity
      val signableBytes = ContainsSignable[IoTransaction].signableBytes(transaction)
      val immutable = ImmutableBytes(signableBytes.value)
      val evidence = ContainsEvidence[ImmutableBytes].sizedEvidence(immutable)
      TransactionId(evidence.digest.value)
    }
    assertEquals(transaction.computeId, expectedId)
    assertEquals(transaction.id, expectedId)
    val withEmbeddedId = transaction.embedId
    assertEquals(withEmbeddedId.transactionId, Some(expectedId))
    assertEquals(withEmbeddedId.id, expectedId)
    assertEquals(withEmbeddedId.containsValidId, true)
  }
}
