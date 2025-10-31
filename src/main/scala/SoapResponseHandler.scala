import SoapUtil.*
import org.apache.ws.security.{WSPasswordCallback, WSSecurityEngine}

import javax.security.auth.callback.{Callback, CallbackHandler, UnsupportedCallbackException}
import scala.util.Try

object SoapResponseHandler {

  def decryptAndVerifyResponse(
                                encryptedResponse: String,
                                keystorePath: String,
                                keystorePassword: String,
                                decryptKeyAlias: String,
                                decryptKeyPassword: String,
                                verifyCertAlias: Option[String] = None
                              ): Try[String] = Try {

    val document = encryptedResponse.toDocument
    val secEngine = new WSSecurityEngine()

    // Create crypto with keystore
    val crypto = createCrypto(keystorePath, keystorePassword)

    // Create password callback handler
    val callbackHandler = new PasswordCallbackHandler(decryptKeyAlias, decryptKeyPassword)

    // Process security header (decrypts and verifies)
    secEngine.processSecurityHeader(document, null, callbackHandler, crypto)

    document.toXmlString
  }
}

// Callback handler for providing passwords
class PasswordCallbackHandler(alias: String, password: String) extends CallbackHandler {
  override def handle(callbacks: Array[Callback]): Unit = {
    callbacks.foreach {
      case pc: WSPasswordCallback if pc.getIdentifier == alias =>
        pc.setPassword(password)
      case pc: WSPasswordCallback =>
        throw new UnsupportedCallbackException(pc, s"Unknown alias: ${pc.getIdentifier}")
      case other =>
        throw new UnsupportedCallbackException(other, "Unsupported callback type")
    }
  }
}
