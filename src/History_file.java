import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
/**
 * Created by luoyinfeng on 10/15/16.
 */
public class History_file {

	final static String FILE_NAME = "historyFile.txt";
	private Init init;
	private byte[] encryptedTextWriting = null;
	private byte[] encryptedTextReading = null;
	private byte[] decryptedText = null;
	private String historyFile;
	private String getHistoryFile;
	private byte[] iv;

	public History_file(Init init){
		this.init = init;
		historyFile = "";
	}

	//decryption method for historyFile file
	public String decrypt(BigInteger candidateHpwd){

		try {
			deserializeObejct(init.getUserName() + "_" + FILE_NAME);    //deserializing the historyFile file
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		
		// setup AES cipher in CBC mode with PKCS #5 padding
		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// setup an IV (Init vector) that should be
		// randomly generated for each input that's encrypted
//		byte[] iv = new byte[cipher.getBlockSize()];

		
		//new SecureRandom().nextBytes(iv);
		IvParameterSpec ivSpec = new IvParameterSpec(iv);

		// hash keyString with SHA-256 and crop the output to 128-bit for key
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		digest.update(candidateHpwd.toByteArray());         //check if this is going to work (keystring.tobytes())
		byte[] key = new byte[16];
		System.arraycopy(digest.digest(), 0, key, 0, key.length);
		SecretKeySpec keySpec = new SecretKeySpec(key, "AES");

		// decrypt
		try {
			cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//	        byte[] decrypted = null;
		try {
			decryptedText = cipher.doFinal(encryptedTextReading);
		} catch (IllegalBlockSizeException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (BadPaddingException e1) {
			// TODO Auto-generated catch block
			System.out.println("Decryption error. Aborting decryption.");
			return "0";
			//e1.printStackTrace();
		}try {
			getHistoryFile = new String(decryptedText, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		historyFile = getHistoryFile;
        return "1";
	}

	//this method deserializes the historyFile file
	private void deserializeObejct(String fileName)throws IOException{
		File file = new File(fileName);
		try{
			ObjectInputStream obj = new ObjectInputStream(new FileInputStream(file));
			iv = (byte[]) obj.readObject();
			encryptedTextReading = (byte[]) obj.readObject(); 
			obj.close();
		}
		catch(ClassNotFoundException e){
			System.out.println(e.getMessage());
		}
		catch(FileNotFoundException fe){
			System.out.println("File not found ");
		}

	}

	//verifies the historyFile file
	public boolean checkDecryption(){
		try{
			if(getHistoryFile.substring(0,7).equals("Feature"))
				return true;
			else
				return false;
		}
		catch(Exception e){

		}
		return false;
	}

	//updates the historyFile file with the feature values
	//obtained from this log in.
	public void update(){
		try {
			updateHistoryFile(); //update the historyFile string
			encrypt(); //encrypts the string
			
			serializeObejct(init.getUserName() + "_" + FILE_NAME); // writes it out to hard disk
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}


	//builds the historyFile file for a particular user
	//appends the feature values from this log in to the old historyFile file
	private void updateHistoryFile(){
		try{

			//Remove the 1st historyFile entry if the historyFile file is already full (has h=10 entries)
			if(isFull()){
				int m = init.get_m();
				int curr = 0;
				//remove m lines
				for(int i = 0; i < m; i++){
					curr = historyFile.indexOf('\n', curr) + 1;
				}
				historyFile = historyFile.substring(curr);
			}

			//Build the latest entry and append it to the end of historyFile file
			StringBuilder strBuilder = new StringBuilder();
			int count = init.get_m();
			for(int i=0; i<count; i++){
				strBuilder.append("Feature");
				strBuilder.append(i);
				strBuilder.append(":");
				strBuilder.append(init.get_features()[i]);
				strBuilder.append("\n");
			}

			historyFile = historyFile.concat(strBuilder.toString());
		}
		catch(Exception e){
		}
		//System.out.println("historyFIle is updated to : " + historyFile + " end of file.");
	}
	
	
	public boolean isFull(){
		//checks if there are already h records in this.historyFile
		int numLines = 0;
		int pos = 0;
		while( (pos = historyFile.indexOf('\n', pos) ) != -1){ numLines++; pos++;}
		return numLines == init.get_h()*init.get_m();
	}
	
	
	//method to encrypt the historyFile file
	private void encrypt(){
		System.out.println("going to encrypt: " + historyFile + " end of file.");

		BigInteger keyString = init.get_Hpwd();

		// setup AES cipher in CBC mode with PKCS #5 padding
		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// setup an IV (Init vector) that should be
		// randomly generated for each input that's encrypted
		iv = new byte[cipher.getBlockSize()];
		
		new SecureRandom().nextBytes(iv);
		
		IvParameterSpec ivSpec = new IvParameterSpec(iv);

		// hash keyString with SHA-256 and crop the output to 128-bit for key
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("digest"+digest);
		System.out.println("keyString"+keyString);
		digest.update(keyString.toByteArray());
		byte[] key = new byte[16];
		System.arraycopy(digest.digest(), 0, key, 0, key.length);
		SecretKeySpec keySpec = new SecretKeySpec(key, "AES");

		// encrypt
		try {
			cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//		byte[] encrypted = null;
		try {
			encryptedTextWriting = cipher.doFinal(historyFile.getBytes("UTF-8"));
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//this method serializes the historyFile file
	private void serializeObejct(String fileName)throws IOException{
		File file = new File(fileName);
		try{

			ObjectOutputStream obj = new ObjectOutputStream(new FileOutputStream(file));
			obj.writeObject(iv);
			obj.writeObject(encryptedTextWriting); 
			obj.close();
		}
		catch(FileNotFoundException fe){

			System.out.println("File not found ");

		}

	}

	public String getHistoryFile() {
		// TODO Auto-generated method stub
		return historyFile;
	}
}
