
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


	public User(Init init){
		this.inst = init.inst;
		this.init = init;
		this.historyFile = init.historyFile;
		this.x_i = new BigInteger[init.get_m()];
		this.y_i = new BigInteger[init.get_m()];
	}

	public String Login(){
		this.get_XY(init.feartures); 					 //pass features value
		this.get_Hpwd();   							//calling the function to calculate hpwd
		this.init.hpwd = input_hwd;
		historyFile.decrypt(input_hwd);						//decrypting the history file.
		boolean result = this.verify();			//verifying the history file.
		if(result)
		{
			System.out.println(init.getUserName()+" login Successful");

		} else {
			System.out.println("Bad feature values or password.");
			return "0";
		}

		this.historyFile.update_hisfile();
		System.out.println("*************updating the historyFile file.*******************");
		//check if the history file is full
		if(historyFile.full()) {
			System.out.println("Update the historyfile and recalculate the mean and standard deviation");
			this.inst.calculateMean(historyFile.getHistoryFile());
			this.inst.calculateStd_Dev(historyFile.getHistoryFile());
			this.inst.disturbValues();
		}
		this.inst.write_ins();
		return "1";
	}

	private void get_XY(int[] featureValues){
		try{
			int counter = featureValues.length;
			System.out.println("*************************************************************************************************************");

			for(int i=0; i<counter; i++)
			{	
				/*getting the alpha and beta values from the Instruction table
				 *and comparing it to threshold value to get the alpha and 
				 *beta values
				 */
				float value = featureValues[i];
				if(value<inst.threshold[i]){			    
					alpha = inst.getAlpha(i);					
					//calculating the x and y values from the alpha and beta values
					x_i[i] = init.P_r(inst.getR(), 2*i, inst.q);
					y_i[i] = alpha.subtract((init.G_pwd(init.get_Possword(), inst.getR(), 2*i, inst.q))).mod(inst.q);
				}
				else
				{
					beta = inst.getBeta(i);				//a needs to be replaced by beta values

					x_i[i] = init.P_r(inst.getR(), 2*i+1, inst.q);
					y_i[i] = beta.subtract((init.G_pwd(init.get_Possword(), inst.getR(), 2*i+1, inst.q))).mod(inst.q);
				}
				System.out.println("x_i["+i+"]"+x_i[i]+"     "+"y_i["+i+"]"+y_i[i]);
			}
			System.out.println("*************************************************************************************************************");


		}
		catch(Exception e){
			System.out.println("Error in calculateXY " + e);
			e.printStackTrace();
		}
	}

	//calculate Hpwd
	private void get_Hpwd(){
		try{
			int count = init.get_m();
			this.input_hwd = new BigInteger("0");
			for(int i=0; i<count; i++){			
				//calculated the hardened password from the values in the alpha beta instruction table
				input_hwd = this.input_hwd.add(y_i[i].multiply(feature_values(i)).mod(inst.q));
			}
			input_hwd = input_hwd.mod(inst.q);
			System.out.println("*************************************************************************************************************");
			System.out.println("calculated hpwd is :"+input_hwd);
			System.out.println("*************************************************************************************************************");
		}
		catch(Exception e){

		}
	}


	//verifies the decrypted file
	private boolean verify(){
		return historyFile.check_intergity();
	}

	//calculate raw feature_values
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
