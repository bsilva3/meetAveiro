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

url = 'http://127.0.0.1:8080/search' # url do serviço

#start = time.time()

# pedido RESTful e tratamento da resposta
response = requests.post(url, data=json.dumps(data))

res = response.json()

print("Name: " + res['name'])
print(res['description'])

#end = time.time()
#print("Time: ")
#print(end - start)
