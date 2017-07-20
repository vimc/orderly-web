rm /etc/montagu/api/token_key -r

mkdir /etc/montagu/api/token_key

openssl genrsa -out /etc/montagu/api/token_key/mykey.pem 512

openssl pkcs8 -topk8 -inform PEM -outform DER -in  /etc/montagu/api/token_key/mykey.pem -out  /etc/montagu/api/token_key/private_key.der -nocrypt

openssl rsa -in /etc/montagu/api/token_key/mykey.pem -pubout -outform DER -out /etc/montagu/api/token_key/public_key.der
