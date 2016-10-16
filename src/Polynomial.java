import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Created by luoyinfeng on 10/14/16.
 */
public class Polynomial {
    public BigInteger[] coeffs;  // coefficients
    public int deg;     // degree of polynomial (0 for the zero polynomial)
    private SecureRandom random;
    // a * x^b
    public Polynomial() {
    }
    public BigInteger nextRandomBigInteger(BigInteger n) {
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        BigInteger num;

        //find a random H that is less than Q
        do{
            byte bytes[] = new byte[20];
            random.nextBytes(bytes);

            num = new BigInteger(bytes);
        }
        while(num.compareTo(BigInteger.ZERO) != 1 && num.compareTo(n) != -1);

        return num;
    }
}
