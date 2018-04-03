'''
Contém métodos para converter ficheiros de imagem para o formato
de string base64 e vice-versa

writeImage(imgstring) -> recebe uma string base64 e constrói o ficheiro de imagem

readImage(imgpath) -> recebe o path para uma imagem e converte-a para string base64
'''

import base64

# imgstring = input()
# imgdata = base64.b64decode(imgstring)
# filename = 'some_image.jpg'  # I assume you have a way of picking unique filenames
# with open(filename, 'wb') as f:
#    f.write(imgdata)
# f gets closed when you exit the with statement
# Now save the value of filename to your database

def writeImage(imgstring):
    '''
    Recria a imagem a partir de uma string base64

    imgstring - imagem em formato string base64
    '''
    imgdata = base64.b64decode(imgstring)
    filename = './temp.jpg'
    with open(filename, 'wb') as f:
        f.write(imgdata)


def readImage(imgpath):
    '''
    Lê uma imagem e converte-a para o formato de string base64

    imgpath - path para o ficheiro de imagem
    '''
    with open(imgpath, "rb") as image_file:
        encoded_string = base64.b64encode(image_file.read())
    base64_string = encoded_string.decode('utf-8')
    return base64_string
