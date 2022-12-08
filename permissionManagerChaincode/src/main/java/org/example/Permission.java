/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.example;

import java.util.HashMap;
import java.util.Map;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import com.owlike.genson.Genson;
import com.owlike.genson.annotation.JsonProperty;

@DataType()
public class Permission {

    private final static Genson genson = new Genson();

    @Property()
    private Map<String,HashMap<String,String>> permissions;

    public Permission(@JsonProperty("permissions") Map<String,HashMap<String,String>> permissions){
        this.permissions = permissions;
    }

    public Map<String,HashMap<String,String>> getPermissions() {
        return permissions;
    }

    public void setPermissions(Map<String,HashMap<String,String>> permissions) {
        this.permissions = permissions;
    }

    public String toJSONString() {
        return genson.serialize(this).toString();
    }

    public static Permission fromJSONString(String json) {
        Permission asset = genson.deserialize(json, Permission.class);
        return asset;
    }
}
