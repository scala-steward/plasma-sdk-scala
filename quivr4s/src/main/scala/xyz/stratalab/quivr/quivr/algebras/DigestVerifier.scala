package xyz.stratalab.quivr.algebras

import xyz.stratalab.common.ContextlessValidation
import xyz.stratalab.quivr.runtime.QuivrRuntimeError
import quivr.models.DigestVerification

/** A trait that provides Digest verification for use in a Dynamic Context */
trait DigestVerifier[F[_]] extends ContextlessValidation[F, QuivrRuntimeError, DigestVerification]
