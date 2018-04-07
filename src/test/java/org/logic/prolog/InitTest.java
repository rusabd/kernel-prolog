package org.logic.prolog;

import org.junit.Before;
import org.junit.Test;
import org.logic.prolog.builtins.Builtins;

public class InitTest {
    @Before
    public void init() {
        Init.startProlog();
    }

    @Test
    public void testInit() {
        Init.builtinDict=new Builtins();
        Init.askProlog("reconsult('"+Init.default_lib+"')");
    }

}