FROM amitrmishra/trg:latest
EXPOSE 5000

CMD ["sh", "-c", "mongod --fork --logpath /var/log/mongod.log; /opt/spark/bin/spark-submit --class amit.trg.Solution --master local[*] /trg/dataprocessing.jar /trg/data; cd /trg/webapp; export FLASK_APP=webapp.py; flask run --host=0.0.0.0"]
