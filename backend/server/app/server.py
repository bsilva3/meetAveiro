'''
Servidor, construído sobre Flask, que expõe uma REST API, cujos serviços se 
aplicam à identificação de imagens e recolha de informação pertinente sobre
o que elas representam (ex: monumentos)
'''
import sys, shutil, subprocess
sys.path.append('../../../database')
from models import *
from math import sqrt

import random
import datetime
import requests
from flask import jsonify, request, Flask, render_template, url_for, send_from_directory, redirect
from flask_bootstrap import Bootstrap
from flask_nav import Nav
from flask_nav.elements import Navbar, Subgroup, View, Link, Text, Separator

from webscrape import search_turismo, process_search, urls
from search import search_wiki
from img_utils import writeImage, readImage
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
app.config['JSON_AS_ASCII'] = False
db.init_app(app)
Bootstrap(app)
nav = Nav(app)

mynav = Navbar('MeetAveiro', 
    View('Home', 'index'),
    View('Stats', 'show_stats'),
    View('Requests', 'show_requests'))
nav.register_element('mynavbar', mynav)


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

@app.route('/resources/retrain', methods=['POST'])
def retrain():
    req = request.get_json(force=True)
    print('OK')
    subprocess.call('./retrain.sh')
    print('Done')
    pending_requests = get_request_files()
    return render_template('index.html', topics=next(os.walk(IMAGE_FOLDER))[1], 
        pending=count_elems_dict(pending_requests))
    

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

@app.route('/resources/topics', methods=['POST'])
def create_topic():
    if request.method == "POST":
        topic = request.form['topic']
        dest_folder = os.path.join(IMAGE_FOLDER, topic)
        if not os.path.exists(dest_folder):
            os.makedirs(dest_folder)
            addConceito(topic, 'admin@ua.pt')
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


#######################################################
######################################################
###################### API ##########################
#######################################################
#######################################################

@app.route('/search', methods=['POST'])
def classify_image():
    res = request.get_json(force=True)
    image = res['image']
    user_email = res['user']
    lat = res['lat']
    lon = res['long']
    # data = res['date']
    print("Request received")
    writeImage(image)
    print("Calling tensorflow.....")
    classification = predict_image('./temp.jpg')
    img_name = classification[0]
    print('Conceito: ' + img_name)
    conceito = db.session.query(Conceito).get(img_name)
    score = classification[1]

    if float(score) < 0.8:
        img_name = 'desconhecido'

    conc_lat = conceito.latitude
    conc_long = conceito.longitude
    raio = conceito.raio

    if sqrt((lat-conc_lat)**2 + (lon-conc_long)**2) > raio:
        img_name = 'desconhecido'

    
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

    while(True):
        print("Looping...")
        foto = addFotografia(None, img_name, user_email, lat, lon, filename,
                        None, datetime.datetime.now(), None, 'pending', score, None)
        if foto is None:
            continue
        else:
            break
    print("Enviando resposta...")
    if img_name != 'desconhecido':
        return jsonify({
            'concept_id' : img_name,
            'name': conceito.nome,
            'description' : conceito.descricao,
            'id' : foto.id
        })
    return jsonify({
        'concept_id': 'desconhecido',
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



@app.route('/resources/events', methods=['GET'])
def get_events():
    with open('./static/results/events.txt') as fp:
        events = fp.read()
    return events


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


@app.route('/resources/routes/byuser', methods=['POST'])
def get_routes():
    req = request.get_json(force=True)
    email = req['user']
    routes = db.session.query(Percurso).filter(Percurso.emailc == email).all()
    if len(routes) == 0:
        return jsonify({
            "routes": []
        })
    res = []
    for r in routes:
        temp = {}
        temp['id'] = r.id
        temp['title'] = r.titulo
        temp['description'] = r.descricao
        res.append(temp)
    return jsonify({
        'routes': res
    })


@app.route('/resources/routes/<int:id>', methods=['GET'])
def get_specific_route(id):
    percurso = db.session.query(Percurso).get(id)
    if percurso is None:
        return jsonify({})
    pontos = db.session.query(Ponto).filter(Ponto.idperc == id).all()
    res = {}
    
    res['title'] = percurso.titulo
    res['description'] = percurso.titulo
    pnts = []
    for p in pontos:
        temp = {}
        temp['latitude'] = p.latitude
        temp['longitude'] = p.longitude
        pnts.append(temp)
    res['trajectory'] = pnts
    return jsonify(res)

@app.route('/resources/routes/instances', methods=['POST'])
def get_route_instances():
    req = request.get_json(force=True)
    email = req['user']
    instances = db.session.query(InstanciaPercurso).filter(InstanciaPercurso.emailc == email).all()
    if len(instances) == 0:
        return jsonify({
            'instances': []
        })
    res = []
    for i in instances:
        temp = {}
        route = db.session.query(Percurso).get(i.idperc)
        temp['route'] = route.titulo
        temp['start'] = i.datainicio
        temp['end'] = i.datafim
        temp['rating'] = i.classificacao
        temp['id'] = i.id
        res.append(temp)

    return jsonify({
        'instances': res
    })

@app.route('/resources/routes/instances/<int:id>', methods=['GET'])
def get_route_instance(id):
    instance = db.session.query(InstanciaPercurso).get(id)
    if instance is None:
        return jsonify({})
    route = db.session.query(Percurso).get(instance.idperc)

    res = {}
    res['title'] = route.titulo
    res['description'] = route.descricao

    fotos = db.session.query(Fotografia).filter(Fotografia.idinstperc==id).all()

    fotografias = []
    for f in fotos:
        try:
            foto = {}
            readImage(f.path)
            if './static' in f.path:
                temp = f.path.replace('./static/img/', '')
                temp = temp.split('/')
                foto['img'] = 'http://192.168.160.192:8080/sendimage/pending/' + str(temp[0]+':'+temp[1])
            else:
                temp = f.path.replace('../', '')
                temp = temp.replace('treino/', '')
                foto['img'] = 'http://192.168.160.192:8080/sendimage/' + temp
            foto['latitude'] = f.latitude
            foto['longitude'] = f.longitude
            foto['id'] = f.id
            foto['date'] = f.datafoto
            fotografias.append(foto)
        except:
            print('Could not find: ' + f.path)

    res['markers'] = fotografias
    pontos = db.session.query(Ponto).filter(Ponto.idperc == route.id).all()

    pnts = []
    for p in pontos:
        temp = {}
        temp['latitude'] = p.latitude
        temp['longitude'] = p.longitude
        pnts.append(temp)

    res['trajectory'] = pnts    

    return jsonify(res)

@app.route('/resources/atractions/<string:id>', methods=['GET'])
def get_atraction(id):
    conceito = db.session.query(Conceito).get(id)
    temp = db.session.query(Fotografia).filter(Fotografia.nomeconc==id).all()
    fotos = []
    fotos.append(random.choice(temp))
    fotos.append(random.choice(temp))
    fotografias = []
    for f in fotos:
        try:
            foto = ""
            readImage(f.path)
            if './static' in f.path:
                temp = f.path.replace('./static/img/', '')
                temp = temp.split('/')
                foto = 'http://192.168.160.192:8080/sendimage/pending/' + str(temp[0]+':'+temp[1])
            else:
                temp = f.path.replace('../', '')
                temp = temp.replace('treino/', '')
                foto = 'http://192.168.160.192:8080/sendimage/' + temp
            fotografias.append(foto)
        except:
            print('Could not find: ' + f.path)
    res = {}
    #res['name'] = conceito.nome
    res['name'] = conceito.nome
    res['id'] = conceito.nomeconceito
    res['description'] = conceito.descricao
    res['latitude'] = conceito.latitude
    res['longitude'] = conceito.longitude
    res['photos'] = fotografias
    return jsonify(res)


@app.route('/resources/atractions', methods=['GET'])
def get_atractions():
    conceitos = Conceito.query.all()
    res = []

    for c in conceitos:
        temp = {}
        temp['id'] = c.nomeconceito
        temp['name'] = c.nome
        temp['latitude'] = c.latitude
        temp['longitude'] = c.longitude
        temp['description'] = c.descricao
        res.append(temp)

    return jsonify({
        'atractions': res
    })

@app.route('/resources/photos/byuser', methods=['POST'])
def get_photo_history():
    req = request.get_json(force=True)
    user = req['user']
    fotos = db.session.query(Fotografia).filter(Fotografia.emailinst == user).order_by(Fotografia.datafoto).all()
    fotografias = []
    for f in fotos:
        try:
            foto = {}
            readImage(f.path)
            foto['latitude'] = f.latitude
            foto['longitude'] = f.longitude
            foto['date'] = f.datafoto
            if f.nomeconc == 'desconhecido':
                foto['concept'] = ''
            else:
                foto['concept'] = f.nomeconc
            if './static' in f.path:
                temp = f.path.replace('./static/img/', '')
                temp = temp.split('/')
                foto['img'] = 'http://192.168.160.192:8080/sendimage/pending/' + str(temp[0]+':'+temp[1])
            else:
                temp = f.path.replace('../', '')
                temp = temp.replace('treino/', '')
                foto['img'] = 'http://192.168.160.192:8080/sendimage/' + temp
            fotografias.append(foto)
        except:
            print('Could not find: ' + f.path)
    return jsonify(fotografias)


@app.route('/resources/routes/search', methods=['POST'])
def search_routes():
    req = request.get_json(force=True)
    user = req['user']
    concept = req['concept']
    percursos = db.session.query(InstanciaPercurso).filter(InstanciaPercurso.emailc==user).all()
    res = []
    for p in percursos:
        fotos = db.session.query(Fotografia).filter(Fotografia.idinstperc==p.id).filter(Fotografia.nomeconc==concept).all()
        if len(fotos) > 0:
            perc = db.session.query(Percurso).filter(Percurso.id==p.idperc).first()
            res.append({
                'title': perc.titulo,
                'description': perc.descricao,
                'id': perc.id
            })
    return jsonify({
        'routes': res
    })


@app.route('/resources/routes/instances/<int:id>/share', methods=['GET'])
def share_map(id):
    instance = db.session.query(InstanciaPercurso).get(id)
    if instance is None:
        return '<h1>Not Found</h1>'
    route = db.session.query(Percurso).get(instance.idperc)

    fotos = db.session.query(Fotografia).filter(Fotografia.idinstperc==id).all()

    fotografias = []
    for f in fotos:
        try:
            foto = {}
            foto['img'] = readImage(f.path)
            foto['latitude'] = f.latitude
            foto['longitude'] = f.longitude
            foto['id'] = f.id
            foto['date'] = f.datafoto
            if './static' in f.path:
                temp = f.path.replace('./static/img/', '')
                temp = temp.split('/')
                foto['path'] = '/sendimage/pending/' + str(temp[0]+':'+temp[1])
            else:
                temp = f.path.replace('../', '')
                temp = temp.replace('treino/', '')
                foto['path'] = '/sendimage/' + temp
            fotografias.append(foto)
        except:
            print('Could not find: ' + f.path)

    pontos = db.session.query(Ponto).filter(Ponto.idperc == route.id).all()

    pnts = []
    for p in pontos:
        temp = {}
        temp['latitude'] = p.latitude
        temp['longitude'] = p.longitude
        pnts.append(temp)
    
    center = {}

    latitudes = 0
    longitudes = 0
    for p in pnts:
        latitudes += p['latitude']
        longitudes += p['longitude']

    center['latitude'] = latitudes/len(pnts)
    center['longitude'] = longitudes/len(pnts) 

    return render_template('instance.html', center=center, points=pnts, fotos=fotografias, title=route.titulo, desc=route.descricao)

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

