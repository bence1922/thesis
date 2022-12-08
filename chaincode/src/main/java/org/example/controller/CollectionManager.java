package org.example.controller;

import org.hyperledger.fabric.contract.Context;

public class CollectionManager {
    static public enum PermissionLevels {
        Private,
        Permissioned,
        Modifiable
    }
   
    public static String getCollectionNameFromContext(Context ctx, PermissionLevels permissionLevel) {
        return getOrgMSPFromContext(ctx) + "_" + permissionLevel + "Collection";
    }

    public static String getCollectionNameWithOrgMSP(String organizationMSP, PermissionLevels permissionLevel) {
        return organizationMSP + "_" + permissionLevel + "Collection";
    }

    public static String getOrgMSPFromContext(Context ctx) {
        return ctx.getClientIdentity().getId().split("=")[1].split(" ")[0] + "MSP";
    }
}