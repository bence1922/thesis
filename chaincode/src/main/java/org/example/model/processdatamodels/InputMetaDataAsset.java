package org.example.model.processdatamodels;

import java.util.List;

import org.example.model.rulesetdatamodels.Input;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import org.json.JSONObject;

import com.owlike.genson.annotation.JsonIgnore;
import com.owlike.genson.annotation.JsonProperty;

import static java.nio.charset.StandardCharsets.UTF_8;

@DataType()
public class InputMetaDataAsset {
    @Property()
    public String decisionTableId;

    @Property()
    public String decisionOwnerMSP;

    @Property()
    public String inputId;

    @Property()
    public String inputName;

    @Property()
    public String typeRef;

    @Property()
    public List<String> defaultValues;

    public InputMetaDataAsset
    (
        @JsonProperty("decisionTableId") String decisionTableId, @JsonProperty("decisionOwnerMSP") String decisionOwnerMSP,
        @JsonProperty("inputId") String inputId,@JsonProperty("inputName") String inputName,
        @JsonProperty("typeRef") String typeRef, @JsonProperty("defaultValues") List<String> defaultValues
    ) 
    {
        this.decisionTableId = decisionTableId;
        this.decisionOwnerMSP = decisionOwnerMSP;
        this.inputId = inputId;
        this.inputName = inputName;
        this.typeRef = typeRef;
        this.defaultValues = defaultValues;
    }

    @JsonIgnore
    public InputMetaDataAsset(Input i, String decisionTableId, String decisionOwnerMSP) {
        this.decisionTableId = decisionTableId;
        this.decisionOwnerMSP = decisionOwnerMSP;
        inputId = i.getInputId();
        inputName = i.getInputName();
        typeRef = i.getTypeRef();
        defaultValues = i.getDefaultValues();
    }
    public byte[] serialize() {
        String jsonStr = new JSONObject(this).toString();
        return jsonStr.getBytes(UTF_8);
    }

    public String toJSONString() {
        return new JSONObject(this).toString();
    }

     //#region getters,setters

    
     public String getDecisionTableId() {
        return decisionTableId;
    }

    public void setDecisionTableId(String decisionTableId) {
        this.decisionTableId = decisionTableId;
    }

         
    public String getDecisionOwnerMSP() {
        return decisionOwnerMSP;
    }

    public void setDecisionOwnerMSP(String decisionOwnerMSP) {
        this.decisionOwnerMSP = decisionOwnerMSP;
    }

    public String getInputId() {
        return inputId;
    }

    public void setInputId(String inputId) {
        this.inputId = inputId;
    }

    public String getInputName() {
        return inputName;
    }

    public void setInputName(String inputName) {
        this.inputName = inputName;
    }

    public String getTypeRef() {
        return typeRef;
    }

    public void setTypeRef(String typeRef) {
        this.typeRef = typeRef;
    }

    public List<String> getDefaultValues() {
        return defaultValues;
    }

    public void setDefaultValues(List<String> defaultValues) {
        this.defaultValues = defaultValues;
    }

   //#endregion

    
}
