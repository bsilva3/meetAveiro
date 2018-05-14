# coding: utf-8

from flask import Flask, render_template
from flask_googlemaps import GoogleMaps
from flask_googlemaps import Map, icons

app = Flask(__name__, template_folder="templates")


GoogleMaps(app, key="AIzaSyCJyCibWzjYod4AYXXIgJXhFyf6f1v7fvc")

@app.route("/")
def mapview():

    polyline = {
        'stroke_color': '#0AB0DE',
        'stroke_opacity': 1.0,
        'stroke_weight': 3,
        'path': [{'lat': 33.678, 'lng': -116.243, 'icon': 'http://maps.google.com/mapfiles/ms/icons/green-dot.png'},
                 {'lat': 33.679, 'lng': -116.244, 'icon': 'http://maps.google.com/mapfiles/ms/icons/green-dot.png'},
                 {'lat': 33.680, 'lng': -116.250, 'icon': 'http://maps.google.com/mapfiles/ms/icons/green-dot.png'},
                 {'lat': 33.681, 'lng': -116.239},
                 {'lat': 33.678, 'lng': -116.243}]
    }

    path1 = [(33.665, -116.235), (33.666, -116.256),
             (33.667, -116.250), (33.668, -116.229)]

    plinemap = Map(
        identifier="plinemap",
        varname="plinemap",
        lat=33.678,
        lng=-116.243,
        polylines=[polyline, path1]
    )

    return render_template(
        'example.html',
        plinemap=plinemap
    )



if __name__ == "__main__":
    app.run(debug=True, use_reloader=True)
