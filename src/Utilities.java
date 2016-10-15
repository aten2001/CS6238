import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
/**
 * Created by luoyinfeng on 10/15/16.
 */
//Provides common crypto or math utilities
public class Utilities {
	private SecureRandom random;
	private MessageDigest md;
	
	public Utilities(){
		try{
			random = SecureRandom.getInstance("SHA1PRNG");
			md = MessageDigest.getInstance("SHA-1");
		} catch(NoSuchAlgorithmException e){
			System.err.println("No such algorithm " + e );
		}
	}
	
	//evalute poly at point x
	public BigInteger evaluatePoly(BigInteger[] poly, BigInteger q, BigInteger x){
		BigInteger runningTotal = new BigInteger("0");
		for(int i = 0; i < poly.length; i++){
			runningTotal = runningTotal.add(x.modPow(new BigInteger(new Integer(i).toString()), q).multiply(poly[i]));
			runningTotal = runningTotal.mod(q);
		}
		return runningTotal;
	}
	
	//generate a poly of degree m-1 with constant term = hpwd
	public BigInteger[] generatePoly(int m, BigInteger hpwd, BigInteger q){
		BigInteger[] coeffs = new BigInteger[m];
		
		coeffs[0] = hpwd; //the constant term is hpwd
		
		for(int i = 1; i < m; i++){
			coeffs[i] = getRandomH(q); //getRandomH method doubles up as get random element \in \Z_q
		}
		return coeffs;
	}
	
	public BigInteger getRandomQ(){
	    BigInteger candidateQ;
		do{
	    	byte bytes[] = new byte[20];
	    	random.nextBytes(bytes);
	    
	    	candidateQ = new BigInteger(bytes);
	    }
		while(candidateQ.compareTo(BigInteger.ZERO) != 1  || isPrime(candidateQ) == false);
		
		return candidateQ;
	}
	
	public BigInteger getRandomH(BigInteger q){
		BigInteger candidateH;
		
		//find a random H that is less than Q
		do{
			byte bytes[] = new byte[20];
			random.nextBytes(bytes);
    
			candidateH = new BigInteger(bytes);
		} 
		while(candidateH.compareTo(q) != -1 || candidateH.compareTo(BigInteger.ZERO) != 1);
		
		return candidateH;
	}
	
	private boolean isPrime(BigInteger num){
		//Perform Miller Rabin's test
		return num.isProbablePrime(10); //99.90234375% confidence
	}
	
	//implementation of P_r() "PRP" with SHA-1 (likely not a PRP?)
	public BigInteger P(BigInteger r, int input, BigInteger q){
		byte[] rB = r.toByteArray();
		byte[] inputB = new byte[1];
		inputB[0] = new Integer(input).byteValue();
		
		int totalInputLen = rB.length + inputB.length;
		byte[] totalInput = new byte[totalInputLen];
		System.arraycopy(inputB, 0, totalInput, 0,             inputB.length);
		System.arraycopy(rB,     0, totalInput, inputB.length, rB.length);
		
		byte[] digest = md.digest(totalInput);
		
		return new BigInteger(digest).mod(q);
	}
	
	//implementation of G_pwd() "PRF" to use to calculate alpha and beta
	public BigInteger G(char[] pwd, BigInteger r, int input, BigInteger q){
		//try just a concatenation of key at the back (prevent length extension?)
		byte[] pwdB = charToByteArray(pwd);
		byte[] rB = r.toByteArray();
		byte[] inputB = new byte[1];
		inputB[0] = new Integer(input).byteValue();
		
		//Concatenate all inputs into 1 long byte array
		int totalInputLen = pwdB.length + rB.length + inputB.length;
		byte[] totalInput = new byte[totalInputLen];
		System.arraycopy(inputB, 0, totalInput, 0,                       inputB.length);
		System.arraycopy(rB,     0, totalInput, inputB.length,           rB.length);
		System.arraycopy(pwdB,   0, totalInput, inputB.length+rB.length, pwdB.length);
		
		
		byte[] digest = md.digest(totalInput);
		return new BigInteger(digest).mod(q);
	}
	public byte[] charToByteArray(char[] pwd){
		byte[] bytes = new byte[pwd.length*2];
		for(int i=0;i<pwd.length;i++) {
		   bytes[i*2] = (byte) (pwd[i] >> 8);
		   bytes[i*2+1] = (byte) pwd[i];
		}
		return bytes;
	}
	
	private void secretShare(){
		
	}
	
	private void OAEP_padding(){
		
	}
	
	private void ecrypt(){
		
	}
	
	private void getPolynomial(){
		
	}
}
