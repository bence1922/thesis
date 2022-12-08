#!/bin/bash
sudo docker rm -f $(sudo docker ps -a -q | grep 8090)
export MICROFAB_CONFIG='{
    "port": 8090,
    "endorsing_organizations":[
        {
            "name": "MinistryOfJustice"
        },
        {
            "name": "CentralCreditInformationSystem"
        },
        {
            "name": "LeasingCompany"
        },
        {
            "name": "NationalBank"
        }
    ],
    "channels":[
        {
            "name": "channel1",
            "endorsing_organizations":[
                "MinistryOfJustice",
                "CentralCreditInformationSystem",
                "LeasingCompany",
                "NationalBank"
            ]
        }
    ]
}'
sudo docker run --name microfab --rm -ti -p 8090:8090 -e MICROFAB_CONFIG="${MICROFAB_CONFIG}"  ibmcom/ibp-microfab
