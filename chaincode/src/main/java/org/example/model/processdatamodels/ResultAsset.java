package org.example.model.processdatamodels;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import org.json.JSONObject;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.List;

import com.owlike.genson.annotation.JsonProperty;

@DataType()
public class ResultAsset {
    @Property()
    List<Integer> result;

    public ResultAsset(@JsonProperty("result") List<Integer> result){
        this.result = result;
    }

    public byte[] serialize() {
        String jsonStr = new JSONObject(this).toString();
        return jsonStr.getBytes(UTF_8);
    }

    public String toJSONString() {
        return new JSONObject(this).toString();
    }

    public List<Integer> getResult() {
        return result;
    }

    public void setResult(List<Integer> result) {
        this.result = result;
    }
}
