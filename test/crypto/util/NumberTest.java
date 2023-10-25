package crypto.util;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

class NumberTest {
    @Test
    void randomBigIntegerTest() {
        BigInteger a = BigInteger.valueOf(0);
        BigInteger b = BigInteger.valueOf(3);
        BigInteger c = Number.randomBigInteger(a, b);
        assertTrue(c.compareTo(a) >= 0 || c.compareTo(b) <= 0);
    }
    @Test
    void isPrimeTest1() {
        BigInteger p = new BigInteger("11279976196039728715831991105194180854592613567725310922366544690792875646509514917723025649621810330104350497563387241812856262019782668005980903355631941");
        assertTrue(Number.isPrime(p));
    }
    @Test
    void isPrimeTest2() {
        BigInteger p = new BigInteger("108083418755392869427329151147828685607600562568892278180370819656018715254035940244836515671681028875912903373104222910122305781282409994297615297795271845182741738435449559877746889949235239359455579948905045454080922371138455158250466766578829995939705823687410350091521022005212424043948918455410955967523");
        assertTrue(Number.isPrime(p));
    }
    @Test
    void isPrimeTest3() {
        BigInteger p = new BigInteger("12");
        assertFalse(Number.isPrime(p));
    }
    @Test
    void isPrimeTest4() {
        BigInteger p = new BigInteger("1080834187553928694273291511478286856076005625688922781803708196560187");
        assertFalse(Number.isPrime(p));
    }

    @Test
    void inverseTest1() {
        BigInteger a = new BigInteger("3");
        BigInteger b = new BigInteger("8");
        BigInteger a_inv_b = new BigInteger("3");
        assertEquals(a_inv_b, Number.inverse(a, b));
    }

    @Test
    void inverseTest2() {
        BigInteger a = new BigInteger("123123123");
        BigInteger b = new BigInteger("7274370177025083653476667991952769190169931358002380117374280724665161641684417107467818206209890674275895357486732479806948458549002663472434235907454973");
        BigInteger a_inv_b = new BigInteger("2483017339267520049779308238961470031285898284289590040504242060182580946156230934212455827602821800942095485847749809709405643491086028687062225533898110");
        assertEquals(a_inv_b, Number.inverse(a, b));
    }

    @Test
    void inverseTest3() {
        int a = 3;
        int b = 8;
        int a_inv_b = 3;
        assertEquals(a_inv_b, Number.inverse(a, b));
    }

    @Test
    void inverseTest4() {
        int a = 123;
        int b = 586429;
        int a_inv_b = 553055;
        assertEquals(a_inv_b, Number.inverse(a, b));
    }

    @Test
    void getPrimeTest1() {
        BigInteger a = Number.getPrime(512);
        assertTrue(Number.isPrime(a));
    }

    @Test
    void sqrtPrimeFieldTest() {
        BigInteger p = Number.getPrime(20);
        BigInteger n = Number.randomBigInteger(p);
        BigInteger r = Number.sqrtPrimeField(n, p);
        System.out.println("p = " + p);
        System.out.println(n);
        System.out.println(r);
        if (r!=null)System.out.println(r.modPow(BigInteger.TWO, p));
    }

}