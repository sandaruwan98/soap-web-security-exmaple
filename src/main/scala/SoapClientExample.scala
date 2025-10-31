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

  // Configuration
  val keystorePath = "sender-keystore.jks"
  val keystorePassword = "changeit"
  val myKeyAlias = "senderkey"
  val myKeyPassword = "changeit"
  val recipientCertAlias = "recipientcert"

  // Sign and encrypt outgoing request
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

  // Decrypt and verify response
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
