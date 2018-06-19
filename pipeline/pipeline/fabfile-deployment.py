from fabric.api import task
from fabric.context_managers import settings

from infrastructure.automation.functions import *


class CiAgent(object):
    DEFAULT_RESOURCES = ['docker']

    def __init__(self, assigned_services):
        self.__assigned_services = assigned_services

    def get_resources(self):
        return map(build_cache_resource_for_service, self.__assigned_services) + CiAgent.DEFAULT_RESOURCES


class EvenSpreadBuildAgentServiceAssignmentStrategy(object):
    def get_services_assigned_to_ci_agent(self, agent_index, services_managed_by_pipeline, ci_agent_count):
        assigned_services_count = self.__get_number_of_services_assigned_to_agent_with_index(
            agent_index,
            services_managed_by_pipeline,
            ci_agent_count
        )

        return map(
            lambda n: self.__get_nth_service_for_agent(agent_index, n, services_managed_by_pipeline, ci_agent_count),
            range(0, assigned_services_count))

    @staticmethod
    def __get_number_of_services_assigned_to_agent_with_index(agent_index, services_managed_by_pipeline,
                                                              ci_agent_count):
        service_count = len(services_managed_by_pipeline)
        minimum_services_per_agent = service_count / ci_agent_count
        remainder_services = service_count % ci_agent_count
        return minimum_services_per_agent + 1 if agent_index < remainder_services else minimum_services_per_agent

    @staticmethod
    def __get_nth_service_for_agent(agent_index, n, services_managed_by_pipeline, ci_agent_count):
        return services_managed_by_pipeline[agent_index + ci_agent_count * n]


class Pipeline(object):
    def __init__(self, ci_agent_count, services_managed_by_pipeline, build_agent_service_assignment_strategy):
        self.__ci_agent_count = ci_agent_count
        self.__services_managed_by_pipeline = services_managed_by_pipeline
        self.__build_agent_service_assignment_strategy = build_agent_service_assignment_strategy
        self.__ci_agents = map(lambda agent_index: self.__create_ci_agent_at_index(agent_index),
                               range(0, self.__ci_agent_count))

    def get_ci_agents(self):
        return self.__ci_agents

    def __create_ci_agent_at_index(self, agent_index):
        assigned_services = self.__build_agent_service_assignment_strategy.get_services_assigned_to_ci_agent(
            agent_index,
            self.__services_managed_by_pipeline,
            self.__ci_agent_count
        )

        return CiAgent(assigned_services)


def build_cache_resource_for_service(service):
    return service + '_build_cache'


@task
def deploy(automation_agent_auto_register_key, automation_agent_root_cert_pem_content,
           automation_server_encrypted_keystore_content, availability_zones, aws_account_id, aws_region, ci_agent_count,
           contact_details, elsevier_cidrs, environment, global_remote_state_s3_bucket, global_remote_state_s3_key,
           global_remote_state_s3_region, public_ssh_key, services_managed_by_pipeline, ssrn_vpc_ip_addresses, vpc_cidr,
           build_monitor_login_credentials, dry_run='yes'):
    pipeline = Pipeline(
        int(ci_agent_count),
        services_managed_by_pipeline.split(','),
        EvenSpreadBuildAgentServiceAssignmentStrategy()
    )

    ci_agent_resources = [','.join(resources) for resources in
                          [ci_agent.get_resources() for ci_agent in pipeline.get_ci_agents()]]
    configuration = {
        'aws': {
            'iam_role': {
                'account_id': aws_account_id,
                'short_name': 'pipeline_deployer',
            }
        },
        'environment': environment,
        'product': 'ssrn_pipeline',
        'terraform': {
            's3_remote_state': {
                'region': aws_region,
                'bucket': 'elsevier-ssrn_pipeline-{environment}'.format(environment=environment),
                'key': 'pipeline.tfstate'
            },
            'variables': {
                'ansible_role_versions': terraform_command_line_map_variable_from({
                    'aptitude_package_recipient': '${APTITUDE_PACKAGE_RECIPIENT_ANSIBLE_ROLE_VERSION}',
                    'automater': '${AUTOMATER_ANSIBLE_ROLE_VERSION}',
                    'automation_agent': '${AUTOMATION_AGENT_ANSIBLE_ROLE_VERSION}',
                    'automation_server': '${AUTOMATION_SERVER_ANSIBLE_ROLE_VERSION}',
                    'aws_api_client': '${AWS_API_CLIENT_ANSIBLE_ROLE_VERSION}',
                    'aws_ssh_server': '${AWS_SSH_SERVER_ANSIBLE_ROLE_VERSION}',
                    'deployer': '${DEPLOYER_ANSIBLE_ROLE_VERSION}',
                    'container_factory': '${CONTAINER_FACTORY_ANSIBLE_ROLE_VERSION}',
                    'frontend_project_builder': '${FRONTEND_PROJECT_BUILDER_ANSIBLE_ROLE_VERSION}',
                    'ssrn_system_simulator': '${SSRN_SYSTEM_SIMULATOR_ANSIBLE_ROLE_VERSION}',
                    'java_project_builder': '${JAVA_PROJECT_BUILDER_ANSIBLE_ROLE_VERSION}',
                    'browser_test_runner': '${BROWSER_TEST_RUNNER_ANSIBLE_ROLE_VERSION}',
                    'long_running_host': '${LONG_RUNNING_HOST_ANSIBLE_ROLE_VERSION}',
                    'clock_synchronization_host': '${CLOCK_SYNCHRONIZATION_HOST_ANSIBLE_ROLE_VERSION}'
                }),
                'automation_agent_auto_register_key': automation_agent_auto_register_key,
                'automation_agent_root_cert_pem_content': automation_agent_root_cert_pem_content,
                'automation_server_encrypted_keystore_content': automation_server_encrypted_keystore_content,
                'availability_zones': terraform_command_line_list_variable_from(availability_zones.split(',')),
                'build_monitor_login_credentials': build_monitor_login_credentials,
                'ci_agent_count': ci_agent_count,
                'ci_agent_resources': terraform_command_line_list_variable_from(ci_agent_resources),
                'aws_region': aws_region,
                'contact_details': contact_details,
                'elsevier_cidrs': terraform_command_line_list_variable_from(elsevier_cidrs.split(',')),
                'environment': environment,
                'global_remote_state_s3_bucket': global_remote_state_s3_bucket,
                'global_remote_state_s3_key': global_remote_state_s3_key,
                'global_remote_state_s3_region': global_remote_state_s3_region,
                'public_ssh_key': public_ssh_key,
                'ssrn_vpc_ip_addresses': terraform_command_line_list_variable_from(ssrn_vpc_ip_addresses.split(',')),
                'vpc_cidr': vpc_cidr
            }
        }
    }

    deploy_to_aws_with_iam_role(configuration, (dry_run.lower() != 'no' and dry_run.lower() != 'n'))
