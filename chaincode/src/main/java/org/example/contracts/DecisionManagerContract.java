package org.example.contracts;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import org.example.controller.CollectionManager;
import org.example.controller.CollectionManager.PermissionLevels;
import org.example.controller.XmlParser;
import org.example.model.processdatamodels.InputMetaDataAsset;
import org.example.model.rulesetdatamodels.DecisionAsset;
import org.example.model.rulesetdatamodels.Input;
import org.example.model.rulesetdatamodels.Output;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.owlike.genson.Genson;


@Contract(name = "DecisionManagerContract",
    info = @Info(title = "Business Rule Executer contract",
                description = "Smart Contract for store Decision tables and make decisions with privacy-aware method.",
                version = "0.0.1",
                license =
                        @License(name = "Apache-2.0",
                                url = ""),
                                contact =  @Contact(email = "benyakbence@edu.bme.hu",
                                                name = "decisionContract",
                                                url = "https://niif.cloud.bme.hu/dashboard/vm/17340")))
@Default
public class DecisionManagerContract implements ContractInterface {

    private final static Genson genson = new Genson();
    public  DecisionManagerContract() {}

    @Transaction()
    public void createDecisionAssetFromDMN(Context ctx, String decisionAssetId) {
        Map<String,Map<String,String>> permissions = new HashMap<>();
        boolean exists = decisionAssetExists(ctx,CollectionManager.getCollectionNameFromContext(ctx, PermissionLevels.Permissioned),decisionAssetId);
        if (exists)
            throw new RuntimeException("The asset decision asset "+decisionAssetId+" already exists");

        Map<String, byte[]> transientData = ctx.getStub().getTransient();
        if (transientData.size() == 0 | !transientData.containsKey("dmn"))
            throw new RuntimeException("The decision model was not specified in transient data. Please try again.");

        String xml = XmlParser.recoverStringParameterFromBytes(transientData.get("dmn"));
        String ownerOrganizationMSP = CollectionManager.getOrgMSPFromContext(ctx);

        permissions.put(decisionAssetId, new HashMap<>());
        permissions.get(decisionAssetId).put(ownerOrganizationMSP,"startProcess");

        try {
            DecisionAsset decisionAsset = XmlParser.createDecisionAssetFromXml(xml);
            decisionAsset.setOwnerOrganizationMSP(ownerOrganizationMSP);
            ctx.getStub().putPrivateData(CollectionManager.getCollectionNameFromContext(ctx, PermissionLevels.Permissioned), decisionAssetId, decisionAsset.serialize());
            for(Input i:decisionAsset.getInputs()){
                InputMetaDataAsset InputMetaDataAsset = new InputMetaDataAsset(i, decisionAsset.decisionId, ownerOrganizationMSP);
                ctx.getStub().putPrivateData(
                    CollectionManager.getCollectionNameWithOrgMSP(i.organizationMSP, PermissionLevels.Modifiable),
                    decisionAssetId, InputMetaDataAsset.serialize()
                );
                permissions.put(i.inputId, new HashMap<>());
                permissions.get(i.inputId).put(i.organizationMSP, "addInput");
            }
            for(Output o: decisionAsset.getOutputs()){
                permissions.put(o.outputId, new HashMap<>());
                for (String orgMSP : o.organizationsMSPs) {
                    permissions.get(o.outputId).put(orgMSP, "getOutput");   
                }
            }
            ObjectMapper mapper = new ObjectMapper();
            String jsonResult = mapper.writerWithDefaultPrettyPrinter()
              .writeValueAsString(permissions);
            
            ctx.getStub().invokeChaincodeWithStringArgs("permissioncc", "addPermissions", jsonResult);
        } catch (Exception e) {
            System.out.println("=====ERROR===== " + e.toString() + " =====ERROR=====");
        } 
    }

    public DecisionAsset readDecisionAsset(Context ctx, String decisionAssetId, String collectionName)  {
        try {
            boolean exists = decisionAssetExists(ctx, collectionName,decisionAssetId);
        if (!exists) {
            throw new RuntimeException("The asset decision asset " + decisionAssetId + " does not exist");
        }
        byte[] privateData = ctx.getStub().getPrivateData(collectionName, decisionAssetId);
        DecisionAsset dec = genson.deserialize(privateData,DecisionAsset.class);
        return dec;
        } catch (Exception e) {
            System.out.println("=====ERROR===== " + e.toString() + " =====ERROR=====");
            return null;
        }
    }

    @Transaction()
    public boolean decisionAssetExists(Context ctx, String collectionName, String decisionAssetId) {
        byte[] buffer = ctx.getStub().getPrivateDataHash(collectionName, decisionAssetId);
        return (buffer != null && buffer.length > 0);
    }

    @Transaction()
    public boolean verifyAsset(Context ctx, String collectionName, String assetId, String objectToVerify) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashByte = digest.digest(objectToVerify.getBytes(StandardCharsets.UTF_8));
            String hashToVerify = new BigInteger(1, hashByte).toString(16);
            byte[] pdHashBytes = ctx.getStub().getPrivateDataHash(collectionName, assetId);
            if (pdHashBytes.length == 0) {
                throw new RuntimeException("No private data hash with the key: " + assetId);
            }
            String actualHash = new BigInteger(1, pdHashBytes).toString(16);
            if (hashToVerify.equals(actualHash)) 
                return true;
            return false;
        } catch (Exception e) {
            System.out.println("=====ERROR===== " + e.toString() + " =====ERROR=====");
            return false;
        }
    }

    @Transaction()
    public boolean verifyAssetWithObjectHash(Context ctx, String collectionName, String assetId, String objectHashToVerify) {
        try {
            String hashToVerify = objectHashToVerify;
            byte[] pdHashBytes = ctx.getStub().getPrivateDataHash(collectionName, assetId);
            if (pdHashBytes.length == 0) {
                throw new RuntimeException("No private data hash with the key: " + assetId);
            }
            String actualHash = new BigInteger(1, pdHashBytes).toString(16);
            if (hashToVerify.equals(actualHash)) 
                return true;
            return false;
        } catch (Exception e) {
            System.out.println("=====ERROR===== " + e.toString() + " =====ERROR=====");
            return false;
        }
    }
}
