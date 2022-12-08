package org.example.model.rulesetdatamodels;

import java.util.ArrayList;
import java.util.List;

import org.example.controller.EvaluateEngine;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import org.json.JSONObject;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.owlike.genson.annotation.JsonIgnore;
import com.owlike.genson.annotation.JsonProperty;

@DataType()
public class DecisionAsset {

    @Property()
    public String decisionId;

    @Property()
    public String decisionName;

    @Property()
    public String hitPolicy;

    @Property()
    private List<Input> inputs;

    @Property()
    private List<Output> outputs;

    @Property()
    private String ownerOrganizationMSP;

    public DecisionAsset(@JsonProperty("decisionId") String decisionId,@JsonProperty("decisionName") String decisionName,
                         @JsonProperty("hitPolicy") String hitPolicy,@JsonProperty("inputs") List<Input> inputs,@JsonProperty("outputs") List<Output> outputs,
                         @JsonProperty("ownerOrganizationMSP") String ownerOrganizationMSP) {
        this.decisionId = decisionId;
        this.decisionName = decisionName;
        this.hitPolicy = hitPolicy;
        this.inputs = inputs;
        this.outputs = outputs;
        this.ownerOrganizationMSP = ownerOrganizationMSP;
    }

    public DecisionAsset(String decisionId, String decisionName, String hitPolicy, List<Input> inputs,
            List<Output> outputs) {
        this.decisionId = decisionId;
        this.decisionName = decisionName;
        this.hitPolicy = hitPolicy;
        this.inputs = inputs;
        this.outputs = outputs;
        this.ownerOrganizationMSP = "";
    }

    public byte[] serialize() {
        String jsonStr = new JSONObject(this).toString();
        return jsonStr.getBytes(UTF_8);
    }

    public String toJSONString() {
        return new JSONObject(this).toString();
    }

    //#region getters, setters

    public String getDecisionId() {
        return decisionId;
    }



    public void setDecisionId(String decisionId) {
        decisionId = this.decisionId;
    }



    public String getDecisionName() {
        return decisionName;
    }



    public void setDecisionName(String decisionName) {
        decisionName = this.decisionName;
    }



    public String getHitPolicy() {
        return hitPolicy;
    }



    public void setHitPolicy(String hitPolicy) {
        this.hitPolicy = hitPolicy;
    }



    public List<Input> getInputs() {
        return inputs;
    }



    public void setInputs(List<Input> inputs) {
        this.inputs = inputs;
    }



    public List<Output> getOutputs() {
        return outputs;
    }



    public void setOutputs(List<Output> outputs) {
        this.outputs = outputs;
    }

    public String getOwnerOrganizationMSP() {
        return ownerOrganizationMSP;
    }

    public void setOwnerOrganizationMSP(String ownerOrganizationMSP) {
        this.ownerOrganizationMSP = ownerOrganizationMSP;
    }


    //#endregion
    @JsonIgnore
    public List<Integer> evaluate(String inputId, String value) {
        for (Input  i : this.getInputs()) {
            if(i.getInputId().equals(inputId)){
                return EvaluateEngine.evaluateBoolean(i.values, value);
            }
        }
        return null;
    }

    @JsonIgnore
    public List<String> getParticipants(){
        List<String> participants = new ArrayList<>();
        for(Input i:this.getInputs()){
            participants.add(i.getOrganizationMSP());
        }
        return participants;
    }

    public String getOutput(List<Integer> results, String outputId) {
        if(this.hitPolicy.equals("COLLECT")){
            return this.getOutputsFromResults(results, outputId).toString();
        }
        if(this.hitPolicy.equals("UNIQUE")){
            List<String> output =  this.getOutputsFromResults(results, outputId);
            if(output.size() > 1)
                return "Hit policy error (UNIQUE)";
            return output.get(0);
        }
        if(this.hitPolicy.equals("FIRST")){
            List<String> output =  this.getOutputsFromResults(results, outputId);
            return output.get(0);
        }
        return "Unknown hit policy";

    }

    private List<String> getOutputsFromResults(List<Integer> results, String outputId) {
        List<String> output = new ArrayList<>();
        for (Output op : this.getOutputs()) {
            if(op.getOutputId().equals(outputId)){
                for (Integer rowId : results) {
                    output.add(op.getValues().get(rowId));
                }
            }
            return output;
        }
        return null;
        
    }
}
