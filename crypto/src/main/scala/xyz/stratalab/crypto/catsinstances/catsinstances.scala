package xyz.stratalab.crypto

//noinspection ScalaWeakerAccess
package object catsinstances {
  trait Implicits extends eqs.Implicits

  object implicits extends Implicits
}