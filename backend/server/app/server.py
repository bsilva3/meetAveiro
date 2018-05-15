'''
Servidor, construído sobre Flask, que expõe uma REST API, cujos serviços se 
aplicam à identificação de imagens e recolha de informação pertinente sobre
o que elas representam (ex: monumentos)
'''
import sys
sys.path.append('../../../database')
from models import *


import datetime
import requests
from flask import jsonify, request, Flask, render_template, url_for, send_from_directory, redirect
from flask_bootstrap import Bootstrap
from flask_nav import Nav
from flask_nav.elements import Navbar, Subgroup, View, Link, Text, Separator

from webscrape import search_turismo, process_search, urls
from search import search_wiki
from img_utils import writeImage
from prediction import predict_image

import os
import shutil
import atexit
from apscheduler.schedulers.background import BackgroundScheduler
from apscheduler.triggers.interval import IntervalTrigger

#import firebase_admin
#from firebase_admin import db
import flask

IMAGE_FOLDER = '../../../../treino'
APP_ROOT = os.path.dirname(os.path.abspath(__file__))

app = Flask(__name__)
app.config.from_object('_config')
db.init_app(app)
Bootstrap(app)
nav = Nav(app)

mynav = Navbar('MeetAveiro', 
    View('Home', 'index'),
    View('Stats', 'show_stats'),
    View('Requests', 'show_requests'))
nav.register_element('mynavbar', mynav)


infos = {
    'biblioteca': 'Localizada no centro do Campus Universitário da UA, a Biblioteca da Universidade de Aveiro constitui-se como um agradável local para leitura, estudo e pesquisa acessível a toda a comunidade académica. Nela são disponibilizados os necessários recursos informativos que servem de suporte ao ensino, aprendizagem e à investigação na Universidade de Aveiro. ',
    'reitoria': 'A Universidade de Aveiro (UA) é um estabelecimento de ensino superior público em Portugal, sediado na cidade de Aveiro. Criada em 1973, num contexto de expansão e renovação do ensino superior em Portugal, a UA logo se transformou numa universidade de referência devido à alta qualidade da sua investigação, do seu corpo docente e das suas infraestruturas.',
    'cantina': 'O edifício onde funciona o Refeitório de Santiago compreende um projecto da autoria do arquitecto Rebello de Andrade (constituído por uma área de 1200 metros quadrados). O Refeitório de Santiago, constituído por duas salas de 400 lugares cada, situa-se no edifício central dos Serviços de Acção Social, onde podem ser fornecidas cerca de 4.000 refeições por dia.',
    'deti': 'O Departamento de Eletrónica, Telecomunicações e Informática (DETI) foi fundado em 1974, com o nome de Departamento de Eletrónica e Telecomunicações, tendo sido um dos primeiros departamentos a iniciar atividade após a criação da Universidade de Aveiro em 1973. Em 2006 foi alterada a sua designação por forma a espelhar a atividade existente no Departamento na área da Informática.',
    'ieeta': 'IEETA is a Computer Science and Engineering / Electronics and Electrical Engineering research unit with 29.55 full-time equivalent (FTE) members with PhD. It is organized in three Groups, two of them more application oriented (Biomedical Informatics and Technologies, and Intelligent Robotics and Systems), the other one of a more fundamental nature (Information Systems and Processing), mapping the major scientific areas of activity of its researchers. Empowered by its internal diversity and strong collaborative environment, IEETA has been able to provide important contributions in problems that require a high level of multidisciplinarity.',
    'complexo pedagógico' : 'Complexo Pedagógico, Tecnológico e Científico da Universidade de Aveiro'
}

#firebase_admin.initialize_app(options={
#    'databaseURL': 'https://<DB_NAME>.firebaseio.com'
#})

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
    if query in urls.keys():
        message = process_search(query)
    else:
        if query == 'reitoria':
            query = 'Universidade de Aveiro'
        message = search_wiki(query)
    return message


@app.route('/')
@app.route('/index')
def index():
    pending_requests = get_request_files()
    return render_template('index.html', topics=next(os.walk(IMAGE_FOLDER))[1], 
        pending=count_elems_dict(pending_requests))

@app.route('/gallery/<string:query>', methods=['GET'])
def show_gallery(query):
    folder = os.path.join(IMAGE_FOLDER, query)
    images = os.listdir(folder)
    return render_template('gallery.html', topic=images, path=query)

@app.route('/stats', methods=['GET'])
def show_stats():
    return render_template('stats.html',
                           totalusers = nTotalUsers(),
                           totalAdmin=10,
                           totalTuristas = 80,
                           totalconcepts = nTotalConcepts(),
                           totalPaths = nTotalPath(),
                           conceitos = infoConceitos(),
                           percursos = infoPercursos(),
                           totalfotos = nTotalFotos())

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
    user_email = res['user']
    lat = res['lat']
    lon = res['long']
#    data = res['date']
    print("Request received")
    writeImage(image)
    print("Calling tensorflow.....")
    classification = predict_image('./temp.jpg')
    img_name = classification[0]
    score = classification[1]
    print(img_name, score)
    folder = os.path.join('./static/img', img_name)
    if not os.path.exists(folder):
        os.makedirs(folder)
    files = os.listdir(folder)
    print("Folder created")
    file_id = str(len(files)) + '.jpg'
    filename = os.path.join(folder, file_id)
    os.rename('./temp.jpg', filename)
    print("Imagem gravada")
    desc = ''

    try:
        desc = infos[img_name.lower()]
    except KeyError:
        desc = img_name
    while(True):
        print("Looping...")
        foto = addFotografia(None, "Biblioteca, Universidade de Aveiro", user_email, lat, lon, filename,
                        None, datetime.datetime.now(), None, 'pending', score, None)
        if foto is None:
            continue
        else:
            break
    print("Enviando resposta...")
    if float(score) >= 0.8:
        return jsonify({
            'name' : img_name,
            'description' : desc,
            'id' : foto.id
        })
    return jsonify({
        'name': 'Desconhecido',
        'description': '',
        'id': foto.id
    })

@app.route('/search/feedback', methods=['POST'])
def send_feedback():
    res = request.get_json(force=True)
    file_id = res['image_id']
    concept = res['concept']
    feedback = res['answer']
    print('feedback ' + str(feedback))
    if feedback == 1:
        #req_path = os.path.join('./static/img', concept)
        #file_path = os.path.join(req_path, file_id)
        foto = getFoto(file_id)
        file_path = foto.path
        dest_path = os.path.join(IMAGE_FOLDER, concept)
        if not os.path.exists(dest_path):
            os.makedirs(dest_path)
        new_id = str(len(os.listdir(dest_path))) + '.jpg'
        new_file_path = os.path.join(dest_path, new_id)
        os.rename(file_path, os.path.join(new_file_path))
        updateFotografia(file_id, None, new_file_path)
        print('feedback')
    return jsonify({
        'status': str(feedback)
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

@app.route('/resources/events', methods=['GET'])
def get_events():
    with open('./static/results/events.txt') as fp:
        events = fp.read()
    return events

@app.route('/resources/topics', methods=['POST'])
def create_topic():
    if request.method == "POST":
        topic = request.form['topic']
        dest_folder = os.path.join(IMAGE_FOLDER, topic)
        if not os.path.exists(dest_folder):
            os.makedirs(dest_folder)
            addConceito(topic, 'admin@admin.pt')
    return redirect(url_for('index'))

@app.route('/resources/topics/manage', methods=['POST'])
def manage_topic():
    res = request.get_json(force=True)
    method = res['method']
    topic = res['topic']
    if method == 'GET':
        print(topic)
        return jsonify({
                'url': url_for('show_gallery', query=topic)
            })
    elif method == 'POST':
        dest_folder = os.path.join(IMAGE_FOLDER, topic)
        if os.path.exists(dest_folder):
            shutil.rmtree(dest_folder, ignore_errors=True)
            deleteConceito(topic)
        return redirect(url_for('index'))


@app.route('/requests')
def show_requests():
    pending_requests = get_request_files()
    return render_template('pending.html', requests=pending_requests, 
        topics=next(os.walk(IMAGE_FOLDER))[1])

@app.route('/requests/change', methods=['POST'])
def change_request():
    folder = './static/img'
    res = request.get_json(force=True)
    filename = res['filename']
    concept = res['path']
    old = res['old']
    req_folder = os.path.join(folder, old, filename)

    file_desc = filename.split('.')
    dest_folder = os.path.join(IMAGE_FOLDER, concept)
    if not os.path.exists(dest_folder):
        os.makedirs(dest_folder)
    files = os.listdir(dest_folder)
    new_file = str(len(files)) + '.' + file_desc[1]
    new_path = os.path.join(dest_folder, new_file)
    os.rename(req_folder, new_path)

    updateFotoByPath(req_folder, new_path)

    return redirect(url_for('show_requests'))

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
        new_path = os.path.join(dest_folder, new_file)
        os.rename(req_folder, new_path)
        updateFotoByPath(req_folder, new_path)
    elif method == 'DELETE':
        os.remove(req_folder)
        deleteFoto(req_folder)
    return redirect(url_for('show_requests'))


@app.route('/resources/routes', methods=['POST'])
def receive_routes():
    res = request.get_json(force=True)
    email = res['user']
    title = res['title']
    start = res['start']
    print('Datas hooray')
    end = res['end']
    description = res['description']
    markers = res['markers']
    trajectory = res['trajectory']

    print('Recebido')
    percurso = addPercurso(email, title, 1, description)
    instancia = addInstanciaPercurso(email, percurso.id, start, end)
    print('Percurso criado')
    marks = markers.split(',')

    for m in marks:
        foto = getFoto(m)
        addPonto(foto.latitude, foto.longitude, percurso.id)
        foto.idinstperc = instancia.id

    
    print('Markers')
    trajs = trajectory.split(';')
    for coord in trajs:
        c = coord.split(',') 
        lat = c[0]
        lon = c[1]
        addPonto(lat, lon, percurso.id)

    print('Trajectory')

    print('Sending answer...')
    
    return jsonify({
        'route': percurso.id,
        'inst': instancia.id
    })



# Background tasks
scheduler = BackgroundScheduler()
scheduler.start()
scheduler.add_job(
    func=search_turismo,
    trigger=IntervalTrigger(minutes=60),
    id='search_turismo',
    name='Saves events to a file every 60 minutes',
    replace_existing=True)
# Shut down the scheduler when exiting the app
atexit.register(lambda: scheduler.shutdown())

