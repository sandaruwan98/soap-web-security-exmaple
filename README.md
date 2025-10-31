# SOAP Web Security Example

A Scala-based example demonstrating SOAP message security using WS-Security (WSS4J) for signing, encryption, decryption, and signature verification of SOAP messages.

## Overview

This project demonstrates how to implement secure SOAP communication using:
- **Digital Signatures** - Ensures message authenticity and integrity
- **Encryption** - Protects message confidentiality
- **Decryption** - Allows authorized recipients to read encrypted messages
- **Signature Verification** - Validates that messages come from trusted sources

## Technology Stack

- **Scala**: 3.3.7
- **SBT**: Build tool
- **WSS4J**: 1.6.19 (Apache Web Services Security for Java)
- **Jakarta SOAP API**: 3.0.2
- **SAAJ Implementation**: 3.0.4

## Project Structure

```
soap-web-security-example/
├── src/main/
│   ├── scala/
│   │   ├── SoapClientExample.scala    # Main example demonstrating usage
│   │   ├── SoapSecurity.scala         # Sign and encrypt operations
│   │   ├── SoapResponseHandler.scala  # Decrypt and verify operations
│   │   └── SoapUtil.scala             # XML/DOM utility helpers
│   └── resources/
│       ├── sender-keystore.jks        # Sender's keystore (private key + recipient cert)
│       ├── sender.crt                 # Sender's public certificate
│       ├── recipient-keystore.jks     # Recipient's keystore (private key + sender cert)
│       ├── recipient.crt              # Recipient's public certificate
│       ├── example-soap-request.xml   # Sample SOAP request
│       └── example-encrypted-and-signed-soap-response.xml  # Sample encrypted response
├── build.sbt
└── README.md
```

## Getting Started

### Prerequisites

- Java 11 or higher
- SBT (Scala Build Tool)

### Installation

1. Compile the project:
```bash
sbt compile
```

2. Run the example:
```bash
sbt run
```

## Usage

### Keystore Configuration

The example uses two keystores:

**Sender's Keystore (`sender-keystore.jks`)**:
- Contains sender's private key (alias: `senderkey`)
- Contains recipient's public certificate (alias: `recipientcert`)
- Password: `changeit`

**Recipient's Keystore (`recipient-keystore.jks`)**:
- Contains recipient's private key (alias: `recipientkey`)
- Contains sender's public certificate (alias: `sendercert`)
- Password: `changeit`

### Signing and Encrypting a SOAP Message

```scala
import SoapSecurity._

val soapRequest = """<?xml version="1.0" encoding="UTF-8"?>
  <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
    <soap:Body>
      <GetUserRequest xmlns="http://example.com/user">
        <UserId>12345</UserId>
      </GetUserRequest>
    </soap:Body>
  </soap:Envelope>"""

val result = SoapSecurity.signAndEncryptMessage(
  soapMessage = soapRequest,
  keystorePath = "sender-keystore.jks",
  keystorePassword = "changeit",
  signKeyAlias = "senderkey",
  signKeyPassword = "changeit",
  encryptCertAlias = "recipientcert"
)

result match {
  case Success(securedMessage) =>
    println("Secured message ready to send")
    // Send to web service...
  case Failure(ex) =>
    println(s"Error: ${ex.getMessage}")
}
```

### Decrypting and Verifying a SOAP Response

```scala
import SoapResponseHandler._

val result = SoapResponseHandler.decryptAndVerifyResponse(
  encryptedResponse = encryptedSoapResponse,
  keystorePath = "sender-keystore.jks",
  keystorePassword = "changeit",
  decryptKeyAlias = "senderkey",
  decryptKeyPassword = "changeit",
  verifyCertAlias = Some("recipientcert")
)

result match {
  case Success(decryptedResponse) =>
    println("Response decrypted and verified")
    // Process response...
  case Failure(ex) =>
    println(s"Error: ${ex.getMessage}")
}
```

## Security Flow

### Outgoing Request (Client → Server)

1. **Sign**: Message is signed with sender's **private key** (`senderkey`)
   - Proves the message came from the sender
   - Ensures message has not been tampered with

2. **Encrypt**: Message is encrypted with recipient's **public certificate** (`recipientcert`)
   - Only the recipient with the matching private key can decrypt
   - Ensures confidentiality

### Incoming Response (Server → Client)

1. **Decrypt**: Response is decrypted with sender's **private key** (`senderkey`)
   - The server encrypted the response using sender's public certificate
   - Only sender can decrypt it

2. **Verify**: Signature is verified using recipient's **public certificate** (`recipientcert`)
   - Confirms the response came from the expected server
   - Ensures response integrity


### Customization

To use your own keystores:

1. Update the keystore paths in `SoapClientExample.scala`
2. Update the key aliases and passwords
3. Ensure keystores are placed in `src/main/resources/`

## Creating Your Own Keystores

```bash
# Generate sender's keypair
keytool -genkeypair -alias senderkey -keyalg RSA -keysize 2048 \
  -keystore sender-keystore.jks -storepass changeit -keypass changeit \
  -dname "CN=Sender,OU=IT,O=Company,L=City,ST=State,C=US"

# Export sender's certificate
keytool -exportcert -alias senderkey -keystore sender-keystore.jks \
  -storepass changeit -file sender.crt

# Import recipient's certificate into sender's keystore
keytool -importcert -alias recipientcert -file recipient.crt \
  -keystore sender-keystore.jks -storepass changeit -noprompt
```

Repeat similar steps for the recipient's keystore.

## Example Output

When you run the example, you'll see:

```
Secured SOAP message:
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Header>
    <wsse:Security xmlns:wsse="...">
      <!-- Signature and encryption headers -->
    </wsse:Security>
  </soap:Header>
  <soap:Body>
    <!-- Encrypted content -->
  </soap:Body>
</soap:Envelope>

Decrypted response:
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <GetUserResponse xmlns="http://example.com/user">
      <User>
        <UserId>12345</UserId>
        <Name>John Doe</Name>
      </User>
    </GetUserResponse>
  </soap:Body>
</soap:Envelope>
```

## References

- [WSS4J Documentation](https://ws.apache.org/wss4j/)
- [WS-Security Specification](https://www.oasis-open.org/committees/tc_home.php?wg_abbrev=wss)
- [Java Keystore Documentation](https://docs.oracle.com/javase/8/docs/technotes/tools/unix/keytool.html)

## License

This is an example project for educational purposes.

## Contributing

Feel free to submit issues or pull requests for improvements.

---

**Note**: This is a demonstration project. For production use, implement proper error handling, logging, key management, and security practices.

