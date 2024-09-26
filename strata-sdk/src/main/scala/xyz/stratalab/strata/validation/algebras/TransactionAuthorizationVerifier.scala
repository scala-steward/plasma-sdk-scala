package xyz.stratalab.sdk.validation.algebras

import co.topl.brambl.models.Datum
import co.topl.brambl.models.transaction.IoTransaction
import xyz.stratalab.common.ContextualValidation
import xyz.stratalab.quivr.runtime.DynamicContext
import xyz.stratalab.sdk.validation.TransactionAuthorizationError

trait TransactionAuthorizationVerifier[F[_]]
    extends ContextualValidation[F, TransactionAuthorizationError, IoTransaction, DynamicContext[F, String, Datum]]
