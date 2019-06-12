from flask import Flask
import json
from helper import MongoHelper

app = Flask(__name__)

mongo_helper = MongoHelper()

@app.route('/')
def default():
	return 'Pass the crimeId to get details'


@app.route('/<crimeId>')
def get_crime_details(crimeId):
	try:
		result_dict = mongo_helper.get_crime_details(crimeId)
		if result_dict:
			expected_output_keys = ['crimeType', 'crimeId', 'districtName', 'longitude', 'lastOutcome', 'latitude']
			for key in expected_output_keys:
				if key not in result_dict:
					result_dict[key] = None
		return json.dumps(result_dict)
	except Exception as e:
		return 'Got server side exception: %s' % (str(e))


if __name__ == '__main__':
    app.run()