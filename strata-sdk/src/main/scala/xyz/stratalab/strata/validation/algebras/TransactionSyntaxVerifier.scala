package xyz.stratalab.sdk.validation.algebras

import co.topl.brambl.models.transaction.IoTransaction
import xyz.stratalab.common.ContextlessValidation
import xyz.stratalab.sdk.validation.TransactionSyntaxError
import cats.data.NonEmptyChain

trait TransactionSyntaxVerifier[F[_]]
    extends ContextlessValidation[F, NonEmptyChain[TransactionSyntaxError], IoTransaction]
