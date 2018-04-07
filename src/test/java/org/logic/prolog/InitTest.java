package org.logic.prolog;

import java.io.InputStreamReader;
import org.junit.Before;
import org.junit.Test;
import org.logic.prolog.builtins.Builtins;
import org.logic.prolog.fluents.DataBase;

public class InitTest {
    @Before
    public void init() {
       Init.startProlog();
    }

    @Test
    public void testInit() {
        Init.builtinDict=new Builtins();
        DataBase.streamToProg(new InputStreamReader(Init.class.getResourceAsStream(Init.default_lib)), true);
    }

}