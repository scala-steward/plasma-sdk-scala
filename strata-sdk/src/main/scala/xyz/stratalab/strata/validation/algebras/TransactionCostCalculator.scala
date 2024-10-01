package xyz.stratalab.sdk.validation.algebras

import xyz.stratalab.sdk.models.transaction.IoTransaction

trait TransactionCostCalculator {

  /**
   * Estimates the cost of including the Transaction in a block.
   * @param transaction The transaction to cost
   * @return a Long value representing the cost
   */
  def costOf(transaction: IoTransaction): Long
}
