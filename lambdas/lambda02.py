import json


def lambda_handler(event, context):
    s3_object_key = event['Records'][0]['s3']['object']['key']

    print(f"S3 object created with object key={s3_object_key}")

    return {
        'statusCode': 200,
        'body': json.dumps('S3 object name logged successfully.')
    }
