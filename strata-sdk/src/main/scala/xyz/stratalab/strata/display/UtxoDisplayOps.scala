package xyz.stratalab.sdk.display

import xyz.stratalab.sdk.codecs.AddressCodecs
import xyz.stratalab.sdk.display.DisplayOps.DisplayTOps
import xyz.stratalab.sdk.models.LockAddress
import xyz.stratalab.sdk.models.transaction.UnspentTransactionOutput

trait UtxoDisplayOps {

  implicit val utxoDisplay: DisplayOps[UnspentTransactionOutput] = (utxo: UnspentTransactionOutput) =>
    Seq(
      padLabel("LockAddress") + utxo.address.display,
      utxo.value.value.display
    ).mkString("\n")

  implicit val lockAddressDisplay: DisplayOps[LockAddress] = (lockAddress: LockAddress) =>
    AddressCodecs.encodeAddress(lockAddress)

}
