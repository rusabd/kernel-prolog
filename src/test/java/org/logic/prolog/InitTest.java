package org.logic.prolog;

import java.io.InputStreamReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.logic.prolog.builtins.Builtins;
import org.logic.prolog.fluents.DataBase;

public class InitTest {

    private DataBase db1;
    private DataBase db2;

    @Before
    public void init() {
       db1 = Init.startProlog();
       db2 = Init.startProlog();
    }

    @Test
    public void testInit() {
        Init.builtinDict = new Builtins(db1);
        db1.streamToProg(new InputStreamReader(Init.class.getResourceAsStream(Init.default_lib)), true);
        db2.streamToProg(new InputStreamReader(Init.class.getResourceAsStream(Init.default_lib)), true);

        db1.streamToProg(new InputStreamReader(InitTest.class.getResourceAsStream("test.pro")), false);
        db2.streamToProg(new InputStreamReader(InitTest.class.getResourceAsStream("test1.pro")), false);

        final String s = Init.askProlog(db1, "x(X)");
        Assert.assertEquals("the(x(1))", s);
        final String s1 = Init.askProlog(db2, "x(X)");
        Assert.assertEquals("the(x(3))", s1);
    }

}