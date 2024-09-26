package xyz.stratalab.crypto.catsinstances.eqs

import cats.Eq
import xyz.stratalab.crypto.hash.digest.implicits._
import xyz.stratalab.crypto.hash.digest.Digest

trait EqInstances {
  implicit def digestEq[T: Digest]: Eq[T] = (digestA, digestB) => digestA.bytes.sameElements(digestB.bytes)
}
