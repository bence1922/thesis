package org.example.model.rulesetdatamodels;

import java.util.ArrayList;
import java.util.List;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import org.json.JSONObject;

import com.owlike.genson.annotation.JsonProperty;
import static java.nio.charset.StandardCharsets.UTF_8;

@DataType
public class Input {

    @Property()
    public String inputId;

    @Property()
    public String inputName;

    @Property()
    public String typeRef;

    @Property()
    public List<String> defaultValues; //from input column meta information

    @Property()
    public List<String> values; //from Rules

    @Property()
    public String organizationMSP;


    public Input(@JsonProperty("inputId") String inputId,@JsonProperty("inputName") String inputName,@JsonProperty("typeRef") String typeRef,
                 @JsonProperty("defaultValues") List<String> defaultValues, @JsonProperty("values") List<String> values,
                 @JsonProperty("organizationMSP") String organizationMSP) {
        this.inputId = inputId;
        this.inputName = inputName;
        this.typeRef = typeRef;
        this.defaultValues = defaultValues;
        this.values = values;
        this.organizationMSP = organizationMSP;
    }

    public Input(String inputId, String inputName, String typeRef, List<String> defaultValues) {
        this.inputId = inputId;
        this.inputName = inputName;
        this.typeRef = typeRef;
        this.defaultValues = defaultValues;
        this.values = new ArrayList<>();
        this.organizationMSP = "";
    }

    public byte[] serialize() {
        String jsonStr = new JSONObject(this).toString();
        return jsonStr.getBytes(UTF_8);
    }

    public String toJSONString() {
        return new JSONObject(this).toString();
    }

    //#region getters,setters

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

    public String getOrganizationMSP() {
        return organizationMSP;
    }

    public void setOrganizationMSP(String organizationMSP) {
        this.organizationMSP = organizationMSP;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    //#endregion
}
