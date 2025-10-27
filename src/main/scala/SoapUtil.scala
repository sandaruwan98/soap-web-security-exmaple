import org.w3c.dom.Document

import java.io.{ByteArrayInputStream, StringWriter}
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.{OutputKeys, TransformerFactory}

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
}
