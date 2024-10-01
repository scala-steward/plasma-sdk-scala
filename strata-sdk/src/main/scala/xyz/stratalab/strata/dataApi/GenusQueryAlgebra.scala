package xyz.stratalab.sdk.dataApi

import cats.effect.kernel.Resource
import cats.effect.kernel.Sync
import xyz.stratalab.sdk.models.LockAddress
import xyz.stratalab.indexer.services.QueryByLockAddressRequest
import xyz.stratalab.indexer.services.TransactionServiceGrpc
import xyz.stratalab.indexer.services.Txo
import xyz.stratalab.indexer.services.TxoState
import io.grpc.ManagedChannel

/**
 * Defines a Genus Query API for interacting with a Genus node.
 */
trait GenusQueryAlgebra[F[_]] {

  /**
   * Query and retrieve a set of UTXOs encumbered by the given LockAddress.
   * @param fromAddress The lock address to query the unspent UTXOs by.
   * @param txoState The state of the UTXOs to query. By default, only unspent UTXOs are returned.
   * @return A sequence of UTXOs.
   */
  def queryUtxo(fromAddress: LockAddress, txoState: TxoState = TxoState.UNSPENT): F[Seq[Txo]]
}

object GenusQueryAlgebra {

  def make[F[_]: Sync](channelResource: Resource[F, ManagedChannel]): GenusQueryAlgebra[F] =
    new GenusQueryAlgebra[F] {

      def queryUtxo(fromAddress: LockAddress, txoState: TxoState = TxoState.UNSPENT): F[Seq[Txo]] = {
        import cats.implicits._
        (for {
          channel <- channelResource
        } yield channel).use { channel =>
          for {
            blockingStub <- Sync[F].point(
              TransactionServiceGrpc.blockingStub(channel)
            )
            response <- Sync[F].blocking(
              blockingStub
                .getTxosByLockAddress(
                  QueryByLockAddressRequest(fromAddress, None, txoState)
                )
            )
          } yield response.txos
        }
      }
    }
}
