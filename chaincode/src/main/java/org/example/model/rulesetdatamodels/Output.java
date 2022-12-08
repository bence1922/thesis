package org.example.model.rulesetdatamodels;

import java.util.ArrayList;
import java.util.List;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import org.json.JSONObject;

import static java.nio.charset.StandardCharsets.UTF_8;
import com.owlike.genson.annotation.JsonProperty;

@DataType
public class Output {


    @Property()
    public String outputId;

    @Property()
    public String outputName;

    @Property()
    public String typeRef;

    @Property()
    public List<String> values;

    @Property()
    public List<String> organizationsMSPs;

    public Output(@JsonProperty("outputId") String outputId,@JsonProperty("outputName") String outputName,
                  @JsonProperty("typeRef") String typeRef, @JsonProperty("values") List<String> values,
                  @JsonProperty("organizationsMSPs") List<String> organizationsMSPs) {
        this.outputId = outputId;
        this.outputName = outputName;
        this.typeRef = typeRef;
        this.values = values;
        this.organizationsMSPs = organizationsMSPs;
    }

    public Output(String outputId, String outputName, String typeRef) {
        this.outputId = outputId;
        this.outputName = outputName;
        this.typeRef = typeRef;
        this.values = new ArrayList<>();
        this.organizationsMSPs = new ArrayList<>();
    }

    public byte[] serialize() {
        String jsonStr = new JSONObject(this).toString();
        return jsonStr.getBytes(UTF_8);
    }

    public String toJSONString() {
        return new JSONObject(this).toString();
    }

    //#region getters,setters

    public String getOutputId() {
        return outputId;
    }

    public void setOutputId(String outputId) {
        this.outputId = outputId;
    }

    public String getOutputName() {
        return outputName;
    }

    public void setOutputName(String outputName) {
        this.outputName = outputName;
    }

    public String getTypeRef() {
        return typeRef;
    }

    public void setTypeRef(String typeRef) {
        this.typeRef = typeRef;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public List<String> getOrganizationsMSPs() {
        return organizationsMSPs;
    }

    public void setOrganizationsMSPs(List<String> organizations) {
        this.organizationsMSPs = organizations;
    }

    //#endregion
}
