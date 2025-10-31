
import SoapUtil.*
import org.apache.ws.security.WSConstants
import org.apache.ws.security.message.{WSSecEncrypt, WSSecHeader, WSSecSignature}

import scala.util.Try

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
}