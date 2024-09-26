package xyz.stratalab.sdk.servicekit

import munit.CatsEffectSuite
import cats.effect.IO
import cats.effect.kernel.Resource
import xyz.stratalab.sdk.dataApi.WalletKeyApiAlgebra
import xyz.stratalab.sdk.wallet.WalletApi

import scala.concurrent.duration.Duration
import java.io.File
import java.nio.file.{Files, Path, Paths}
import xyz.stratalab.sdk.builders.TransactionBuilderApi
import xyz.stratalab.sdk.constants.NetworkConstants._
import xyz.stratalab.sdk.dataApi.WalletStateAlgebra
import xyz.stratalab.sdk.syntax.cryptoToPbKeyPair
import xyz.stratalab.crypto.generation.KeyInitializer.Instances.extendedEd25519Initializer
import xyz.stratalab.crypto.signing.{ExtendedEd25519, KeyPair}
import quivr.models

import java.sql.Connection

trait BaseSpec extends CatsEffectSuite with WalletStateResource {
  override val munitTimeout: Duration = Duration(180, "s")

  val TEST_DIR = "./tmp"
  val testDir: File = Paths.get(TEST_DIR).toFile
  val DB_FILE = s"$TEST_DIR/wallet.db"
  val walletKeyApi: WalletKeyApiAlgebra[IO] = WalletKeyApi.make[IO]()
  val walletApi: WalletApi[IO] = WalletApi.make[IO](walletKeyApi)

  val transactionBuilderApi: TransactionBuilderApi[IO] =
    TransactionBuilderApi.make[IO](PRIVATE_NETWORK_ID, MAIN_LEDGER_ID)
  val dbConnection: Resource[IO, Connection] = walletResource(DB_FILE)
  val walletStateApi: WalletStateAlgebra[IO] = WalletStateApi.make[IO](dbConnection, walletApi)

  def mockMainKeyPair: models.KeyPair = {
    implicit val extendedEd25519Instance: ExtendedEd25519 = new ExtendedEd25519
    val sk = extendedEd25519Initializer.random()
    cryptoToPbKeyPair(KeyPair(sk, extendedEd25519Instance.getVerificationKey(sk)))
  }

  private def removeDir() =
    if (testDir.exists()) {
      Paths.get(TEST_DIR).toFile.listFiles().map(_.delete()).mkString("\n")
      Files.deleteIfExists(Paths.get(TEST_DIR))
    }

  val testDirectory: FunFixture[Path] = FunFixture[Path](
    setup = { _ =>
      removeDir()
      Files.createDirectory(Paths.get(TEST_DIR))
    },
    teardown = { _ => removeDir() }
  )
}
