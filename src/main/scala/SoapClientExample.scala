import scala.util.{Success, Failure}
import scala.io.Source

object SoapClientExample extends App {

  private val requestFileStream = Option(getClass.getClassLoader.getResourceAsStream("example-soap-request.xml")).getOrElse {
    throw new IllegalArgumentException(s"Example response file not found in resources")
  }
  private val responseFileStream = Option(getClass.getClassLoader.getResourceAsStream("example-encrypted-and-signed-soap-response.xml")).getOrElse {
    throw new IllegalArgumentException(s"Example response file not found in resources")
  }

  private val soapRequest: String = Source.fromInputStream(requestFileStream, "UTF-8").mkString
  private val soapEncryptedResponse: String = Source.fromInputStream(responseFileStream, "UTF-8").mkString

  requestFileStream.close()
  responseFileStream.close()

  // Sender's keystore (contains the sender's private key and recipient's public certificate)
  // This keystore is loaded from the classpath resources folder
  val keystorePath = "sender-keystore.jks"
  val keystorePassword = "changeit"  // Password to access the keystore file

  // Sender's private key (used for signing outgoing messages and decrypting incoming responses)
  // - Alias: identifies which key entry to use from the keystore
  // - Password: protects the private key entry within the keystore
  val myKeyAlias = "senderkey"
  val myKeyPassword = "changeit"

  // Recipient's public certificate (used for encrypting outgoing messages and verifying incoming signatures)
  // This certificate is also stored in the sender's keystore for convenience
  val recipientCertAlias = "recipientcert"

  // ============================================================================
  // Outgoing Request: Sign and Encrypt
  // ============================================================================
  // Operation flow:
  // 1. Sign with sender's private key (myKeyAlias) - proves message authenticity
  // 2. Encrypt with recipient's public certificate (recipientCertAlias) - ensures confidentiality
  // Only the recipient with the matching private key can decrypt this message
  SoapSecurity.signAndEncryptMessage(
    soapRequest,
    keystorePath,
    keystorePassword,
    myKeyAlias,
    myKeyPassword,
    recipientCertAlias
  ) match {
    case Success(securedMessage) =>
      println("Secured SOAP message:")
      println(securedMessage)

      // Send securedMessage to web service...
      // val response = sendToWebService(securedMessage)

    case Failure(ex) =>
      println(s"Failed to secure message: ${ex.getMessage}")
  }

  // ============================================================================
  // Incoming Response: Decrypt and Verify
  // ============================================================================
  // Operation flow:
  // 1. Decrypt with sender's private key (myKeyAlias) - the response was encrypted with sender's public certificate
  // 2. Verify signature with recipient's public certificate (recipientCertAlias) - confirms response authenticity
  // This ensures the response came from the expected recipient and was not tampered with
  SoapResponseHandler.decryptAndVerifyResponse(
    soapEncryptedResponse,
    keystorePath,
    keystorePassword,
    myKeyAlias,
    myKeyPassword,
    Some(recipientCertAlias)
  ) match {
    case Success(decryptedResponse) =>
      println("Decrypted response:")
      println(decryptedResponse)
    case Failure(ex) =>
      println(s"Failed to process response: ${ex.getMessage}")
  }
}
