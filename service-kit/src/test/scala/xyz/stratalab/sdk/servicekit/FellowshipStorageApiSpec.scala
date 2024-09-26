package xyz.stratalab.sdk.servicekit

import cats.effect.IO
import xyz.stratalab.sdk.dataApi.{FellowshipStorageAlgebra, WalletFellowship}
import munit.CatsEffectSuite
import xyz.stratalab.sdk.constants.NetworkConstants

class FellowshipStorageApiSpec extends CatsEffectSuite with BaseSpec {

  val fellowshipApi: FellowshipStorageAlgebra[IO] = FellowshipStorageApi.make[IO](dbConnection)

  testDirectory.test("addFellowship then findFellowships") { _ =>
    val fellowship = WalletFellowship(2, "testFellowship")
    assertIO(
      for {
        init <- walletStateApi
          .initWalletState(NetworkConstants.PRIVATE_NETWORK_ID, NetworkConstants.MAIN_NETWORK_ID, mockMainKeyPair)
        _           <- fellowshipApi.addFellowship(fellowship)
        fellowships <- fellowshipApi.findFellowships()
      } yield fellowships.length == 3 && fellowships.last == fellowship,
      true
    )
  }
}
