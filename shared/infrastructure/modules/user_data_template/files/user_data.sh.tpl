#! /usr/bin/env bash

exec > >(tee /var/log/user_data.log) 2>&1

set -o pipefail -o nounset -o errexit

echo "Running user data script..."

err_report() {
    echo "Error on line $${1}"
}

trap 'err_report $${LINENO}' ERR

function install_ansible {
    echo "Installing Ansible..."
    apt-get install -y software-properties-common
    apt-add-repository -y ppa:ansible/ansible
    apt-get update -y
    apt-get install -y ansible
}

function provision_using_ansible {
    local temporary_directory="$(mktemp -d)"

    pushd "$${temporary_directory}"

    echo "Writing Ansible requirements file..."
    cat > requirements.yml <<'EOL'
${ansible_requirements_file_content}
EOL

    echo "Installing required Ansible roles..."
    ansible-galaxy install --roles-path=roles --role-file=requirements.yml

    echo "Writing Ansible playbook..."
    cat > playbook.yml <<'EOL'
${ansible_playbook_file_content}
EOL

    echo "Running Ansible playbook..."
    # HOME environment variable set as a workaround for a bug detailed in https://github.com/ansible/ansible/issues/21562 and https://github.com/ansible/ansible/issues/20332
    HOME=/root PYTHONUNBUFFERED=1 ANSIBLE_NOCOLOR=true ansible-playbook \
    -vvv \
    --inventory="localhost," \
    --connection=local \
    --become \
    --become-method=sudo \
    playbook.yml

    popd
}

function clean_up_aptitude_resources {
    echo "Cleaning up aptitude resources..."
    apt-get -y autoremove
    apt-get clean
}

install_ansible
provision_using_ansible
clean_up_aptitude_resources

echo "User data script completed successfully"