package org.openmrs.module.ncd.utilities;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Test {

    public static void test(String dateString) {
        
        try {
            Date retVal = new SimpleDateFormat("yyyyMMdd").parse(dateString);
            System.out.println(retVal);
        }
        catch (Exception e) {
            System.out.println("exception");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub

        test("20080611");
        test("20070103093400");
    }
}
