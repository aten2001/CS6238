
import java.math.BigInteger;
import java.security.MessageDigest;
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
	private Utilities util;
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
	public Init(int[] features,int m){
		util = new Utilities();
		this.m = m;
		h = Main.h;
		userName = "Luoyin";
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
	public String init_verify(){
		inst.readInstrTable(); // read the alpha and beta values

		user = new User(inst, this, historyFile);
		return user.doLogin();
		
	}
	
	public void clearPassword(){
		java.util.Arrays.fill(pwd, ' ');
		hpwd = null; // not really overwriting since BigIntegers are immutable
	}
	
	//this is when initializing from an existing file
	public void doInit(){
		
	}
	public void askUserForPassword(){
		//Use System.in or scanner class to get user's password and set to this.pwd
	}
	
	protected void choosePolynomial(){
		polynomial = util.generatePoly(m, hpwd, q);
		
	}
	private void chooseQ(){
		//q is the prime 
		q = util.getRandomQ();
	}
	
	private void chooseHPwd(){
		//hpwd is the hardened password
		hpwd = util.getRandomH(q);
	}
	
	//This is run when a new user is created
	//Or else the instruction table is usually
	//read from a file instead. 
	private void generateInstructionTable(){
		//Choose the system parameters for the 1st time
		chooseQ();
		//System.out.println("q is just chosen. it is " + q);
		chooseHPwd();
		//System.out.println("immediately hpwd is " + hpwd + " and compare is " + hpwd.compareTo(BigInteger.ZERO) );
		//System.out.println("polynomial is just chosen. q is " + q);
		choosePolynomial();
		
		//Calculate the alpha and beta values
		inst.buildInstrTable(); //calculate the alpha and beta values
		//System.out.println("instruction table is just built. q is " + q);
		inst.writeInstrTable(); //write it to disk for future logins to use
	}
	
	//This is called when an existing user logs off
	//The polynomial is changed to a new random one.
	public void randomizeInstructionTable(){
		this.q = inst.q;
		choosePolynomial();
		//Calculate the alpha and beta values
		inst.buildInstrTable(); //calculate the alpha and beta values
		System.out.println("Instruction table is rebuilt. ");
	}
	
	
	//This is run to create a new historyFile file
	//for a new user. Or else the historyFile is usually
	//read from a file instead.
	private void generateHistoryFile(){
		historyFile.update(); //create new historyFile file and encrypt and write it to disk.
	}


	//Getters
	public BigInteger get_q(){return q;}
	public BigInteger get_Hpwd(){
		return hpwd;
	}
	public int get_h(){
		return h;
	}
	public Utilities getUtil() {
		return util;
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
