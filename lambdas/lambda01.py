import boto3
import json

SNS_QUEUE_NAME = "kir-uploads-notification-queue"
TOPIC_ARN = "arn:aws:sns:us-east-1:XXXXXXXXXXXX:kir-uploads-notification-topic"
REGION = "us-east-1"
API = "API"
DETAIL_TYPE_PARAM = "detail-type"
RECEIVE_MESSAGES_TIMEOUT = 3
MAX_NUMBER_OF_MESSAGES = 3

sns_client = boto3.client('sns', region_name=REGION)
sqs_client = boto3.client('sqs', region_name=REGION)


def lambda_handler(event, context):
    detail = event.get(DETAIL_TYPE_PARAM, API)

    processed_messages = process_queue_messages()

    print(f"Request Source={detail}; "
          f"Processed Messages count={processed_messages}; "
          f"Handled Request for ARN={TOPIC_ARN}; "
          f"Function Name={context.function_name}; "
          f"Remaining Time in millis={context.get_remaining_time_in_millis()}")

    return {
        "statusCode": 200,
        "body": json.dumps("")
    }


def process_queue_messages():
    queue_url = sqs_client.get_queue_url(QueueName=SNS_QUEUE_NAME)['QueueUrl']

    messages = []
    for _ in range(MAX_NUMBER_OF_MESSAGES):
        response = sqs_client.receive_message(
            QueueUrl=queue_url,
            MaxNumberOfMessages=MAX_NUMBER_OF_MESSAGES,
            WaitTimeSeconds=RECEIVE_MESSAGES_TIMEOUT
        )
        messages.extend(response.get('Messages', []))

    if not messages:
        print("Messages not found. End processing")
        return 0

    formatted_messages = []

    for msg in messages:
        msg_body_json = json.loads(msg['Body'])
        name = msg_body_json['name']
        size = msg_body_json['size']
        url = msg_body_json['url']
        formatted_message = f"\n\n\nFile {name} was uploaded.\nSize: {size}\nUrl: {url} \n\n\n"
        formatted_messages.append(formatted_message)

    delimiter = '========================='
    combined_message = delimiter.join(formatted_messages)
    print(f"Result message = \n\n{combined_message}")

    sns_client.publish(
        TopicArn=TOPIC_ARN,
        Subject="Processed SQS Queue Messages",
        Message=combined_message
    )

    for msg in messages:
        print(f"Deleting message id={msg['MessageId']}")
        sqs_client.delete_message(QueueUrl=queue_url, ReceiptHandle=msg['ReceiptHandle'])

    return len(messages)
