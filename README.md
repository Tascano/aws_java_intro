
# AWS Java Project

This project demonstrates a simple Java-based AWS Lambda function that processes a CSV file from S3 and stores the data in DynamoDB.

## Steps to Deploy

1. **Create an S3 bucket** and upload your CSV file.
2. **Create a DynamoDB table** with the appropriate schema. Name it `YourDynamoDBTableName` or modify the Lambda code accordingly.
3. **Create an IAM role** for the Lambda function with permissions to access S3 and DynamoDB. Use the provided `iam_policy.json` file for the necessary permissions.
4. **Deploy the Lambda function using the AWS CLI**:
   - Package your code with Maven: `mvn clean package`
   - Deploy the package using AWS CLI: `aws lambda create-function --function-name csvProcessor --runtime java11 --role [Role ARN] --handler com.example.lambda.LambdaCSVProcessor::handleRequest --zip-file fileb://target/csv-processor-1.0-SNAPSHOT-jar-with-dependencies.jar`
5. **Configure the Lambda function** to trigger on new file uploads in the S3 bucket. This can be done via the AWS Management Console or AWS CLI.

Note: Replace `[Role ARN]` with the ARN of the IAM role you created.
