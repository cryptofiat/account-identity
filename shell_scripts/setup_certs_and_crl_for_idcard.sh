sudo mkdir /etc/apache2/certs
sudo mkdir /etc/apache2/certs/eid
sudo chown root:root /etc/apache2/certs/eid
sudo chmod 750 /etc/apache2/certs/eid

# NB: Review https://sk.ee/en/repository/certs/ to make sure you download all the necessary certificates
wget https://sk.ee/upload/files/EE_Certification_Centre_Root_CA.pem.crt
wget https://sk.ee/upload/files/ESTEID-SK_2011.pem.crt
wget https://sk.ee/upload/files/ESTEID-SK_2015.pem.crt
cat EE_Certification_Centre_Root_CA.pem.crt ESTEID-SK_2011.pem.crt ESTEID-SK_2015.pem.crt > esteid-ca.crt
rm EE_Certification_Centre_Root_CA.pem.crt ESTEID-SK_2011.pem.crt ESTEID-SK_2015.pem.crt

sudo mv esteid-ca.crt /etc/apache2/certs/eid/
sudo chown root:root /etc/apache2/certs/eid/esteid-ca.crt
sudo chmod 640 /etc/apache2/certs/eid/esteid-ca.crt

sudo mkdir /etc/apache2/certs/eid/crl
sudo chown root:root /etc/apache2/certs/eid/crl
sudo chmod 750 /etc/apache2/certs/eid/crl

wget https://sk.ee/crls/eeccrca/eeccrca.crl
wget https://sk.ee/repository/crls/esteid2011.crl
wget https://sk.ee/crls/esteid/esteid2015.crl
openssl crl -in eeccrca.crl -out eeccrca.crl -inform DER
openssl crl -in esteid2011.crl -out esteid2011.crl -inform DER
openssl crl -in esteid2015.crl -out esteid2015.crl -inform DER
sudo ln -s eeccrca.crl `openssl crl -hash -noout -in eeccrca.crl`.r0
sudo ln -s esteid2011.crl `openssl crl -hash -noout -in esteid2011.crl`.r0
sudo ln -s esteid2015.crl `openssl crl -hash -noout -in esteid2015.crl`.r0
sudo chown root:root *.crl
sudo chmod 640 *.crl
sudo chown root:root *.r0
sudo chmod 640 *.r0
sudo mv *.crl /etc/apache2/certs/eid/crl
sudo mv *.r0 /etc/apache2/certs/eid/crl

# pscp -i SSLKEYFILE C:\XXX\certs\eid\renew_crl.sh ec2-user@SERVERIP:/home/ec2-user/
# sudo mv /home/ec2-user/renew_crl.sh /etc/apache2/certs/eid/
# sudo chown root:root /etc/apache2/certs/eid/renew_crl.sh
# sudo chmod 750 /etc/apache2/certs/eid/renew_crl.sh

# pscp -i SSLKEYFILE C:\XXX\certs\eid\update-id-card-crl ec2-user@SERVERIP:/home/ec2-user/
# sudo mv /home/ec2-user/update-id-card-crl /etc/cron.d/
# sudo chown root:root /etc/cron.d/update-id-card-crl
# sudo chmod 644 /etc/cron.d/update-id-card-crl

