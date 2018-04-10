'''
Contém métodos para classificar imagens usando a API do Clarifai

predict_image(filename) -> recebe o path para uma imagem e usa a API do
Clarifai para a identificar e classificar
'''

from clarifai.rest import ClarifaiApp
from clarifai.rest import Image as ClImage
import subprocess
import os

API_KEY = 'e25c8e658b6e4594abd9403ee7ea2508' # chave do Bruno

def predict_image_clarifai(filename):
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
 
def predict_image(filename):
    foto_teste = "./temp.jpg"
    cur_dir = os.path.join(os.path.dirname(os.path.abspath(__file__)), foto_teste)
    bashCommand = "./exec.sh " + cur_dir
    process = subprocess.Popen(bashCommand.split(), stdout=subprocess.PIPE)
    output, error = process.communicate()
    temp = ('unknown', 0)
    with open("../../../../tensorflow-for-poets-2/temp.txt", "r") as f:
        for i, line in enumerate(f):
            if i == 3:
                temp = line.split(' ')
                break
    return temp
    

#predict_image('')