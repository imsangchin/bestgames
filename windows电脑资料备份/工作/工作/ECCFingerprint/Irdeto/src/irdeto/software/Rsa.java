package irdeto.software;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.Cipher;

public class Rsa {

	private String modulus = "10103166745709600780215616551837697832816413714471062522342538060943596036859967333870827790358555455232243383580565187280643159050869924436081447583051139";
	private String publicExponent = "65537";

	private PublicKey getPublicKey(String modulus, String publicExponent)
			throws Exception {

		BigInteger m = new BigInteger(modulus);

		BigInteger e = new BigInteger(publicExponent);

		RSAPublicKeySpec keySpec = new RSAPublicKeySpec(m, e);

		KeyFactory keyFactory = KeyFactory.getInstance("RSA");

		PublicKey publicKey = keyFactory.generatePublic(keySpec);

		return publicKey;

	}

	// leave a interface to dynamic set public key
	public void SetKey(String key) {
		modulus = key;
	}

	// use rsa to encrypt the information
	public byte[] encrypt(byte[] content) {

		try {
			PublicKey publicKey = getPublicKey(modulus, publicExponent);
			Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			byte[] enBytes = cipher.doFinal(content);
			return enBytes;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
