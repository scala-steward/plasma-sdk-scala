---
sidebar_position: 2
title: Easy Api
---

For the most common use cases, we provide an easy-to-use API that allows you to interact with the platform without having 
to worry about the underlying details. This API is available in the Service Kit.

## Initialization

To initialize the Easy API, you need to create an instance of the `EasyApi` class using the provided function. 
This class provides a set of methods that allow you to interact with the platform.

```scala
case class InitArgs(
    networkId:    Int = MAIN_NETWORK_ID,
    ledgerId:     Int = MAIN_LEDGER_ID,
    host:         String = "localhost",
    port:         Int = 9084,
    secure:       Boolean = false,
    dbFile:       String = "wallet.db",
    keyFile:      String = "keyFile.json",
    mnemonicFile: String = "mnemonic.txt",
    passphrase:   Option[String] = None
)

def initialize[F[_]: Async](
    password: String,
    args:     InitArgs = InitArgs()
): F[EasyApi[F]]
```

Optionally, you can provide a custom configuration to the `initialize` method. The configuration allows you to specify the
network to use, the node to connect to, and wallet storage files. If you don't provide a configuration, the default one will be used.

### Example

```scala
package xyz.stratalab.strata.servicekit

import cats.effect.IO

class Demo {
  val initialization = for {
    api <- EasyApi.initialize[IO]("your-wallet-password")
    /*
    * You can now the the api object to interact with the SDK here
    */
  } yield api
}
```

## Usage

TBD