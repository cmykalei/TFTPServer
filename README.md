# UML Outline for TFTP Server Program

## TFTPServer Class

| **Attributes**       | **Type**        |
|----------------------|-----------------|
| `serverSocket`       | `DatagramSocket`|
| `port`               | `int`           |
| `maxRetransmissions` | `int`           |

| **Methods**                 | **Parameters**                       | **Return** |
|-----------------------------|--------------------------------------|------------|
| `startServer`               |                                      | `void`     |
| `sendError`                 | `InetAddress, int, int`              | `void`     |
| `openSocket`                | `InetAddress, int, String`           | `void`     |
| `reTransmit`                | `int`                                | `void`     |
| `handleTransfer`            | `InetAddress, int, File`             | `void`     |

## TFTPClient Class

| **Attributes**  | **Type**        |
|-----------------|-----------------|
| `clientSocket`  | `DatagramSocket`|
| `serverPort`    | `int`           |
| `serverAddress` | `InetAddress`   |

| **Methods**                 | **Parameters**           | **Return** |
|-----------------------------|--------------------------|------------|
| `sendRRQ`                   | `String`                 | `void`     |
| `recvData`                  |                          | `void`     |
| `sendAck`                   | `int`                    | `void`     |
| `recvNext`                  |                          | `void`     |
| `checkEOF`                  | `byte[]`                 | `void`     |
| `handleLoss`                |                          | `void`     |

## PacketHandler Class

| **Attributes** | **Type**         |
|----------------|------------------|
| `packet`       | `DatagramPacket` |

| **Methods**                 | **Parameters**                  | **Return** |
|-----------------------------|---------------------------------|------------|
| `createRRQ`                 | `String`                        | `byte[]`   |
| `createData`                | `int, byte[]`                   | `byte[]`   |
| `createAck`                 | `int`                           | `byte[]`   |
| `createError`               | `int, String`                   | `byte[]`   |
| `parsePacket`               | `DatagramPacket`                | `void`     |

## FileHandler Class

| **Attributes** | **Type** |
|----------------|----------|
| `file`         | `File`   |

| **Methods**                 | **Parameters**              | **Return** |
|-----------------------------|-----------------------------|------------|
| `openFile`                  | `String`                    | `File`     |
| `readBlock`                 | `int, int`                  | `byte[]`   |
| `writeBlock`                | `byte[], int`               | `void`     |
| `isEOF`                     | `byte[]`                    | `boolean`  |
