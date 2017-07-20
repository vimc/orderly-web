#!/usr/bin/env bash

# create directory if it doesnt already exist
if [ ! -d "/etc/montagu/api/token_key" ] 
then
	mkdir /etc/montagu/api/token_key
fi

# create key if it doesnt already exist
if [ ! -f "/etc/montagu/api/token_key/mykey.pem" ]
then 
	openssl genrsa -out /etc/montagu/api/token_key/mykey.pem 512
fi

# create RSA DER format private key
if [ ! -f "/etc/montagu/api/token_key/private_key.der" ]
then 
openssl pkcs8 -topk8 -inform PEM -outform DER -in  /etc/montagu/api/token_key/mykey.pem -out  /etc/montagu/api/token_key/private_key.der -nocrypt
fi

# create RSA DER format public key
if [ ! -f "/etc/montagu/api/token_key/public_key.der" ]
then 
openssl rsa -in /etc/montagu/api/token_key/mykey.pem -pubout -outform DER -out /etc/montagu/api/token_key/public_key.der
fi

if [ ! -f "app/demo/orderly.sqlite" ]
then
	./gradlew :app:generateTestData
fi

./gradlew :run
