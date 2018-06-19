from ..functions import with_iam_role
from fabric.api import local


class S3Bucket:
    def __init__(self, aws_region, aws_account_id, environment, bucket_name):
        self.__environment = environment
        self.__aws_account_id = aws_account_id
        self.__aws_region = aws_region
        self.__bucket_name = bucket_name

    def publish_as_public_s3_object(self, source_file, destination_key):
        iam_role = {'account_id': self.__aws_account_id, 'short_name': 'artifacts_publisher', }

        s3_url = 's3://{bucket_name}/{object_key}'.format(
            bucket_name=self.__bucket_name, object_key=destination_key
        )

        s3_command = 'aws s3 --region {region} cp --acl public-read {source_file} {s3_url}'.format(
            region=self.__aws_region, source_file=source_file, s3_url=s3_url
        )

        with_iam_role(lambda: local(s3_command), 'ssrn', iam_role, self.__environment)

    def public_url_for(self, object_key):
        return 'https://s3.amazonaws.com/{bucket_name}/{object_key}'.format(
            bucket_name=self.__bucket_name, object_key=object_key
        )
