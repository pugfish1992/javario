package com.pugfish1992.javario.example;

import com.pugfish1992.javario.annotation.FieldOption;
import com.pugfish1992.javario.annotation.ModelSchema;
import com.pugfish1992.javario.annotation.ModelSchemaOption;

/**
 * Created by daichi on 10/25/17.
 */

@ModelSchema("Mario")
//@ModelSchemaOption(constVarNamePrefix = "OUSKE")
public class MarioSchema {

//    @FieldOption(fieldName = "mario_shout", constVarName = "SHOUT")
    String shout;

//    @FieldOption(constVarName = "HAS_HAS_BROTHER")
    boolean hasBrothers;
    int kill;
    int life;
}
