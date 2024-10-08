package xyz.stratalab.strata.servicekit

import cats.Monad
import cats.arrow.FunctionK
import cats.data.EitherT
import cats.effect.Async
import cats.implicits.{catsSyntaxApplicativeError, catsSyntaxEitherId, toBifunctorOps, toFlatMapOps, toFunctorOps}
import co.topl.brambl.models.TransactionId
import xyz.stratalab.sdk.builders.TransactionBuilderApi
import xyz.stratalab.sdk.constants.NetworkConstants.{MAIN_LEDGER_ID, MAIN_NETWORK_ID}
import xyz.stratalab.sdk.dataApi._
import xyz.stratalab.sdk.servicekit._
import xyz.stratalab.sdk.wallet.{Credentialler, CredentiallerInterpreter, WalletApi}

import java.nio.charset.StandardCharsets

class EasyApi[F[
  _
]: Monad: WalletKeyApiAlgebra: WalletStateAlgebra: TemplateStorageAlgebra: FellowshipStorageAlgebra: TransactionBuilderApi: WalletApi: BifrostQueryAlgebra: GenusQueryAlgebra: Credentialler] {
  val walletKeyApiAlgebra: WalletKeyApiAlgebra[F] = implicitly[WalletKeyApiAlgebra[F]]
  val walletStateAlgebra: WalletStateAlgebra[F] = implicitly[WalletStateAlgebra[F]]
  val templateStorageAlgebra: TemplateStorageAlgebra[F] = implicitly[TemplateStorageAlgebra[F]]
  val fellowshipStorageAlgebra: FellowshipStorageAlgebra[F] = implicitly[FellowshipStorageAlgebra[F]]
  val transactionBuilderApi: TransactionBuilderApi[F] = implicitly[TransactionBuilderApi[F]]
  val walletApi: WalletApi[F] = implicitly[WalletApi[F]]
  val bifrostQueryAlgebra: BifrostQueryAlgebra[F] = implicitly[BifrostQueryAlgebra[F]]
  val genusQueryAlgebra: GenusQueryAlgebra[F] = implicitly[GenusQueryAlgebra[F]]
  val credentialler: Credentialler[F] = implicitly[Credentialler[F]]

  // TODO: To be completed in TSDK-888
  def transferFunds(): F[TransactionId] =
    for {
      unproven <- transactionBuilderApi
        .buildTransferAllTransaction(???, ???, ???, ???, ???, ???)
        .map(_.toOption.getOrElse(throw new RuntimeException("Unable to build transaction")))
      proven <- credentialler.prove(unproven)
      res    <- bifrostQueryAlgebra.broadcastTransaction(proven)
    } yield res
}

object EasyApi {

  case class UnableToInitializeSdk(err: Throwable = null)
      extends RuntimeException(s"Unable to initialize SDK: ${err.getMessage}", err)

  case class InitArgs(
    networkId:    Int = MAIN_NETWORK_ID,
    ledgerId:     Int = MAIN_LEDGER_ID,
    host:         String = "localhost",
    port:         Int = 9084,
    secure:       Boolean = false,
    dbFile:       String = "wallet.db",
    keyFile:      String = "keyFile.json",
    mnemonicFile: String = "mnemonic.txt", // only needed for wallet creation
    passphrase:   Option[String] = None // only needed for wallet creation
  )

  def initialize[F[_]: Async](
    password: String,
    args:     InitArgs = InitArgs()
  ): F[EasyApi[F]] = initialize(password.getBytes(StandardCharsets.UTF_8), args)

  def initialize[F[_]: Async](
    password: Array[Byte],
    args:     InitArgs
  ): F[EasyApi[F]] = {
    implicit val wka: WalletKeyApiAlgebra[F] = WalletKeyApi.make[F]()
    implicit val wa: WalletApi[F] = WalletApi.make[F](wka)
    val walletConn = WalletStateResource.walletResource(args.dbFile)
    implicit val tsa: TemplateStorageAlgebra[F] = TemplateStorageApi.make[F](walletConn)
    implicit val fsa: FellowshipStorageAlgebra[F] = FellowshipStorageApi.make[F](walletConn)
    val channelResource = RpcChannelResource.channelResource[F](args.host, args.port, args.secure)
    implicit val gq: GenusQueryAlgebra[F] = GenusQueryAlgebra.make[F](channelResource)
    implicit val bq: BifrostQueryAlgebra[F] = BifrostQueryAlgebra.make[F](channelResource)
    implicit val tba: TransactionBuilderApi[F] =
      TransactionBuilderApi.make[F](args.networkId, args.ledgerId)

    implicit val wsa: WalletStateAlgebra[F] = WalletStateApi.make[F](walletConn, wa)

    implicit val fTof: FunctionK[F, F] = FunctionK.id[F]
    for {
      wallet <- wa.loadWallet(args.keyFile)
      keyPairRes <- wallet match {
        case Left(_) =>
          (for {
            vault <- EitherT(
              wa
                .createAndSaveNewWallet[F](
                  password,
                  args.passphrase,
                  name = args.keyFile,
                  mnemonicName = args.mnemonicFile
                )
                .map(_.leftMap(new RuntimeException("Unable to create wallet", _)).map(_.mainKeyVaultStore))
            )
            mainKey <- EitherT(
              wa
                .extractMainKey(vault, password)
                .map(_.leftMap(new RuntimeException("Unable to extract main key", _)))
            )
            res <- EitherT(
              wsa
                .initWalletState(args.networkId, args.ledgerId, mainKey)
                .map(_ => mainKey.asRight[RuntimeException])
                .handleError(e => (new RuntimeException("Unable to initialize wallet state", e)).asLeft)
            )
          } yield res).value
        case Right(vs) =>
          (for {
            mainKey <- EitherT(
              wa.extractMainKey(vs, password).map(_.leftMap(new RuntimeException("Unable to extract main key", _)))
            )
            _ <- EitherT(
              wsa
                .validateWalletInitialization(args.networkId, args.ledgerId, mainKey)
                .map(_.leftMap(m => new RuntimeException(s"Wallet state invalid: ${m.mkString("[", ",", "]")}")))
            )
          } yield mainKey).value
      }
    } yield keyPairRes match {
      case Left(err) => throw UnableToInitializeSdk(err)
      case Right(mainKey) =>
        implicit val c: Credentialler[F] = CredentiallerInterpreter.make[F](wa, wsa, mainKey)
        new EasyApi[F]
    }
  }
}
