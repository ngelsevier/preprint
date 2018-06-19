output bastion_iam_instance_profile_name {
  value = "${aws_iam_instance_profile.bastion.name}"
}

output pipeline_automation_server_iam_instance_profile_name {
  value = "${aws_iam_instance_profile.pipeline_automation_server.name}"
}

output ci_agent_iam_instance_profile_name {
  value = "${aws_iam_instance_profile.ci_agent.name}"
}

output production_deployment_agent_iam_instance_profile_name {
  value = "${aws_iam_instance_profile.production_deployment_agent.name}"
}

output qa_deployment_agent_iam_instance_profile_name {
  value = "${aws_iam_instance_profile.qa_deployment_agent.name}"
}

output controller_automation_server_iam_instance_profile_name {
  value = "${aws_iam_instance_profile.controller_automation_server.name}"
}

output controller_agent_iam_instance_profile_name {
  value = "${aws_iam_instance_profile.controller_agent.name}"
}