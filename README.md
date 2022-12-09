# student-benyak-bsc-dbrms
BSc thesis project of Bence Benyák on distributed business rule execution over blockchain

## Quick start

```
./testMicroFab_START.sh
cd chaincode/ ; gradle build ; cd ..
cd permissionManagerChaincode/ ; gradle build ; cd ..
cd collectionGenerator/ ; mvn install ; cd ..
```
1. Connect to HLF 2.0 network
2. Deploy packaged chaincodes (with collection.json)
3. Invoke chaincodes
