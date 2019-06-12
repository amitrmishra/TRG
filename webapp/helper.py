from pymongo import MongoClient

HOST = "localhost"
PORT = 27017
USER = "root"
PASSWORD = "example"


class MongoHelper:
	def __init__(self):
		self.client = MongoClient(HOST, PORT, username=USER, password=PASSWORD)
		self.db = self.client.trg
		self.collection = self.db.trg.crimeanalysis
		
	def get_crime_details(self, crime_id):
		result = self.collection.find({"crimeId": crime_id}, {"_id": 0})
		if result.count() == 0:
			return {}
		else:
			return result.next()