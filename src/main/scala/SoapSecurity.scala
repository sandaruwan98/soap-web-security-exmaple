
import java.util.Properties
import java.security.KeyStore
import java.io.FileInputStream
import scala.util.{Try, Using}
import SoapUtil.*
import org.apache.ws.security.WSConstants
import org.apache.ws.security.components.crypto.{CryptoFactory, Merlin}
import org.apache.ws.security.message.{WSSecEncrypt, WSSecHeader, WSSecSignature}

object SoapSecurity {

  def signAndEncryptMessage(
                             soapMessage: String,
                             keystorePath: String,
                             keystorePassword: String,
                             signKeyAlias: String,
                             signKeyPassword: String,
                             encryptCertAlias: String
                           ): Try[String] = Try {

    // Convert string to DOM Document
    val document = soapMessage.toDocument

    // Create security header
    val secHeader = new WSSecHeader()
    secHeader.insertSecurityHeader(document)

    // Load keystore
    val crypto = createCrypto(keystorePath, keystorePassword)

    // Add signature
    val signature = new WSSecSignature()
    signature.setUserInfo(signKeyAlias, signKeyPassword)
    signature.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE)
    signature.build(document, crypto, secHeader)

    // Add encryption
    val encrypt = new WSSecEncrypt()
    encrypt.setUserInfo(encryptCertAlias)
    encrypt.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE)
    encrypt.setSymmetricEncAlgorithm(WSConstants.AES_256)
    encrypt.build(document, crypto, secHeader)

    // Convert back to string
    document.toXmlString
  }

  private def createCrypto(keystorePath: String, keystorePassword: String): Merlin = {
    val properties = new Properties()
    properties.setProperty("org.apache.wss4j.crypto.provider",
      "org.apache.ws.security.components.crypto.Merlin")
    properties.setProperty("org.apache.wss4j.crypto.merlin.keystore.file", keystorePath)
    properties.setProperty("org.apache.wss4j.crypto.merlin.keystore.type", "JKS")
    properties.setProperty("org.apache.wss4j.crypto.merlin.keystore.password", keystorePassword)

    val crypto = CryptoFactory.getInstance(properties).asInstanceOf[Merlin]

    // Load keystore
    Using(new FileInputStream(keystorePath)) { fis =>
      val keystore = KeyStore.getInstance("JKS")
      keystore.load(fis, keystorePassword.toCharArray)
      crypto.setKeyStore(keystore)
    }.get

    crypto
  }
}