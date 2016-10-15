
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Scanner;

/**
 * Created by luoyinfeng on 10/15/16.
 */
//This is a (big) entity class 
//Main holds a (singleton) Init object
//The Init object stores system wide parameters
//like q, m, the polynomial, the hpwd, etc
//Main passes the init object around to whoever needs the information
public class Init {
    private SecureRandom random;
    private MessageDigest md;
	private String userName;
	int[] feartures; // raw feature values (answers to questions)
	private char[] pwd; //normal, unhardened password
	protected BigInteger hpwd; //hardened password
	
	private BigInteger q; // the 160 bit prime number that's modulus group
	private BigInteger[] polynomial;

	private final int m;  // how many questions/feature answers
	private final int h;  // how mBigIntegerany records to keep in historyFile file
	
	private History_file historyFile;
	private Ins_table inst;
	private User user;

	//This constructor initializes variables common to both 
	//the NewUser and ExistingUser use cases.
	public Init(int[] features,int m,String userName){

        try{
            random = SecureRandom.getInstance("SHA1PRNG");
            md = MessageDigest.getInstance("SHA-1");
        } catch(NoSuchAlgorithmException e){
            System.err.println("No such algorithm " + e );
        }
		this.m = m;
		h = Main.h;
		this.userName = userName;
        String temp="CorrectPassword";
		pwd = temp.toCharArray();

		this.feartures = features;
		inst = new Ins_table(this);
		historyFile = new History_file(this);
	}

	public void initialization(int i){
		//Generate files for this new user...
		if(i==0) {
			generateInstructionTable();
			System.out.println("hpwd is " + hpwd);
			System.out.println("q is " + q);
			generateHistoryFile();
		}
		else
		{
            inst.readInstrTable(); // read the alpha and beta values
            user = new User(inst,  this, historyFile);
            user.doLogin();
		}
		
	} 
	public String user_verify(){
		inst.readInstrTable(); // read the alpha and beta values

		user = new User(inst, this, historyFile);
		return user.doLogin();
		
	}
	
	//This is run when a new user is created
	//Or else the instruction table is usually
	//read from a file instead. 
	private void generateInstructionTable(){

        q = getRandomQ();
		//System.out.println("q is just chosen. it is " + q);
        hpwd = getRandomH(q);
		//System.out.println("q is just chosen. it is " + q );
        polynomial = generatePoly(m, hpwd, q);
        //System.out.println("polynomial is just chosen. q is " + q);


		//buildInstrTable
		inst.buildInstrTable();
        //encrypted and write it to disk
		inst.writeInstrTable();
	}

	//This is run to create a new historyFile file
	//for a new user. Or else the historyFile is usually
	//read from a file instead.
	private void generateHistoryFile(){
		historyFile.update(); //create new historyFile file and encrypt and write it to disk.
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

	//Getters
	public BigInteger get_q(){return q;}
	public BigInteger get_Hpwd(){
		return hpwd;
	}
	public int get_h(){
		return h;
	}

	public int get_m() {
		return m;
	}
	public BigInteger[] getPolynomial() {
		return polynomial;
	}
	public char[] getPwd() {
		return pwd;
	}
	public String getUserName() {
		return userName;
	}
	public int[] get_features() {return feartures;}
}
