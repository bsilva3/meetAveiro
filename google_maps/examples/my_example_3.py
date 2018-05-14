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
        'path': [{'lat': 33.678, 'lng': -116.243},
                 {'lat': 33.679, 'lng': -116.244},
                 {'lat': 33.680, 'lng': -116.250},
                 {'lat': 33.681, 'lng': -116.239},
                 {'lat': 33.678, 'lng': -116.243}]
    }

    plinemap = Map(
        identifier="plinemap",
        varname="plinemap",
        lat=33.678,
        lng=-116.243,
        polylines=[polyline],
        markers=[
                    {
                        'lat':  33.678,
                        'lng':   -116.243,
                        'infobox': "<h4>Olá</h4><img src='https://i.ytimg.com/vi/YCaGYUIfdy4/maxresdefault.jpg' width='100' />"
                    }
                ],
	style="height:300px;width:500px;margin:100px 0 0 100px;"
    )

    return render_template(
        'example.html',
        plinemap=plinemap
    )



if __name__ == "__main__":
    app.run(debug=True, use_reloader=True)
