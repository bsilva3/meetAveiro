import requests
import json
from img_utils import readImage
#import time


img = readImage('/home/chico/Pictures/bibs.jpg')

data = {
    "image" : img
}

#print(data)

url = 'http://127.0.0.1:8080/search'

#start = time.time()

response = requests.post(url, data=json.dumps(data))

res = response.json()

print("Name: " + res['name'])
print(res['description'])

#end = time.time()
#print("Time: ")
#print(end - start)
