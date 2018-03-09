from clarifai.rest import ClarifaiApp
from clarifai.rest import Image as ClImage
import json

API_KEY = 'ca151274dc0546cdb191aff8fa00ec17'

def predict_image(filename):
    app = ClarifaiApp(api_key=API_KEY)

    model_title = "Universidade de Aveiro"
    model = app.models.get(model_title)
    #prever
    image = ClImage(filename=filename)
    return model.predict([image])
 
