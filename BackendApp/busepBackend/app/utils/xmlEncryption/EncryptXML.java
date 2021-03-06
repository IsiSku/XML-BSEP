package utils.xmlEncryption;

import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.Certificate;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.apache.xml.security.encryption.EncryptedData;
import org.apache.xml.security.encryption.EncryptedKey;
import org.apache.xml.security.encryption.XMLCipher;
import org.apache.xml.security.encryption.XMLEncryptionException;
import org.apache.xml.security.keys.KeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Created by Nemanja on 7/6/2016.
 */
public class EncryptXML {
	static {
		//staticka inicijalizacija
		Security.addProvider(new BouncyCastleProvider());
		org.apache.xml.security.Init.init();
	}

	public SecretKey generateDataEncryptionKey() {

		try {
			KeyGenerator keyGenerator = KeyGenerator.getInstance("AES"); //Koristimo AES jer je brz i siguran
			return keyGenerator.generateKey();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 *
	 * @param doc XML dokument koji se kriptuje
	 * @param key Tajni ključ koji se koristi za AES simetričnu enkripciju
	 * @param certificate Sertifikat onoga za koga se kriptuje XML. Odavde se izvlači public key
	 * @param encryptTag Tag od kojeg će se enkriptovati XML. Svi njegovi potomci se enkriptuju takođe.
	 * @return Enkriptovan XML dokument
	 */
	public Document encrypt(Document doc, SecretKey key, Certificate certificate, String encryptTag) {

		try {
			//cipher za kriptovanje tajnog kljuca,
			//Koristi se Javni RSA kljuc za kriptovanje
			XMLCipher keyCipher = XMLCipher.getInstance(XMLCipher.RSA_v1dot5);
			//inicijalizacija za kriptovanje tajnog kljuca javnim RSA kljucem
			keyCipher.init(XMLCipher.WRAP_MODE, certificate.getPublicKey());
			EncryptedKey encryptedKey = keyCipher.encryptKey(doc, key);

			//cipher za kriptovanje XML-a
			XMLCipher xmlCipher = XMLCipher.getInstance(XMLCipher.AES_128); //Ovaj smo koristili i kada smo generisali tajni ključ
			//inicijalizacija za kriptovanje
			xmlCipher.init(XMLCipher.ENCRYPT_MODE, key);

			//u EncryptedData elementa koji se kriptuje kao KeyInfo stavljamo kriptovan tajni kljuc
			EncryptedData encryptedData = xmlCipher.getEncryptedData();
			//kreira se KeyInfo
			KeyInfo keyInfo = new KeyInfo(doc);
			keyInfo.addKeyName("Kriptovani tajni kljuc");
			//postavljamo kriptovani kljuc
			keyInfo.add(encryptedKey);
			//postavljamo KeyInfo za element koji se kriptuje
			encryptedData.setKeyInfo(keyInfo);

			//trazi se element ciji sadrzaj se kriptuje
			NodeList targetTags = doc.getElementsByTagName(encryptTag);

			Element targetTag = (Element) targetTags.item(0);

			xmlCipher.doFinal(doc, targetTag, true); //kriptuje sa sadrzaj

			return doc;

		} catch (XMLEncryptionException e) {
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
