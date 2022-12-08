package org.example.model.processdatamodels;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import org.json.JSONObject;

import com.owlike.genson.annotation.JsonProperty;
import static java.nio.charset.StandardCharsets.UTF_8;

@DataType
public class PrivateInputValueAsset {
    @Property()
    private String value;

    public byte[] serialize() {
        String jsonStr = new JSONObject(this).toString();
        return jsonStr.getBytes(UTF_8);
    }

    public String toJSONString() {
        return new JSONObject(this).toString();
    }

    public PrivateInputValueAsset(@JsonProperty("value") String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
