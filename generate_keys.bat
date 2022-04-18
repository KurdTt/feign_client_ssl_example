:: Generate key pair for info client
keytool -genkeypair -keyalg RSA -keysize 2048 -alias info -dname "CN=Info Certificate,OU=3DS,O=Visiona,L=Krakow,C=PL" -ext "SAN:c=DNS:localhost,IP:127.0.0.1" -validity 3650 -keystore info.p12 -storepass changeit -keypass changeit -deststoretype pkcs12

:: Generate key pair for status client
keytool -genkeypair -keyalg RSA -keysize 2048 -alias status -dname "CN=Status Certificate,OU=3DS,O=Visiona,L=Krakow,C=PL" -ext "SAN:c=DNS:localhost,IP:127.0.0.1" -validity 3650 -keystore status.p12 -storepass changeit -keypass changeit -deststoretype pkcs12

:: Create empty truststore
keytool -genkeypair -alias fakealias -storepass changeit -keypass changeit -keystore certs.p12 -dname "CN=Developer, OU=Department, O=Company, L=City, ST=State, C=CA"
keytool -delete -alias fakealias -storepass changeit -keystore certs.p12

:: Import cert from info
keytool -export -keystore info.p12 -alias info -file infoCert.crt -storepass changeit
keytool -importcert -file infoCert.crt -alias info -keystore certs.p12 -storepass changeit -noprompt

:: Import cert from status
keytool -export -keystore status.p12 -alias status -file statusCert.crt -storepass changeit
keytool -importcert -file statusCert.crt -alias status -keystore certs.p12 -storepass changeit -noprompt

:: Create truststore only with cert info
keytool -genkeypair -alias fakealias -storepass changeit -keypass changeit -keystore infocert.p12 -dname "CN=Developer, OU=Department, O=Company, L=City, ST=State, C=CA"
keytool -delete -alias fakealias -storepass changeit -keystore infocert.p12
keytool -importcert -file infoCert.crt -alias info -keystore infocert.p12 -storepass changeit -noprompt

:: Create truststore only with cert info
keytool -genkeypair -alias fakealias -storepass changeit -keypass changeit -keystore statuscert.p12 -dname "CN=Developer, OU=Department, O=Company, L=City, ST=State, C=CA"
keytool -delete -alias fakealias -storepass changeit -keystore statuscert.p12
keytool -importcert -file statusCert.crt -alias status -keystore statuscert.p12 -storepass changeit -noprompt

:: Delete unnecessary files
del statusCert.crt
del infoCert.crt