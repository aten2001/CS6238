import javax.crypto.*;
import java.io.*;
import javax.crypto.spec.*;
import java.math.BigInteger;
import java.security.*;
/**
 * Created by luoyinfeng on 10/12/16.
 */
public class History_file {

	final static String FILE_NAME = "historyFile.txt";
	private Init init;
	private byte[] encrypted_Writing = null;
	private byte[] encrypted_Reading = null;
	private byte[] decryptedText = null;
	private String history;
	private String getHistory;
	private byte[] init_varable_random;

	public History_file(Init init){
		this.init = init;
        history = "";
	}
    //method to encrypt the historyFile file
    //*****************refernce from Passage***************
    private void encrypt(){
        System.out.println("going to encrypt: " + history + " end of file.");

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
        init_varable_random = new byte[cipher.getBlockSize()];

        new SecureRandom().nextBytes(init_varable_random);

        IvParameterSpec ivSpec = new IvParameterSpec(init_varable_random);

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
            System.out.println("*************************************************************************************************************");
            System.out.println("Historyfile content before encrypyion:"+history);
            encrypted_Writing = cipher.doFinal(history.getBytes("UTF-8"));
            System.out.println("Historyfile content after encrypyion:"+encrypted_Writing);
            System.out.println("*************************************************************************************************************");

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
	//decryption method for historyFile file
    //*****************refernce from Passage***************
	public String decrypt(BigInteger candidateHpwd){
		try {
            deserialize(init.getUserName() + "_" + FILE_NAME);    //deserializing the historyFile file
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
		IvParameterSpec ivSpec = new IvParameterSpec(init_varable_random);
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		digest.update(candidateHpwd.toByteArray());
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
			decryptedText = cipher.doFinal(encrypted_Reading);
		} catch (IllegalBlockSizeException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (BadPaddingException e1) {
			// TODO Auto-generated catch block
			System.out.println("Decryption error. Aborting decryption.");
			return "0";
			//e1.printStackTrace();
		}try {
            getHistory = new String(decryptedText, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        history = getHistory;
        System.out.println("*************************************************************************************************************");
        System.out.println("Historyfile content after decrypyion:"+history);
        System.out.println("*************************************************************************************************************");

        return "1";
	}
	//verifies the historyFile file
	public boolean check_intergity(){
		try{
			if(getHistory.substring(0,7).equals("Feature"))
				return true;
			else
				return false;
		}
		catch(Exception e){

		}
		return false;
	}
	//updates  feature values from successful login in
	public void update_hisfile(){
		try {
            update_History(); //update the historyFile string
			encrypt(); //encrypts the string

            serialize(init.getUserName() + "_" + FILE_NAME); // writes it out to hard disk
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}
	private void update_History(){
		try{
			//Remove the 1st historyFile entry if the historyFile file is already full (has h=10 entries)
			if(full()){
				int m = init.get_m();
				int curr = 0;
				//remove m lines
				for(int i = 0; i < m; i++){
					curr = history.indexOf('\n', curr) + 1;
				}
                history = history.substring(curr);
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

            history = history.concat(strBuilder.toString());
		}
		catch(Exception e){
		}
	}

    //checks if there are already h records in this.historyFile
	public boolean full(){
		int numLines = 0;
		int pos = 0;
		while( (pos = history.indexOf('\n', pos) ) != -1){ numLines++; pos++;}
		return numLines == init.get_h()*init.get_m();
	}
	

	//this method serializes the historyFile file
	private void serialize(String fileName)throws IOException{
		File file = new File(fileName);
		try{

			ObjectOutputStream obj = new ObjectOutputStream(new FileOutputStream(file));
			obj.writeObject(init_varable_random);
			obj.writeObject(encrypted_Writing);
			obj.close();
		}
		catch(FileNotFoundException fe){
			System.out.println("File not found ");
		}

	}
    //this method deserializes the historyFile file
    private void deserialize(String fileName)throws IOException{
        File file = new File(fileName);
        try{
            ObjectInputStream obj = new ObjectInputStream(new FileInputStream(file));
            init_varable_random = (byte[]) obj.readObject();
            encrypted_Reading = (byte[]) obj.readObject();
            obj.close();
        }
        catch(ClassNotFoundException e){
            System.out.println(e.getMessage());
        }
        catch(FileNotFoundException fe){
            System.out.println("File not found ");
        }

    }
	public String getHistoryFile() {
		// TODO Auto-generated method stub
		return history;
	}
}
