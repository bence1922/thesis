package org.example.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import scala.util.Either;

import org.camunda.feel.FeelEngine;
import org.camunda.feel.impl.SpiServiceLoader;

public class EvaluateEngine {
    final static FeelEngine engine = new FeelEngine.Builder()
                .valueMapper(SpiServiceLoader.loadValueMapper())
                .functionProvider(SpiServiceLoader.loadFunctionProvider())
                .build();

    public static List<Integer> evaluateBoolean(List<String> rules, String value){
        List<Integer> list = new ArrayList<>();
        Boolean booleanValue = Boolean.valueOf(value);
        Map<String, Object> variables  = new HashMap<String, Object>() {{
            put("x", booleanValue);
        }};
        int i = 1;
        for (String r : rules) {
            String expression = "x="+r; 
            final Either<FeelEngine.Failure, Object> result = engine.evalExpression(expression, variables);
            if(result.isRight()){
                if(result.isRight() && result.toString().equals("Right(true)")){
                    list.add(i);
                }
            }
            else{
                result.left();
            }
            i++;
        }
        return list;
    }
    public static List<Integer> evaluateNum(List<String> rules, int value){
        List<Integer> list = new ArrayList<>();
        Map<String, Object> variables  = new HashMap<String, Object>() {{
            put("x", value);
        }};
        int i = 1;
        for (String r : rules) {
            String expression = "x"+r;
            final Either<FeelEngine.Failure, Object> result = engine.evalExpression(expression, variables);
            if(result.isRight()){
                if(result.isRight() && result.toString().equals("Right(true)")){
                    list.add(i);
                }
            }
            i++;
        }
        return list;
    }
}