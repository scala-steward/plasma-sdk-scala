package xyz.stratalab.sdk.common

import xyz.stratalab.sdk.MockHelpers
import xyz.stratalab.sdk.common.ContainsImmutable.ContainsImmutableTOps
import xyz.stratalab.sdk.common.ContainsImmutable.instances._
import xyz.stratalab.sdk.common.ContainsSignable.ContainsSignableTOps
import xyz.stratalab.sdk.common.ContainsSignable.instances._
import xyz.stratalab.sdk.models.box.Attestation

class ContainsSignableSpec extends munit.FunSuite with MockHelpers {

  test("IoTransaction.signable should return the same bytes as IoTransaction.immutable minus the Proofs") {
    // withProofs has non-empty proofs for all the proofs. noProofs has proofs stripped away
    val withProofs = txFull.copy(inputs = txFull.inputs.map(stxo => stxo.copy(attestation = nonEmptyAttestation)))
    val emptyAttestation = Attestation().withPredicate(inPredicateLockFullAttestation.copy(responses = Seq.empty))
    val noProofs = withProofs.copy(inputs = withProofs.inputs.map(stxo => stxo.copy(attestation = emptyAttestation)))
    val signableFull = withProofs.signable.value
    val immutableFull = withProofs.immutable.value
    val immutableNoProofs = noProofs.immutable.value
    // The only difference between immutableFull and immutableEmpty is the Proofs
    val proofsImmutableSize = immutableFull.size - immutableNoProofs.size
    assertEquals(proofsImmutableSize > 0, true)
    assertEquals(signableFull.size, immutableFull.size - proofsImmutableSize)
    assertEquals(signableFull.size, immutableNoProofs.size)
  }

  test("The Proofs in an IoTransaction changing should not alter the transaction's signable bytes") {
    val withProofs = txFull.copy(inputs = txFull.inputs.map(stxo => stxo.copy(attestation = nonEmptyAttestation)))
    val signableFull = withProofs.signable.value
    val signableEmpty = txFull.signable.value
    // The only difference between signableFull and signableEmpty is the Proofs
    assertEquals(signableFull.size, signableEmpty.size)
  }
}
