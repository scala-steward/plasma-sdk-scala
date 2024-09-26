package xyz.stratalab.sdk.display

import xyz.stratalab.sdk.display.DisplayOps.DisplayTOps
import xyz.stratalab.sdk.validation.TransactionAuthorizationError.AuthorizationFailed
import xyz.stratalab.sdk.validation.TransactionSyntaxError._
import xyz.stratalab.sdk.validation.{TransactionSyntaxError, ValidationError}
import xyz.stratalab.quivr.runtime.QuivrRuntimeError
import xyz.stratalab.quivr.runtime.QuivrRuntimeErrors.ContextError.{
  FailedToFindDatum,
  FailedToFindDigestVerifier,
  FailedToFindInterface,
  FailedToFindSignatureVerifier
}
import xyz.stratalab.quivr.runtime.QuivrRuntimeErrors.ValidationError.{
  EvaluationAuthorizationFailed,
  LockedPropositionIsUnsatisfiable,
  MessageAuthorizationFailed,
  UserProvidedInterfaceFailure
}

trait ValidationErrorDisplayOps {

  implicit val validationErrorDisplay: DisplayOps[ValidationError] = {
    case err: TransactionSyntaxError => err.display
    case err: AuthorizationFailed    => err.display
    case _                           => "Unknown validation error" // Should not get here
  }

  implicit val syntaxErrorDisplay: DisplayOps[TransactionSyntaxError] = {
    case EmptyInputs               => "Transaction has no inputs"
    case _: DuplicateInput         => "Transaction has duplicate inputs"
    case ExcessiveOutputsCount     => "Transaction has too many outputs"
    case _: InvalidTimestamp       => "Transaction has an invalid timestamp"
    case _: InvalidSchedule        => "Transaction has an invalid schedule"
    case _: NonPositiveOutputValue => "Transaction has an output with a non-positive quantity value"
    case _: InsufficientInputFunds => "Transaction inputs cannot satisfy outputs"
    case _: InvalidProofType       => "Transaction has a proof whose type does not match its corresponding proposition"
    case InvalidDataLength         => "Transaction has an invalid size"
    case _: InvalidUpdateProposal  => "Transaction has an invalid UpdateProposal"
  }

  implicit val authorizationErrorDisplay: DisplayOps[AuthorizationFailed] = (err: AuthorizationFailed) =>
    s"Authorization failed. Causes:\n" + err.errors.map("- " + _.display).mkString("\n")

  implicit val quivrErrorDisplay: DisplayOps[QuivrRuntimeError] = {
    case FailedToFindDigestVerifier        => "Failed to find digest verifier"
    case FailedToFindSignatureVerifier     => "Failed to find signature verifier"
    case FailedToFindDatum                 => "Failed to find datum"
    case FailedToFindInterface             => "Failed to find interface"
    case UserProvidedInterfaceFailure      => "User provided interface failure"
    case LockedPropositionIsUnsatisfiable  => "Locked proposition is unsatisfiable"
    case MessageAuthorizationFailed(proof) => s"Transaction Bind on proof is invalid. Proof: ${proof.display}"
    case EvaluationAuthorizationFailed(proposition, proof) =>
      Seq(
        "Proof does not satisfy proposition.",
        proposition.display,
        proof.display
      ).mkString("\n")
    case _ => "Unknown Quivr Runtime error" // Should not get here
  }
}
