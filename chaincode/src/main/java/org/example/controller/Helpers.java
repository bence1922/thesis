package org.example.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.example.model.rulesetdatamodels.DecisionAsset;
import org.example.model.rulesetdatamodels.Input;
import org.example.model.rulesetdatamodels.Output;

public class Helpers {
    
    public static DecisionAsset createDec(int columnNumber, int rules){
        List<Input> inputs = new ArrayList<Input>();
        List<Output> outputs = new ArrayList<Output>();
        List<String> outputValues = new ArrayList<String>();
        for (int j = 0; j < rules; j++){
            outputValues.add((j+1) + ".rule matched");
        }
        Output output = new Output("o","o","string",outputValues,null);
        outputs.add(output);
        Random r = new Random();
        for (int i = 0; i < columnNumber; i++){
            List<String> inputValues = new ArrayList<String>();
            for (int j = 0; j < rules; j++){
                String value = "> " + r.nextInt(rules);
                inputValues.add(value);
            }
            Input input = new Input("i" + i,"i","int",null,inputValues,"org");
            inputs.add(input);

        }
        DecisionAsset dec = new DecisionAsset("dec","dec","Collect",inputs,outputs);
        return dec;
    }
}