'''
Servidor, construído sobre Flask, que expõe uma REST API, cujos serviços se 
aplicam à identificação de imagens e recolha de informação pertinente sobre
o que elas representam (ex: monumentos)
'''

# from app import app
from flask import jsonify, request, Flask, render_template, url_for, send_from_directory, redirect
from flask_bootstrap import Bootstrap
from flask_nav import Nav
from flask_nav.elements import Navbar, Subgroup, View, Link, Text, Separator

import webscrape
from search import search_wiki
from img_utils import writeImage
from prediction import predict_image
import os

IMAGE_FOLDER = './Pictures'
APP_ROOT = os.path.dirname(os.path.abspath(__file__))

app = Flask(__name__)
app.config.from_object('_config')
Bootstrap(app)
nav = Nav(app)

mynav = Navbar('MeetAveiro', 
    View('Home', 'index'),
    View('Stats', 'show_stats'),
    View('Requests', 'show_requests'))
nav.register_element('mynavbar', mynav)

def get_request_files():
    folder = './static/img'
    folder_size = len(folder)
    dict_maps = {}
    for path, subdirs, files in os.walk(folder):
        for name in files:
            concept = path[folder_size+1:]
            if concept not in dict_maps:
                dict_maps[concept] = []
            dict_maps[concept] += [name]
    return dict_maps

def count_elems_dict(dic):
    keys = dic.keys()
    count = 0
    for k in keys:
        count += len(dic[k])
    return count

def process_image_search(query):
    '''
    Extrai informação (a partir da wikimedia API ou por web crawling)
    de acordo com o edificio identificado

    query - nome do edificio
    '''
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
    pending_requests = get_request_files()
    return render_template('index.html', topics=next(os.walk(IMAGE_FOLDER))[1], pending=count_elems_dict(pending_requests))

@app.route('/gallery/<string:query>', methods=['GET'])
def show_gallery(query):
    folder = os.path.join(IMAGE_FOLDER, query)
    images = os.listdir(folder)
    return render_template('gallery.html', topic=images, path=query)

@app.route('/stats', methods=['GET'])
def show_stats():
    return render_template('stats.html')

@app.route('/sendimage/<string:topic>/<string:filename>')
def send_image(filename, topic):
    if topic != 'pending':
        folder = os.path.join(IMAGE_FOLDER, topic)
    else:
        names = filename.split(':')
        folder = os.path.join('./static/img', names[0])
        filename = names[1]
    return send_from_directory(folder,filename)

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
    classification = predict_image('./temp.jpg')
    # print(classification)
    img_name = classification["outputs"][0]["data"]["concepts"][0]["name"]
    # print(img_name.lower())

    folder = os.path.join('./static/img', img_name)
    if not os.path.exists(folder):
        os.makedirs(folder)
    files = os.listdir(folder)

    file_id = str(len(files)) + '.jpg'
    filename = os.path.join(folder, file_id)
    os.rename('./temp.jpg', filename)

    return jsonify({
        'name' : img_name,
        'description' : process_image_search(img_name),
        'id' : file_id
    })

@app.route('/search/feedback', methods=['POST'])
def send_feedback():
    res = request.get_json(force=True)
    file_id = res['id']
    concept = res['concept']
    feedback = res['feedback']
    
    if feedback == 1:
        req_path = os.path.join('./static/img', concept)
        file_path = os.path.join(req_path, file_id)
        dest_path = os.path.join(IMAGE_FOLDER, concept)
        if not os.path.exists(dest_path):
            os.makedirs(dest_path)
        new_id = str(len(os.listdir(dest_path))) + '.jpg'
        new_file_path = os.path.join(dest_path, new_id)
        os.rename(file_path, os.path.join(new_file_path))

    return jsonify({
        'status': 'OK'
    })

@app.route('/resources/delimage', methods=['POST'])
def delete_image():
    res = request.get_json(force=True)
    filename = res['filename']
    concept = res['topic']
    filepath = os.path.join(IMAGE_FOLDER, concept, filename)
    os.remove(filepath)
    return redirect(url_for('show_gallery', query=concept))

@app.route('/resources/upload/<string:topic>', methods=['POST'])
def upload(topic):
    folder = os.path.join(IMAGE_FOLDER, topic)
    for file in request.files.getlist("file"):
        filename = file.filename
        destination = os.path.join(folder, filename)
        # print(destination)
        file.save(destination)
    return redirect(url_for('show_gallery', query=topic))

@app.route('/requests')
def show_requests():
    pending_requests = get_request_files()
    return render_template('pending.html', requests=pending_requests)

@app.route('/requests/manage', methods=['POST', 'DELETE'])
def manage_requests():
    folder = './static/img'
    res = request.get_json(force=True)
    filename = res['filename']
    concept = res['path']
    method = res['operation']
    req_folder = os.path.join(folder, concept, filename)

    if method == 'POST':
        file_desc = filename.split('.')
        dest_folder = os.path.join(IMAGE_FOLDER, concept)
        if not os.path.exists(dest_folder):
            os.makedirs(dest_folder)
        files = os.listdir(dest_folder)
        new_file = str(len(files)) + '.' + file_desc[1]
        os.rename(req_folder, os.path.join(dest_folder, new_file))
    elif method == 'DELETE':
        os.remove(req_folder)
    
    return redirect(url_for('show_requests'))
