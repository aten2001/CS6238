
import java.math.BigInteger;
import java.security.*;


/**
 * Created by luoyinfeng on 10/11/16.
 */

public class Init {
    private SecureRandom random;
    private MessageDigest md;
	private String user_Name;

	private String pwd;
	protected BigInteger hpwd; //hardened password
	
	private BigInteger q; // the 160 bit prime
	private Polynomial polynomial_f;

	private int m;  // how many features
	private int h;  // how many records in historyFile file
	
	public History_file historyFile;
    int[] feartures; // raw feature values
	public Ins_table inst;
	private User user;


	public Init(int[] features,int m,String userName){
        polynomial_f=new Polynomial();
        try{
            random = SecureRandom.getInstance("SHA1PRNG");
            md = MessageDigest.getInstance("SHA-1");
        } catch(NoSuchAlgorithmException e){
            System.err.println("No such algorithm " + e );
        }
		this.m = m;
		this.h = Main.h;
		this.user_Name = userName;

		pwd = "CorrectPassword";

		this.feartures = features;
		this.inst = new Ins_table(this);
		this.historyFile = new History_file(this);
	}

	public void initialization(int i){
		if(i==0) {
			generateInstructionTable();
			generateHistoryFile();
		}
		else
		{
            this.inst.read_ins(); // read the alpha and beta values
            this.user = new User(  this);
            this.user.Login();
		}
		
	} 
	public String user_verify(){
		this.inst.read_ins(); // read the alpha and beta values

		this.user = new User(this);
		return this.user.Login();
		
	}
	
	//generateInstructionTable
	private void generateInstructionTable(){
        q = get_random_q();
        hpwd = get_random_h(q);
        System.out.println("*************************************************************************************************************");
        System.out.println("this time hpwd is :"+hpwd);
        System.out.println("*************************************************************************************************************");
        polynomial_f.coeffs = generatePoly(m, hpwd, q);

		//buildInstrTable
		inst.newInstrTable();
        //encrypted and write it to disk
		inst.write_ins();
	}

	//generateHistoryFile
	private void generateHistoryFile(){
		historyFile.update_hisfile(); //create new historyFile file and encrypt and write it to disk.
	}
    //evalute poly at point x
    public BigInteger slove_Poly(BigInteger[] poly, BigInteger q, BigInteger x){
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
            coeffs[i] = get_random_h(q); //getRandomH method doubles up as get random element \in \Z_q
        }
        return coeffs;
    }

    public BigInteger get_random_q(){
        BigInteger input_q;
        do{
            byte bytes[] = new byte[20];
            random.nextBytes(bytes);

            input_q = new BigInteger(bytes);
        }
        while(input_q.compareTo(BigInteger.ZERO) != 1  || isPrime(input_q) == false);

        return input_q;
    }

    public BigInteger get_random_h(BigInteger q){
        BigInteger candidateH;

        //find a random h that is less than q
        do{
            byte bytes[] = new byte[20];
            random.nextBytes(bytes);

            candidateH = new BigInteger(bytes);
        }
        while(candidateH.compareTo(q) != -1 || candidateH.compareTo(BigInteger.ZERO) != 1);

        return candidateH;
    }

    private boolean isPrime(BigInteger num){
        return num.isProbablePrime(10);
    }

    public BigInteger P_r(BigInteger r, int input, BigInteger q){
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


    public BigInteger G_pwd(char[] pwd, BigInteger r, int input, BigInteger q){
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
		return polynomial_f.coeffs;
	}
	public char[] get_Possword() {
		return pwd.toCharArray();
	}
	public String getUserName() {
		return user_Name;
	}
	public int[] get_features() {return feartures;}
}
