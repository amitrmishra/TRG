### Table of contents;
- [ Steps followed ](#steps)
- [ Solution design ](#design)
- [ Running the solution ](#run)
- [ Querying through API ](#query)
- [ Stopping the docker container ](#stop)
- [ Additional information ](#additional)


<a name="steps"></a>
### Steps followed:

 - Downloaded the data files from https://data.police.uk/data/ for the desired date ranges and saved it to data directory
 - Parsed and extracted fields from CSV using Apache Spark
 - Joined the datasets and saved the final structured result in MongoDB
 - Launched a flask web application to serve user requests
 - Created own docker image and hosted it in docker hub for easy distribution

<a name="design"></a>
### Solution design:
![Image](images/Design.jpg)

1. Data Processing

 - We use Apache Spark (2.4) to read the CSV files and join the crime data with outcomes.
 - In case of the outcome files have duplicate crime id, we take the latest one based on the field 'Month'
 - The result of the join is saved to MongoDB using mongodb spark connector

2. MongoDB

 - It stores the crime data analysis result.
 - It is written by spark application and read by the web application

3. Web application

 - It is a flask application which takes the crime id from user and gets the corresponding details from mongo db.


<a name="run"></a>
### Running the solution:

 - Download the Dockerfile and docker-compose.yml from https://github.com/amitrmishra/TRG

 - Launch the docker container

`amitranjan$ docker-compose -f docker-compose.yml up -d`

Output:
```Creating network "trg_default" with the default driver
Creating trg_full_solution_1 ... done
```

<a name="query"></a>
### Querying through API

`amitranjan$ curl http://localhost:5000/98096d1a69205691a56b89c1182eadd6aaf15400ea18da134e0023f20aba5cdb`

Output:
```
{"crimeType": "Criminal damage and arson", "crimeId": "98096d1a69205691a56b89c1182eadd6aaf15400ea18da134e0023f20aba5cdb", "districtName": "avon-and-somerset", "longitude": "-2.513308", "lastOutcome": "Under investigation", "latitude": "51.409456"}
```

`amitranjan$ curl http://localhost:5000/7984cd127f0fa49c7fc6de29e042b51881910a716de1d12c49f7bbe9a809ecd4`

Output:
```
{"crimeType": "Vehicle crime", "crimeId": "7984cd127f0fa49c7fc6de29e042b51881910a716de1d12c49f7bbe9a809ecd4", "districtName": "avon-and-somerset", "longitude": null, "lastOutcome": "Offender given suspended prison sentence", "latitude": null}
```

`amitranjan$ curl http://localhost:5000/008c91375f27d3ec3b21d79e21fe5398accbf230483e8c60f4f63d86e7592f4a`

Output:
```
{"crimeType": "Violence and sexual offences", "crimeId": "008c91375f27d3ec3b21d79e21fe5398accbf230483e8c60f4f63d86e7592f4a", "districtName": "north-yorkshire", "longitude": "-1.084675", "lastOutcome": "Unable to prosecute suspect", "latitude": "53.972721"}
```

### Query using browser

![Image](images/API%20Call.png)

<a name="stop"></a>
### Stopping the docker container
`amitranjan$ docker-compose -f docker-compose.yml down`

Output:
```
Stopping trg_full_solution_1 ... done
Removing trg_full_solution_1 ... done
Removing network trg_default
```

<a name="additional"></a>
### Additional information

1. Building spark application:
  - Checkout project: https://github.com/amitrmishra/TRG/tree/master/dataprocessing
  - In the project home directory, run: `sbt clean assembly` to build the fat jar

2. Installing and configuring mongodb:
  - Create a `/etc/yum.repos.d/mongodb-org-4.0.repo` file with below content
  ```
  [mongodb-org-4.0]
  name=MongoDB Repository
  baseurl=https://repo.mongodb.org/yum/redhat/$releasever/mongodb-org/4.0/x86_64/
  gpgcheck=1
  enabled=1
  gpgkey=https://www.mongodb.org/static/pgp/server-4.0.asc
  ```
  - Install mongodb: `yum install -y mongodb-org`
  - Create directories on local: `mkdir -p /data/db /var/log`
  - Launch mongodb: `mongod --fork --logpath /var/log/mongod.log`
  - Adding a user using mongo shell
  `mongo`
  ```
  use admin
  db.createUser(
    {
      user: "root",
      pwd: "example",
      roles: [ { role: "userAdminAnyDatabase", db: "admin" }, "readWriteAnyDatabase" ]
    }
  )
  ```

3. Running flask web application
  - Install dependencies
  ```
   pip install pymongo
   pip install Flask
  ```
  - Run the application
  ```
  export FLASK_APP=webapp.py
  flask run --host=0.0.0.0
  ```
