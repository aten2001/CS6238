import java.io.*;
import java.math.BigInteger;
/**
 * Created by luoyinfeng on 10/12/16.
 */
public class Ins_table {
	int m;
	int h;
	BigInteger r;
	Polynomial polynomial_f;
	BigInteger q; // the modulo group
	char[] pwd;
	BigInteger hpwd; // hardened password
	int[] threshold;
	double[] mean;
	double[] Standard_Deviation;
	BigInteger[] alpha;
	BigInteger[] beta;
	Init init;
	private String FILE_NAME = "_instruction_table.txt";


	public Ins_table(Init init) {
		this.init = init;
		this.m = init.get_m();
		this.h = init.get_h();
		this.polynomial_f=new Polynomial();
		this.pwd = init.get_Possword();

		alpha = new BigInteger[m];
		beta = new BigInteger[m];
		threshold = new int[m];
		for(int i = 0; i < m; i++){
			threshold[i] = Main.t_i;
		}
		
	}


	public void newInstrTable(){
		q = init.get_q();
		r = init.get_random_h(q);
		hpwd = init.get_Hpwd();
		polynomial_f.coeffs = init.getPolynomial();
		for (int i = 0; i < m; i++) {
			alpha[i] = calculateAlphaBeta(r, i*2, pwd);
			beta[i] = calculateAlphaBeta(r, i*2+1, pwd);
		}
	}
	
	//Not all alpha and beta values can be good. Some of them has to be bad to reflect the user's patterns
	//*****************refernce from Passage***************
	public void disturbValues(){
		for(int i = 0; i < mean.length; i++){
			//if feature is distinguishing
			if( Math.abs(mean[i] - threshold[i]) > Main.k_i * Standard_Deviation[i]){
					if(mean[i] < threshold[i]){ 
						System.out.println("disturbing beta " + i); 
						beta[i] = init.get_random_h(q);
					}
					else{
						System.out.println("disturbing alpha " + i);
						alpha[i] = init.get_random_h(q);
					}
			}			
		}		
	}

	//This method calculates the threshold values for each feature vector
	public void calculateMean(String historyFile){
		mean = new double[m];
		InputStream is = new ByteArrayInputStream(historyFile.getBytes());
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		try {
			while ((line = br.readLine()) != null) {
				int colon = line.indexOf(":");
				int index = Integer.parseInt(line.substring(7,colon));
				mean[index] = mean[index] + Double.parseDouble(line.substring(colon + 1));
			}
			for(int i=0 ; i<mean.length ; i++) {
				mean[i] = mean[i]/h;
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	//calculate Standard_Deviation
	public void calculateStd_Dev(String historyFile){
		Standard_Deviation = new double[m];
		InputStream is = new ByteArrayInputStream(historyFile.getBytes());
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		try {
			while ((line = br.readLine()) != null) {
				int colon = line.indexOf(":");
				int index = Integer.parseInt(line.substring(7,colon));
				Standard_Deviation[index] = Standard_Deviation[index] + Math.pow((Double.parseDouble(line.substring(colon + 1)) - mean[index]) , 2);
			}
			for(int i=0 ; i<mean.length ; i++) {Standard_Deviation[i] = Math.sqrt(Standard_Deviation[i]/h);}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	// read q to file,write r to file and alpha and beta
	public void read_ins(){
		File file = new File(init.getUserName() + FILE_NAME);
		try {
			ObjectInputStream obj;
			obj = new ObjectInputStream(new FileInputStream(file));
			q = (BigInteger) obj.readObject();
			r = (BigInteger) obj.readObject();
			int count = init.get_m();
			for (int i = 0; i < count; i++) {
				alpha[i] = (BigInteger) obj.readObject();
				beta[i] = (BigInteger) obj.readObject();
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
	// write q to file,write r to file and alpha and beta
	public void write_ins() {
		File file = new File(init.getUserName() + FILE_NAME);
		try {
			ObjectOutputStream writer;
			writer = new ObjectOutputStream(new FileOutputStream(file));
			writer.writeObject(q);
			writer.writeObject(r);
			int counter = init.get_m();
			System.out.println("*************************************************************************************************************");
			for (int i = 0; i < counter; i++) {
				writer.writeObject(alpha[i]);
				writer.writeObject(beta[i]);
				//**********************************
				//we can check instruction table here
				//**********************************
				System.out.println("alpha["+i+"]"+alpha[i]+"     "+"beta["+i+"]"+beta[i]);
			}
			System.out.println("*************************************************************************************************************");
			writer.close();
		} catch (FileNotFoundException fe) {
			System.out.println("File not found ");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	//calculate the Alpha and Beta value which means
	private BigInteger calculateAlphaBeta(BigInteger r, int input, char[] pwd) {
		BigInteger randomizedX = init.P_r(r, input, q);
		BigInteger y = init.slove_Poly(polynomial_f.coeffs, q, randomizedX); // this is
		BigInteger g = init.G_pwd(pwd, r, input, q);
		return y.add(g).mod(q);
	}



	//Getter
	public BigInteger getR() {
		return r;
	}
	public BigInteger getAlpha(int i) {return alpha[i];}
	public BigInteger getBeta(int i) {return beta[i];}
}
