#!/bin/bash
sudo docker rm -f $(sudo docker ps -a -q | grep 8091)
export MICROFAB_CONFIG='{
    "port": 8091,
    "endorsing_organizations":[
        {
            "name": "Org1"
        },
        {
            "name": "Org2"
        },
        {
            "name": "Org3"
        }
    ],
    "channels":[
        {
            "name": "testchannel",
            "endorsing_organizations":[
                "Org1",
                "Org2",
                "Org3"
            ]
        }
    ]
}'
sudo docker run --name decison --rm -ti -p 8091:8091 -e MICROFAB_CONFIG="${MICROFAB_CONFIG}"  ibmcom/ibp-microfab
