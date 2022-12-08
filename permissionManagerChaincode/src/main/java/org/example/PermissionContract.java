/*
 * SPDX-License-Identifier: Apache-2.0
 */
package org.example;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Transaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.HashMap;
import java.util.Map;

@Contract(name = "PermissionContract",
    info = @Info(title = "Permission Manager contract",
                description = "Smart Contract to store roles and check access to objects.",
                version = "0.0.1",
                license =
                    @License(name = "Apache-2.0",
                    url = ""),
                    contact =  @Contact(email = "benyakbence@edu.bme.hu",
                                    name = "permissionContract",
                                    url = "https://niif.cloud.bme.hu/dashboard/vm/17340")))
@Default
public class PermissionContract implements ContractInterface {
    public  PermissionContract() {

    }
    @Transaction()
    public boolean permissionExists(Context ctx, String permissionId) {
        byte[] buffer = ctx.getStub().getState(permissionId);
        return (buffer != null && buffer.length > 0);
    }

    @Transaction()
    public void addPermissions(Context ctx, String permissionId, String jsonInput) throws JsonMappingException, JsonProcessingException {
        boolean exists = permissionExists(ctx,permissionId);
        if (exists) {
            throw new RuntimeException("The asset "+permissionId+" already exists");
        }
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<HashMap<String,HashMap<String, String>>> typeRef = new TypeReference<HashMap<String,HashMap<String,String>>>(){};
        Map<String,HashMap<String, String>> map = mapper.readValue(jsonInput, typeRef);
        Permission asset = new Permission(map);
        ctx.getStub().putState(permissionId, asset.toJSONString().getBytes(UTF_8));
    }

    @Transaction()
    public String getPermission(Context ctx, String permissionId, String objectId, String OrgMSP, String role){
        try {
            Permission permission = Permission.fromJSONString(ctx.getStub().getState(permissionId).toString());
            if(permission.getPermissions().get(objectId).get(OrgMSP).equals(role))
                return "ok";
        } catch (Exception e) {
            System.out.println("=====ERROR===== " + e.toString() + " =====ERROR=====");
        }
        return "denied";
    }
    
}
