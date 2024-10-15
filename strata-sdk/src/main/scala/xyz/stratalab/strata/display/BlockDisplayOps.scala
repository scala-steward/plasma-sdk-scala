package xyz.stratalab.sdk.display

import xyz.stratalab.sdk.utils.Encoding
import xyz.stratalab.consensus.models.BlockId

trait BlockDisplayOps {

  implicit val blockIdDisplay: DisplayOps[BlockId] = (blockId: BlockId) =>
    Encoding.encodeToBase58(blockId.value.toByteArray())
}
