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
        processAsset.setReamainingInputCount(1);
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
        ProcessAsset processAsset = genson.deserialize(ctx.getStub().getPrivateData(CollectionManager.getCollectionNameFromContext(ctx, PermissionLevels.Permissioned), processId),ProcessAsset.class);
        String[] args = {"getPermission", processAsset.getDecisionId(), inputId, CollectionManager.getOrgMSPFromContext(ctx), "addInput"};
        Response res = ctx.getStub().invokeChaincodeWithStringArgs("permissioncc", args);
        if(!res.getPayload().toString().equals("ok"))
            throw new RuntimeException("Permission denied");

        PrivateInputValueAsset privateValueAsset = new PrivateInputValueAsset(value);
        ctx.getStub().putPrivateData(CollectionManager.getCollectionNameFromContext(ctx, PermissionLevels.Private), processId, privateValueAsset.serialize());
        InputMetaDataAsset inputMetaDataAsset = genson.deserialize(ctx.getStub().getPrivateData(CollectionManager.getCollectionNameFromContext(ctx, PermissionLevels.Modifiable), processAsset.getDecisionId()), InputMetaDataAsset.class);
        String decisionOwner = inputMetaDataAsset.getDecisionOwnerMSP();
        DecisionAsset decisionAsset = decisionManagerContract.readDecisionAsset(ctx, processAsset.getDecisionId(), CollectionManager.getCollectionNameWithOrgMSP(decisionOwner,PermissionLevels.Permissioned ));
        List<Integer> result = decisionAsset.evaluate(inputId, value);
        ResultAsset resultAsset = new ResultAsset(result);
        ctx.getStub().putPrivateData(CollectionManager.getCollectionNameFromContext(ctx, PermissionLevels.Permissioned), processId, resultAsset.toJSONString());
    }

    @Transaction()
    public String calculateOutput(Context ctx, String processId, String outputId) {
        ProcessAsset processAsset = readProcessAsset(ctx, processId, CollectionManager.getCollectionNameFromContext(ctx, PermissionLevels.Permissioned));
        String[] args = {"getPermission", processAsset.getDecisionId(), outputId, CollectionManager.getOrgMSPFromContext(ctx), "calculateOutput"};
        Response res = ctx.getStub().invokeChaincodeWithStringArgs("permissioncc", args);
        if(!res.getPayload().toString().equals("ok"))
            throw new RuntimeException("Permission denied");

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
        return decisionAsset.getOutput(results, outputId);
    }

    @Transaction()
    public ProcessAsset readProcessAsset(Context ctx, String processAssetId, String collectionName) {
        byte[] privateData = ctx.getStub().getPrivateData(collectionName, processAssetId);
        ProcessAsset processAsset = genson.deserialize(privateData,ProcessAsset.class);
        return processAsset;
    }
}
