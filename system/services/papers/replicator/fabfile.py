from fabric.api import local, task

from infrastructure.automation.functions import *


@task
def build():
    in_fabfile_directory(lambda: local('gradle clean shadowJar'))


@task
def package(target_package_name='package.tgz'):
    def function():
        return local('tar --create --gzip --dereference --file {target_package_name} '
                     '--transform "s|^fabfile-publication.py$|fabfile.py|" '
                     '--transform "s|^out/libs/||" '
                     'fabfile-publication.py infrastructure out/libs/old-platform-contract-tests.jar'
                     .format(target_package_name=target_package_name))

    in_fabfile_directory(function)
