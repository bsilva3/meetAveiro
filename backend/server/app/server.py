# from app import app
from flask import jsonify, request, Flask
import webscrape
from search import search_wiki
from img_utils import writeImage
from prediction import predict_image
import os

app = Flask(__name__)

def process_image_search(query):
    message = 'not found'
    if query in webscrape.urls.keys():
        message = webscrape.process_search(query)
    else:
        if query == 'reitoria':
            query = 'Universidade de Aveiro'
        message = search_wiki(query)
    return message

@app.route('/')
@app.route('/index')
def index():
    return jsonify({ 'message' : 'Hello World' })

@app.route('/search/<string:query>', methods=['GET'])
def do_search(query):
    message = process_image_search(query)
    return jsonify({
        'name' : query,
        'description' : message
    })

@app.route('/search', methods=['POST'])
def classify_image():
    res = request.get_json(force=True)
    image = res['image']
    writeImage(image)
    #classification = predict_image('/home/chico/Pictures/temp.jpg')
    classification = predict_image(os.getcwd()+"/temp.jpg")
    print(classification)
    img_name = classification["outputs"][0]["data"]["concepts"][0]["name"]
    print(img_name.lower())
    return jsonify({
        'name' : img_name,
        'description' : process_image_search(img_name.lower())
    })

if __name__ == '__main__':
    app.run(debug=True, port=8080, host="0.0.0.0")
