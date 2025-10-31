import org.apache.ws.security.WSSecurityEngine
import org.apache.ws.security.WSPasswordCallback

import javax.security.auth.callback.{Callback, CallbackHandler, UnsupportedCallbackException}
import SoapUtil.*
import org.apache.ws.security.components.crypto.{CryptoFactory, Merlin}

import java.security.KeyStore
import java.util.Properties
import scala.util.{Try, Using}

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
    val crypto = createCryptoWithKey(
      keystorePath,
      keystorePassword,
      decryptKeyAlias,
      decryptKeyPassword,
      verifyCertAlias
    )

    // Create password callback handler
    val callbackHandler = new PasswordCallbackHandler(decryptKeyAlias, decryptKeyPassword)

    // Process security header (decrypts and verifies)
    secEngine.processSecurityHeader(document, null, callbackHandler, crypto)

    document.toXmlString
  }

  private def createCryptoWithKey(
                                   keystorePath: String,
                                   keystorePassword: String,
                                   keyAlias: String,
                                   keyPassword: String,
                                   certAlias: Option[String]
                                 ): Merlin = {
    val properties = new Properties()
    properties.setProperty("org.apache.wss4j.crypto.provider",
      "org.apache.ws.security.components.crypto.Merlin")

    val crypto = CryptoFactory.getInstance(properties).asInstanceOf[Merlin]

    // Load original keystore from classpath
    val keystoreStream = Option(getClass.getClassLoader.getResourceAsStream(keystorePath)).getOrElse {
      throw new IllegalArgumentException(s"Keystore not found in resources: $keystorePath")
    }
    
    val originalKeystore = Using(keystoreStream) { stream =>
      val ks = KeyStore.getInstance("JKS")
      ks.load(stream, keystorePassword.toCharArray)
      ks
    }.get

    // Create temporary keystore with required keys
    val tempKeystore = KeyStore.getInstance("JKS")
    tempKeystore.load(null, null)

    // Add private key for decryption
    val key = originalKeystore.getKey(keyAlias, keyPassword.toCharArray)
    val certChain = originalKeystore.getCertificateChain(keyAlias)
    tempKeystore.setKeyEntry(keyAlias, key, keyPassword.toCharArray, certChain)

    // Add certificate for signature verification if provided
    certAlias.foreach { alias =>
      val cert = originalKeystore.getCertificate(alias)
      tempKeystore.setCertificateEntry(alias, cert)
    }

    crypto.setKeyStore(tempKeystore)
    crypto
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
