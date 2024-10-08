package xyz.stratalab.sdk.servicekit

import cats.effect.IO
import co.topl.brambl.models.Indices
import munit.CatsEffectSuite
import xyz.stratalab.strata.servicekit.EasyApi
import xyz.stratalab.strata.servicekit.EasyApi.{InitArgs, UnableToInitializeSdk}

class EasyApiSpec extends CatsEffectSuite with BaseSpec {

  private def getFileName(name: String) = s"$TEST_DIR/$name"

  private val testArgs = InitArgs(
    dbFile = getFileName("easyApi.db"),
    keyFile = getFileName("easyApiKey.json"),
    mnemonicFile = getFileName("easyApiMnemonic.txt")
  )

  testDirectory.test("Initialize Easy API > Happy path") { _ =>
    assertIO(
      for {
        apiFirst  <- EasyApi.initialize[IO]("password", testArgs)
        idxFirst  <- apiFirst.walletStateAlgebra.getCurrentIndicesForFunds("self", "default", None)
        _         <- apiFirst.walletStateAlgebra.updateWalletState("test", "test", None, None, Indices(1, 1, 2))
        apiSecond <- EasyApi.initialize[IO]("password", testArgs)
        idxSecond <- apiSecond.walletStateAlgebra.getCurrentIndicesForFunds("self", "default", None)
      } yield idxFirst.contains(Indices(1, 1, 1)) && idxSecond.contains(Indices(1, 1, 2)),
      true
    )
  }

  testDirectory.test("Initialize Easy API > invalid password") { _ =>
    interceptMessageIO[UnableToInitializeSdk]("Unable to initialize SDK: Unable to extract main key")(for {
      createWallet <- EasyApi.initialize[IO]("password", testArgs)
      accessWallet <- EasyApi.initialize[IO]("wrong-password", testArgs)
    } yield true)
  }

  testDirectory.test("Initialize Easy API > keyfile exists but mnemonic file does not") { _ =>
    assertIO(
      for {
        createWallet <- EasyApi.initialize[IO]("password", testArgs)
        accessWallet <- EasyApi.initialize[IO]("password", testArgs.copy(mnemonicFile = getFileName("nonExistent.txt")))
      } yield true,
      true
    ) // no exception since mnemonic file is only used for wallet creation
  }

  testDirectory.test("Initialize Easy API > mnemonic file exists but keyfile does not") { _ =>
    interceptMessageIO[UnableToInitializeSdk]("Unable to initialize SDK: Unable to create wallet")(for {
      createWallet <- EasyApi.initialize[IO]("password", testArgs)
      accessWallet <- EasyApi.initialize[IO]("password", testArgs.copy(keyFile = getFileName("nonExistent.json")))
    } yield true)
  }

  testDirectory.test("Initialize Easy API > keyfile exists but state does not") { _ =>
    interceptMessageIO[UnableToInitializeSdk](
      "Unable to initialize SDK: Wallet state invalid: [State not initialized]"
    )(for {
      createWallet <- EasyApi.initialize[IO]("password", testArgs)
      accessWallet <- EasyApi.initialize[IO]("password", testArgs.copy(dbFile = getFileName("nonExistent.db")))
    } yield true)
  }

  testDirectory.test("Initialize Easy API > state exists but keyfile does not") { _ =>
    interceptMessageIO[UnableToInitializeSdk]("Unable to initialize SDK: Unable to initialize wallet state")(for {
      createWallet <- EasyApi.initialize[IO]("password", testArgs)
      accessWallet <- EasyApi.initialize[IO](
        "password",
        testArgs.copy(keyFile = getFileName("nonExistent.json"), mnemonicFile = getFileName("nonExistent.txt"))
      )
    } yield true)
  }

  testDirectory.test("Initialize Easy API > keyfile and state exists but mismatched") { _ =>
    interceptMessageIO[UnableToInitializeSdk]("Unable to initialize SDK: Wallet state invalid: [mainKey mismatch]")(
      for {
        createWallet1 <- EasyApi.initialize[IO]("password", testArgs)
        createWallet2 <- EasyApi.initialize[IO](
          "password",
          testArgs.copy(
            keyFile = getFileName("second.json"),
            mnemonicFile = getFileName("second.txt"),
            dbFile = getFileName("second.db")
          )
        )
        accessWallet <- EasyApi.initialize[IO]("password", testArgs.copy(dbFile = getFileName("second.db")))
      } yield true
    )
  }
}
