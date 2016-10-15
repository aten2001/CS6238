import java.io.*;
import java.math.BigInteger;
/**
 * Created by luoyinfeng on 10/15/16.
 */
public class Ins_table {

	BigInteger[] polynomial;
	BigInteger q; // the modulo group
	BigInteger[] alpha;
	BigInteger[] beta;
	char[] pwd;
	BigInteger hpwd; // hardened password
	int m;
	int h;
	BigInteger r;
	int[] threshold;
	double[] mean;
	double[] std_dev;

	Init init;
	private String FILE_NAME = "_inst.txt";

	public BigInteger getR() {
		return r;
	}

	public Ins_table(Init init) {
		this.init = init;
		this.m = init.get_m();
		this.h = init.get_h();

		this.pwd = init.getPwd();

		alpha = new BigInteger[m];
		beta = new BigInteger[m];
		threshold = new int[m];
		for(int i = 0; i < m; i++){
			threshold[i] = Main.t_i;
		}
		
	}

	//this is run once for a new user or when existing user 
	//needs to change his polynomial
	public void buildInstrTable(){

		q = init.get_q();
		//System.out.println("In Instruction table: the q i got is: " + q);
		r = init.getRandomH(q);
		hpwd = init.get_Hpwd();
		polynomial = init.getPolynomial();
		//System.out.println("Calculating alpha beta now..");
		for (int i = 0; i < m; i++) {
			alpha[i] = calculateAlpha(r, i, pwd);
			beta[i] = calculateBeta(r, i, pwd);
		}
	}
	
	//Not all alpha and beta values can be good. Some 
	//of them has to be bad to reflect the user's patterns
	public void disturbValues(){
		for(int i = 0; i < mean.length; i++){
			
			//if feature is distinguishing
			if( Math.abs(mean[i] - threshold[i]) > Main.k_i * std_dev[i]){
					if(mean[i] < threshold[i]){ 
						System.out.println("disturbing beta " + i); 
						beta[i] = init.getRandomH(q);
					}
					else{
						System.out.println("disturbing alpha " + i);
						alpha[i] = init.getRandomH(q);
					}
			}			
		}		
	}

	//This method calculates the threshold values for each feature vector
	public void updateMean(String historyFile){
		mean = new double[m];

		// convert String into InputStream
		InputStream is = new ByteArrayInputStream(historyFile.getBytes());

		// read it with BufferedReader
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		String line;
		try {
			while ((line = br.readLine()) != null) {
				int colon = line.indexOf(":");
				int index = Integer.parseInt(line.substring(7,colon));

				mean[index] = mean[index] + Double.parseDouble(line.substring(colon + 1));

			}

			for(int i=0 ; i<mean.length ; i++)
			{
				mean[i] = mean[i]/h;
			//	System.out.println("feature values: " + mean[i]);
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	//Input is this.mean
	//Output is this.std_dev
	public void calculateStd_Dev(String historyFile){
		std_dev = new double[m];
		// convert String into InputStream
		InputStream is = new ByteArrayInputStream(historyFile.getBytes());

		// read it with BufferedReader
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		String line;
		try {
			while ((line = br.readLine()) != null) {
				int colon = line.indexOf(":");
				int index = Integer.parseInt(line.substring(7,colon));

				std_dev[index] = std_dev[index] + Math.pow((Double.parseDouble(line.substring(colon + 1)) - mean[index]) , 2);

			}

			for(int i=0 ; i<mean.length ; i++)
			{
				std_dev[i] = Math.sqrt(std_dev[i]/h);
	//			System.out.println("feature values: " + std_dev[i]);
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void readInstrTable(){
		//get userName from this.init.userName
		//open the file with filename of userName + "_inst.txt"
		//read q from file
		//read r from file
		//for i = 0 to m
		//read obj = alpha[i] from file
		//read obj = beta[i] from file
		//read obj/float = threshold[i] from file (not sure if you can write float directly)
		//close file

		File file = new File(init.getUserName() + FILE_NAME);
		try {
			ObjectInputStream obj;

			obj = new ObjectInputStream(new FileInputStream(file));
			q = (BigInteger) obj.readObject();
			//System.out.println("q is " + q);
			r = (BigInteger) obj.readObject();
			int count = init.get_m();
			for (int i = 0; i < count; i++) {
				alpha[i] = (BigInteger) obj.readObject();
				beta[i] = (BigInteger) obj.readObject();
				//threshold[i] = (Double) obj.readObject();
			}
			obj.close();
		} catch (ClassNotFoundException e) {
			System.out.println(e.getMessage());
		} catch (FileNotFoundException fe) {
			System.out.println("File not found ");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void writeInstrTable() {
		// get user name from this.init.userName
		// file name is userName + "_inst.txt"
		// write q to file
		// write r to file
		// for i = 0 to m
		// write obj = alpha[i] to file
		// write obj = beta[i] to file
		// write obj/float = threshold[i] to file
		// close file

		// The name of the file to open.

		File file = new File(init.getUserName() + FILE_NAME);
		try {

			ObjectOutputStream obj;

			obj = new ObjectOutputStream(new FileOutputStream(file));
			obj.writeObject(q);
			//System.out.println("q is " + q);
			obj.writeObject(r);

			int count = init.get_m();
			for (int i = 0; i < count; i++) {
				obj.writeObject(alpha[i]);
				obj.writeObject(beta[i]);
				//obj.writeObject(threshold[i]);
			}

			obj.close();
		} catch (FileNotFoundException fe) {

			System.out.println("File not found ");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public BigInteger getAlpha(int i) {
		return alpha[i];
	}

	public BigInteger getBeta(int i) {
		return beta[i];
	}

	// r is unique to each user, see page 74 of Monrose section 5.1 item 1
	private BigInteger calculateAlpha(BigInteger r, int i, char[] pwd) {
		return calculateAlphaBeta(r, i * 2, pwd);
	}

	// wrapper for calculateAlphaBeta()
	private BigInteger calculateBeta(BigInteger r, int i, char[] pwd) {
		return calculateAlphaBeta(r, i * 2 + 1, pwd);

	}

	private BigInteger calculateAlphaBeta(BigInteger r, int input, char[] pwd) {
		BigInteger randomizedX = init.P(r, input, q);
		//System.out.println("X[" + input + "] is " + randomizedX);
		BigInteger y = init.evaluatePoly(polynomial, q, randomizedX); // this is
		// y_{ai}^0/1
		// in
		// Monrose
		// section
		// 5.1
		// item
		// 2
		//System.out.println("y[" + input + "] is " + y);
		BigInteger g = init.G(pwd, r, input, q);
		return y.add(g).mod(q);
	}
}
