'''
Exemplo de um cliente RESTful para a API exposta pelo servidor
criado em server.py
'''

import requests
import json
from img_utils import readImage
#import time

#################################################
# Cliente para testar funcionamento da REST API #
#################################################

img = readImage('/home/chico/Pictures/bibs.jpg') # converter imagem para string base64

# json para o pedido
data = {
    "image" : img
}

#print(data)

url = 'http://192.168.193.213:8080/search' # url do serviço

#start = time.time()

# pedido RESTful e tratamento da resposta
response = requests.post(url, data=json.dumps(data))

res = response.json()

name = res['name']
f_id = res['id']
print("Name: " + res['name'])
print(res['description'])

data = {
    'id' : f_id,
    'concept' : name,
    'feedback' : 1
}

url = 'http://192.168.193.213:8080/search/feedback' # url do serviço

response = requests.post(url, data=json.dumps(data))

res = response.json()

print('DONE')

#end = time.time()
#print("Time: ")
#print(end - start)
