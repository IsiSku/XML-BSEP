package controllers;

import java.security.PrivateKey;
import java.security.cert.Certificate;

import javax.crypto.SecretKey;

import org.w3c.dom.Document;

import play.mvc.results.Ok;
import play.mvc.results.Result;
import utils.KeystoreUtils;
import utils.XMLUtils;
import utils.xmlEncryption.DecryptXML;
import utils.xmlEncryption.EncryptXML;

/**
 * Created by Nemanja on 9/6/2016.
 */
public class TransferXML extends AppController {

	public static Result transfer(){
		String IN_FILE = "./xml/akt.xml";  //TODO stavi da je ovo parametar metode
		EncryptXML encryptXMLutil = new EncryptXML();
		XMLUtils xmlUtil = new XMLUtils();
		DecryptXML decryptXMLutil = new DecryptXML();
		KeystoreUtils keystoreUtils = new KeystoreUtils();

		Document doc = xmlUtil.loadDocument(IN_FILE);

		SecretKey secretKey = encryptXMLutil.generateDataEncryptionKey();

		Certificate cert = keystoreUtils.readCertificate("./app/keystores/odbornik1.jks", "odbornik1", "odbornik1");    //TODO sertifikat korisnika

		doc = encryptXMLutil.encrypt(doc, secretKey, cert, "Propis");   //TODO Propis

		xmlUtil.saveDocument(doc, "./xmlDestination/aktEnc.xml");   //TODO nepotreban korak, samo radi demonstracije


		//Dekripcija-----------------------------------------------------------

		//doc = xmlUtil.loadDocument(IN_FILE);
		PrivateKey pk = keystoreUtils.readPrivateKey("./app/keystores/odbornik1.jks", "odbornik1", "odbornik1");
		doc = decryptXMLutil.decrypt(doc, pk);

		xmlUtil.saveDocument(doc, "./xmlDestination/aktDec.xml");

		return new Ok();
	}
}
