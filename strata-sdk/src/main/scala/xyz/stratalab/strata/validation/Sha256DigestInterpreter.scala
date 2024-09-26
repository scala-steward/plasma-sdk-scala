package xyz.stratalab.sdk.validation

import cats.Monad
import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxEitherId}
import xyz.stratalab.crypto.hash.implicits.sha256Hash
import xyz.stratalab.quivr.algebras.DigestVerifier
import xyz.stratalab.quivr.runtime.QuivrRuntimeError
import xyz.stratalab.quivr.runtime.QuivrRuntimeErrors.ValidationError.{
  LockedPropositionIsUnsatisfiable,
  UserProvidedInterfaceFailure
}
import quivr.models.{Digest, DigestVerification, Preimage}

/**
 * Validates that a Sha256 digest is valid.
 */
object Sha256DigestInterpreter {

  def make[F[_]: Monad](): DigestVerifier[F] = new DigestVerifier[F] {

    /**
     * Validates that an Sha256 digest is valid.
     * @param t DigestVerification object containing the digest and preimage
     * @return The DigestVerification object if the digest is valid, otherwise an error
     */
    override def validate(t: DigestVerification): F[Either[QuivrRuntimeError, DigestVerification]] = t match {
      case DigestVerification(Digest(expectedDigest, _), Preimage(p, salt, _), _) =>
        val testHash: Array[Byte] = sha256Hash.hash(p.toByteArray ++ salt.toByteArray).value
        if (java.util.Arrays.equals(testHash, expectedDigest.toByteArray))
          t.asRight[QuivrRuntimeError].pure[F]
        else
          (LockedPropositionIsUnsatisfiable: QuivrRuntimeError).asLeft[DigestVerification].pure[F]
      case _ =>
        (UserProvidedInterfaceFailure: QuivrRuntimeError).asLeft[DigestVerification].pure[F]
    }
  }
}
