from fabric.api import task
from fabric.colors import green, yellow

from automation.functions import *


@task
def generate_gocd_tls_files(aws_region, domain, kms_master_key_arn):
    keystore_password = 'serverKeystorepa55w0rd'

    with lcd(get_fabric_file_directory_path()):
        working_directory_path = local('mktemp -d', capture=True)

        try:
            with lcd(working_directory_path):
                local('sudo openssl req -x509 -nodes -days 365 -newkey rsa:2048 '
                      '-subj "/CN={domain}/O=Elsevier Ltd" '
                      '-keyout go-server.key '
                      '-out go-server.crt'.format(domain=domain))

                local('sudo openssl pkcs12 -passout pass:{password} -inkey go-server.key -in go-server.crt '
                      '-export -out go-server.crt.pkcs12'.format(password=keystore_password))

                local(
                    'keytool -importkeystore -srckeystore go-server.crt.pkcs12 '
                    '-srcstoretype PKCS12 -srcalias 1 -srcstorepass "{keystore_password}" '
                    '-destkeystore go-server-keystore -destalias cruise '
                    '-deststorepass "{keystore_password}" '
                    '-destkeypass "{keystore_password}"'.format(keystore_password=keystore_password)
                )

                local('openssl x509 -in go-server.crt -out go-server.pem -outform PEM')

                kms_encrypted_base64_encoded_keystore_content = kms_encrypt_and_base64_encode_content_in(
                    os.path.join(working_directory_path, 'go-server-keystore'),
                    aws_region,
                    kms_master_key_arn
                )

                base64_encoded_root_certificate_pem_content = local(
                    'awk \'NF {sub(/\r/, ""); printf "%s\\n",$0;}\' go-server.pem | base64 --wrap 0',
                    capture=True
                )

                print green('-----------------------------------------', bold=True)
                print green(' AGENT ROOT CERTIFICATE (BASE64-ENCODED) ', bold=True)
                print green('-----------------------------------------', bold=True)
                print yellow('{}\n'.format(base64_encoded_root_certificate_pem_content))

                print green('------------------------------------------------', bold=True)
                print green(' SERVER KEYSTORE (KMS-ENCRYTED, BASE64-ENCODED) ', bold=True)
                print green('------------------------------------------------', bold=True)
                print yellow('{}\n'.format(kms_encrypted_base64_encoded_keystore_content))
        finally:
            local('rm -rf {working_directory_path}'.format(working_directory_path=working_directory_path))
