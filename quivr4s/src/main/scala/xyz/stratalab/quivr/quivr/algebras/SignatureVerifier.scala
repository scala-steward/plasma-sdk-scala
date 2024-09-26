package xyz.stratalab.quivr.algebras

import xyz.stratalab.common.ContextlessValidation
import xyz.stratalab.quivr.runtime.QuivrRuntimeError
import quivr.models.SignatureVerification

/** A trait that provides Signature verification for use in a Dynamic Context */
trait SignatureVerifier[F[_]] extends ContextlessValidation[F, QuivrRuntimeError, SignatureVerification]
