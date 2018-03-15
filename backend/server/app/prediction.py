'''
Contém métodos para classificar imagens usando a API do Clarifai

predict_image(filename) -> recebe o path para uma imagem e usa a API do
Clarifai para a identificar e classificar
'''

from clarifai.rest import ClarifaiApp
from clarifai.rest import Image as ClImage
import json

API_KEY = 'ca151274dc0546cdb191aff8fa00ec17' # chave do Bruno

def predict_image(filename):
    '''
    Usa a API do Clarifai para identificar o conceito apresentado numa imagem

    filename - path para o ficheiro de imagem a enviar para a API
    '''
    app = ClarifaiApp(api_key=API_KEY)

    model_title = "Universidade de Aveiro"
    model = app.models.get(model_title)
    #prever
    image = ClImage(filename=filename)
    return model.predict([image])
 
