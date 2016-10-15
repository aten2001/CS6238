
import java.math.BigInteger;

/**
 * Created by luoyinfeng on 10/15/16.
 */
public class User {

	private BigInteger alpha;
	private BigInteger beta;
	private BigInteger[] xValues;
	private BigInteger[] yValues;
	Ins_table inst;
	Utilities util;
	Init init;
	History_file historyFile;
	private BigInteger candidateHpwd; 


	public User(Ins_table inst, Utilities util, Init init, History_file historyFile){
		this.inst = inst;
		this.util = util;
		this.init = init;
		this.historyFile = historyFile;
		this.xValues = new BigInteger[init.get_m()];
		this.yValues = new BigInteger[init.get_m()];
	}
	public void doinit() {
		this.calculateXY(init.feartures); 					 //pass feature value
		this.calculateHpwd();   							//calling the function to calculate Hpwd
		//System.out.println("hpwd is " + candidateHpwd);
		this.init.hpwd = candidateHpwd;
		String result=this.decryptHistoryFile();							//decrypting the historyFile file.
		boolean status = this.verifyHistoryFile();			//verifying the historyFile file.

		System.out.println("User Successful");


		this.updateHistoryFile();							//updating the historyFile file.

		init.randomizeInstructionTable();

		//if there are more than h log ins already, update mean and standard deviation.
		if(historyFile.isFull()) {
			System.out.println("File is full, updating mean and standard deviation");
			inst.updateMean(historyFile.getHistoryFile());
			inst.calculateStd_Dev(historyFile.getHistoryFile());
			inst.disturbValues();
		}
		inst.writeInstrTable();
	}
	//assumes threshold, feature values, pwd are already read. 
	public String doLogin(){
		this.calculateXY(init.feartures); 					 //pass feature value
		this.calculateHpwd();   							//calling the function to calculate Hpwd
		//System.out.println("hpwd is " + candidateHpwd);
		this.init.hpwd = candidateHpwd;
		String result=this.decryptHistoryFile();							//decrypting the historyFile file.
		boolean status = this.verifyHistoryFile();			//verifying the historyFile file.
		if(status)
		{
			System.out.println("User Successful");

		} else {
			System.out.println("Bad feature values or password.");
			return "0";
		}

		this.updateHistoryFile();							//updating the historyFile file.
		
		init.randomizeInstructionTable();
		
		//if there are more than h log ins already, update mean and standard deviation.
		if(historyFile.isFull()) {
			System.out.println("File is full, updating mean and standard deviation");
			inst.updateMean(historyFile.getHistoryFile());
			inst.calculateStd_Dev(historyFile.getHistoryFile());
			inst.disturbValues();
		}		
		inst.writeInstrTable();
		return "1";
	}

	private void calculateXY(int[] featureValues){
		try{
			int count = featureValues.length;
			for(int i=0; i<count; i++)
			{	
				/*getting the alpha and beta values from the Instruction table
				 *and comparing it to threshold value to get the alpha and 
				 *beta values
				 */
				float value = featureValues[i];
				if(value<inst.threshold[i]){			    
					alpha = inst.getAlpha(i);					
					//calculating the x and y values from the alpha and beta values
					xValues[i] = util.P(inst.getR(), 2*i, inst.q);
					yValues[i] = alpha.subtract((util.G(init.getPwd(), inst.getR(), 2*i, inst.q))).mod(inst.q);
				}
				else
				{
					beta = inst.getBeta(i);				//a needs to be replaced by beta values

					xValues[i] = util.P(inst.getR(), 2*i+1, inst.q);
					yValues[i] = beta.subtract((util.G(init.getPwd(), inst.getR(), 2*i+1, inst.q))).mod(inst.q);
				}
				//System.out.println("x[" + i + "] is " + xValues[i]);
				//System.out.println("y[" + i + "] is " + yValues[i]);
			}

		}
		catch(Exception e){
			System.out.println("Error in calculateXY " + e);
			e.printStackTrace();
		}
	}

	//method to calculate Hpwd
	private void calculateHpwd(){
		try{
			int count = init.get_m();
			this.candidateHpwd = new BigInteger("0");
			for(int i=0; i<count; i++){			
				//calculated the hardened password from the 
				//values in the alpha beta instruction table
				candidateHpwd = this.candidateHpwd.add(yValues[i].multiply(Lamda(i)).mod(inst.q));
			}
			candidateHpwd = candidateHpwd.mod(inst.q);
			//System.out.println("Candidate Hpwd: " + candidateHpwd);
			//System.out.println("q is " + inst.q);
		}
		catch(Exception e){

		}
	}

	//calls the decrypt method in the historyFile file
	private String decryptHistoryFile(){
		return historyFile.decrypt(candidateHpwd);
	}

	//verifies the decrypted file
	private boolean verifyHistoryFile(){
		return historyFile.checkDecryption();
	}

	//updated the historyFile file
	//calls a method in the historyFile class.
	private void updateHistoryFile(){
		historyFile.update();
	}

	private void generateInstructionsTable(String pass){
		//TODO: the instruction table needs to be build
	}

	//method to calculate lamda that is used in Hped calculation
	private BigInteger Lamda(int i){
		BigInteger lamda = new BigInteger("1");
		try{ 
			int count = init.get_m();
			for(int j =0; j<count; j++){
				if(i!=j){
					BigInteger int1 = (xValues[j].subtract(xValues[i])).mod(inst.q);
					BigInteger int2 = int1.modInverse(inst.q);
					BigInteger int3 = (xValues[j].multiply(int2)).mod(inst.q);
					lamda = (lamda.multiply(int3)).mod(inst.q);
//					lamda = lamda.multiply(xValues[j].multiply((xValues[j].subtract(xValues[i]).mod(inst.q)).modInverse(inst.q)));
				}
			}
			//System.out.println("The value of lamda:  "+ lamda);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return lamda;
	}

	//getter for candidate password
	public BigInteger getCandidateHpwd() {
		return candidateHpwd;
	}


}
