# crosscheck
##Why
- Requirements: From reports sent from Adyen (CSV format), we need to
1. Cross-check the transactions amount and
2. Return the transactions which are not matched with our data
3. Calculate and return the settlement amounts to be received by each side (MxPay/Adyen/Patners) for each matched transaction.

##What
- Create an web access point for users to upload csv report files
- Output: The reponse which should be in RESTful (JSON) format contains:
1. The unmatched transaction list with their reference id and the reasons why those transactions were not matched.
2. The settlement amounts of matched transactions
3. The csv result file with 2 additional columns: is_matched and reason
4. The results should be displayed on UI.

{
"unmatched_transactions": {
"transaction 1": "reason...",
"transaction 2": "reason...",
...
},
"settlement": {
"adyen": { "total": ..., "transactions":{"transaction 1": ..., "transaction 2": ..., ...},
"partners": { "total": ..., "transactions":{"transaction 1": ..., "transaction 2": ..., ...},
"mxpay": { "total": ..., "transactions":{"transaction 1": ..., "transaction 2": ..., ...}
}
},
"result_file": "/path/result_file.csv"
}


##How
1. Create DB immigration file to create two additional columns for Transactions table: is_matched and reason
2. Create a web form to upload files.
3. Create POST request send the uploaded files to the server
4. Process the csv files to build custom queries
- Find the unmatced transactions and reasons
- Calculate settlement amounts for matched transactions
5. Create and return the results in JSON.