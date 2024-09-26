package xyz.stratalab.sdk.syntax

import xyz.stratalab.sdk.common.ContainsEvidence
import xyz.stratalab.sdk.common.ContainsImmutable.instances.lockImmutable
import co.topl.brambl.models.LockAddress
import co.topl.brambl.models.LockId
import co.topl.brambl.models.box.Lock

import scala.language.implicitConversions

trait LockSyntax {

  implicit def lockAsLockSyntaxOps(lock: Lock): LockSyntaxOps =
    new LockSyntaxOps(lock)

  implicit def predicateLockAsLockSyntaxOps(lock: Lock.Predicate): PredicateLockSyntaxOps =
    new PredicateLockSyntaxOps(lock)
}

class LockSyntaxOps(val lock: Lock) extends AnyVal {

  def lockAddress(network: Int, ledger: Int): LockAddress =
    LockAddress(
      network,
      ledger,
      LockId(
        ContainsEvidence[Lock].sizedEvidence(lock).digest.value
      )
    )
}

class PredicateLockSyntaxOps(val lock: Lock.Predicate) extends AnyVal {

  def lockAddress(network: Int, ledger: Int): LockAddress =
    LockAddress(
      network,
      ledger,
      LockId(
        ContainsEvidence[Lock].sizedEvidence(Lock().withPredicate(lock)).digest.value
      )
    )
}
