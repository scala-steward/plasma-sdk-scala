package org.plasmalabs.crypto.hash

import cats.data.{Validated, ValidatedNec}

import scala.language.implicitConversions

package object digest {

  /**
   * Represents a digest with a size and the ability to convert to and from bytes.
   *
   * @tparam T the implemented digest type
   */
  trait Digest[T] {

    /**
     * The digest size.
     *
     * @return the size of the digest
     */
    def size: Int

    /**
     * Gets a validated digest from an array of bytes.
     *
     * @param bytes the bytes to be converted into a digest
     * @return a validated digest with a possible invalid digest error
     */
    def from(bytes: Array[Byte]): ValidatedNec[InvalidDigestFailure, T]

    /**
     * Gets the bytes representing the digest.
     *
     * @param d the digest to convert to bytes
     * @return the bytes of the digest
     */
    def bytes(d: T): Array[Byte]

    def empty: T = from(Array.fill(size)(0: Byte))
      .getOrElse(throw new Error(s"Failed to validate empty digest of size $size!"))
  }

  object Digest {
    def apply[T](implicit instance: Digest[T]): Digest[T] = instance

    trait Ops[T] {
      def typeClassInstance: Digest[T]
      def self: T

      def size: Int = typeClassInstance.size
      def bytes: Array[Byte] = typeClassInstance.bytes(self)
    }

    trait ToDigestOps {

      implicit def toDigestOps[T](target: T)(implicit tc: Digest[T]): Ops[T] = new Ops[T] {
        def typeClassInstance: Digest[T] = tc
        def self: T = target
      }
    }
  }

  case class Digest32(val value: Array[Byte])

  object Digest32 {
    val size = 32

    /**
     * Gets a validated Digest32 guaranteed to be 32 bytes long.
     *
     * @param bytes the bytes to convert to a digest
     * @return the digest or an invalid error
     */
    def validated(bytes: Array[Byte]): ValidatedNec[InvalidDigestFailure, Digest32] =
      Validated.condNec(bytes.length == size, Digest32(bytes), IncorrectSize)
  }

  case class Digest64(val value: Array[Byte])

  object Digest64 {
    val size = 64

    /**
     * Gets a validated Digest64 guaranteed to be 64 bytes long.
     *
     * @param bytes the bytes to convert to a digest
     * @return the digest or an invalid error
     */
    def validated(bytes: Array[Byte]): ValidatedNec[InvalidDigestFailure, Digest64] =
      Validated.condNec(bytes.length == size, Digest64(bytes), IncorrectSize)
  }

  sealed trait InvalidDigestFailure
  case object IncorrectSize extends InvalidDigestFailure

  trait Instances {

    implicit val digestDigest32: Digest[Digest32] = new Digest[Digest32] {
      override def size: Int = Digest32.size

      override def from(bytes: Array[Byte]): ValidatedNec[InvalidDigestFailure, Digest32] = Digest32.validated(bytes)

      override def bytes(d: Digest32): Array[Byte] = d.value
    }

    implicit val digestDigest64: Digest[Digest64] = new Digest[Digest64] {
      override def size: Int = Digest64.size

      override def from(bytes: Array[Byte]): ValidatedNec[InvalidDigestFailure, Digest64] = Digest64.validated(bytes)

      override def bytes(d: Digest64): Array[Byte] = d.value
    }
  }

  trait DigestImplicits extends Instances with Digest.ToDigestOps

  object implicits extends DigestImplicits
}
