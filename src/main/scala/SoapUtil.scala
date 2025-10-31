import org.apache.ws.security.components.crypto.{CryptoFactory, Merlin}
import org.w3c.dom.Document

import java.io.{ByteArrayInputStream, StringWriter}
import java.security.KeyStore
import java.util.Properties
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.{OutputKeys, TransformerFactory}
import scala.util.Using

object SoapUtil {

  implicit class RichString(str: String) {
    def toDocument: Document = {
      val factory = DocumentBuilderFactory.newInstance()
      factory.setNamespaceAware(true)
      val builder = factory.newDocumentBuilder()
      val in = new ByteArrayInputStream(str.getBytes("UTF-8"))
      builder.parse(in)
    }
  }

  implicit class RichDocument(document: Document) {
    def toXmlString: String = {
      val transformer = TransformerFactory.newInstance().newTransformer()
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
      val stringWriter = new StringWriter()
      transformer.transform(new DOMSource(document), new StreamResult(stringWriter))
      stringWriter.toString
    }
  }

  def createCrypto(keystorePath: String, keystorePassword: String): Merlin = {
    val properties = new Properties()
    properties.setProperty("org.apache.wss4j.crypto.provider",
      "org.apache.ws.security.components.crypto.Merlin")
    properties.setProperty("org.apache.wss4j.crypto.merlin.keystore.type", "JKS")
    properties.setProperty("org.apache.wss4j.crypto.merlin.keystore.password", keystorePassword)

    val crypto = CryptoFactory.getInstance(properties).asInstanceOf[Merlin]

    // Load keystore from classpath
    val keystoreStream = Option(getClass.getClassLoader.getResourceAsStream(keystorePath)).getOrElse {
      throw new IllegalArgumentException(s"Keystore not found in resources: $keystorePath")
    }

    Using(keystoreStream) { stream =>
      val keystore = KeyStore.getInstance("JKS")
      keystore.load(stream, keystorePassword.toCharArray)
      crypto.setKeyStore(keystore)
    }.get

    crypto
  }
}
