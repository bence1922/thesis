package org.example.contracts;

import java.util.ArrayList;
import java.util.List;

import org.example.controller.CollectionManager;
import org.example.controller.CollectionManager.PermissionLevels;
import org.example.model.processdatamodels.InputMetaDataAsset;
import org.example.model.processdatamodels.PrivateInputValueAsset;
import org.example.model.processdatamodels.ProcessAsset;
import org.example.model.processdatamodels.ResultAsset;
import org.example.model.rulesetdatamodels.DecisionAsset;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.Chaincode.Response;

import com.owlike.genson.Genson;

@Contract(name = "ProcessManagerContract",
    info = @Info(title = "ProcessManager contract",
                description = "Contract for manage decision-making process on a decision table.",
                version = "0.0.1",
                license =
                    @License(name = "Apache-2.0",
                    url = ""),
                    contact =  @Contact(email = "benyakbence@edu.bme.hu",
                                    name = "decisionContract",
                                    url = "https://niif.cloud.bme.hu/dashboard/vm/17340")))
@Default
public class ProcessManagerContract implements ContractInterface {

    private final static Genson genson = new Genson();
    DecisionManagerContract decisionManagerContract = new DecisionManagerContract();
    
    public ProcessManagerContract() {}

    @Transaction()
    public void startProcess(Context ctx, String processId, String decisionId) {
        String[] args = {"getPermission", decisionId, decisionId, CollectionManager.getOrgMSPFromContext(ctx), "startProcess"};
        Response res = ctx.getStub().invokeChaincodeWithStringArgs("permissioncc", args);
        if(!res.getPayload().toString().equals("ok"))
            throw new RuntimeException("Permission denied");
            
        String ownerOrganizationMSP = CollectionManager.getOrgMSPFromContext(ctx);
        DecisionAsset decisionAsset = decisionManagerContract.readDecisionAsset(ctx, decisionId, CollectionManager.getCollectionNameFromContext(ctx, PermissionLevels.Permissioned));
        List<String> participants = decisionAsset.getParticipants();
        ProcessAsset processAsset = new ProcessAsset(processId, "started", ownerOrganizationMSP, decisionAsset.getInputs().size(), decisionId);
        ctx.getStub().putPrivateData(CollectionManager.getCollectionNameFromContext(ctx, PermissionLevels.Permissioned),processId, processAsset.serialize());
        processAsset.setremainingInputCount(1);
        processAsset.setStatus("input data required");
        for (String organization : participants) {
            ctx.getStub().putPrivateData(CollectionManager.getCollectionNameWithOrgMSP(organization, PermissionLevels.Modifiable), processId, processAsset.serialize());
        }
    }

    @Transaction()
    public String getInputMetaData(Context ctx, String processId) {    
        String collName = CollectionManager.getCollectionNameFromContext(ctx, PermissionLevels.Modifiable);
        ProcessAsset processAsset = readProcessAsset(ctx, processId, collName);
        InputMetaDataAsset inputMetaDataAsset = genson.deserialize(
            ctx.getStub().getPrivateData(collName, processAsset.getDecisionId()), InputMetaDataAsset.class
        );
        return inputMetaDataAsset.toJSONString();
    }

    @Transaction()
    public void addInputData(Context ctx, String processId, String inputId, String value) {
        ProcessAsset processAsset = genson.deserialize(ctx.getStub().getPrivateData(CollectionManager.getCollectionNameFromContext(ctx, PermissionLevels.Private), processId),ProcessAsset.class);
        String[] args = {"getPermission", processAsset.getDecisionId(), inputId, CollectionManager.getOrgMSPFromContext(ctx), "addInput"};
        Response res = ctx.getStub().invokeChaincodeWithStringArgs("permissioncc", args);
        if(!res.getPayload().toString().equals("ok"))
            throw new RuntimeException("Permission denied");
        PrivateInputValueAsset privateValueAsset = new PrivateInputValueAsset(value);
        if(assetExists(ctx, processId, CollectionManager.getCollectionNameFromContext(ctx, PermissionLevels.Private)) || processAsset.getStatus().equals("decision done"))
            throw new RuntimeException("The input has already provided");
        ctx.getStub().putPrivateData(CollectionManager.getCollectionNameFromContext(ctx, PermissionLevels.Private), processId, privateValueAsset.serialize());
        ProcessAsset processAssetOverall = genson.deserialize(ctx.getStub().getPrivateData(CollectionManager.getCollectionNameWithOrgMSP(processAsset.getProcessOwnerOrganization(), PermissionLevels.Permissioned), processId),ProcessAsset.class);
        processAssetOverall.decreaseRemaining();
        InputMetaDataAsset inputMetaDataAsset = genson.deserialize(ctx.getStub().getPrivateData(CollectionManager.getCollectionNameFromContext(ctx, PermissionLevels.Modifiable), processAsset.getDecisionId()), InputMetaDataAsset.class);
        String decisionOwner = inputMetaDataAsset.getDecisionOwnerMSP();
        DecisionAsset decisionAsset = decisionManagerContract.readDecisionAsset(ctx, processAsset.getDecisionId(), CollectionManager.getCollectionNameWithOrgMSP(decisionOwner,PermissionLevels.Permissioned ));
        List<Integer> result = decisionAsset.evaluate(inputId, value);
        ResultAsset resultAsset = new ResultAsset(result);
        processAsset.decreaseRemaining();
        ctx.getStub().putPrivateData(CollectionManager.getCollectionNameFromContext(ctx, PermissionLevels.Permissioned), processId, resultAsset.toJSONString());
    }

    @Transaction()
    public void calculateOutput(Context ctx, String processId, String outputId) {
        try {
            ProcessAsset processAsset = readProcessAsset(ctx, processId, CollectionManager.getCollectionNameFromContext(ctx, PermissionLevels.Permissioned));
            String[] args = {"getPermission", processAsset.getDecisionId(), outputId, CollectionManager.getOrgMSPFromContext(ctx), "calculateOutput"};
            Response res = ctx.getStub().invokeChaincodeWithStringArgs("permissioncc", args);
            if(!res.getPayload().toString().equals("ok"))
                throw new RuntimeException("Permission denied");
            ProcessAsset processAssetOverall = genson.deserialize(ctx.getStub().getPrivateData(CollectionManager.getCollectionNameWithOrgMSP(processAsset.getProcessOwnerOrganization(), PermissionLevels.Permissioned), processId),ProcessAsset.class);
            if (!processAssetOverall.getStatus().equals("decision done")) 
                throw new RuntimeException("Missing input(s)");
            DecisionAsset decisionAsset = decisionManagerContract.readDecisionAsset(ctx, processAsset.getDecisionId(), CollectionManager.getCollectionNameWithOrgMSP(processAsset.getProcessOwnerOrganization(), PermissionLevels.Permissioned));
            List<Integer> results = new ArrayList<>();
            int i = 0;
            for (String participant : decisionAsset.getParticipants()) {
                List<Integer> result = genson.deserialize(ctx.getStub().getPrivateData(CollectionManager.getCollectionNameWithOrgMSP(participant, PermissionLevels.Permissioned), processId), ResultAsset.class).getResult();
                if(i == 0){
                    results.addAll(result);
                }
                else {
                    for (Integer rowNumber : results) {
                        if(!result.contains(rowNumber))
                            results.remove(rowNumber);
                    }
                }
            }
            PrivateInputValueAsset privAsset = new PrivateInputValueAsset(results.toString());
            ctx.getStub().putPrivateData(CollectionManager.getCollectionNameFromContext(ctx, PermissionLevels.Private), processId, privAsset.serialize());
        
        } catch (Exception e) {
            System.out.println("=====ERROR===== " + e.toString() + " =====ERROR=====");
        }
    }

    @Transaction()
    public String getOutput(Context ctx, String processId, String outputId) {
        try {
            ProcessAsset processAsset = readProcessAsset(ctx, processId, CollectionManager.getCollectionNameFromContext(ctx, PermissionLevels.Permissioned));
            String[] args = {"getPermission", processAsset.getDecisionId(), outputId, CollectionManager.getOrgMSPFromContext(ctx), "calculateOutput"};
            Response res = ctx.getStub().invokeChaincodeWithStringArgs("permissioncc", args);
            if(!res.getPayload().toString().equals("ok"))
                throw new RuntimeException("Permission denied");
            PrivateInputValueAsset asset = genson.deserialize(ctx.getStub().getPrivateData(CollectionManager.getCollectionNameFromContext(ctx, PermissionLevels.Private), processId),PrivateInputValueAsset.class);
            return asset.getValue();
        } catch (Exception e) {
            System.out.println("=====ERROR===== " + e.toString() + " =====ERROR=====");
            return e.toString();
        }
    }

    @Transaction()
    public String readProcessStatus(Context ctx, String processAssetId, String collectionName) {
        try {
            if (assetExists(ctx, collectionName, processAssetId)) {
                throw new RuntimeException("The asset does not exist.");
            }
            byte[] privateData = ctx.getStub().getPrivateData(collectionName, processAssetId);
            ProcessAsset processAsset = genson.deserialize(privateData,ProcessAsset.class);
            return processAsset.getStatus();
        } catch (Exception e) {
            System.out.println("=====ERROR===== " + e.toString() + " =====ERROR=====");
            return e.toString();
        }   
    }

    private ProcessAsset readProcessAsset(Context ctx, String processAssetId, String collectionName){
        byte[] privateData = ctx.getStub().getPrivateData(collectionName, processAssetId);
        ProcessAsset processAsset = genson.deserialize(privateData,ProcessAsset.class);
        return processAsset;
    }

    private boolean assetExists(Context ctx, String collectionName, String assetId) {
        byte[] buffer = ctx.getStub().getPrivateDataHash(collectionName, assetId);
        return (buffer != null && buffer.length > 0);
    }
}
 