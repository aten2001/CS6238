
import java.math.BigInteger;

/**
 * Created by luoyinfeng on 10/13/16.
 */
public class User {

	private BigInteger alpha;
	private BigInteger beta;
	private BigInteger[] x_i;
	private BigInteger[] y_i;
	public  Ins_table inst;
	public  Init init;
	public  History_file historyFile;
	private BigInteger input_hwd;


	public User(Ins_table inst, Init init, History_file historyFile){
		this.inst = inst;
		this.init = init;
		this.historyFile = historyFile;
		this.x_i = new BigInteger[init.get_m()];
		this.y_i = new BigInteger[init.get_m()];
	}

	public String doLogin(){
		this.calculateXY(init.feartures); 					 //pass feature value
		this.calculateHpwd();   							//calling the function to calculate Hpwd
		this.init.hpwd = input_hwd;
		this.decryptHistoryFile();							//decrypting the historyFile file.
		boolean status = this.verifyHistoryFile();			//verifying the historyFile file.
		if(status)
		{
			System.out.println(init.getUserName()+" login Successful");

		} else {
			System.out.println("Bad feature values or password.");
			return "0";
		}

		this.updateHistoryFile();							//updating the historyFile file.

		//if there are more than h log ins already, update mean and standard deviation.
		if(historyFile.isFull()) {
			System.out.println("Update the historyfile and recalculate the mean and standard deviation");
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
					x_i[i] = init.P(inst.getR(), 2*i, inst.q);
					y_i[i] = alpha.subtract((init.G(init.getPwd(), inst.getR(), 2*i, inst.q))).mod(inst.q);
				}
				else
				{
					beta = inst.getBeta(i);				//a needs to be replaced by beta values

					x_i[i] = init.P(inst.getR(), 2*i+1, inst.q);
					y_i[i] = beta.subtract((init.G(init.getPwd(), inst.getR(), 2*i+1, inst.q))).mod(inst.q);
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
			this.input_hwd = new BigInteger("0");
			for(int i=0; i<count; i++){			
				//calculated the hardened password from the 
				//values in the alpha beta instruction table
				input_hwd = this.input_hwd.add(y_i[i].multiply(feature_values(i)).mod(inst.q));
			}
			input_hwd = input_hwd.mod(inst.q);

		}
		catch(Exception e){

		}
	}

	//calls the decrypt method in the historyFile file
	private String decryptHistoryFile(){
		return historyFile.decrypt(input_hwd);
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

	//method to calculate feature_values that is used in Hped calculation
	private BigInteger feature_values(int i){
		BigInteger feature_values = new BigInteger("1");
		try{ 
			int count = init.get_m();
			for(int j =0; j<count; j++){
				if(i!=j){
					BigInteger int1 = (x_i[j].subtract(x_i[i])).mod(inst.q);
					BigInteger int2 = int1.modInverse(inst.q);
					BigInteger int3 = (x_i[j].multiply(int2)).mod(inst.q);
					feature_values = (feature_values.multiply(int3)).mod(inst.q);
				}
			}

		}
		catch(Exception e){
			e.printStackTrace();
		}
		return feature_values;
	}

}
