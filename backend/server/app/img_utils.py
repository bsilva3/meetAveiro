import base64

# imgstring = input()
# imgdata = base64.b64decode(imgstring)
# filename = 'some_image.jpg'  # I assume you have a way of picking unique filenames
# with open(filename, 'wb') as f:
#    f.write(imgdata)
# f gets closed when you exit the with statement
# Now save the value of filename to your database

def writeImage(imgstring):
    imgdata = base64.b64decode(imgstring)
    filename = '/home/chico/Pictures/temp.jpg'
    with open(filename, 'wb') as f:
        f.write(imgdata)


def readImage(imgpath):
    with open(imgpath, "rb") as image_file:
        encoded_string = base64.b64encode(image_file.read())
    base64_string = encoded_string.decode('utf-8')
    return base64_string
