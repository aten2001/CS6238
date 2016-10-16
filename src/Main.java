import java.io.*;

/**
 * Created by luoyinfeng on 10/10/16.
 */
public class Main {

    public static final int h = 5; //number of records to keep in historyFile

    public static int counter=0;
    public static void main(String[] args) {
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("input.txt")) {
                try (BufferedReader br = new BufferedReader(new FileReader(args[0]))) {
                    String line1, line2;
                    PrintWriter writer = new PrintWriter("output.txt", "UTF-8");
                    while (true) {
                        if ((line1 = br.readLine()) == null)
                            break;
                        if ((line2 = br.readLine()) == null)
                            break;
                        String[] strArray = line2.split(",");
                        int[] features = new int[strArray.length];
                        for (int i = 0; i < strArray.length; i++) {
                            features[i] = Integer.parseInt(strArray[i]);
                        }
                        if(counter<5)
                        {

                            Init init = new Init(features,strArray.length,args[1]);
                            init.initialization(counter); //fill the init object with values needed later on.
                            writer.println("1");
                        }
                        else
                        {
                            Init init = new Init(features,strArray.length,args[1]);
                            writer.println(init.user_verify());
                        }
                        counter++;
                        System.out.println();
                        System.out.println();
                        System.out.println();
                        System.out.println();
                        System.out.println();
                    }
                    writer.close();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.err.println("Invalid argument " + args[0]);
            }
        } else {
            System.err.println("Invalid argument ");
        }
    }
    public static int t_i = 10; //threshold value
    public static double k_i = 2.5; // system parameter
}
