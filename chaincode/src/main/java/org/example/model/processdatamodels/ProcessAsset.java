package org.example.model.processdatamodels;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import org.json.JSONObject;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.owlike.genson.annotation.JsonProperty;

@DataType()
public class ProcessAsset {
    @Property()
    private String processId;

    @Property()
    private String status;

    @Property()
    private String decisionId;

    @Property()
    private String processOwnerOrganization;

    @Property()
    private int remainingInputCount;

    public ProcessAsset(@JsonProperty("processId") String processId, @JsonProperty("status") String status,
            @JsonProperty("processOwnerOrganization") String processOwnerOrganization, @JsonProperty("remainingInputCount") int remainingInputCount,
            @JsonProperty("decisionId") String decisionId){
        this.processId = processId;
        this.status = status;
        this.processOwnerOrganization = processOwnerOrganization;
        this.decisionId = decisionId;
        this.remainingInputCount = remainingInputCount;
    }

    public byte[] serialize() {
        String jsonStr = new JSONObject(this).toString();
        return jsonStr.getBytes(UTF_8);
    }

    public String toJSONString() {
        return new JSONObject(this).toString();
    }

    public void decreaseRemaining(){
        remainingInputCount--;
        if (remainingInputCount == 0)
            status = "decision done";
    }

    //#region getters, setters

    public String getProcessId() {
        return processId;
    }


    public void setProcessId(String processId) {
        this.processId = processId;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProcessOwnerOrganization() {
        return processOwnerOrganization;
    }

    public void setProcessOwnerOrganization(String processOwnerOrganization) {
        this.processOwnerOrganization = processOwnerOrganization;
    }

    public int getremainingInputCount() {
        return remainingInputCount;
    }

    public void setremainingInputCount(int remainingInputCount) {
        this.remainingInputCount = remainingInputCount;
    }

    public String getDecisionId() {
        return decisionId;
    }

    public void setDecisionId(String decisionId) {
        this.decisionId = decisionId;
    }

    
    //#endregion
}
