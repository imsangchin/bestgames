package com.Irdeto;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.RSAPrivateKeySpec;

import javax.crypto.Cipher;
  
public class Rsa {   
	
	private PrivateKey private_Key;
	private String modulus = "10103166745709600780215616551837697832816413714471062522342538060943596036859967333870827790358555455232243383580565187280643159050869924436081447583051139";   
     //int length = modulus.length();
     // String publicExponent = "65537";   

    private String privateExponet = "367979294475011322800474185715497882523349856362702385535371444397399388741997039894583483410120364529325888461124714276674612930833020362278754665756193"; 
	
	public Rsa()
	{
		  
        try
        {
        	private_Key = getPrivateKey(modulus,privateExponet);
        }
        catch(Exception e)
        {
        	
        }
	}
	
      private  PrivateKey getPrivateKey(String modulus,String privateExponent) throws Exception {   
  
            BigInteger m = new BigInteger(modulus);   
  
            BigInteger e = new BigInteger(privateExponent);   
  
            RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(m,e);   
  
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");   
  
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);   
  
            return privateKey;   
  
      }   
  
    
  
      public  byte[] decrypt(byte[] content)
      {    
    	  try
    	  {
            Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");   
 
            private_Key = getPrivateKey(modulus,privateExponet);
	        cipher.init(Cipher.DECRYPT_MODE, private_Key);   
  
	        byte[]deBytes = cipher.doFinal(content);   
	        return deBytes;
  
           }
           catch(Exception e)
           {
        	   e.printStackTrace();
        	   return null;
           }

      }   
  
}  
