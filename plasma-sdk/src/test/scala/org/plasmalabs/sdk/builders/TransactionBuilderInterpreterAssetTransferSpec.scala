package org.plasmalabs.sdk.builders

import org.plasmalabs.sdk.models.box.Value
import org.plasmalabs.sdk.models.transaction.IoTransaction
import org.plasmalabs.sdk.syntax.LvlType
import org.plasmalabs.sdk.syntax.ioTransactionAsTransactionSyntaxOps
import org.plasmalabs.sdk.syntax.valueToTypeIdentifierSyntaxOps

class TransactionBuilderInterpreterAssetTransferSpec extends TransactionBuilderInterpreterSpecBase {

  test("unsupported token type in txos is filtered out/ignored") {
    val testTx = buildTransferAmountTransaction
      .withTokenIdentifier(assetGroupSeries.value.typeIdentifier)
      .withTxos(mockTxos :+ valToTxo(Value.defaultInstance)) // Value.empty
      .run
    val expectedTx = buildTransferAmountTransaction
      .withTokenIdentifier(assetGroupSeries.value.typeIdentifier)
      .withTxos(mockTxos) // The only difference is the unsupported txo is not present
      .run
    assert(
      (testTx.isRight && expectedTx.isRight) &&
      sortedTx(testTx.toOption.get).computeId == sortedTx(expectedTx.toOption.get).computeId
    )
  }

  test("buildTransferAmountTransaction > quantity to transfer is non positive") {
    val testTx = buildTransferAmountTransaction
      .withTokenIdentifier(assetGroupSeries.value.typeIdentifier)
      .withAmount(0)
      .run
    assertEquals(testTx, Left(UserInputErrors(Seq(UserInputError(s"quantity to transfer must be positive")))))
  }

  test("buildTransferAmountTransaction > a txo isnt tied to lockPredicateFrom") {
    val testTx = buildTransferAmountTransaction
      .withTokenIdentifier(assetGroupSeries.value.typeIdentifier)
      .withTxos(mockTxos :+ valToTxo(lvlValue, trivialLockAddress))
      .run
    assertEquals(
      testTx,
      Left(UserInputErrors(Seq(UserInputError(s"every lock in the txos must correspond to lockPredicateFrom"))))
    )
  }

  test("buildTransferAmountTransaction > non sufficient funds") {
    val testTx = buildTransferAmountTransaction
      .withTokenIdentifier(assetGroupSeries.value.typeIdentifier)
      .withAmount(4)
      .run
    assertEquals(
      testTx,
      Left(
        UserInputErrors(
          Seq(
            UserInputError(
              s"All tokens selected to transfer do not have enough funds to transfer. The desired quantity to transfer is 4 but the 2 tokens selected to transfer only have a combined quantity of 2."
            )
          )
        )
      )
    )
  }

  test("buildTransferAmountTransaction > fee not satisfied") {
    val testTx = buildTransferAmountTransaction
      .withTokenIdentifier(assetGroupSeries.value.typeIdentifier)
      .withFee(3)
      .run
    assertEquals(
      testTx,
      Left(
        UserInputErrors(
          Seq(UserInputError(s"Not enough LVLs in input to satisfy fee"))
        )
      )
    )
  }

  test("buildTransferAmountTransaction > [complex, GROUP_AND_SERIES] duplicate inputs are merged and split correctly") {
    val testTx = buildTransferAmountTransaction
      .withTokenIdentifier(assetGroupSeries.value.typeIdentifier)
      .run
    val expectedTx = IoTransaction.defaultInstance
      .withDatum(txDatum)
      .withInputs(buildStxos())
      .withOutputs(
        // to recipient
        buildRecipientUtxos(List(assetGroupSeries))
        ++
        // change due to excess fee and transfer input
        buildChangeUtxos(List(lvlValue, assetGroupSeries))
        ++
        // change values unaffected by recipient transfer and fee
        buildChangeUtxos(
          mockChange
            .filterNot(v => List(LvlType, assetGroupSeries.value.typeIdentifier).contains(v.value.typeIdentifier))
        )
      )
    assertEquals(
      sortedTx(testTx.toOption.get).computeId,
      sortedTx(expectedTx).computeId
    )
  }

  test("buildTransferAmountTransaction > [complex, GROUP] duplicate inputs are merged and split correctly") {
    val testTx = buildTransferAmountTransaction
      .withTokenIdentifier(assetGroup.value.typeIdentifier)
      .run
    val expectedTx = IoTransaction.defaultInstance
      .withDatum(txDatum)
      .withInputs(buildStxos())
      .withOutputs(
        // to recipient
        buildRecipientUtxos(List(assetGroup))
        ++
        // change due to excess fee and transfer input
        buildChangeUtxos(List(lvlValue, assetGroup))
        ++
        // change values unaffected by recipient transfer and fee
        buildChangeUtxos(
          mockChange.filterNot(v => List(LvlType, assetGroup.value.typeIdentifier).contains(v.value.typeIdentifier))
        )
      )
    assertEquals(
      sortedTx(testTx.toOption.get).computeId,
      sortedTx(expectedTx).computeId
    )
  }

  test("buildTransferAmountTransaction > [complex, SERIES] duplicate inputs are merged and split correctly") {
    val testTx = buildTransferAmountTransaction
      .withTokenIdentifier(assetSeries.value.typeIdentifier)
      .run
    val expectedTx = IoTransaction.defaultInstance
      .withDatum(txDatum)
      .withInputs(buildStxos())
      .withOutputs(
        // to recipient
        buildRecipientUtxos(List(assetSeries))
        ++
        // change due to excess fee and transfer input
        buildChangeUtxos(List(lvlValue, assetSeries))
        ++
        // change values unaffected by recipient transfer and fee
        buildChangeUtxos(
          mockChange.filterNot(v => List(LvlType, assetSeries.value.typeIdentifier).contains(v.value.typeIdentifier))
        )
      )
    assertEquals(
      sortedTx(testTx.toOption.get).computeId,
      sortedTx(expectedTx).computeId
    )
  }

  test("buildTransferAmountTransaction > [simplest case] no change, only 1 output") {
    val txos = Seq(valToTxo(assetGroupSeries))
    val testTx = buildTransferAmountTransaction
      .withTokenIdentifier(assetGroupSeries.value.typeIdentifier)
      .withTxos(txos)
      .withFee(0)
      .run
    val expectedTx = IoTransaction.defaultInstance
      .withDatum(txDatum)
      .withInputs(buildStxos(txos))
      .withOutputs(buildRecipientUtxos(List(assetGroupSeries)))
    assertEquals(testTx.toOption.get.computeId, expectedTx.computeId)
  }

  test("buildTransferAmountTransaction > IMMUTABLE asset quantity descriptor in transfer type") {
    val testTx = buildTransferAmountTransaction
      .withTokenIdentifier(assetGroupSeriesImmutable.value.typeIdentifier)
      .run
    assertEquals(
      testTx,
      Left(
        UserInputErrors(
          Seq(
            UserInputError(s"Invalid asset quantity descriptor type. If identifier is an asset, it must be liquid.")
          )
        )
      )
    )
  }

  test("buildTransferAmountTransaction > FRACTIONABLE asset quantity descriptor in transfer type") {
    val testTx = buildTransferAmountTransaction
      .withTokenIdentifier(assetGroupSeriesFractionable.value.typeIdentifier)
      .run
    assertEquals(
      testTx,
      Left(
        UserInputErrors(
          Seq(
            UserInputError(s"Invalid asset quantity descriptor type. If identifier is an asset, it must be liquid.")
          )
        )
      )
    )
  }

  test("buildTransferAmountTransaction > ACCUMULATOR asset quantity descriptor in transfer type") {
    val testTx = buildTransferAmountTransaction
      .withTokenIdentifier(assetGroupSeriesAccumulator.value.typeIdentifier)
      .run
    assertEquals(
      testTx,
      Left(
        UserInputErrors(
          Seq(
            UserInputError(s"Invalid asset quantity descriptor type. If identifier is an asset, it must be liquid.")
          )
        )
      )
    )
  }
}
