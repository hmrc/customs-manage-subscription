# Customs Manage Subscription
 
This microservice notifies a ROSM user with an email regarding the outcome of their subscription (enrolment) with Customs.

# Requirements

This service is written in [Scala](http://www.scala-lang.org/) and [Play](http://playframework.com/), and requires a Java 8 [JRE] to run.

## Endpoints

### POST  /:formBundleId

#### Request headers specification:
HTTP Header | Acceptable value
------------|-----------------
Content-Type | application/json

We don't validate Accept header to allow tax-enrolments callbacks. 


#### Request body specification:
Field Name | Size | Field Type | Description
-----------|------|------------|------------
url | |String | Complete URL (with parameters expanded) which will allow details to be determined about the subscription (if successful) - the form bundle ID. (Not used)
state | 9 | String | Indicates success /failure: ["SUCCEEDED", "ERROR"]
errorResponse | | String | Body of GG/ETMP response in the case of state == "ERROR".  (Not used)

#### Example Request body:
```json
{
    "url": "http://domain:port/tax-enrolments/subscriptions/72138873457",
    "state": "ERROR",
    "errorResponse": "...."
}
```

#### Response code specification:
* **204** If the request is processed successful
* **400** This status code will be returned in case of incorrect data, incorrect data format, missing parameters etc are provided in the request
* **500** In case of a system error such as time out, server down etc, this HTTP status code will be returned
* **405** Any operation other than POST will be responded with "405 Method not allowed" HTTP error


### POST  /handle-subscription

#### Request headers specification:
HTTP Header | Acceptable value
------------|-----------------
Content-Type | application/json
Accept | application/vnd.hmrc.1.0+json
Authorization | Bearer SOME_TOKEN_VALUE

#### Example Request body:
```json
 {
        "formBundleId": "formBundleId",
        "recipienttDetails": {
            "recipientFullName": "fullName",
            "recipientEmailAddress": "a@b.com"
        },
        "sapNumber": "sapNumber"
}
```

#### Response code specification:
* **204** If the request is processed successful
* **400** This status code will be returned in case of incorrect data, incorrect data format, missing parameters etc are provided in the request
* **406** This status code will be returned in case of invalid or missing accept header
* **415** This status code will be returned in case of invalid or missing content type header
* **500** In case of a system error such as time out, server down etc, this HTTP status code will be returned
* **405** Any operation other than POST will be responded with "405 Method not allowed" HTTP error



### PUT  /save4later/:id/:key

#### Request headers specification:
HTTP Header | Acceptable value
------------|-----------------
Content-Type | application/json
Accept | application/vnd.hmrc.1.0+json


#### Example Request body:
```json
 {
       
  "completionDate": "5 May 2017",
  "journey": "GetYourEORI",
  "orgName": "Test Company Name",
  "recipientEmailAddress": "a@b.com",
  "recipientFullName": "Full Name"
 
  }
```

#### Response code specification:
* **201** If the request is processed successful
* **400** This status code will be returned in case of incorrect data, incorrect data format, missing parameters etc are provided in the request
* **404** This status code will be returned in case of empty body 


### GET  /save4later/:id/:key

#### Example Request 
```/save4later/id-1/key-1```

#### Example Response body
```json
{
  "completionDate": "5 May 2017",
  "journey": "GetYourEORI",
  "orgName": "Test Company Name",
  "recipientEmailAddress": "a@b.com",
  "recipientFullName": "Full Name"
}
``` 

#### Response code specification:
* **200** If the request is processed successful
* **404** This status code will be returned in case of id and key not found


### DELETE  /save4later/:id/:key
#### Example Request 
```/save4later/id-1/key-1```

#### Response code specification:
* **204** If the request is processed successful
* **404** This status code will be returned in case of id and key not found

### DELETE  /save4later/:id
#### Example Request 
```/save4later/id-1```

#### Response code specification:
* **204** If the request is processed successful
* **404** This status code will be returned in case of id not found


### POST  /notifyRCM

#### Request headers specification:
HTTP Header | Acceptable value
------------|-----------------
Content-Type | application/json
Accept | application/vnd.hmrc.1.0+json
Authorization | Bearer SOME_TOKEN_VALUE

#### Example Request body:
```json
 {
        "timestamp": "23-May-2019 20:10:10",
        "name": "fullName",
        "email": "test@email.com"
        "eori": "GBXXXXXXXXX000"
}
```

#### Response code specification:
* **204** If the request is processed successful
* **400** This status code will be returned in case of incorrect data, incorrect data format, missing parameters etc are provided in the request
* **406** This status code will be returned in case of invalid or missing accept header
* **415** This status code will be returned in case of invalid or missing content type header
* **500** In case of a system error such as time out, server down etc, this HTTP status code will be returned
* **405** Any operation other than POST will be responded with "405 Method not allowed" HTTP error


# Acronyms

In the context of this application we use the following acronyms and define their 
meanings. Provided you will also find a web link to discover more about the systems
and technology. 

* [API]: Application Programming Interface

* [JRE]: Java Runtime Environment

* [JSON]: JavaScript Object Notation

* [CDS]: Custom Declaration Services 

* [URL]: Uniform Resource Locator


# License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").


[API]: https://en.wikipedia.org/wiki/Application_programming_interface
[JRE]: http://www.oracle.com/technetwork/java/javase/overview/index.html
[JSON]: http://www.json.org/
[CDS]: https://www.gov.uk/government/collections/customs-handling-of-import-and-export-freight-chief-replacement-programme
[URL]: https://en.wikipedia.org/wiki/Uniform_Resource_Locator
