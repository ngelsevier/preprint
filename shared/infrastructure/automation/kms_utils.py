import boto3
import base64
import getpass


def get_password():
    pprompt = lambda: (getpass.getpass(), getpass.getpass('Retype password: '))

    p1, p2 = pprompt()
    while p1 != p2:
        print('Passwords do not match. Try again')
        p1, p2 = pprompt()

    return p1


def kms_encrypt(key_id):
    session = boto3.session.Session()

    kms = session.client('kms')

    password = get_password()
    stuff = kms.encrypt(KeyId=key_id, Plaintext=password)
    binary_encrypted = stuff[u'CiphertextBlob']
    encrypted_password = base64.b64encode(binary_encrypted)
    print(encrypted_password.decode())


def kms_decrypt(encrypted_password):
    session = boto3.session.Session()
    kms = session.client('kms')
    binary_data = base64.b64decode(encrypted_password)
    meta = kms.decrypt(CiphertextBlob=binary_data)
    plaintext = meta[u'Plaintext']
    print(plaintext.decode())
